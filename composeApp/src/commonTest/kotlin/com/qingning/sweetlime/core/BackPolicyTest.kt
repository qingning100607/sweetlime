package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 返回键决策的回归。
 *
 * 这三条顺序很关键：把「在工具页直接退出」写成结果，用户就会觉得返回键坏了；
 * 把「转换页还要先跳一下」写成结果，就退不出去。
 */
class BackPolicyTest {

    @Test
    fun secondLevelPageIsPoppedFirst() {
        // 有二级页时，不管底栏停在哪一页，都先退二级页
        assertEquals(BackAction.PopPage, resolveBackAction(atRoot = false, selectedTab = 0))
        assertEquals(BackAction.PopPage, resolveBackAction(atRoot = false, selectedTab = 2))
    }

    @Test
    fun otherTabsGoBackToConvertTab() {
        assertEquals(BackAction.ToConvertTab, resolveBackAction(atRoot = true, selectedTab = 1))
        assertEquals(BackAction.ToConvertTab, resolveBackAction(atRoot = true, selectedTab = 2))
    }

    @Test
    fun convertTabExitsTheApp() {
        assertEquals(BackAction.ExitApp, resolveBackAction(atRoot = true, selectedTab = 0))
    }
}
