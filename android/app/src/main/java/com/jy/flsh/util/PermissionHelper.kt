package com.jy.flsh.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 动态权限申请辅助
 */
object PermissionHelper {

    const val REQUEST_LOCATION = 3001

    fun hasLocationPermission(activity: Activity): Boolean {
        return false
//        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity, callback: (Boolean) -> Unit) {
        pendingLocationCallback = callback
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION
        )
    }

    /** 由 MainActivity.onRequestPermissionsResult 转发 */
    fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            pendingLocationCallback?.invoke(granted)
            pendingLocationCallback = null
        }
    }

    private var pendingLocationCallback: ((Boolean) -> Unit)? = null
}