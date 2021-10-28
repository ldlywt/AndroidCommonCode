package com.ldlywt.commoncode.livedata

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData

class NetworkWatchLiveData(context: Context) : LiveData<NetworkInfo?>() {
    private val mContext = context.applicationContext
    private val mNetworkReceiver: NetworkReceiver = NetworkReceiver()
    private val mIntentFilter: IntentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    override fun onActive() {
        mContext.registerReceiver(mNetworkReceiver, mIntentFilter)
    }

    override fun onInactive() = mContext.unregisterReceiver(mNetworkReceiver)

    private class NetworkReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = manager.activeNetworkInfo
            sInstance.postValue(activeNetwork)
        }
    }

    companion object {

        private lateinit var sInstance: NetworkWatchLiveData

        @MainThread
        fun get(context: Context): NetworkWatchLiveData {
            sInstance = if (Companion::sInstance.isInitialized) sInstance else NetworkWatchLiveData(context)
            return sInstance
        }
    }
}