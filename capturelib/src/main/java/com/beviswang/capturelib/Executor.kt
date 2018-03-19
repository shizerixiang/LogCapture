package com.beviswang.capturelib

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 执行消息任务
 * Created by shize on 2018/2/6.
 */
class ExecutorContext<T>(val weakRef: WeakReference<T>)
/** 日志过滤关键字 */
private val logKey = "-ExecutorFile-"
/** Single thread executor. */
private val exServer = Executors.newSingleThreadExecutor()!!

/**
 * 只存在一个线程的异步任务
 * 可以通过 [uiThread] 返回到主线程中
 */
fun <T> T.doSend(exService: ExecutorService = exServer,
                 task: ExecutorContext<T>.() -> Unit): Future<*> {
    val context = ExecutorContext(WeakReference(this))
    return exService.submit { context.task() }
}

/**
 * 在 [doSend] 子线程中返回主线程执行任务
 * In the [doSend] method, return the UI thread to execute the task.
 */
fun <T> ExecutorContext<T>.uiThread(f: (T) -> Unit): Boolean {
    val ref = weakRef.get() ?: return false
    if (ContextHelper.mainThread == Thread.currentThread()) {
        f(ref)
    } else {
        ContextHelper.handler.post { f(ref) }
    }
    return true
}

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

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
    val mainThread: Thread = Looper.getMainLooper().thread
}