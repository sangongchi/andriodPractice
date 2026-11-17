package com.jy.flsh

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jy.flsh.bridge.JSBridge
import com.jy.flsh.config.AppConfig

/**
 * WebView 容器管理：配置、生命周期、缓存策略、错误处理
 */
class WebViewManager(
    private val webView: WebView
) {

    private val mainHandler = Handler(Looper.getMainLooper())

    /** 本轮已尝试过的地址，避免备用地址循环重试 */
    private val triedUrls = mutableListOf<String>()

    /** 当前正在尝试的地址，供错误页展示 */
    private var currentUrl: String = AppConfig.DEFAULT_URL

    private var timeoutTask: Runnable? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun setup(jsBridge: JSBridge) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            // 开发环境允许 HTTP 时放开混合内容限制，生产仅 HTTPS 则禁止
            mixedContentMode = if (AppConfig.ALLOW_HTTP) {
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            } else {
                WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
        }

        // 注入原生桥接对象，前端通过 window.AndroidBridge 调用
        webView.addJavascriptInterface(jsBridge, "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelLoadTimeout()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // 仅主框架加载失败才切换地址/展示错误页，子资源失败不影响页面
                if (request?.isForMainFrame == true) {
                    cancelLoadTimeout()
                    handleLoadFailure()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                // 仅允许白名单域名内的导航，其余拦截（安全策略）
                if (!isAllowedUrl(url)) return true
                // 从错误页点击“重试”回到入口地址时，重置重试链路
                if (url == AppConfig.DEFAULT_URL) {
                    triedUrls.clear()
                    currentUrl = url
                    scheduleLoadTimeout()
                }
                return false
            }
        }

        webView.webChromeClient = WebChromeClient()
    }

    /** 加载默认地址，失败时按备用列表尝试 */
    fun loadDefaultUrl() {
        triedUrls.clear()
        loadWithFallback(AppConfig.DEFAULT_URL)
    }

    fun loadUrl(url: String) {
        triedUrls.clear()
        loadWithFallback(url)
    }

    /** 判断 URL 是否在白名单内 */
    fun isAllowedUrl(url: String): Boolean {
        val host = Uri.parse(url).host ?: return false
        val scheme = Uri.parse(url).scheme ?: return false
        if (!AppConfig.ALLOW_HTTP && scheme != "https") return false
        return AppConfig.ALLOWED_HOSTS.contains(host)
    }

    private fun loadWithFallback(url: String) {
        currentUrl = url
        if (!triedUrls.contains(url)) {
            triedUrls.add(url)
        }
        webView.loadUrl(url)
        scheduleLoadTimeout()
    }

    /** 主框架失败或超时：尝试下一个备用地址，全部失败后展示错误页 */
    private fun handleLoadFailure() {
        cancelLoadTimeout()
        val next = (listOf(AppConfig.DEFAULT_URL) + AppConfig.FALLBACK_URLS)
            .firstOrNull { !triedUrls.contains(it) }
        if (next != null) {
            loadWithFallback(next)
        } else {
            showErrorPage()
        }
    }

    private fun scheduleLoadTimeout() {
        cancelLoadTimeout()
        val task = Runnable { handleLoadFailure() }
        timeoutTask = task
        mainHandler.postDelayed(task, AppConfig.LOAD_TIMEOUT_MS)
    }

    private fun cancelLoadTimeout() {
        timeoutTask?.let { mainHandler.removeCallbacks(it) }
        timeoutTask = null
    }

    /** 内置离线/错误页（打包进 App，断网时兜底） */
    private fun showErrorPage() {
        val html = """
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
            <body style="text-align:center;padding-top:35%;font-family:sans-serif;background:#f5f5f5;color:#333">
                <h2>⚠️ 网络连接失败</h2>
                <p>无法访问：$currentUrl</p>
                <p style="color:#888;font-size:14px">请检查网络，或确认开发服务已启动</p>
                <button onclick="location.href='${AppConfig.DEFAULT_URL}'" style="padding:12px 40px;font-size:16px;border:none;border-radius:8px;background:#1A73E8;color:#fff">重试</button>
            </body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL(AppConfig.DEFAULT_URL, html, "text/html", "UTF-8", null)
    }

    fun destroy() {
        cancelLoadTimeout()
        webView.stopLoading()
        webView.settings.javaScriptEnabled = false
        webView.removeAllViews()
        webView.destroy()
    }
}
