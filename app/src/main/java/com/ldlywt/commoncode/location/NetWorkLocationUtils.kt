package com.ldlywt.commoncode.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build

object NetWorkLocationUtils {

    @SuppressLint("MissingPermission")
    fun getNetLocation(context: Context, callback: (location: Location) -> Unit) {

        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, context.mainExecutor) { location ->
                callback.invoke(location)
            }
        } else {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, { location ->
                callback.invoke(location)
            }, null)
        }
    }
}