package com.beviswang.logcapture

import android.app.Application
import com.beviswang.capturelib.handler.CrashHandler

/**
 * 演示 Demo
 * Created by shize on 2018/3/19.
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在 Application 中进行崩溃日志初始化
        CrashHandler.instance.init(this)
    }
}