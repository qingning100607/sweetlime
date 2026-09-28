package com.qingning.sweetlime.ui.nav

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

/**
 * 应用的路由。整个层级可序列化，所以返回栈能在配置变化 / 进程被杀之后原样恢复。
 *
 * 路由只带**可序列化的轻量参数**（id / 下标 / 原文），真正的样式对象交给注册表还原 ——
 * 这样路由不依赖任何运行时对象，恢复时才不会出岔子。
 */
@Serializable
sealed interface Route : NavKey {

    /** 底层：主页 / 工具 / 收藏（含底部导航栏与右下角设置）。 */
    @Serializable
    data object Home : Route

    /** 分类页：某个分组下的全部样式。 */
    @Serializable
    data class Group(val groupId: String) : Route

    /** 单个样式详情：[styleId] + 当时的原文 [input] 就能算出同样的结果。 */
    @Serializable
    data class Item(val styleId: String, val input: String) : Route

    /** 工具页：拼音 / 拆字 / 数字大写 / 飞鸟文 / 特殊符号。 */
    @Serializable
    data class Tool(val id: String) : Route

    /** 特殊符号里的某一个分类。 */
    @Serializable
    data class Symbols(val index: Int) : Route

    /** 设置。 */
    @Serializable
    data object Settings : Route

    /** 全局搜索。 */
    @Serializable
    data object Search : Route

    /** 隐私政策。 */
    @Serializable
    data object Privacy : Route

    /** 开源许可。 */
    @Serializable
    data object Licenses : Route
}