package com.ldlywt.commoncode.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class FusedLocationHelper constructor(context: Context, externalScope: CoroutineScope) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = createLocationRequest()

    private val TAG = "SharedLocationManager"

    private fun createLocationRequest() = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 2000
        numUpdates = 1
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _locationUpdates: SharedFlow<Location> = callbackFlow<Location> {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result ?: return
                Log.d(TAG, "New location: ${result.lastLocation}")
                offer(result.lastLocation)
            }

        }
        Log.d(TAG, "Starting location updates")

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Stopping location updates")
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    @ExperimentalCoroutinesApi
    fun locationFlow(): Flow<Location> {
        return _locationUpdates
    }
}