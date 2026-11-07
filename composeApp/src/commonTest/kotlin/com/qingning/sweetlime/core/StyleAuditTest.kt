package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals

class StyleAuditTest {

    private val sample = "Ab1中你好(x)"

    @Test
    fun dumpAllStylesForReview() {
        val all = TransformRegistry.all
        val out = StringBuilder()
        out.append("=== 样式全表审计 AUDIT-BEGIN ===\n")
        out.append("共 ").append(all.size).append(" 条；取样输入：").append(sample).append("\n")
        var group = ""
        for (t in all) {
            if (t.group.name != group) {
                group = t.group.name
                out.append("\n### ").append(group).append("（").append(t.group.label).append("）\n")
            }
            out.append("AUDIT\t").append(t.id).append("\t").append(t.title).append("\t")
                .append(t.transform(sample)).append("\n")
        }
        out.append("=== 样式全表审计 AUDIT-END ===\n")
        println(out)
    }

    @Test
    fun styleIdsAreUnique() {
        val ids = TransformRegistry.all.map { it.id }
        val dup = ids.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertEquals(emptySet(), dup.keys, "样式 id 撞车：$dup")
    }

    private val knownDuplicates = emptySet<Pair<StyleGroup, String>>()

    @Test
    fun styleTitlesAreUniqueWithinGroup() {

        val dup = TransformRegistry.all
            .groupingBy { it.group to it.title }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        assertEquals(knownDuplicates, dup, "同分组内样式重名变了：$dup")
    }
}
