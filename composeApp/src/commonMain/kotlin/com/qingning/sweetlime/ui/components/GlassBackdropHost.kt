package com.qingning.sweetlime.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

@Composable
fun GlassBackdropHost(content: @Composable () -> Unit) {
    val flowing = LocalFlowingBackground.current
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(
        LocalGlassBackdrop provides if (flowing) null else backdrop,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            content()
        }
    }
}