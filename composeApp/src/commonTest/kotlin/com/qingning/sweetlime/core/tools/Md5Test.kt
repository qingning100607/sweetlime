package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class Md5Test {

    @Test
    fun rfcVectors() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5.hash(""))
        assertEquals("900150983cd24fb0d6963f7d28e17f72", Md5.hash("abc"))
        assertEquals("5d41402abc4b2a76b9719d911017c592", Md5.hash("hello"))
        assertEquals(
            "9e107d9d372bb6826bd81d3542a419d6",
            Md5.hash("The quick brown fox jumps over the lazy dog"),
        )
    }

    @Test
    fun multibyteUtf8() {

        assertEquals("65396ee4aad0b4f17aacd1c6112ee364", Md5.hash("你好世界"))

        assertEquals("3c3bd526d9852519e7c09f10b3430c58", Md5.hash("🍋"))
    }

    @Test
    fun paddingBoundaries() {

        assertEquals("ef1772b6dff9a122358552954ad0df65", Md5.hash("a".repeat(55)))
        assertEquals("3b0c8ac703f828b04c6c197006d17218", Md5.hash("a".repeat(56)))
        assertEquals("b06521f39153d618550606be297466d5", Md5.hash("a".repeat(63)))
        assertEquals("014842d480b571495a4a0363793f7367", Md5.hash("a".repeat(64)))
        assertEquals("c743a45e0d2e6a95cb859adae0248435", Md5.hash("a".repeat(65)))
        assertEquals("8a7bd0732ed6a28ce75f6dabc90e1613", Md5.hash("a".repeat(119)))
        assertEquals("5f61c0ccad4cac44c75ff505e1f1e537", Md5.hash("a".repeat(120)))
    }

    @Test
    fun longInput() {
        assertEquals("cabe45dcc9ae5b66ba86600cca6b8ba8", Md5.hash("a".repeat(1000)))
    }

    @Test
    fun sixteenCharForm() {
        assertEquals("3cd24fb0d6963f7d", Md5.hash16("abc"))
        assertEquals(32, Md5.hash("abc").length)
        assertEquals(16, Md5.hash16("abc").length)
    }
}