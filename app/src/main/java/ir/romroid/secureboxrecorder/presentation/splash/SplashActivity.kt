package ir.romroid.secureboxrecorder.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import ir.romroid.secureboxrecorder.base.component.BaseActivity
import ir.romroid.secureboxrecorder.databinding.ActivitySplashBinding
import ir.romroid.secureboxrecorder.presentation.main.MainActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySplashBinding
        get() = ActivitySplashBinding::inflate

    override fun viewHandler(savedInstanceState: Bundle?) {
        binding?.apply {
            root.postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 1000)
        }
    }

}