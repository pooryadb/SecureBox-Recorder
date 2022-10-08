package ir.romroid.secureboxrecorder.base.component

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.ext.getAttrColor
import ir.romroid.secureboxrecorder.ext.isDarkTheme
import ir.romroid.secureboxrecorder.ext.logE
import ir.romroid.secureboxrecorder.utils.language.LocaleUtils
import ir.romroid.secureboxrecorder.utils.state.WindowInsetsHelper


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    internal lateinit var windowInsetsHelper: WindowInsetsHelper

    protected abstract val bindingInflater: (LayoutInflater) -> VB
    protected var binding: ViewBinding? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.setLocale(this)
        resetTitle()
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)

        binding = bindingInflater.invoke(layoutInflater)

        setContentView(requireNotNull(binding).root)

        windowInsetsHelper = WindowInsetsHelper(window, binding?.root)
        configNavigationAndStatusBar()

        viewHandler(savedInstanceState)

        initObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.setLocale(this)
    }

    protected abstract fun viewHandler(savedInstanceState: Bundle?)
    protected open fun initObservers() {}

    /**
     * fixes android RTL
     */
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(
                componentName, PackageManager.GET_META_DATA
            ).labelRes
            if (label != 0) {
                setTitle(label)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.logE("resetTitle error")
        }
    }

    private fun configNavigationAndStatusBar() {
        window.statusBarColor = getAttrColor(android.R.attr.statusBarColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.rootView.windowInsetsController?.let {
                it.setSystemBarsAppearance(
                    if (isDarkTheme()) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )

                it.setSystemBarsAppearance(
                    if (isDarkTheme()) 0 else WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )

            }
        } else {
            val lightBackground = getAttrColor(android.R.attr.statusBarColor)
            val darkBackground = getAttrColor(android.R.attr.navigationBarColor)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = darkBackground
            } else {
                window.navigationBarColor = if (isDarkTheme()) darkBackground else lightBackground
            }

            window.statusBarColor = if (isDarkTheme()) darkBackground else lightBackground
        }


    }

}