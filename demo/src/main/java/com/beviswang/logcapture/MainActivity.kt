package com.beviswang.logcapture

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.beviswang.capturelib.handler.CrashHandler
import com.beviswang.capturelib.handler.ICrashLogListener
import com.beviswang.capturelib.util.PermissionHelper

class MainActivity : AppCompatActivity(), PermissionHelper.OnRequestPermissionsResultCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 申请权限
//        requestDangerousPermission()
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        // 在 MainActivity 中处理在崩溃时的操作
        CrashHandler.hintMsg = "程序崩溃" // 立即崩溃时的提示消息，默认为：程序崩溃
        CrashHandler.dirName = "demo" // 崩溃时，日志文件存储文件夹的名称，最好是独一无二的文件夹名称，生成路径在扩展存储的根目录
        CrashHandler.autoClearDay = 5 // 崩溃日志最大保存期限，单位：天 ，默认 5 天
        CrashHandler.crashTime = 6000 // 崩溃时的缓冲时间，单位：毫秒，默认为 3 秒
        // 崩溃日志处理回调，在该回调中对日志文件或信息进行处理
        CrashHandler.instance.setCrashLogListener(object : ICrashLogListener {
            override fun onCrash(crashLog: String) {
                Toast.makeText(this@MainActivity, crashLog + "", Toast.LENGTH_LONG).show()
            }

            override fun onCrashLogFileCreated(path: String) {
                Toast.makeText(this@MainActivity, path + "", Toast.LENGTH_LONG).show()
            }
        })
        throw ExceptionInInitializerError("Activity 初始化异常！")
    }

    /**
     * 申请危险权限
     */
    private fun requestDangerousPermission() {
        if (PermissionHelper.requestPermissions(this, PERMISSION_REQUEST_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)) initData() else finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?, isAllGranted: Boolean) {
        Log.i(javaClass.simpleName, "同意授予权限！")
        initData()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?, isAllDenied: Boolean) {
        Log.e(javaClass.simpleName, "拒绝授予权限！")
        finish()
    }

    companion object {
        /**
         * 权限申请代码
         */
        private val PERMISSION_REQUEST_CODE: Int = 0x010
    }
}
