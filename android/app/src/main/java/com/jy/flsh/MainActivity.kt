package com.jy.flsh

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.jy.flsh.bridge.CameraBridge
import com.jy.flsh.bridge.JSBridge
import com.jy.flsh.bridge.LocationBridge
import com.jy.flsh.bridge.ScanBridge
import com.jy.flsh.databinding.ActivityMainBinding
import com.jy.flsh.scan.ScanActivity
import com.jy.flsh.util.PermissionHelper

/**
 * 主界面：WebView 容器 + 桥接入口
 */
class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView
        private set

    private lateinit var binding: ActivityMainBinding
    private lateinit var webViewManager: WebViewManager
    private lateinit var jsBridge: JSBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView
        webViewManager = WebViewManager(webView)
        jsBridge = JSBridge(this)

        webViewManager.setup(jsBridge)
        webViewManager.loadDefaultUrl()
    }

    override fun onBackPressed() {
        // 优先返回网页历史，否则退出
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // 扫码结果回传
            ScanActivity.REQUEST_CODE -> {
                val result = data?.getStringExtra("result")
                if (result != null) {
                    ScanBridge.onResult(result)
                } else {
                    ScanBridge.onCancel()
                }
            }
            // 拍照结果回传
            CameraBridge.REQUEST_CODE -> {
                CameraBridge.onResult(this, resultCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onPermissionResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        webViewManager.destroy()
        super.onDestroy()
    }
}