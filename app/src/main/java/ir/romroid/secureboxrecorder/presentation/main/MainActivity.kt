package ir.romroid.secureboxrecorder.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseActivity
import ir.romroid.secureboxrecorder.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    private var isHome: Boolean = true

    override fun viewHandler(savedInstanceState: Bundle?) {
        binding?.apply {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragMain) as NavHostFragment
            listenNavController(navHostFragment.navController)
        }
    }

    private fun listenNavController(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            isHome = destination.id == controller.graph.startDestination
        }
    }

    override fun onBackPressed() {
        if (isHome) {
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