package com.beviswang.capturelib

import android.util.Log
import java.lang.ref.WeakReference

/**
 * 执行消息任务
 * Created by shize on 2018/2/6.
 */
class ExecutorContext<T>(val weakRef: WeakReference<T>)
/** 日志过滤关键字 */
private const val logKey = "-ExecutorFile-"

/**
 * Print the log of Info type.
 *
 * @param msg Print log content.
 */
fun <T> T.logI(msg: String?) {
    val clazzName =logKey + (this as Any).javaClass.simpleName
    if (msg == null) {
        Log.i(clazzName, "$logKey null")
        return
    }
    Log.i(clazzName, msg)
}

/**
 * Print the log of Error type.
 *
 * @param msg Print log content.
 */
fun <T> T.logE(msg: String?) {
    val clazzName = logKey + (this as Any).javaClass.simpleName
    if (msg == null) {
        Log.e(clazzName, "$logKey null")
        return
    }
    Log.e(clazzName, msg)
}

/**
 * Print the log of Debug type.
 *
 * @param msg Print log content.
 */
fun <T> T.logD(msg: String?) {
    val clazzName = logKey+ (this as Any).javaClass.simpleName
    if (msg == null) {
        Log.d(clazzName, "$logKey null")
        return
    }
    Log.d(clazzName, msg)
}

/**
 * Print the log of Warning type.
 *
 * @param msg Print log content.
 */
fun <T> T.logW(msg: String?) {
    val clazzName =logKey + (this as Any).javaClass.simpleName
    if (msg == null) {
        Log.w(clazzName, "$logKey null")
        return
    }
    Log.w(clazzName, msg)
}