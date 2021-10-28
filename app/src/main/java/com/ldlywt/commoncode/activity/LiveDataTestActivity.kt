package com.ldlywt.commoncode.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ldlywt.commoncode.R
import com.ldlywt.commoncode.databinding.ActivityLiveDataTestBinding
import com.ldlywt.commoncode.ktx.toast
import com.ldlywt.commoncode.livedata.RequestPermissionLiveData
import com.ldlywt.commoncode.livedata.TakePhotoLiveData
import com.ldlywt.commoncode.livedata.TimerGlobalLiveData

class LiveDataTestActivity : AppCompatActivity(R.layout.activity_live_data_test) {

    private val mBinding by lazy { ActivityLiveDataTestBinding.inflate(layoutInflater) }

    private var takePhotoLiveData: TakePhotoLiveData =
        TakePhotoLiveData(activityResultRegistry, "key")

    private var requestPermissionLiveData = RequestPermissionLiveData(activityResultRegistry, "key")

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result: Boolean ->
            toast("request permission $result")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        init()
    }

    private fun init() {

        takePhotoLiveData.observe(this) { bitmap ->
            mBinding.imageView.setImageBitmap(bitmap)
        }

        mBinding.btTakePhoto.setOnClickListener {
            takePhotoLiveData.takePhoto()
        }

        mBinding.btStopTimer.setOnClickListener {
            //启动全局计算器
            //TimerGlobalLiveData.get().startTimer()
            TimerGlobalLiveData.get().cancelTimer()
        }

        TimerGlobalLiveData.get().observe(this) {
            Log.i("LiveDataTestActivity", "GlobalTimer value: == $it")
        }

        mBinding.btRequestPermission.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        mBinding.btRequestPermissionV2.setOnClickListener {
            requestPermissionLiveData.requestPermission(Manifest.permission.RECORD_AUDIO)
        }

        requestPermissionLiveData.observe(this) {
            toast("权限RECORD_AUDIO请求结果   $it")
        }

        mBinding.btBack.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("key", "返回消息"))
            finish()
        }
    }
}