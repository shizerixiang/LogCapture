## 程序崩溃日志小工具 v1.1.0

### 版本 v1.0.1
#### 修改崩溃日志文件缓存位置，现在缓存不需要读写权限，默认位置改为软件 Cache 目录下

### 版本 v1.1.0
#### 修改文件目录结构，完善下载类 DownloadOpt.kt，现在可以回调一些下载信息

### 添加依赖方式：
#### Step 1. 添加以下语句到你 Project 的 build.gradle 下

``` gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

#### Step 2. 添加依赖库到你 Module 的 build.gradle 下

``` gradle
dependencies {
        implementation 'com.github.shizerixiang:LogCapture:v1.1.0'
}
```

### Using Kotlin
Application：
``` kotlin
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在 Application 中进行崩溃日志初始化
        CrashHandler.instance.init(this)
    }
}
```

Activity：
``` kotlin
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
```

