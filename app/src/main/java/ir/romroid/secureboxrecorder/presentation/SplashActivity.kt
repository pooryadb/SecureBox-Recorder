package ir.romroid.secureboxrecorder.presentation

import android.os.Bundle
import android.view.LayoutInflater
import ir.romroid.secureboxrecorder.base.component.BaseActivity
import ir.romroid.secureboxrecorder.base.ext.logD
import ir.romroid.secureboxrecorder.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySplashBinding
        get() = ActivitySplashBinding::inflate

    override fun viewHandler(savedInstanceState: Bundle?) {
        binding?.apply {
            root.postDelayed({
                "finish".logD("pdb")
//                startActivity(MainActivity.getIntent(this@SplashActivity))
                finish()
            }, 1000)
        }
    }

}