package com.beviswang.capturelib.handler

import android.os.Build
import android.content.pm.PackageManager
import android.os.SystemClock
import android.os.Looper
import android.widget.Toast
import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import com.beviswang.capturelib.util.FileHelper
import com.beviswang.capturelib.util.SystemHelper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 崩溃日志捕获处理类
 * <h3> 全局捕获异常 </h3>
 * <br> 当程序发生Uncaught异常的时候,有该类来接管程序,并记录错误日志 </br>
 * Created by shize on 2018/3/19.
 */
@SuppressLint("SimpleDateFormat")
class CrashHandler
/** 保证只有一个CrashHandler实例  */
private constructor() : Thread.UncaughtExceptionHandler {
    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null
    // 用来存储设备信息和异常信息
    private val info = HashMap<String, String>()
    // 用于格式化日期,作为日志文件名的一部分，这里使用时间戳
    private val formatter = SystemHelper.systemTimeStamp
    // 崩溃日志回调接口
    private var listener: ICrashLogListener? = null

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
        autoClear(autoClearDay)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            SystemClock.sleep(crashTime)
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息; 否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null)
            return false
        try {
            // 使用Toast来显示异常信息
            object : Thread() {
                override fun run() {
                    Looper.prepare()
                    Toast.makeText(mContext, hintMsg, Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
            }.start()
            // 收集设备参数信息
            collectDeviceInfo(mContext)
            // 保存日志文件
            saveCrashInfoFile(ex)
            SystemClock.sleep(crashTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.packageManager
            val pi = pm.getPackageInfo(ctx.packageName,
                    PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = pi.versionName + ""
                val versionCode = pi.versionCode.toString()
                info.put("versionName", versionName)
                info.put("versionCode", versionCode)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(dirName, "an error occured when collect package info", e)
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                info.put(field.name, field.get(null).toString())
            } catch (e: Exception) {
                Log.e(dirName, "an error occured when collect crash info", e)
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun saveCrashInfoFile(ex: Throwable): String? {
        val sb = StringBuffer()
        try {
            val sDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = sDateFormat.format(java.util.Date())
            sb.append("\r\n" + date + "\n")
            for ((key, value) in info) {
                sb.append(key + "=" + value + "\n")
            }

            val writer = StringWriter()
            val printWriter = PrintWriter(writer)
            ex.printStackTrace(printWriter)
            var cause: Throwable? = ex.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            printWriter.flush()
            printWriter.close()
            val result = writer.toString()
            object :Thread(){
                override fun run() {
                    Looper.prepare()
                    listener?.onCrash(result)
                    Looper.loop()
                }
            }.start()
            sb.append(result)
            return writeFile(sb.toString())
        } catch (e: Exception) {
            Log.e(dirName, "an error occured while writing file...", e)
            sb.append("an error occured while writing file...\r\n")
            writeFile(sb.toString())
        }
        return null
    }

    @Throws(Exception::class)
    private fun writeFile(sb: String): String {
        val fileName = "$formatter.txt"
        val path = globalPath
        val dir = File(path)
        if (!dir.exists())
            dir.mkdirs()
        val fos = FileOutputStream(path + fileName, true)
        fos.write(sb.toByteArray())
        fos.flush()
        fos.close()
        object :Thread(){
            override fun run() {
                Looper.prepare()
                listener?.onCrashLogFileCreated(path + fileName)
                Looper.loop()
            }
        }.start()
        return fileName
    }

    /**
     * 文件删除
     * @param autoClearDay 文件保存天数
     */
    private fun autoClear(autoClearDay: Int) {
        val clearDayAgo = formatter - (autoClearDay * 86400) // 保存的最大时限，即最小时间戳
        val dir = File(globalPath)
        if (dir.isDirectory) dir.listFiles().forEach {
            try {
                if (it.nameWithoutExtension.toInt() < clearDayAgo) FileHelper.deleteFile(it)
            } catch (e: NumberFormatException) {
                FileHelper.deleteFile(it)
            }
        }
    }

    /**
     * 设置崩溃日志监听器
     *
     * @param listener 监听器
     */
    fun setCrashLogListener(listener: ICrashLogListener) {
        this.listener = listener
    }

    companion object {
        var dirName = "Crash" // 日志保存文件夹名称
        var autoClearDay = 5 // 自动删除日志文件的期限日期 单位：天
        var hintMsg = "程序崩溃" // 程序崩溃提示信息
        var crashTime:Long = 3000 // 崩溃上传及提示信息时间
        /** 获取CrashHandler实例 ,单例模式  */
        @SuppressLint("StaticFieldLeak")
        val instance = CrashHandler()

        private val globalPath: String
            get() = (Environment.getExternalStorageDirectory().absolutePath
                    + File.separator + dirName + File.separator)
    }

}