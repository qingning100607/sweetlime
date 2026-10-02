package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.tools.ResourceSniffer.Kind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResourceSnifferTest {

    private val base = "https://www.example.com/blog/post.html"

    @Test
    fun absolutizeBasicForms() {
        assertEquals("https://www.example.com/blog/img/logo.png", ResourceSniffer.absolutize(base, "img/logo.png"))
        assertEquals("https://www.example.com/static/a.css", ResourceSniffer.absolutize(base, "/static/a.css"))
        assertEquals("https://cdn.other.com/x.js", ResourceSniffer.absolutize(base, "//cdn.other.com/x.js"))
        assertEquals("http://plain.com/y", ResourceSniffer.absolutize(base, "http://plain.com/y"))
        assertEquals("https://www.example.com/img/up.png", ResourceSniffer.absolutize(base, "../img/up.png"))
        assertEquals("https://www.example.com/blog/post.html?q=1", ResourceSniffer.absolutize(base, "?q=1"))
        assertEquals("https://www.example.com/a/b/c.png", ResourceSniffer.absolutize(base, "/a/./b/../b/c.png"))
        // 没有路径的基准地址
        assertEquals("https://www.example.com/x.png", ResourceSniffer.absolutize("https://www.example.com", "x.png"))
    }

    @Test
    fun absolutizeSkipsNonHttp() {
        assertNull(ResourceSniffer.absolutize(base, "data:image/png;base64,AAAA"))
        assertNull(ResourceSniffer.absolutize(base, "javascript:void(0)"))
        assertNull(ResourceSniffer.absolutize(base, "#top"))
        assertNull(ResourceSniffer.absolutize(base, "   "))
        assertNull(ResourceSniffer.absolutize(base, "mailto:a@b.com"))
    }

    @Test
    fun classifyByExtension() {
        assertEquals(Kind.IMAGE, ResourceSniffer.classify("https://a.com/x.PNG"))
        assertEquals(Kind.IMAGE, ResourceSniffer.classify("https://a.com/x.jpeg?v=2"))
        assertEquals(Kind.STYLE, ResourceSniffer.classify("https://a.com/x.css"))
        assertEquals(Kind.SCRIPT, ResourceSniffer.classify("https://a.com/x.js#hash"))
        assertEquals(Kind.FONT, ResourceSniffer.classify("https://a.com/f.woff2"))
        assertEquals(Kind.MEDIA, ResourceSniffer.classify("https://a.com/v.mp4"))
        assertEquals(Kind.OTHER, ResourceSniffer.classify("https://a.com/api/get"))
    }

    private val html = """
        <!doctype html><html><head><title>t</title>
        <link rel="stylesheet" href="/css/site.css">
        <link rel="icon" href="favicon.ico">
        <link rel="canonical" href="/post.html">
        <style>.hero{background:url('../img/hero.jpg')}</style>
        </head><body>
        <img src="img/logo.png" srcset="img/logo@2x.png 2x, img/logo@3x.png 3x" data-src="img/lazy.webp">
        <script src="//cdn.example.com/a.js"></script>
        <video src="v/clip.mp4" poster="img/poster.jpg"><source src="v/clip.webm"></video>
        <iframe src="frame.html"></iframe>
        <div style="background-image:url(/img/bg.png)"></div>
        <meta property="og:image" content="https://cdn.example.com/og.jpg">
        <a href="/page2.html">下一页</a>
        <img src="data:image/png;base64,iVBORw0KGgo=">
        </body></html>
    """.trimIndent()

    @Test
    fun sniffsHtml() {
        val found = ResourceSniffer.sniffHtml(html, base)
        assertEquals(
            listOf(
                "https://www.example.com/img/hero.jpg",
                "https://www.example.com/css/site.css",
                "https://www.example.com/blog/favicon.ico",
                "https://www.example.com/blog/img/logo.png",
                "https://www.example.com/blog/img/lazy.webp",
                "https://www.example.com/blog/img/logo@2x.png",
                "https://www.example.com/blog/img/logo@3x.png",
                "https://cdn.example.com/a.js",
                "https://www.example.com/blog/v/clip.mp4",
                "https://www.example.com/blog/img/poster.jpg",
                "https://www.example.com/blog/v/clip.webm",
                "https://www.example.com/blog/frame.html",
                "https://www.example.com/img/bg.png",
                "https://cdn.example.com/og.jpg",
            ),
            found.map { it.url },
        )
        // 类型要分对：样式表 / 脚本 / 图片 / 视频
        assertEquals(Kind.STYLE, found[1].kind)
        assertEquals(Kind.IMAGE, found[3].kind)
        assertEquals(Kind.SCRIPT, found[7].kind)
        assertEquals(Kind.MEDIA, found[8].kind)
        // 同一个地址出现两次只留一条
        assertEquals(found.size, found.map { it.url }.distinct().size)
    }

    @Test
    fun sniffsCss() {
        val css = """
            @import "theme/base.css";
            .a { background: url(/img/a.png); }
            @font-face { src: url('fonts/x.woff2') format('woff2'); }
            .b { background: url("/img/a.png"); }
            .c { background: url(data:image/gif;base64,AAA); }
        """.trimIndent()
        val found = ResourceSniffer.sniffCss(css, "https://a.com/css/main.css", from = "https://a.com/css/main.css")
        assertEquals(
            listOf(
                "https://a.com/img/a.png",
                "https://a.com/css/fonts/x.woff2",
                "https://a.com/css/theme/base.css",
            ),
            found.map { it.url },
        )
        assertEquals(Kind.FONT, found[1].kind)
        assertEquals(Kind.STYLE, found[2].kind)
        assertEquals("https://a.com/css/main.css", found[0].from)
    }

    @Test
    fun respectsLimit() {
        val many = (1..20).joinToString("") { "<img src=\"i$it.png\">" }
        assertEquals(3, ResourceSniffer.sniffHtml(many, base, limit = 3).size)
    }

    @Test
    fun suggestsSaveFileName() {
        assertEquals("logo.png", ResourceSniffer.suggestFileName("https://a.com/img/logo.png"))
        assertEquals("logo.png", ResourceSniffer.suggestFileName("https://a.com/img/logo.png?v=2"))
        assertEquals("a.mp4", ResourceSniffer.suggestFileName("https://cdn.a.com/v/a.mp4#t=1"))
        // 以 / 结尾（目录式地址）就取最后一段当名字
        assertEquals("img", ResourceSniffer.suggestFileName("https://a.com/img/"))
        assertEquals("a.com", ResourceSniffer.suggestFileName("https://a.com"))
    }
}
