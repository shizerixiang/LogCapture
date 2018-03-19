## 程序崩溃日志小工具 v1.0.0

### 添加依赖方式：
#### Step 1. 添加以下语句到你 Project 的 build.gradle 下

``` java
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

#### Step 2. 添加依赖库到你 Module 的 build.gradle 下

``` java
	dependencies {
	        compile 'com.github.shizerixiang:LogCapture:v1.0.0'
	}
```
