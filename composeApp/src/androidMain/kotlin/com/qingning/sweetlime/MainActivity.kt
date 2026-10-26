package com.qingning.sweetlime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qingning.sweetlime.ui.SweetLimeApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注意：这里**不调** enableEdgeToEdge()。
        // 系统栏外观跟着 App 自己的深浅色走，而且是「深浅色一变就重新应用」——
        // 这件事放在 Compose 侧做（见 ui/effect/SystemBarAppearance.android.kt），
        setContent {
            SweetLimeApp()
        }
    }
}
