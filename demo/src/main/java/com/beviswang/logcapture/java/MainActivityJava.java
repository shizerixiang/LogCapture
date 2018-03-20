package com.beviswang.logcapture.java;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.beviswang.capturelib.handler.CrashHandler;
import com.beviswang.capturelib.handler.ICrashLogListener;
import com.beviswang.capturelib.util.PermissionHelper;
import com.beviswang.logcapture.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by shize on 2018/3/20.
 */
public class MainActivityJava extends AppCompatActivity implements PermissionHelper.OnRequestPermissionsResultCallbacks {
    private static int PERMISSION_REQUEST_CODE = 0x102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestDangerousPermission();
    }

    /**
     * 初始化数据
     */
    private void initData(){
        // 在 MainActivity 中处理在崩溃时的操作
        CrashHandler.Companion.setHintMsg("程序崩溃"); // 立即崩溃时的提示消息，默认为：程序崩溃
        CrashHandler.Companion.setDirName("demo"); // 崩溃时，日志文件存储文件夹的名称，最好是独一无二的文件夹名称，生成路径在扩展存储的根目录
        CrashHandler.Companion.setAutoClearDay(5); // 崩溃日志最大保存期限，单位：天 ，默认 5 天
        CrashHandler.Companion.setCrashTime(6000); // 崩溃时的缓冲时间，单位：毫秒，默认为 3 秒
        // 崩溃日志处理回调，在该回调中对日志文件或信息进行处理
        CrashHandler.Companion.getInstance().setCrashLogListener(new ICrashLogListener() {
            @Override
            public void onCrash(@NotNull String crashLog) {
                Toast.makeText(MainActivityJava.this, crashLog + "", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCrashLogFileCreated(@NotNull String path) {
                Toast.makeText(MainActivityJava.this, path + "", Toast.LENGTH_LONG).show();
            }
        });
        throw new ExceptionInInitializerError("Activity 初始化异常！");
    }

    private void requestDangerousPermission(){
        if (PermissionHelper.INSTANCE.requestPermissions(this, PERMISSION_REQUEST_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)) initData(); else finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @org.jetbrains.annotations.Nullable List<String> perms, boolean isAllGranted) {
        Log.i(getClass().getSimpleName(), "同意授予权限！");
        initData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @org.jetbrains.annotations.Nullable List<String> perms, boolean isAllDenied) {
        Log.e(getClass().getSimpleName(), "拒绝授予权限！");
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.INSTANCE.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
