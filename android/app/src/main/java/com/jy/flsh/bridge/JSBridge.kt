package com.jy.flsh.bridge

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import com.jy.flsh.BuildConfig
import com.jy.flsh.MainActivity
import com.jy.flsh.config.AppConfig
import com.jy.flsh.detail.DetailActivity
import org.json.JSONException
import org.json.JSONObject

/**
 * 原生桥接入口
 *
 * 前端通过 window.AndroidBridge 调用以下方法：
 *   scan(json, callbackId) / takePhoto / getLocation / setItem / getItem / removeItem / clearStorage / getAppInfo / closeApp
 *
 * 线程约束：@JavascriptInterface 方法默认运行在 WebView 的 "JavaBridge" 线程，
 * 而 WebView.getUrl() / evaluateJavascript() 以及 Activity.startActivityForResult()
 * 都必须在创建 WebView 的主线程执行，否则会抛
 * "A WebView method was called on thread 'JavaBridge'"，
 * JS 侧表现为 "Java exception was raised during method invocation"（扫码直接失败）。
 * 因此所有桥接实现统一经 [invoke] 切到主线程，并把异常转成错误回调，绝不向 JS 抛异常。
 *
 * 安全策略：所有接口调用前校验当前页面 host 是否在白名单内
 */
class JSBridge(private val activity: MainActivity) {

    // ==================== 扫码 ====================
    @JavascriptInterface
    fun scan(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            ScanBridge.start(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    // ==================== 拍照 ====================
    @JavascriptInterface
    fun takePhoto(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            CameraBridge.start(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    // ==================== 定位 ====================
    @JavascriptInterface
    fun getLocation(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            LocationBridge.get(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    // ==================== 本地存储 ====================
    @JavascriptInterface
    fun setItem(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            StorageBridge.setItem(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    @JavascriptInterface
    fun getItem(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            StorageBridge.getItem(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    @JavascriptInterface
    fun removeItem(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            StorageBridge.removeItem(activity, json ?: "{}") { result -> resolve(callbackId, result) }
        }
    }

    @JavascriptInterface
    fun clearStorage(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            StorageBridge.clear(activity) { result -> resolve(callbackId, result) }
        }
    }

    // ==================== 页面跳转 ====================
    @JavascriptInterface
    fun openDetail(json: String?, callbackId: String?) {
        invoke(callbackId) {
            Log.d(TAG, "openDetail 入参: json=$json, callbackId=$callbackId")
            if (!checkHost(callbackId)) return@invoke
            val obj = try {
                JSONObject(json ?: "{}")
            } catch (e: JSONException) {
                Log.w(TAG, "openDetail 参数解析失败: ${e.message}")
                reject(callbackId, "INVALID_PARAM", "参数不是合法 JSON")
                return@invoke
            }
            val rawId = obj.opt("id")
            val rawTitle = obj.opt("title")
            Log.d(
                TAG,
                "openDetail 解析结果: id=$rawId (${rawId?.javaClass?.simpleName}), title=$rawTitle (${
                    rawTitle?.javaClass?.simpleName
                })"
            )
            if (rawId !is String && rawId !is Number) {
                Log.w(TAG, "openDetail id 类型非法，原始 json=$json")
                reject(callbackId, "INVALID_PARAM", "id 必须是字符串或数字")
                return@invoke
            }
            val id = rawId.toString().trim()
            if (id.isBlank()) {
                reject(callbackId, "INVALID_PARAM", "缺少 id")
                return@invoke
            }
            if (id.startsWith("{") || id.startsWith("[")) {
                Log.w(TAG, "openDetail id 是 JSON 字符串，疑似把 JS 事件对象序列化后传了进来: $id")
                reject(callbackId, "INVALID_PARAM", "id 不能是 JSON/对象字符串")
                return@invoke
            }
            val title = if (rawTitle is String) rawTitle.takeIf { it.isNotBlank() } else null
            Log.d(TAG, "openDetail 跳转参数: id=$id, title=$title")
            activity.startActivity(DetailActivity.newIntent(activity, id, title))
            resolve(callbackId, JSONObject().put("code", 0).toString())
        }
    }

    // ==================== 应用信息 ====================
    @JavascriptInterface
    fun getAppInfo(json: String?, callbackId: String?) {
        invoke(callbackId) {
            if (!checkHost(callbackId)) return@invoke
            val info = JSONObject().apply {
                put("appVersion", BuildConfig.VERSION_NAME)
                put("osVersion", Build.VERSION.RELEASE)
                put("deviceModel", Build.MODEL)
                put("currentUrl", activity.webView.url ?: "")
            }
            resolve(callbackId, info.toString())
        }
    }

    // ==================== 关闭应用 ====================
    @JavascriptInterface
    fun closeApp(json: String?, callbackId: String?) {
        invoke(callbackId) {
            activity.finish()
        }
    }

    // ==================== 内部工具 ====================

    /**
     * 在主线程执行桥接逻辑，并捕获所有异常
     *
     * 不能让异常逃逸出 @JavascriptInterface 方法，否则 WebView 会向 JS 抛
     * "Java exception was raised during method invocation"，前端只能看到一句无意义的报错。
     */
    private fun invoke(callbackId: String?, block: () -> Unit) {
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
            try {
                block()
            } catch (t: Throwable) {
                Log.e(TAG, "原生桥接调用异常: ${t.message}", t)
                reject(callbackId, "INTERNAL_ERROR", "原生调用失败：${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    /** 校验当前页面 host 是否在白名单内，不通过时直接回调错误 */
    private fun checkHost(callbackId: String?): Boolean {
        if (isAllowedHost()) return true
        reject(callbackId, "FORBIDDEN", "当前页面不在白名单内")
        return false
    }

    /** 校验当前页面 host 是否在白名单内（必须在主线程调用） */
    private fun isAllowedHost(): Boolean {
        val url = activity.webView.url ?: return false
        val host = Uri.parse(url).host ?: return false
        return AppConfig.ALLOWED_HOSTS.contains(host)
    }

    /** 将结果回调给前端（主线程执行） */
    private fun resolve(callbackId: String?, resultJson: String) {
        val id = callbackId ?: return
        activity.runOnUiThread {
            try {
                val js = "window.__nativeCallbacks && window.__nativeCallbacks['$id'] && window.__nativeCallbacks['$id']($resultJson);"
                activity.webView.evaluateJavascript(js, null)
            } catch (t: Throwable) {
                Log.e(TAG, "回调前端失败: ${t.message}", t)
            }
        }
    }

    /** 拒绝调用（返回错误结果） */
    private fun reject(callbackId: String?, code: String, message: String) {
        Log.w(TAG, "桥接调用被拒绝[$code]: $message")
        val result = JSONObject().apply {
            put("code", 1)
            put("errorCode", code)
            put("message", message)
        }
        resolve(callbackId, result.toString())
    }

    companion object {
        private const val TAG = "JSBridge"
    }
}
