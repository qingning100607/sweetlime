#!/usr/bin/env python3
"""
生成 SweetLime 的应用图标（可复现，不依赖设计稿）。

用法（在仓库根目录）：
    python3 tools/make_icon.py
依赖：Pillow（`pip install pillow`）

设计：青柠绿渐变背景 + 白色柠檬片与叶子。
三层都是自适应图标（Android 8.0+）的标准结构：
    ic_launcher_background.png   背景层，铺满整张画布
    ic_launcher_foreground.png   前景层，白色柠檬片 + 叶子（真的会画出果瓣分界）
    ic_launcher_monochrome.png   单色层，纯白剪影，给 Android 13+ 主题图标用

画布约定（这条最容易踩）：自适应图标是 108x108 的层，只有中间 72x72 必然可见，
圆形掩码最多裁到半径 36，所以**所有不透明像素到中心的距离必须 <= 34**（留余量）。
脚本每次生成都会校验这一条，超了就打印报警 —— 不用靠眼睛看。
"""
import math
import os

from PIL import Image, ImageDraw

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(REPO, "composeApp", "src", "androidMain", "res")
SS = 4  # 超采样倍数，最后一次性缩小做抗锯齿

# 密度 -> 画布边长（108dp * 倍率）
DENSITIES = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}

# ---- 配色 ----
BG_FROM = (155, 225, 93)     # #9BE15D 青柠黄绿
BG_TO = (0, 168, 107)        # #00A86B 青柠绿
WHITE = (255, 255, 255)

# ---- 几何（单位：108 画布的 dp）----
CENTER = (54.0, 58.0)        # 柠檬片圆心（略低于正中，给上方留出叶子的位置）
R_RIND = 23.0                # 外皮半径
R_FLESH = 19.6               # 果肉半径（外皮与果肉之间留一圈深色做分界）
R_CORE = 3.0                 # 中心小圆
SEG_START, SEG_END = 3.0, 18.0   # 分瓣线从哪到哪
SEG_WIDTH = 2.0
SEG_COUNT = 6

STEM_FROM = (69.8, 41.5)     # 果柄：从柠檬片右上边缘往外
STEM_TO = (73.0, 38.0)
LEAF_A = (70.5, 40.0)        # 叶子：一枚杏仁形，从果柄顶端再往外斜上
LEAF_B = (77.0, 33.0)
LEAF_HALF = 4.0

SAFE_RADIUS = 34.0           # 安全区半径，见文件头说明


def bg_layer(size: int) -> Image.Image:
    """背景层：对角渐变，铺满整个画布（掩码裁完也不会露白）。"""
    w = size * SS
    img = Image.new("RGB", (w, w))
    px = img.load()
    for y in range(w):
        for x in range(w):
            t = (x + y) / (2.0 * (w - 1))
            px[x, y] = (
                round(BG_FROM[0] + (BG_TO[0] - BG_FROM[0]) * t),
                round(BG_FROM[1] + (BG_TO[1] - BG_FROM[1]) * t),
                round(BG_FROM[2] + (BG_TO[2] - BG_FROM[2]) * t),
            )
    return img.resize((size, size), Image.LANCZOS)


def _scaled(p):
    return (p[0] * SS, p[1] * SS)


def _disc(draw, center, r, color):
    cx, cy = _scaled(center)
    rr = r * SS
    draw.ellipse([cx - rr, cy - rr, cx + rr, cy + rr], fill=color)


def fruit_silhouette(mono: bool) -> Image.Image:
    """前景层：白色柠檬片 + 叶子。mono=True 时只留剪影（给主题图标用）。"""
    w = 108 * SS
    img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # 外皮（整圆）
    _disc(d, CENTER, R_RIND, WHITE + (255,))

    if not mono:
        # 外皮与果肉之间的分界：用背景色画一个略大的圆，再盖上果肉
        _disc(d, CENTER, R_FLESH + 1.3, BG_TO + (255,))
        _disc(d, CENTER, R_FLESH, WHITE + (255,))

        # 分瓣线：从中心小圆向外辐射，用背景色，端头圆润
        cx, cy = _scaled(CENTER)
        for i in range(SEG_COUNT):
            a = math.radians(-90 + i * (360 / SEG_COUNT))
            x1 = cx + math.cos(a) * SEG_START * SS
            y1 = cy + math.sin(a) * SEG_START * SS
            x2 = cx + math.cos(a) * SEG_END * SS
            y2 = cy + math.sin(a) * SEG_END * SS
            d.line([x1, y1, x2, y2], fill=BG_TO + (255,), width=round(SEG_WIDTH * SS))
            for (x, y) in ((x1, y1), (x2, y2)):
                rr = SEG_WIDTH * SS / 2
                d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=BG_TO + (255,))

        # 中心小圆盖回白色，形成「芯」
        _disc(d, CENTER, R_CORE, WHITE + (255,))

    # 果柄
    d.line(
        [_scaled(STEM_FROM)[0], _scaled(STEM_FROM)[1],
         _scaled(STEM_TO)[0], _scaled(STEM_TO)[1]],
        fill=WHITE + (255,), width=round(2.6 * SS),
    )

    # 叶子：两段二次曲线拼成的杏仁形
    a, b = LEAF_A, LEAF_B
    mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
    dx, dy = b[0] - a[0], b[1] - a[1]
    ln = math.hypot(dx, dy)
    nx, ny = -dy / ln, dx / ln  # 法线方向
    c1 = (mx + nx * LEAF_HALF, my + ny * LEAF_HALF)
    c2 = (mx - nx * LEAF_HALF, my - ny * LEAF_HALF)

    def qbez(p0, p1, p2, steps=40):
        return [
            (
                (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t ** 2 * p2[0],
                (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t ** 2 * p2[1],
            )
            for t in (i / steps for i in range(steps + 1))
        ]

    outline = qbez(a, c1, b) + qbez(b, c2, a)
    d.polygon([(x * SS, y * SS) for x, y in outline], fill=WHITE + (255,))

    return img


def check(img: Image.Image, name: str, verbose: bool = True) -> bool:
    """校验：所有不透明像素必须在安全区内。返回是否通过。"""
    alpha = img.split()[3]
    size = img.size[0]
    scale = 108.0 / size
    worst = 0.0
    for y in range(size):
        for x, v in enumerate(alpha.crop((0, y, size, y + 1)).getdata()):
            if v > 8:
                worst = max(worst, math.hypot(x * scale - 54, y * scale - 54))
    ok = worst <= SAFE_RADIUS
    if verbose:
        print(f"  {name}: 最远像素距中心 {worst:.1f}dp " + ("OK" if ok else "!! 超安全区"))
    return ok


def main() -> None:
    all_ok = True
    for dens, size in DENSITIES.items():
        folder = os.path.join(OUT, f"mipmap-{dens}")
        os.makedirs(folder, exist_ok=True)

        bg = bg_layer(size)
        bg.save(os.path.join(folder, "ic_launcher_background.png"))

        fg = fruit_silhouette(mono=False).resize((size, size), Image.LANCZOS)
        fg.save(os.path.join(folder, "ic_launcher_foreground.png"))

        mono = fruit_silhouette(mono=True).resize((size, size), Image.LANCZOS)
        mono.save(os.path.join(folder, "ic_launcher_monochrome.png"))

        print(f"{dens} ({size}px):")
        all_ok &= check(fg, "前景")
        all_ok &= check(mono, "单色")

    # 预览图（给人看的，不进 APK）
    prev = Image.new("RGBA", (108 * 3 + 40, 108 + 20), (240, 240, 240, 255))
    bg = bg_layer(108).convert("RGBA")
    fg = fruit_silhouette(False).resize((108, 108), Image.LANCZOS)
    mono = fruit_silhouette(True).resize((108, 108), Image.LANCZOS)
    combined = bg.copy()
    combined.paste(fg, (0, 0), fg)
    prev.paste(bg, (10, 10), bg)
    prev.paste(fg, (10, 10), fg)
    prev.paste(combined, (10 + 128, 10))
    prev.paste(mono, (10 + 256, 10))
    out = os.path.join(REPO, "icon_preview.png")
    prev.convert("RGB").save(out)
    print("预览图:", out)
    print("安全区校验:", "全部通过" if all_ok else "有层超出安全区，请调整几何")


if __name__ == "__main__":
    main()