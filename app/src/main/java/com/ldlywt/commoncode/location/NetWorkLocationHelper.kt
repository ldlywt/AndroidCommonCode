package com.ldlywt.commoncode.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class NetWorkLocationHelper(context: Context, externalScope: CoroutineScope) {

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    suspend fun getNetLocation(context: Context, callback: (location: Location) -> Unit) {

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


    @SuppressLint("MissingPermission")
    suspend fun getNetLocation(context: Context): Location? = suspendCancellableCoroutine { continuation ->
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            continuation.resume(null)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, context.mainExecutor) { location ->
                continuation.resume(location)
            }
        } else {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, { location ->
                continuation.resume(location)
            }, Looper.getMainLooper())
        }
    }

    /**
     * 注意！不要在每个函数调用时创建新的实例
     * 切勿在调用某个函数调用返回时，使用 shareIn 或 stateIn 创建新的数据流。
     * 这样会在每次函数调用时创建一个新的 SharedFlow 或 StateFlow，而它们将会一直保持在内存中，直到作用域被取消或者在没有任何引用时被垃圾回收。
     */
    @ExperimentalCoroutinesApi
    fun getNetLocationFlow(): Flow<Location?> {
        return _locationUpdates
    }

    /**
     * 返回Flow流封装的，支持操作符，支持背压
     */
    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow<Location?> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, context.mainExecutor) { location ->
                offer(location)
            }
            awaitClose()
        } else {
            val locationListener = LocationListener { location -> offer(location) }
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper())
            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

}