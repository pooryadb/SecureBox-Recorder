package ir.romroid.secureboxrecorder.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseActivity
import ir.romroid.secureboxrecorder.databinding.ActivityMainBinding
import ir.romroid.secureboxrecorder.ext.logListE

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    private lateinit var navHostFragment: NavHostFragment

    override fun viewHandler(savedInstanceState: Bundle?) {
        binding?.apply {
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragMain) as NavHostFragment
            listenNavController(navHostFragment.navController)
        }
    }

    private fun listenNavController(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, _, _ ->
            controller.backQueue.logListE("pdb backQueue")
        }
    }

    override fun onBackPressed() {
        if (navHostFragment.navController.backQueue.size == 2) {//1 is home_graph & 2 is last fragment
            doubleExit(false)
        } else {
            super.onBackPressed()
        }
    }

    private var doubleBackToExit = false
    private fun doubleExit(forceExit: Boolean) {
        if (doubleBackToExit) {
            if (forceExit)
                finishAndRemoveTask()
            else
                super.onBackPressed()

            return
        }

        this.doubleBackToExit = true
        Toast.makeText(this, getString(R.string.exit_app), Toast.LENGTH_SHORT).show()

        binding?.root?.postDelayed({ doubleBackToExit = false }, 2000)
    }

}