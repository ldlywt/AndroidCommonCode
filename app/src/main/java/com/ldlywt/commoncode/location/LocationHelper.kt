package com.ldlywt.commoncode.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import kotlinx.coroutines.delay

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getLocation(context: Context, timeout: Long, callback: (location: Location?) -> Unit) {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            var bestLocation: Location? = null
            var hasSendResult = false

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (isBetterLocation(location, bestLocation)) {
                    bestLocation = location
                }
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (isBetterLocation(location, bestLocation)) {
                    bestLocation = location
                }
            }

            var gpsListener: LocationListener?
            var networkListener: LocationListener? = null
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (isBetterLocation(location, bestLocation)) {
                        bestLocation = location
                    }
                    locationManager.removeUpdates(this)
                    gpsListener = null
                    if (bestLocation != null) {
                        callback(bestLocation)
                        hasSendResult = true
                        networkListener?.let {
                            locationManager.removeUpdates(it)
                        }
                    }
                }
            }.also { gpsListener = it }

            networkListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (isBetterLocation(location, bestLocation)) {
                        bestLocation = location
                    }
                    locationManager.removeUpdates(this)
                    networkListener = null
                }
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsListener?.let {
                    requestSingleUpdate(context, locationManager, LocationManager.GPS_PROVIDER, it) {
                        callback(bestLocation)
                        hasSendResult = true
                    }
                }
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkListener?.let {
                    requestSingleUpdate(context, locationManager, LocationManager.NETWORK_PROVIDER, it) {
                        callback(bestLocation)
                        hasSendResult = true
                    }
                }
            }

            delay(timeout)

            if (!hasSendResult) callback(bestLocation)

            gpsListener?.let { locationManager.removeUpdates(it) }

            networkListener?.let { locationManager.removeUpdates(it) }
        } catch (t: SecurityException) {
            callback(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestSingleUpdate(context: Context, locationManager: LocationManager, provider: String, locationListener: LocationListener, resultCallback: ((location: Location) -> Unit)? = null) {
        if (!locationManager.isProviderEnabled(provider)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(provider, null, context.mainExecutor) { location ->
                location?.let {
                    resultCallback?.invoke(it)
                }

            }
        } else {
            locationManager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper())
        }
    }


    private fun isBetterLocation(location: Location?, currentBestLocation: Location?): Boolean {
        if (location == null) {
            return false
        }
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        val TWO_MINUTES = 1000 * 60 * 2

        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        // Check if the old and new location are from the same provider
        val isFromSameProvider = location.provider == currentBestLocation.provider

        // Not significantly newer or older, so check for Accuracy
        if (isMoreAccurate) {
            // If more accurate return true
            return true
        } else if (isNewer && !isLessAccurate) {
            // Same accuracy but newer, return true
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            // Accuracy is less (not much though) but is new, so if from same
            // provider return true
            return true
        }
        return false
    }
}