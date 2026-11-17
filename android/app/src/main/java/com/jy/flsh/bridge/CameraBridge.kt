package com.jy.flsh.bridge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.jy.flsh.BuildConfig
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 拍照桥接：调用系统相机拍照，返回文件路径
 */
object CameraBridge {

    private var pendingCallback: ((String) -> Unit)? = null
    private var photoFile: File? = null

    fun start(activity: Activity, json: String, callback: (String) -> Unit) {
        pendingCallback = callback
        val file = createImageFile(activity)
        photoFile = file
        val uri = FileProvider.getUriForFile(
            activity,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        activity.startActivityForResult(intent, REQUEST_CODE)
    }

    /** 由 MainActivity.onActivityResult 转发 */
    fun onResult(activity: Activity, resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            onCancel()
            return
        }
        val file = photoFile ?: run { onCancel(); return }
        val result = JSONObject().apply {
            put("code", 0)
            put("filePath", file.absolutePath)
            put("width", 0)
            put("height", 0)
        }
        pendingCallback?.invoke(result.toString())
        pendingCallback = null
    }

    fun onCancel() {
        val result = JSONObject().apply {
            put("code", 1)
            put("message", "用户取消")
        }
        pendingCallback?.invoke(result.toString())
        pendingCallback = null
    }

    private fun createImageFile(activity: Activity): File {
        val dir = File(activity.filesDir, "photos").apply { mkdirs() }
        val name = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        return File(dir, name)
    }

    const val REQUEST_CODE = 2001
}