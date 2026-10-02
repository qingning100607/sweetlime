package com.qingning.sweetlime.core.tools

/**
 * 网页资源嗅探：从 HTML / CSS 文本里把「页面引用了哪些资源」挑出来。
 *
 * 和浏览器扩展那种嗅探不同，这里只做**静态解析**：看 HTML 里写死的地址
 * （img / link / script / video / iframe / 内联 style / <style> 块），
 * 再跟进页面里引用的 CSS 文件，把里面的 `url()` 和 `@import` 也扒出来。
 * 走 JS 动态插进去的资源、以及 XHR 拉的数据不在覆盖范围内（那需要真跑一遍页面）。
 *
 * 全部是纯函数，方便单测：给一段 HTML + 一个基准地址，得到一串绝对地址。
 */
object ResourceSniffer {

    /** 资源类型。顺序也是界面上的显示顺序。 */
    enum class Kind { IMAGE, MEDIA, FONT, STYLE, SCRIPT, FRAME, OTHER }

    /**
     * 一条资源。
     *
     * [url] 已经绝对化；[from] 记录它是从哪找到的 —— null 表示页面本身，
     * 否则是「某个 CSS 文件里的引用」（界面上会标出来，方便判断优先级）。
     */
    data class Resource(val url: String, val kind: Kind, val from: String? = null)

    /** 单个页面的资源数上限，防止某些站点上万条把内存和界面拖死。 */
    const val DEFAULT_LIMIT = 600

    private val TAG = Regex("<([a-zA-Z][a-zA-Z0-9]*)((?:[^>\"']|\"[^\"]*\"|'[^']*')*)>")
    private val ATTR = Regex(
        "([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s>]+))",
    )
    private val STYLE_BLOCK = Regex("<style[^>]*>(.*?)</style>", RegexOption.IGNORE_CASE)
    private val CSS_URL = Regex("url\\(\\s*['\"]?([^)'\"]+)['\"]?\\s*\\)", RegexOption.IGNORE_CASE)
    private val CSS_IMPORT = Regex(
        "@import\\s+(?:url\\(\\s*)?['\"]?([^)'\";]+)",
        RegexOption.IGNORE_CASE,
    )

    /** 扩展名 → 类型。有些资源（比如接口地址）没有扩展名，那就归到 OTHER。 */
    private val BY_EXTENSION: Map<String, Kind> = buildMap {
        for (e in listOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "ico", "avif", "jfif")) {
            put(e, Kind.IMAGE)
        }
        for (e in listOf("mp4", "webm", "mkv", "mov", "avi", "flv", "m4v", "ts")) {
            put(e, Kind.MEDIA)
        }
        for (e in listOf("mp3", "m4a", "aac", "ogg", "oga", "wav", "flac", "opus")) {
            put(e, Kind.MEDIA)
        }
        for (e in listOf("woff", "woff2", "ttf", "otf", "eot")) {
            put(e, Kind.FONT)
        }
        put("css", Kind.STYLE)
        for (e in listOf("js", "mjs", "cjs")) {
            put(e, Kind.SCRIPT)
        }
        for (e in listOf("html", "htm", "shtml")) {
            put(e, Kind.FRAME)
        }
    }

    /**
     * 解析 HTML。[baseUrl] 是页面地址（要用**重定向之后**的地址，否则相对路径会解析错）。
     */
    fun sniffHtml(html: String, baseUrl: String, limit: Int = DEFAULT_LIMIT): List<Resource> {
        val out = LinkedHashMap<String, Resource>()

        fun add(ref: String?, kind: Kind, from: String? = null) {
            if (out.size >= limit) return
            val url = ref?.let { absolutize(baseUrl, it) } ?: return
            // 同一条地址只留第一次出现的类型：同一张图既当 icon 又当 og:image 时，
            // 先出现的那个判断通常更准（比如 .ico）。
            if (!out.containsKey(url)) out[url] = Resource(url, kind, from)
        }

        fun addByExtension(ref: String?, from: String? = null) {
            val url = ref?.let { absolutize(baseUrl, it) } ?: return
            add(url, classify(url), from)
        }

        fun addSrcSet(value: String?, from: String? = null) {
            value ?: return
            // srcset 是「地址 宽度描述符, 地址 描述符」，一个候选里可能带空格，取第一段。
            for (part in value.split(',')) {
                val candidate = part.trim().substringBefore(' ')
                if (candidate.isNotEmpty()) addByExtension(candidate, from)
            }
        }

        // <style> 块里写死的背景图 / 字体
        for (block in STYLE_BLOCK.findAll(html)) {
            for (m in CSS_URL.findAll(block.groupValues[1])) addByExtension(m.groupValues[1])
        }

        for (tag in TAG.findAll(html)) {
            val name = tag.groupValues[1].lowercase()
            val attrs = parseAttributes(tag.groupValues[2])
            when (name) {
                "img" -> {
                    addByExtension(attrs["src"])
                    // 懒加载站点常把真地址藏在这几个属性里
                    addByExtension(attrs["data-src"])
                    addByExtension(attrs["data-original"])
                    addByExtension(attrs["data-lazy-src"])
                    addByExtension(attrs["data-echo"])
                    addSrcSet(attrs["srcset"])
                    addSrcSet(attrs["data-srcset"])
                }
                "source" -> {
                    addByExtension(attrs["src"])
                    addSrcSet(attrs["srcset"])
                }
                "link" -> {
                    val rel = attrs["rel"]?.lowercase().orEmpty()
                    val kind = when {
                        rel.contains("stylesheet") -> Kind.STYLE
                        rel.contains("icon") -> Kind.IMAGE
                        rel.contains("preload") || rel.contains("prefetch") -> {
                            when (attrs["as"]?.lowercase()) {
                                "image" -> Kind.IMAGE
                                "style" -> Kind.STYLE
                                "script" -> Kind.SCRIPT
                                "font" -> Kind.FONT
                                "video", "audio" -> Kind.MEDIA
                                else -> Kind.OTHER
                            }
                        }
                        else -> null
                    }
                    // 只认得出用途的 rel：不然 rel=canonical / alternate 这些会把一堆页面塞进来。
                    if (kind != null) add(attrs["href"]?.let { absolutize(baseUrl, it) }, kind)
                }
                "script" -> addByExtension(attrs["src"])
                "video" -> {
                    addByExtension(attrs["src"])
                    add(attrs["poster"]?.let { absolutize(baseUrl, it) }, Kind.IMAGE)
                }
                "audio" -> addByExtension(attrs["src"])
                "iframe", "frame" -> addByExtension(attrs["src"])
                "embed" -> addByExtension(attrs["src"])
                "object" -> addByExtension(attrs["data"])
                "meta" -> {
                    val key = (attrs["property"] ?: attrs["name"])?.lowercase()
                    if (key == "og:image" || key == "twitter:image" || key == "og:video") {
                        add(attrs["content"]?.let { absolutize(baseUrl, it) }, Kind.IMAGE)
                    }
                }
            }
            // 内联 style="background:url(...)"
            attrs["style"]?.let { style ->
                for (m in CSS_URL.findAll(style)) addByExtension(m.groupValues[1])
            }
        }
        return out.values.toList()
    }

    /**
     * 解析 CSS 文件里引用的资源。[from] 是这份 CSS 自己的地址，写进结果里方便溯源。
     */
    fun sniffCss(css: String, baseUrl: String, from: String? = null, limit: Int = DEFAULT_LIMIT): List<Resource> {
        val out = LinkedHashMap<String, Resource>()
        for (m in CSS_URL.findAll(css)) {
            if (out.size >= limit) break
            val url = absolutize(baseUrl, m.groupValues[1]) ?: continue
            if (!out.containsKey(url)) out[url] = Resource(url, classify(url), from)
        }
        for (m in CSS_IMPORT.findAll(css)) {
            if (out.size >= limit) break
            val url = absolutize(baseUrl, m.groupValues[1]) ?: continue
            if (!out.containsKey(url)) out[url] = Resource(url, Kind.STYLE, from)
        }
        return out.values.toList()
    }

    /**
     * 从地址里猜一个保存用的文件名：取最后一段；最后一段是空的（以 / 结尾）
     * 就退回主机名；都拿不到就给个兜底名。
     *
     * 真正的「去掉非法字符 / 重名加序号」由各平台的下载实现负责。
     */
    fun suggestFileName(url: String): String {
        val path = url.substringBefore('#').substringBefore('?').trimEnd('/')
        val last = path.substringAfterLast('/')
        if (last.isNotEmpty()) {
            // 有些地址最后一段自带查询串残留（比如 img.png%3Fv=2），清一下常见的编码
            return last.replace("%3F", "").replace("%3f", "")
        }
        val host = url.substringAfter("://", "").substringBefore('/')
        return host.ifEmpty { "resource" }
    }

    /** 按扩展名猜类型。 */
    fun classify(url: String): Kind {
        val path = url.substringBefore('#').substringBefore('?')
        val ext = path.substringAfterLast('.', "").lowercase()
        return BY_EXTENSION[ext] ?: Kind.OTHER
    }

    /**
     * 把相对地址变成绝对地址；不该跟的（data: / javascript: / #锚点 / 空）返回 null。
     *
     * 这就是浏览器里那套基础规则，够用的子集：完整地址、`//host/x`、`/x`、`x`、`../x`、`?q`。
     */
    fun absolutize(baseUrl: String, ref: String): String? {
        val raw = ref.trim()
        if (raw.isEmpty()) return null
        val lower = raw.lowercase()
        if (
            lower.startsWith("data:") || lower.startsWith("javascript:") ||
            lower.startsWith("mailto:") || lower.startsWith("tel:") ||
            lower.startsWith("about:") || raw.startsWith("#")
        ) {
            return null
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) return raw

        val scheme = baseUrl.substringBefore("://", "https")
        val rest = baseUrl.substringAfter("://", "")
        if (rest.isEmpty()) return null
        val authority = rest.substringBefore('/')

        if (raw.startsWith("//")) return "$scheme:$raw"
        // 注意这里要自己补上那个 `/`：normalizePath 会把开头那段空的路径段丢掉。
        if (raw.startsWith("/")) return "$scheme://$authority/${normalizePath(raw)}"

        // 相对地址：先丢掉基准地址里最后一段（可能是文件名）。
        val basePath = if (rest.contains('/')) rest.substringAfter('/') else ""
        val dir = if (basePath.contains('/')) basePath.substringBeforeLast('/') + "/" else ""
        if (raw.startsWith("?")) return "$scheme://$authority/$basePath$raw"
        return "$scheme://$authority/${normalizePath(dir + raw)}"
    }

    /** 把 `a/../b/./c` 这类路径化简，并保留结尾的 `/`。 */
    private fun normalizePath(path: String): String {
        val suffixStart = path.indexOfFirst { it == '#' || it == '?' }.let { if (it < 0) path.length else it }
        val suffix = path.substring(suffixStart)
        val segments = path.substring(0, suffixStart)
        val stack = ArrayDeque<String>()
        for (segment in segments.split('/')) {
            when (segment) {
                "", "." -> Unit
                ".." -> if (stack.isNotEmpty()) stack.removeLast()
                else -> stack.addLast(segment)
            }
        }
        val trailing = if (segments.endsWith("/") && stack.isNotEmpty()) "/" else ""
        return stack.joinToString("/") + trailing + suffix
    }

    /** 把 `<img src="a" data-x='b'>` 这种属性串解析成表；同名属性取第一个。 */
    private fun parseAttributes(raw: String): Map<String, String> {
        if (raw.isEmpty()) return emptyMap()
        val map = HashMap<String, String>(8)
        for (m in ATTR.findAll(raw)) {
            val name = m.groupValues[1].lowercase()
            val value = when {
                m.groupValues[3].isNotEmpty() -> m.groupValues[3]
                m.groupValues[4].isNotEmpty() -> m.groupValues[4]
                else -> m.groupValues[5]
            }
            if (!map.containsKey(name)) map[name] = value
        }
        return map
    }
}