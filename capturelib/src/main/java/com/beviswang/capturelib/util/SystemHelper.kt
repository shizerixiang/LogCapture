package com.beviswang.capturelib.util

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import android.app.Activity
import android.hardware.Camera
import android.support.annotation.RequiresApi
import android.telephony.TelephonyManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 系统数据处理工具类
 * Created by shize on 2017/12/27.
 */
object SystemHelper {
    /**
     * 获取系统时间
     */
    val systemTime: String
        @RequiresApi(Build.VERSION_CODES.N)
        get() {
            val strTime: String
            strTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val c1 = Calendar.getInstance()
                getTimeString(c1.get(Calendar.HOUR_OF_DAY)) + ":" +
                        getTimeString(c1.get(Calendar.MINUTE)) + ":" +
                        getTimeString(c1.get(Calendar.SECOND))
            } else {
                val c2 = java.util.Calendar.getInstance()
                getTimeString(c2.get(java.util.Calendar.HOUR_OF_DAY)) + ":" +
                        getTimeString(c2.get(java.util.Calendar.MINUTE)) + ":" +
                        getTimeString(c2.get(java.util.Calendar.SECOND))
            }
            return strTime
        }

    /**
     * 获取系统时间戳
     * 单位为秒 s
     */
    val systemTimeStamp: Long
        get() {
            return System.currentTimeMillis() / 1000
        }

    /**
     * 获取系统日期
     */
    val systemDate: String
        @RequiresApi(Build.VERSION_CODES.N)
        get() {
            val strDate: String
            strDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val c1 = Calendar.getInstance()
                (c1.get(Calendar.YEAR)).toString() + "-" + (c1.get(Calendar.MONTH) + 1).toString() +
                        "-" + c1.get(Calendar.DAY_OF_MONTH)
            } else {
                val c2 = java.util.Calendar.getInstance()
                (c2.get(java.util.Calendar.YEAR)).toString() + "-" +
                        (c2.get(java.util.Calendar.MONTH) + 1).toString() + "-" +
                        c2.get(java.util.Calendar.DAY_OF_MONTH)
            }
            return strDate
        }

    /** @return 以 YYMMDDhhmmss 的格式取时间 */
    val yyMMddHHmmssData: String
        get() {
            return SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(Date())
        }

    /**
     * 日期格式字符串转换成时间戳
     * @param date_str 字符串日期
     * @return s
     */
    fun yyMMddHHmmss2TimeStamp(date_str: String): Long {
        try {
            val sdf = SimpleDateFormat("yyMMddHHmmss", Locale.CHINA)
            return sdf.parse(date_str).time / 1000
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 将时间戳转换为时间 YYMMDDhhmmss 格式
     */
    fun stampToDate(s: Long): String {
        val res: String
        val simpleDateFormat = SimpleDateFormat("yyMMddHHmmss", Locale.CHINA)
        val date = Date(s)
        res = simpleDateFormat.format(date)
        return res
    }

    /**
     * 将毫秒转换为显示时间字符串，从秒显示到小时
     *
     * @param timeValue   总时间 ms
     * @return 显示时间字符串
     */
    fun getConvertedTime(timeValue: Long): String {
        // 将毫秒转化为秒
        val durationS = (timeValue / 1000).toInt()
        return getTimeString(durationS / 3600) + ":" + getTimeString(durationS / 60 % 60) +
                ":" + getTimeString(durationS % 60)
    }

    /**
     * 将毫秒转换为显示时间字符串，从分钟显示到小时
     *
     * @param timeValue   总时间 min
     * @return 显示时间字符串
     */
    fun getConvertedTimeFromM(timeValue: Int): String {
        // 将毫秒转化为秒
        return getTimeString(timeValue / 60) + ":" + getTimeString(timeValue % 60)
    }

    /**
     * 将时间转化为字符串

     * @param time 时间
     * *
     * @return String
     */
    private fun getTimeString(time: Int): String {
        return if (time < 10) "0" + time else time.toString()
    }

    /**
     * 获取 GPS 状态信息
     *
     * @param context 上下文
     * @return 是否开启
     */
    fun getGPSState(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * @return 测试当前摄像头能否被使用
     */
    fun isCameraCanUse(): Boolean {
        var canUse = true
        var mCamera: Camera? = null
        try {
            mCamera = Camera.open()
        } catch (e: Exception) {
            canUse = false
        }
        if (canUse) {
            mCamera!!.release()
            mCamera = null
        }
        return canUse
    }

    /**
     * 改变 GPS 状态
     *
     * @param context 上下文
     */
    fun changeGPSState(context: Context) {
        val before = getGPSState(context)
        val resolver = context.contentResolver
        if (before) {
            Settings.Secure.putInt(resolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
        } else {
            Settings.Secure.putInt(resolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY)
        }
    }

    /**
     * 检测是否有可用网络活动
     * @param context 上下文
     * @return 网络是否可用
     */
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        networks.filter { connectivityManager.getNetworkInfo(it).state == NetworkInfo.State.CONNECTED }.forEach { return true }
        return false
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    fun getSystemModel(): String {
        return android.os.Build.MODEL
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    fun getDeviceBrand(): String {
        return android.os.Build.BRAND
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    @SuppressLint("MissingPermission")
    fun getIMEI(ctx: Context): String {
        val tm = ctx.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
        return tm.deviceId
    }
}