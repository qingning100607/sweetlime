package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals

class BackPolicyTest {

    @Test
    fun secondLevelPageIsPoppedFirst() {

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
