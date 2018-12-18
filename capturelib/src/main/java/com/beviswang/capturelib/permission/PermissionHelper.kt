package com.beviswang.capturelib.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

import java.util.ArrayList

/**
 * 权限申请工具类
 * Created by shize on 2017/12/21.
 */

object PermissionHelper {
    /**
     * 是否需要检查权限
     */
    private fun needCheckPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * 获取sd存储卡读写权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getExternalStoragePermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    /**
     * 获取拍照权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getCameraPermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    /**
     * 获取麦克风权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getAudioPermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    /**
     * 获取定位权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getLocationPermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * 获取读取联系人权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getContactsPermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.READ_CONTACTS)
    }

    /**
     * 获取发送短信权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getSendSMSPermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.SEND_SMS)
    }

    /**
     * 获取拨打电话权限
     *
     * @return 是否已经获取权限，没有自动申请
     */
    fun getCallPhonePermissions(activity: Activity, requestCode: Int): Boolean {
        return requestPermissions(activity, requestCode, Manifest.permission.CALL_PHONE)
    }

    private fun getDeniedPermissions(activity: Activity, vararg permissions: String): List<String>? {
        if (!needCheckPermission()) {
            return null
        }
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        return if (!deniedPermissions.isEmpty()) {
            deniedPermissions
        } else null
    }

    /**
     * 是否拥有权限
     */
    private fun hasPermissions(activity: Activity, vararg permissions: String): Boolean {
        if (!needCheckPermission()) {
            return true
        }
        return permissions.none {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 是否拒绝了再次申请权限的请求（点击了不再询问）
     */
    private fun deniedRequestPermissionsAgain(activity: Activity, vararg permissions: String): Boolean {
        if (!needCheckPermission()) {
            return false
        }
        val deniedPermissions = getDeniedPermissions(activity, *permissions)
        deniedPermissions?.filter {
            ContextCompat.checkSelfPermission(activity, it) !=
                    PackageManager.PERMISSION_DENIED && !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }?.forEach {
            //当用户之前已经请求过该权限并且拒绝了授权这个方法返回true
            return true
        }
        return false
    }

    /**
     * 打开app详细设置界面<br></br>
     *
     *
     * 在 onActivityResult() 中没有必要对 resultCode 进行判断，因为用户只能通过返回键才能回到我们的 App 中，<br></br>
     * 所以 resultCode 总是为 RESULT_CANCEL，所以不能根据返回码进行判断。<br></br>
     * 在 onActivityResult() 中还需要对权限进行判断，因为用户有可能没有授权就返回了！<br></br>
     */
    private fun startApplicationDetailsSettings(activity: Activity, requestCode: Int) {
        Toast.makeText(activity, "点击权限，并打开全部权限", Toast.LENGTH_SHORT).show()

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 申请权限
     * 使用onRequestPermissionsResult方法，实现回调结果或者自己普通处理
     * 注意：这里需要通过判断返回值来确定是否已经获取到权限
     *
     * @return 是否已经获取权限
     */
    fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String): Boolean {
        if (!needCheckPermission()) {
            return true
        }
        if (!hasPermissions(activity, *permissions)) {
            if (deniedRequestPermissionsAgain(activity, *permissions)) {
                startApplicationDetailsSettings(activity, requestCode)
                //返回结果onActivityResult
            } else {
                val deniedPermissions = getDeniedPermissions(activity, *permissions)
                if (deniedPermissions != null) {
                    ActivityCompat.requestPermissions(activity, deniedPermissions.toTypedArray(), requestCode)
                    //返回结果onRequestPermissionsResult
                }
            }
            return false
        }
        return true
    }

    /**
     * 申请权限返回方法
     * 在 Activity 下的同名方法中调用即可
     * 通过回调接口判断权限是否申请成功
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                   grantResults: IntArray, callBack: OnRequestPermissionsResultCallbacks) {
        val granted = ArrayList<String>()
        val denied = ArrayList<String>()
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }

        if (!granted.isEmpty()) {
            callBack.onPermissionsGranted(requestCode, granted, denied.isEmpty())
        }
        if (!denied.isEmpty()) {
            callBack.onPermissionsDenied(requestCode, denied, granted.isEmpty())
        }
    }

    /**
     * 申请权限返回接口
     */
    interface OnRequestPermissionsResultCallbacks {
        /**
         * @param isAllGranted 是否全部同意
         */
        fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?, isAllGranted: Boolean)

        /**
         * @param isAllDenied 是否全部拒绝
         */
        fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?, isAllDenied: Boolean)

    }
}
