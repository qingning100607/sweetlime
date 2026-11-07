package com.qingning.sweetlime.ui.nav

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Home : Route

    @Serializable
    data class Group(val groupId: String) : Route

    @Serializable
    data class Item(val styleId: String, val input: String) : Route

    @Serializable
    data class Tool(val id: String) : Route

    @Serializable
    data class Symbols(val index: Int) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Privacy : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Licenses : Route
}