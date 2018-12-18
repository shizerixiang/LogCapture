package com.beviswang.capturelib.net

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.io.File
import java.lang.ref.WeakReference

/**
 * 安装包下载类
 * @author BevisWang
 * @date 2018/12/18 13:35
 */
class DownloadOpt(context: Context) {
    // 上下文
    private val context = WeakReference<Context>(context)
    /** 获取下载管理器 */
    private val mDownloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var mTaskId: Long = 0
    // 下载回调
    private var mCallback: DownloadCallback? = null
    // 是否下载结束
    private var isDownloadOver: Boolean = true
    /** 文件名 */
    var mAPKFileName = "app-release.apk"

    /**
     * 下载文件
     *
     * @param url 下载地址
     * @param callback 下载状态回调 default:null
     */
    fun downloadAPK(url: String, callback: DownloadCallback? = null) {
        mCallback = callback
        // 创建下载目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir()
        // 创建请求对象
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("自动升级程序")
        /** 设置用于下载时的网络状态 */
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setMimeType("application/vnd.android.package-archive")
        /** 设置通知栏是否可见 */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        /** 设置漫游状态下是否可以下载 */
        request.setAllowedOverRoaming(false)
        /** 如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
        我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true. */
        request.setVisibleInDownloadsUi(true)
        // 先移除已经存在的安装文件
        removeOldAPK()
        /** 设置文件保存路径 */
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mAPKFileName)
        startTimer()
        isDownloadOver = false
        // 将下载任务加入下载队列，否则不会进行下载
        mTaskId = mDownloadManager.enqueue(request)
        registerReceiver()
    }

    /** 定时更新 UI */
    private fun startTimer() {
        Thread(Runnable {
            while (!isDownloadOver) {
                context.get()?.sendBroadcast(Intent(ACTION_DOWNLOAD_MSG))
                Thread.sleep(1000)
            }
        }).start()
    }

    /** 下载状态广播接收器 */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkDownloadStatus()
        }
    }

    /** 检测下载状态 */
    private fun checkDownloadStatus() {
        val query = DownloadManager.Query()
        // 筛选下载任务，传入任务ID，可变参数
        query.setFilterById(mTaskId)
        val c = mDownloadManager.query(query)
        if (c.moveToFirst()) {
            val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_PAUSED -> mCallback?.onPaused()
                DownloadManager.STATUS_PENDING -> mCallback?.onPending()
                DownloadManager.STATUS_RUNNING -> {
                    // 已下载文件大小
                    val curSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    // 文件总大小
                    val totalSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    mCallback?.onRunning(curSize, totalSize)
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    isDownloadOver = true
                    val filePath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + mAPKFileName
                    mCallback?.onSuccessful(filePath)
                    // 普通提示用户安装 APK
                    installAPK(filePath)
                    c.close()
                    unRegister()
                }
                DownloadManager.STATUS_FAILED -> {
                    isDownloadOver = true
                    mCallback?.onFailed()
                    c.close()
                    unRegister()
                }
            }
        }
    }

    /** 注册下载状态监听广播 */
    private fun registerReceiver() {
        val iFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        iFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        iFilter.addAction(ACTION_DOWNLOAD_MSG)
        context.get()?.registerReceiver(mReceiver, iFilter)
    }

    /** 解绑广播接收器 */
    private fun unRegister() {
        context.get()?.unregisterReceiver(mReceiver)
    }

    /** 移除老 APK 文件 */
    private fun removeOldAPK() {
        val dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val filePath = dirPath + File.separator + mAPKFileName
        val file = File(filePath)
        if (file.exists() && file.isFile) {
            file.delete()
        }
    }

    /**
     * 提示安装程序
     * 路径：Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/app-release.apk"
     *
     * @param path 安装包文件路径
     */
    private fun installAPK(path: String) {
        // 非安装文件则不进行安装
        if (!mAPKFileName.toLowerCase().contains(".apk".toLowerCase())) return
        Log.d(javaClass.simpleName, "开始安装程序")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse("file://$path"), "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 4.0 以上系统弹出安装成功打开界面
        context.get()?.startActivity(intent)
    }

    /** 下载状态回调接口 */
    interface DownloadCallback {
        /** 下载暂停 */
        fun onPaused() {}

        /** 下载延迟 */
        fun onPending() {}

        /** 正在下载 */
        fun onRunning(curSize: Int, totalSize: Int) {}

        /** 下载成功 */
        fun onSuccessful(path: String)

        /** 下载失败 */
        fun onFailed()
    }

    companion object {
        private const val ACTION_DOWNLOAD_MSG = "com.beviswang.capturelib.download.message"
    }
}