package com.qingning.sweetlime

import android.app.Application
import android.content.Context

/**
 * 全局 Application 上下文持有者，供 clipboard / 持久化等平台能力使用。
 */
object AppContext {
    private var application: Application? = null

    fun init(app: Application) {
        application = app
    }

    fun get(): Context = requireNotNull(application) {
        "AppContext 尚未初始化，请确认 AndroidManifest 中已声明 SweetLimeApplication"
    }
}

class SweetLimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
    }
}