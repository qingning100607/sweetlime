/*
 * SweetLime 的原生实现（Android / arm64-v8a）。
 *
 * 为什么这些功能用 C++：
 *   - MD5 / Base64 / URL 编解码：逐字节循环，Kotlin 侧每步都要处理装箱与边界检查；
 *   - 花体映射（数学字母区、圆圈、小型大写、倒置、盲文、符文、全角半角、间隔、反转）：
 *     逐字符查表 + 拼字符串，长文本时分配压力全在 Kotlin 那侧，这里一次分配就够。
 *
 * 行为必须和 Kotlin 版**逐字节一致** —— 设备上有一份「Kotlin ↔ C++ 对拍」自检
 * （NativeSelfCheck.kt），对不上会打进 logcat；Kotlin 版同时作为非 Android 的兜底。
 *
 * 表（tables.h）由 /root/gen_cpp_tables.py 从 Kotlin 源码自动生成，不手抄。
 */
#include <jni.h>
#include <cstdint>
#include <cstring>
#include <string>
#include <vector>
#include <cmath>

#include "tables.h"

namespace {

using u16 = std::u16string;

// ---------------------------------------------------------------- 基础工具

void appendCodePoint(u16 &out, uint32_t cp) {
    if (cp <= 0xFFFF) {
        out.push_back((char16_t) cp);
    } else {
        const uint32_t v = cp - 0x10000;
        out.push_back((char16_t) (0xD800 + (v >> 10)));
        out.push_back((char16_t) (0xDC00 + (v & 0x3FF)));
    }
}

bool isHighSurrogate(char16_t c) { return c >= 0xD800 && c <= 0xDBFF; }

bool isLowSurrogate(char16_t c) { return c >= 0xDC00 && c <= 0xDFFF; }

/** UTF-16 -> UTF-8（只在 URL 解码那条路上用到：把非 %XX 的字符原样落成字节）。 */
void appendUtf8(std::vector<uint8_t> &out, char16_t hi, char16_t lo = 0) {
    uint32_t cp;
    if (isHighSurrogate(hi) && lo != 0 && isLowSurrogate(lo)) {
        cp = 0x10000 + ((uint32_t) (hi - 0xD800) << 10) + (uint32_t) (lo - 0xDC00);
    } else if (isHighSurrogate(hi) || isLowSurrogate(hi)) {
        // 孤立代理（半个 emoji）：Java 的 String.getBytes(UTF-8) 会写成 '?'，
        // Kotlin 那份就是走 Java 的，所以这里也写 '?' —— 两边结果才对得上。
        // （真实 URL 里不会出现这种字符，但「对拍」要求任何输入都一致。）
        out.push_back((uint8_t) '?');
        return;
    } else {
        cp = hi;
    }
    if (cp < 0x80) {
        out.push_back((uint8_t) cp);
    } else if (cp < 0x800) {
        out.push_back((uint8_t) (0xC0 | (cp >> 6)));
        out.push_back((uint8_t) (0x80 | (cp & 0x3F)));
    } else if (cp < 0x10000) {
        out.push_back((uint8_t) (0xE0 | (cp >> 12)));
        out.push_back((uint8_t) (0x80 | ((cp >> 6) & 0x3F)));
        out.push_back((uint8_t) (0x80 | (cp & 0x3F)));
    } else {
        out.push_back((uint8_t) (0xF0 | (cp >> 18)));
        out.push_back((uint8_t) (0x80 | ((cp >> 12) & 0x3F)));
        out.push_back((uint8_t) (0x80 | ((cp >> 6) & 0x3F)));
        out.push_back((uint8_t) (0x80 | (cp & 0x3F)));
    }
}

const Pair16 *findPair(const Pair16 *table, int count, uint16_t from) {
    // 表都不大（最多 80 项），线性扫足够；按 from 递增排过，命中就走，未到就继续。
    for (int i = 0; i < count; i++) {
        if (table[i].from == from) return &table[i];
    }
    return nullptr;
}

// ---------------------------------------------------------------- MD5（RFC 1321）

uint32_t rotateLeft(uint32_t v, int bits) { return (v << bits) | (v >> (32 - bits)); }

const uint32_t MD5_K[64] = {
    0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
    0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be, 0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
    0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
    0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed, 0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
    0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c, 0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
    0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05, 0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
    0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039, 0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
    0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1, 0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391,
};

const int MD5_S[64] = {
    7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
    5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
    4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
    6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
};

/** 32 位小写十六进制 MD5，和 Kotlin 版逐字节一致。 */
std::string md5Hex(const uint8_t *data, size_t len) {
    const uint64_t bitLength = (uint64_t) len * 8ULL;
    // 和 Kotlin 一样：((len + 8) / 64 + 1) * 64
    const size_t padded = ((len + 8) / 64 + 1) * 64;
    std::vector<uint8_t> msg(padded, 0);
    if (len) std::memcpy(msg.data(), data, len);
    msg[len] = 0x80;
    for (int i = 0; i < 8; i++) {
        msg[padded - 8 + i] = (uint8_t) ((bitLength >> (8 * i)) & 0xFF);
    }

    uint32_t a0 = 0x67452301, b0 = 0xefcdab89, c0 = 0x98badcfe, d0 = 0x10325476;
    for (size_t offset = 0; offset < padded; offset += 64) {
        uint32_t w[16];
        for (int i = 0; i < 16; i++) {
            const uint8_t *p = msg.data() + offset + i * 4;
            w[i] = (uint32_t) p[0] | ((uint32_t) p[1] << 8) | ((uint32_t) p[2] << 16) |
                   ((uint32_t) p[3] << 24);
        }
        uint32_t a = a0, b = b0, c = c0, d = d0;
        for (int i = 0; i < 64; i++) {
            uint32_t f;
            int g;
            if (i < 16) {
                f = (b & c) | (~b & d);
                g = i;
            } else if (i < 32) {
                f = (d & b) | (~d & c);
                g = (5 * i + 1) % 16;
            } else if (i < 48) {
                f = b ^ c ^ d;
                g = (3 * i + 5) % 16;
            } else {
                f = c ^ (b | ~d);
                g = (7 * i) % 16;
            }
            const uint32_t old = d;
            d = c;
            c = b;
            b += rotateLeft(a + f + MD5_K[i] + w[g], MD5_S[i]);
            a = old;
        }
        a0 += a;
        b0 += b;
        c0 += c;
        d0 += d;
    }

    static const char *HEX = "0123456789abcdef";
    std::string out;
    out.reserve(32);
    const uint32_t regs[4] = {a0, b0, c0, d0};
    for (uint32_t v : regs) {
        for (int i = 0; i < 4; i++) {
            const uint32_t byte = (v >> (8 * i)) & 0xFF;
            out.push_back(HEX[(byte >> 4) & 0x0F]);
            out.push_back(HEX[byte & 0x0F]);
        }
    }
    return out;
}

// ---------------------------------------------------------------- Base64

const char *B64_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

std::string base64Encode(const uint8_t *data, size_t len) {
    std::string out;
    out.reserve((len + 2) / 3 * 4);
    size_t i = 0;
    while (i < len) {
        const uint32_t b0 = data[i];
        const uint32_t b1 = (i + 1 < len) ? data[i + 1] : 0;
        const uint32_t b2 = (i + 2 < len) ? data[i + 2] : 0;
        out.push_back(B64_TABLE[b0 >> 2]);
        out.push_back(B64_TABLE[((b0 & 0x03) << 4) | (b1 >> 4)]);
        out.push_back((i + 1 < len) ? B64_TABLE[((b1 & 0x0F) << 2) | (b2 >> 6)] : '=');
        out.push_back((i + 2 < len) ? B64_TABLE[b2 & 0x3F] : '=');
        i += 3;
    }
    return out;
}

/** Kotlin 的 Char.isWhitespace() 常遇到的那几档（ASCII 空白 + 常见 Unicode 空白）。 */
bool isWhitespace(char16_t c) {
    switch (c) {
        case 0x09: case 0x0A: case 0x0B: case 0x0C: case 0x0D: case 0x20:
        case 0x85: case 0xA0: case 0x1680: case 0x2028: case 0x2029:
        case 0x202F: case 0x205F: case 0x3000:
            return true;
        default:
            return c >= 0x2000 && c <= 0x200A;
    }
}

/** 容错解码：忽略空白、接受 URL-safe 的 - 与 _、允许缺省 padding。非法字符返回 false。 */
bool base64Decode(const u16 &text, std::vector<uint8_t> &out) {
    // 先按 Kotlin 的顺序：去空白 → -/_ 换回 +/ → 去掉尾部 =
    std::u16string clean;
    clean.reserve(text.size());
    for (char16_t c : text) {
        if (isWhitespace(c)) continue;
        if (c == u'-') clean.push_back(u'+');
        else if (c == u'_') clean.push_back(u'/');
        else clean.push_back(c);
    }
    while (!clean.empty() && clean.back() == u'=') clean.pop_back();

    uint32_t buffer = 0;
    int bits = 0;
    for (char16_t c : clean) {
        int value = -1;
        if (c >= u'A' && c <= u'Z') value = c - u'A';
        else if (c >= u'a' && c <= u'z') value = c - u'a' + 26;
        else if (c >= u'0' && c <= u'9') value = c - u'0' + 52;
        else if (c == u'+') value = 62;
        else if (c == u'/') value = 63;
        if (value < 0) return false;
        buffer = (buffer << 6) | (uint32_t) value;
        bits += 6;
        if (bits >= 8) {
            bits -= 8;
            out.push_back((uint8_t) ((buffer >> bits) & 0xFF));
        }
    }
    return true;
}

// ---------------------------------------------------------------- URL 编解码

bool isSafeUrl(char16_t c) {
    if ((c >= u'A' && c <= u'Z') || (c >= u'a' && c <= u'z') || (c >= u'0' && c <= u'9')) return true;
    switch (c) {
        case u'-': case u'_': case u'.': case u'!': case u'~':
        case u'*': case u'\'': case u'(': case u')':
            return true;
        default:
            return false;
    }
}

std::string urlEncode(const uint8_t *data, size_t len) {
    static const char *HEX = "0123456789ABCDEF";
    std::string out;
    out.reserve(len * 3);
    for (size_t i = 0; i < len; i++) {
        const uint32_t v = data[i];
        const char16_t c = (char16_t) v;
        if (isSafeUrl(c)) {
            out.push_back((char) c);
        } else {
            out.push_back('%');
            out.push_back(HEX[v >> 4]);
            out.push_back(HEX[v & 0x0F]);
        }
    }
    return out;
}

int hexValue(char16_t c) {
    if (c >= u'0' && c <= u'9') return c - u'0';
    if (c >= u'a' && c <= u'f') return c - u'a' + 10;
    if (c >= u'A' && c <= u'F') return c - u'A' + 10;
    return -1;
}

/** + 当空格；%XX 两位十六进制；其余字符按 UTF-8 原样落下。失败返回 false。 */
bool urlDecode(const u16 &text, std::vector<uint8_t> &out) {
    size_t i = 0;
    while (i < text.size()) {
        const char16_t c = text[i];
        if (c == u'%') {
            if (i + 2 >= text.size()) return false;
            const int hi = hexValue(text[i + 1]);
            const int lo = hexValue(text[i + 2]);
            if (hi < 0 || lo < 0) return false;
            out.push_back((uint8_t) ((hi << 4) | lo));
            i += 3;
        } else if (c == u'+') {
            out.push_back((uint8_t) ' ');
            i++;
        } else {
            if (isHighSurrogate(c) && i + 1 < text.size() && isLowSurrogate(text[i + 1])) {
                appendUtf8(out, c, text[i + 1]);
                i += 2;
            } else {
                appendUtf8(out, c);
                i++;
            }
        }
    }
    return true;
}

// ---------------------------------------------------------------- 花体映射

const MathStyleDef *findMathStyle(const std::string &id) {
    for (int i = 0; i < kMathStyleCount; i++) {
        if (id == kMathStyles[i].id) return &kMathStyles[i];
    }
    return nullptr;
}

/** 数学字母符号区那一套：先查补位表，再按区间平移。映射不到就原样保留。 */
void mapMathChar(const MathStyleDef &def, char16_t ch, u16 &out) {
    if (const Pair16 *p = def.exceptions ? findPair(def.exceptions, def.exceptionCount, ch) : nullptr) {
        appendCodePoint(out, p->to);
        return;
    }
    if (ch >= u'A' && ch <= u'Z') {
        appendCodePoint(out, def.upper + (uint32_t) (ch - u'A'));
    } else if (ch >= u'a' && ch <= u'z') {
        appendCodePoint(out, def.lower + (uint32_t) (ch - u'a'));
    } else if (def.digits >= 0 && ch >= u'0' && ch <= u'9') {
        appendCodePoint(out, (uint32_t) def.digits + (uint32_t) (ch - u'0'));
    } else {
        out.push_back(ch);
    }
}

/** 圆圈体：A-Z→①..、a-z→ⓐ..、0→⓪、1-9→①..（和 Kotlin 的 circledTransform 一致）。 */
void mapCircled(char16_t ch, u16 &out, bool bold) {
    if (bold) {
        if (ch >= u'A' && ch <= u'Z') {
            appendCodePoint(out, 0x1F150 + (uint32_t) (ch - u'A'));
            return;
        }
        if (ch >= u'a' && ch <= u'z') {
            appendCodePoint(out, 0x1F150 + (uint32_t) (ch - u'a'));
            return;
        }
        out.push_back(ch);
        return;
    }
    if (ch >= u'A' && ch <= u'Z') {
        appendCodePoint(out, 0x24B6 + (uint32_t) (ch - u'A'));
    } else if (ch >= u'a' && ch <= u'z') {
        appendCodePoint(out, 0x24D0 + (uint32_t) (ch - u'a'));
    } else if (ch == u'0') {
        appendCodePoint(out, 0x24EA);
    } else if (ch >= u'1' && ch <= u'9') {
        appendCodePoint(out, 0x2460 + (uint32_t) (ch - u'1'));
    } else {
        out.push_back(ch);
    }
}

/** 整体按码点倒序（不拆代理对）。 */
void reverseByCodePoint(const u16 &in, u16 &out) {
    size_t i = in.size();
    while (i > 0) {
        const char16_t lo = in[i - 1];
        if (isLowSurrogate(lo) && i >= 2 && isHighSurrogate(in[i - 2])) {
            out.push_back(in[i - 2]);
            out.push_back(lo);
            i -= 2;
        } else {
            out.push_back(lo);
            i -= 1;
        }
    }
}

/**
 * 21 个样式的原生映射。返回 false =「这个 id 不归我管」，由 Kotlin 版接管。
 */
bool mapStyle(const std::string &id, const u16 &in, u16 &out) {
    if (const MathStyleDef *def = findMathStyle(id)) {
        out.reserve(in.size() * 2);
        for (char16_t ch : in) mapMathChar(*def, ch, out);
        return true;
    }
    if (id == "latin_circled" || id == "latin_bold_circled") {
        const bool bold = id == "latin_bold_circled";
        out.reserve(in.size() * 2);
        for (char16_t ch : in) mapCircled(ch, out, bold);
        return true;
    }
    if (id == "latin_small_caps") {
        out.reserve(in.size());
        for (char16_t ch : in) {
            const Pair16 *p = findPair(kSmallCaps, kSmallCapsCount, ch);
            if (p) appendCodePoint(out, p->to); else out.push_back(ch);
        }
        return true;
    }
    if (id == "latin_upside_down") {
        // Kotlin：按 \n 分行，每行先整体倒序、再把每个字符换成倒过来看的字形。
        out.reserve(in.size() * 2);
        size_t start = 0;
        while (start <= in.size()) {
            size_t end = in.find(u'\n', start);
            const bool hasBreak = end != std::u16string::npos;
            const size_t lineEnd = hasBreak ? end : in.size();
            u16 line = in.substr(start, lineEnd - start);
            u16 reversed;
            reverseByCodePoint(line, reversed);
            for (char16_t ch : reversed) {
                const Pair16 *p = findPair(kFlip, kFlipCount, ch);
                if (p) appendCodePoint(out, p->to); else out.push_back(ch);
            }
            if (!hasBreak) break;
            out.push_back(u'\n');
            start = end + 1;
        }
        return true;
    }
    if (id == "alias_reverse") {
        out.reserve(in.size());
        reverseByCodePoint(in, out);
        return true;
    }
    if (id == "alias_rtl_flip") {
        out.reserve(in.size() + 2);
        out.push_back(u'\u202E');
        out.append(in);
        out.push_back(u'\u202C');
        return true;
    }
    if (id == "alias_fullwidth") {
        out.reserve(in.size());
        for (char16_t ch : in) {
            if (ch == u' ') out.push_back(u'\u3000');
            else if (ch >= u'!' && ch <= u'~') out.push_back((char16_t) (ch + 0xFEE0));
            else out.push_back(ch);
        }
        return true;
    }
    if (id == "alias_halfwidth") {
        out.reserve(in.size());
        for (char16_t ch : in) {
            if (ch == u'\u3000') out.push_back(u' ');
            else if (ch >= u'\uFF01' && ch <= u'\uFF5E') out.push_back((char16_t) (ch - 0xFEE0));
            else out.push_back(ch);
        }
        return true;
    }
    if (id == "alias_dot_spacing" || id == "alias_star_spacing") {
        const char16_t mark = id == "alias_dot_spacing" ? u'\u00B7' : u'\u22C6';
        out.reserve(in.size() * 2);
        bool first = true;
        // 按**码点**走：emoji / 增补平面字符是代理对，按 code unit 走会把分隔符
        // 插进两个代理中间，产出非法 UTF-16（渲染成 �）。与 Kotlin 侧保持一致。
        for (size_t i = 0; i < in.size();) {
            const char16_t ch = in[i];
            const size_t len = (ch >= 0xD800 && ch <= 0xDBFF && i + 1 < in.size() &&
                                in[i + 1] >= 0xDC00 && in[i + 1] <= 0xDFFF) ? 2 : 1;
            if (len == 1 && (ch == u'\n' || ch == u'\r')) {
                out.push_back(ch);
                first = true;
            } else {
                if (!first) out.push_back(mark);
                for (size_t k = 0; k < len; ++k) out.push_back(in[i + k]);
                first = false;
            }
            i += len;
        }
        return true;
    }
    if (id == "alias_braille" || id == "alias_rune") {
        const Pair16 *table = id == "alias_braille" ? kBraille : kRunes;
        const int count = id == "alias_braille" ? kBrailleCount : kRuneCount;
        out.reserve(in.size());
        for (char16_t ch : in) {
            char16_t lower = ch;
            if (ch >= u'A' && ch <= u'Z') lower = (char16_t) (ch + 32);
            const Pair16 *p = findPair(table, count, lower);
            if (p) appendCodePoint(out, p->to); else out.push_back(ch);
        }
        return true;
    }
    if (id == "alias_keycap") {
        out.reserve(in.size() * 3);
        for (char16_t ch : in) {
            if (ch >= u'0' && ch <= u'9') {
                out.push_back(ch);
                out.push_back(u'\uFE0F');
                out.push_back(u'\u20E3');
            } else {
                out.push_back(ch);
            }
        }
        return true;
    }
    return false;
}

// ---------------------------------------------------------------- JNI 桥

u16 toString(JNIEnv *env, jstring s) {
    if (s == nullptr) return u16();
    const jsize len = env->GetStringLength(s);
    const jchar *chars = env->GetStringChars(s, nullptr);
    u16 out((const char16_t *) chars, (size_t) len);
    env->ReleaseStringChars(s, chars);
    return out;
}

jstring toJString(JNIEnv *env, const u16 &s) {
    return env->NewString((const jchar *) s.data(), (jsize) s.size());
}

jbyteArray toJByteArray(JNIEnv *env, const std::vector<uint8_t> &bytes) {
    auto arr = env->NewByteArray((jsize) bytes.size());
    if (arr != nullptr && !bytes.empty()) {
        env->SetByteArrayRegion(arr, 0, (jsize) bytes.size(), (const jbyte *) bytes.data());
    }
    return arr;
}

std::vector<uint8_t> toBytes(JNIEnv *env, jbyteArray arr) {
    std::vector<uint8_t> out;
    if (arr == nullptr) return out;
    const jsize len = env->GetArrayLength(arr);
    out.resize((size_t) len);
    if (len > 0) env->GetByteArrayRegion(arr, 0, len, (jbyte *) out.data());
    return out;
}

}  // namespace

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeMd5(JNIEnv *env, jobject, jbyteArray bytes) {
    const std::vector<uint8_t> data = toBytes(env, bytes);
    const std::string hex = md5Hex(data.data(), data.size());
    return env->NewStringUTF(hex.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeBase64Encode(JNIEnv *env, jobject, jbyteArray bytes) {
    const std::vector<uint8_t> data = toBytes(env, bytes);
    const std::string out = base64Encode(data.data(), data.size());
    return env->NewStringUTF(out.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeBase64Decode(JNIEnv *env, jobject, jstring text) {
    const u16 in = toString(env, text);
    std::vector<uint8_t> out;
    out.reserve(in.size() * 3 / 4 + 2);
    if (!base64Decode(in, out)) return nullptr;
    return toJByteArray(env, out);
}

JNIEXPORT jstring JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeUrlEncode(JNIEnv *env, jobject, jbyteArray bytes) {
    const std::vector<uint8_t> data = toBytes(env, bytes);
    const std::string out = urlEncode(data.data(), data.size());
    return env->NewStringUTF(out.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeUrlDecodeBytes(JNIEnv *env, jobject, jstring text) {
    const u16 in = toString(env, text);
    std::vector<uint8_t> out;
    out.reserve(in.size());
    if (!urlDecode(in, out)) return nullptr;
    return toJByteArray(env, out);
}

JNIEXPORT jstring JNICALL
Java_com_qingning_sweetlime_core_NativeText_nativeMapStyle(JNIEnv *env, jobject, jstring id, jstring text) {
    const char *idChars = env->GetStringUTFChars(id, nullptr);
    const std::string styleId = idChars ? idChars : "";
    if (idChars) env->ReleaseStringUTFChars(id, idChars);
    const u16 in = toString(env, text);
    u16 out;
    if (!mapStyle(styleId, in, out)) return nullptr;
    return toJString(env, out);
}

}  // extern "C"