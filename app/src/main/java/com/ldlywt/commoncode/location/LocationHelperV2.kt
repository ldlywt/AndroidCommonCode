package com.ldlywt.commoncode.location

import android.content.Context
import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 先获取网络定位 ，如果当前在室内，拿不到 gps 的情况下，设置2s 超时，如果拿的到 gps 位置，就返回最新的 gps 位置
 */
class LocationHelperV2(val context: Context, val externalScope: CoroutineScope) {

    suspend fun getLocation(): Location? {
        var location: Location? =
            NetWorkLocationHelper(context, externalScope).getNetLocation(context)
        withTimeoutOrNull(2000) {
            FusedLocationHelper(context, externalScope)
                .locationFlow()
                .collect {
                    location = it
                }
        }
        return location
    }
}