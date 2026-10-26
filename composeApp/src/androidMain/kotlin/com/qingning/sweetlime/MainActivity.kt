package com.qingning.sweetlime

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qingning.sweetlime.ui.SweetLimeApp
import com.qingning.sweetlime.ui.effect.AppDarkMode
import com.qingning.sweetlime.ui.effect.systemIsDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 系统栏（状态栏 / 导航栏）的图标颜色必须跟**App 自己的**深浅色一致。
        // 无参的 enableEdgeToEdge() 用的是“跟系统走”的默认实现，而这个默认实现会给
        // 窗口挂一个 EdgeToEdgeCallback —— 之后每次 insets 变化（输入法、旋转、导航方式
        // 切换…）它都会按**系统**的 night 配置重新写一遍系统栏外观，把 Compose 那边设的
        // 值踩回去。所以这里把 detectDarkMode 换成读 AppDarkMode（Compose 侧每帧同步）。
        val detectDark: (android.content.res.Resources) -> Boolean = { res ->
            AppDarkMode.isDark ?: res.systemIsDark()
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detectDark),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detectDark),
        )

        setContent {
            SweetLimeApp()
        }
    }
}
