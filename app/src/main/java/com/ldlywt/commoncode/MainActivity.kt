package com.ldlywt.commoncode

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ldlywt.commoncode.activity.LiveDataTestActivity
import com.ldlywt.commoncode.databinding.ActivityMainBinding
import com.ldlywt.commoncode.ktx.toast
import com.ldlywt.commoncode.view.LifecycleView

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                toast(activityResult.data?.getStringExtra("key") ?: "")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.btLiveData.setOnClickListener {
            activityResultLauncher.launch(Intent(this, LiveDataTestActivity::class.java))
        }
        mBinding.root.addView(LifecycleView(this, lifecycleOwner = this))
    }
}