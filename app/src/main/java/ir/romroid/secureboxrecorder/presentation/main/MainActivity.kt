package ir.romroid.secureboxrecorder.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseActivity
import ir.romroid.secureboxrecorder.databinding.ActivityMainBinding
import ir.romroid.secureboxrecorder.ext.logListE
import ir.romroid.secureboxrecorder.ext.toast

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    private lateinit var navHostFragment: NavHostFragment

    private var doubleBackToExit = false

    override fun viewHandler(savedInstanceState: Bundle?) {
        binding?.apply {
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragMain) as NavHostFragment
            listenNavController(navHostFragment.navController)
        }
    }

    override fun onBackPressed() {
        if (navHostFragment.navController.backQueue.size == 2) {//1 is the graph & 2 is last fragment
            doubleExit(false)
        } else {
            super.onBackPressed()
        }
    }

    private fun listenNavController(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, _, _ ->
            controller.backQueue.logListE("$TAG backQueue")
        }
    }

    private fun doubleExit(forceExit: Boolean) {
        if (doubleBackToExit) {
            if (forceExit)
                finishAndRemoveTask()
            else
                super.onBackPressed()

            return
        }

        this.doubleBackToExit = true
        toast(getString(R.string.exit_app))

        binding?.root?.postDelayed({ doubleBackToExit = false }, BACK_EXIT_DELAY_MILLIS)
    }

    private companion object {
        const val TAG = "MainActivity"
        const val BACK_EXIT_DELAY_MILLIS = 2000L
    }

}