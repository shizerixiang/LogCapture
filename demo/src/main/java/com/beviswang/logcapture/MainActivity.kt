package com.beviswang.logcapture

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.beviswang.capturelib.log.CrashLogger
import com.beviswang.capturelib.net.DownloadOpt
import com.beviswang.capturelib.permission.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PermissionHelper.OnRequestPermissionsResultCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 申请权限
        requestDangerousPermission()
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        // 在 MainActivity 中处理在崩溃时的操作
        CrashLogger.hintMsg = "程序崩溃" // 立即崩溃时的提示消息，默认为：程序崩溃
        CrashLogger.dirName = "demo" // 崩溃时，日志文件存储文件夹的名称，最好是独一无二的文件夹名称，生成在程序缓存文件夹内
        CrashLogger.autoClearDay = 5 // 崩溃日志最大保存期限，单位：天 ，默认 5 天
        CrashLogger.crashTime = 6000 // 崩溃时的缓冲时间，单位：毫秒，默认为 3 秒
        // 崩溃日志处理回调，在该回调中对日志文件或信息进行处理
        CrashLogger.instance.setCrashLogListener(object : CrashLogger.ICrashLogListener {
            override fun onCrash(crashLog: String) {
                Toast.makeText(this@MainActivity, crashLog + "", Toast.LENGTH_LONG).show()
            }

            override fun onCrashLogFileCreated(path: String) {
                Toast.makeText(this@MainActivity, path + "", Toast.LENGTH_LONG).show()
            }
        })
        exBtn.setOnClickListener { throw ExceptionInInitializerError("Activity 初始化异常！") }
        downloadBtn.setOnClickListener {
            downloadAPK()
        }
    }

    private fun downloadAPK() {
        val downloader = DownloadOpt(this@MainActivity)
        downloader.mAPKFileName = resources.getString(R.string.app_name) + BuildConfig.VERSION_NAME + ".apk"
        downloader.downloadAPK("http://dl-cdn.coolapkmarket.com/down/apk_upload/2018/1108/f81b5d8361e1e197cd336a443710532e-0-o_1crp2etomvdkhmr1kfq1ghh18c6-uid-408649.apk?_upt=763ea4c41545122171",
                object : DownloadOpt.DownloadCallback {
                    override fun onPaused() {
                        showMsg("下载暂停！")
                    }

                    override fun onPending() {
                        showMsg("下载延迟！")
                    }

                    override fun onRunning(curSize: Int, totalSize: Int) {
                        showMsg("开始下载！$curSize / $totalSize")
                    }

                    override fun onSuccessful(path: String) {
                        showMsg("下载成功！path=$path")
                    }

                    override fun onFailed() {
                        showMsg("下载失败！")
                    }
                })
    }

    private fun showMsg(str: String) {
        Log.e(javaClass.simpleName, str)
    }

    /**
     * 申请危险权限
     */
    private fun requestDangerousPermission() {
        if (PermissionHelper.requestPermissions(this, PERMISSION_REQUEST_CODE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE)) initData()
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
        private const val PERMISSION_REQUEST_CODE: Int = 0x010
    }
}
