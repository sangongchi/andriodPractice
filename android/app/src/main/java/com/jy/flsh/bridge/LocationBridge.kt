package com.jy.flsh.bridge

import android.app.Activity
import android.location.Location
import android.location.LocationManager
import com.jy.flsh.util.PermissionHelper
import org.json.JSONObject

/**
 * 定位桥接：GPS + 网络双源定位，不依赖 GMS
 */
object LocationBridge {

    private var pendingCallback: ((String) -> Unit)? = null

    fun get(activity: Activity, json: String, callback: (String) -> Unit) {
        pendingCallback = callback
        if (!PermissionHelper.hasLocationPermission(activity)) {
            PermissionHelper.requestLocationPermission(activity) { granted ->
                if (granted) {
                    doGet(activity)
                } else {
                    reject("定位权限被拒绝")
                }
            }
        } else {
            doGet(activity)
        }
    }

    private fun doGet(activity: Activity) {
        val lm = activity.getSystemService(Activity.LOCATION_SERVICE) as LocationManager
        val location = getBestLocation(lm)
        if (location == null) {
            reject("无法获取定位，请检查定位开关")
            return
        }
        val result = JSONObject().apply {
            put("code", 0)
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("accuracy", location.accuracy)
            put("provider", location.provider)
            put("timestamp", location.time)
        }
        pendingCallback?.invoke(result.toString())
        pendingCallback = null
    }

    /** 取 GPS 与网络定位中最新的一次 */
    private fun getBestLocation(lm: LocationManager): Location? {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        return providers
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private fun reject(message: String) {
        val result = JSONObject().apply {
            put("code", 2)
            put("message", message)
        }
        pendingCallback?.invoke(result.toString())
        pendingCallback = null
    }
}