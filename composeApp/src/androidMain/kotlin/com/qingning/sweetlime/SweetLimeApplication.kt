package com.qingning.sweetlime

import android.app.Application
import android.content.Context

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