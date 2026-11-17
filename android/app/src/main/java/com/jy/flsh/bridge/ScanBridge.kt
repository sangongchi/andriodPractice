package com.jy.flsh.bridge

import android.app.Activity
import android.content.Intent
import com.jy.flsh.scan.ScanActivity
import org.json.JSONObject

/**
 * 扫码桥接：启动 ScanActivity，接收识别结果并回调前端
 */
object ScanBridge {

    private var pendingCallback: ((String) -> Unit)? = null

    fun start(activity: Activity, json: String, callback: (String) -> Unit) {
        pendingCallback = callback
        val intent = Intent(activity, ScanActivity::class.java)
        activity.startActivityForResult(intent, ScanActivity.REQUEST_CODE)
    }

    /** 由 MainActivity.onActivityResult 转发 */
    fun onResult(resultJson: String) {
        pendingCallback?.invoke(resultJson)
        pendingCallback = null
    }

    fun onCancel() {
        val result = JSONObject().apply {
            put("code", 1)
            put("message", "用户取消")
        }
        onResult(result.toString())
    }
}