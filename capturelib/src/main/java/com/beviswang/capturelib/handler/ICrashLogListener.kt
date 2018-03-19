package com.beviswang.capturelib.handler

/**
 * 异常崩溃日志发生接口
 * Created by shize on 2018/3/19.
 */
interface ICrashLogListener {
    /**
     * 出现异常崩溃时，日志信息的显示
     *
     * @param crashLog 日志信息显示字符串
     */
    fun onCrash(crashLog: String)

    /**
     * 当出现异常崩溃时，需要进行的日志上传操作
     *
     * @param path 崩溃日志路径
     */
    fun onCrashLogFileCreated(path: String)
}