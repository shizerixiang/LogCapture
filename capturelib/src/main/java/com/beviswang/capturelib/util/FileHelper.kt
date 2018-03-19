package com.beviswang.capturelib.util

import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Base64
import com.beviswang.capturelib.logE
import java.io.*

/**
 * 文件处理工具类
 * Created by shize on 2018/1/18.
 */
object FileHelper {
    /**
     * 根据byte数组，生成文件
     *
     * @param bfile 文件数组
     * @param filePath 文件存放路径
     * @param fileName 文件名称
     */
    fun byte2File(bfile: ByteArray, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        val file: File
        try {
            val dir = File(filePath) // 文件夹文件
            if (!dir.exists() && !dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(filePath + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(bfile)
            bos.flush()
        } catch (e: Exception) {
            logE("文件写入失败！")
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 根据 bitmap ，生成图片文件
     *
     * @param bitmap bitmap
     * @param filePath 存放文件路径
     * @param fileName 文件名称
     */
    fun bitmap2File(bitmap: Bitmap, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        val file: File
        try {
            val dir = File(filePath) // 文件夹文件
            if (!dir.exists() && !dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(filePath + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            bos.flush()
        } catch (e: Exception) {
            logE("文件写入失败！")
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获得指定文件的byte数组
     *
     * @param filePath 文件绝对路径
     * @return 文件的 byteArray ，若读取失败则为 null
     */
    fun file2Byte(filePath: String): ByteArray? {
        var bos: ByteArrayOutputStream? = null
        var bis: BufferedInputStream? = null
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException("file not exists")
            }
            bos = ByteArrayOutputStream(file.length().toInt())
            bis = BufferedInputStream(FileInputStream(file))
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var len: Int
            loop@ while (true) {
                len = bis.read(buffer, 0, bufSize)
                if (len == -1) break@loop
                bos.write(buffer, 0, len)
            }
            return bos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                bis?.close()
                bos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(file: File) {
        if (!file.exists()) {
            logE("没有该文件！")
            return
        } else {
            if (file.isFile) {
                file.delete()
                return
            }
            if (file.isDirectory) {
                val childFile = file.listFiles()
                if (childFile == null || childFile.isEmpty()) {
                    file.delete()
                    return
                }
                for (f in childFile) {
                    deleteFile(f)
                }
                file.delete()
            }
        }
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param filePath
     * @param deleteThisPath
     * @return
     */
    fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) {
                    // 如果下面还有文件
                    val files = file.listFiles()
                    for (i in files!!.indices) {
                        deleteFolderFile(files[i].absolutePath, true)
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) {
                        // 如果是文件，删除
                        file.delete()
                    } else {
                        // 目录
                        if (file.listFiles()!!.isEmpty()) {
                            // 目录下没有文件或者目录，删除
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 将文件转成 base64 ByteArray
     * @param path 文件路径
     * @return  Base64 ByteArray
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encodeBase64File(path: String): ByteArray {
        val file = File(path)
        val inputFile = FileInputStream(file)
        val buffer = ByteArray(file.length().toInt())
        inputFile.read(buffer)
        inputFile.close()
        return Base64.encodeToString(buffer, Base64.DEFAULT).toByteArray()
    }

    /**
     * 将base64字符解码保存文件
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decoderBase64File(base64Code: String, targetPath: String) {
        val buffer = Base64.decode(base64Code, Base64.DEFAULT)
        val out = FileOutputStream(targetPath)
        out.write(buffer)
        out.close()
    }
}