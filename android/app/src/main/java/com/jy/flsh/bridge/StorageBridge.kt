package com.jy.flsh.bridge

import android.content.Context
import org.json.JSONObject

/**
 * 本地存储桥接：SharedPreferences 轻量 KV 存储
 * 业务关键数据（token、配置、草稿）建议走此通道，跨页面共享、持久
 */
object StorageBridge {

    private const val PREFS_NAME = "flsh_storage"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setItem(context: Context, json: String, callback: (String) -> Unit) {
        val obj = JSONObject(json)
        val key = obj.optString("key")
        val value = obj.optString("value")
        if (key.isBlank()) { reject(callback, "key 不能为空"); return }
        prefs(context).edit().putString(key, value).apply()
        callback(JSONObject().put("code", 0).toString())
    }

    fun getItem(context: Context, json: String, callback: (String) -> Unit) {
        val obj = JSONObject(json)
        val key = obj.optString("key")
        val value = prefs(context).getString(key, null)
        val result = JSONObject().apply {
            put("code", 0)
            put("value", value ?: JSONObject.NULL)
        }
        callback(result.toString())
    }

    fun removeItem(context: Context, json: String, callback: (String) -> Unit) {
        val obj = JSONObject(json)
        val key = obj.optString("key")
        prefs(context).edit().remove(key).apply()
        callback(JSONObject().put("code", 0).toString())
    }

    fun clear(context: Context, callback: (String) -> Unit) {
        prefs(context).edit().clear().apply()
        callback(JSONObject().put("code", 0).toString())
    }

    private fun reject(callback: (String) -> Unit, message: String) {
        callback(JSONObject().apply {
            put("code", 1)
            put("message", message)
        }.toString())
    }
}