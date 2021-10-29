package com.ldlywt.commoncode.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * see https://xuyisheng.top/lifecycle/
 */
class LifecycleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, lifecycleOwner: LifecycleOwner)
    : View(context, attrs, defStyleAttr), LifecycleEventObserver {

    init {
        Log.i("wutao--> ", "init: ")
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun release() {
        Log.i("wutao--> ", "release")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                release()
                source.lifecycle.removeObserver(this)
            }
            Lifecycle
                .Event.ON_RESUME -> {
                Log.i("wutao--> ", "ON_RESUME: ")
            }
        }
    }

}