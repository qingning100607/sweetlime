package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 把每个样式的「名字 → 实际输出」全表打出来（走测试 stdout，落进 test-results 的 XML 里）。
 *
 * 起因：有样式叫「彩色字母」，输出却是区域指示符（源站就这么叫，我照抄了名字），
 * 用户看到「名字和东西对不上」。与其一个个猜，不如把全表摊开看。
 *
 * 声明：commonTest 里不能碰 java.io（多平台源码集），所以只打印、不写文件。
 */
class StyleAuditTest {

    /** 取样：大小写、数字、中文、空格、括号各来一点。 */
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

    /**
     * 已知重复：**现在是空的**。
     *
     * 曾经有 4 对（花样网名 3 对 + 翅膀装饰 1 对）同组同名、输出也一模一样，
     * 用户会看到两条完全一样的条目；已按用户要求删掉重复的那一条。
     *
     * 这里钉的是「不允许重复」：以后往表里加样式时，一旦撞出同组重名，测试立刻红。
     */
    private val knownDuplicates = emptySet<Pair<StyleGroup, String>>()

    @Test
    fun styleTitlesAreUniqueWithinGroup() {
        // 同一分组里两个样式同名，用户就分不清点哪个（跨分组同名可以接受）
        val dup = TransformRegistry.all
            .groupingBy { it.group to it.title }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        assertEquals(knownDuplicates, dup, "同分组内样式重名变了：$dup")
    }
}
