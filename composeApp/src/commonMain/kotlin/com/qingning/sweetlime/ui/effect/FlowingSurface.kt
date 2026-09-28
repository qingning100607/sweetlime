/*
 * Copyright 2026 Proify, Tomakino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 本文件移植并修改自开源项目 lyricon（https://github.com/kifranei/lyricon），
 * 原实现位于 app/src/main/kotlin/io/github/proify/lyricon/app/compose/AppComposable.kt。
 * 按 Apache-2.0 第 4(b) 条声明修改：
 *   1. 只覆盖上游覆盖的那几档（surfaceContainer 系列 + surfaceVariant + dividerLine），
 *      也不改上游的 surface —— 二级页/设置页要的是「和主页一模一样的实流光」，
 *      那件事交给页面自己的 flowingPageLayer()（见 FlowingLayer.kt），不靠把 surface 挖空；
 *   2. 明暗判断改为直接在调用点用主题背景亮度求，不再依赖外部传入；
 *   3. 提取成一个独立的小组件，包在页面容器外面；
 *   4. 顺带通过 LocalFlowingBackground 把「当前是不是流光模式」告诉顶栏
 *      （上游是在 AppComposable 里直接把 hazeState 置空的）。
 */
package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 当前是不是「流光背景」模式。
 *
 * 顶栏玻璃、页面底色之类的地方要知道这件事：上游 lyricon 在流光模式下会把顶栏的
 * haze 模糊整个关掉（`hazeState = null`），让底层流光直接透上来。这里用一个
 * CompositionLocal 传下去，省得给每个页面都加一个布尔参数。
 */
val LocalFlowingBackground = staticCompositionLocalOf { false }

/**
 * 「流光背景」的透光层。
 *
 * 开启时把 Miuix 主题里那几档**不透明的容器色**（卡片、列表项默认取的就是它们）
 * 换成半透明，让底下的流光透上来 —— 卡片是「浮在流光上的一层薄白」，而不是把
 * 流光挡在外面的实心白块。
 *
 * 半透明卡片上，分隔线会显得格外突兀，所以顺手把 `dividerLine` 置成透明。
 *
 * 页面自身的底色**不在这里处理**：上游 lyricon 的页面是「不铺自己的底色」，
 * 而 SweetLime 每个二级页都自己铺了一层 `background(surface)`，所以那边由页面
 * 自己再刷一层实流光（`Modifier.flowingPageLayer()`，见 FlowingLayer.kt），
 * 效果就是「二级页背景和主页长得一模一样」。
 *
 * 明暗用**主题背景亮度**实时判断（响应式），而不是某个全局标记 —— 否则切主题的
 * 那一瞬间会用错色板：暗色主题配上浅色流光会糊成一片。
 *
 * [enabled] 为 false 时**原样**直接渲染内容，一行 MiuixTheme 都不套，保证不开流光的
 * 时候观感和以前完全一致。
 */
@Composable
fun FlowingSurface(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        CompositionLocalProvider(LocalFlowingBackground provides false) {
            content()
        }
        return
    }

    val cs = MiuixTheme.colorScheme
    val isDarkBg = cs.background.luminance() < 0.5f
    // 暗色底下流光本身就亮，卡片要压得更实一点才读得清字；浅色则反过来，敢多透一些。
    val cardAlpha = if (isDarkBg) 0.42f else 0.55f

    MiuixTheme(
        colors = cs.copy(
            // 卡片 / 列表项默认取的就是这几档：这几档必须留住一点白，字才读得清。
            surfaceContainer = cs.surfaceContainer.copy(alpha = cardAlpha),
            surfaceContainerHigh = cs.surfaceContainerHigh.copy(alpha = cardAlpha),
            surfaceContainerHighest = cs.surfaceContainerHighest.copy(alpha = cardAlpha),
            surfaceVariant = cs.surfaceVariant.copy(alpha = cardAlpha),
            dividerLine = Color.Transparent,
        ),
    ) {
        CompositionLocalProvider(LocalFlowingBackground provides true) {
            content()
        }
    }
}
