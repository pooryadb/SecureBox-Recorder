package ir.romroid.secureboxrecorder.base.component

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import ir.romroid.common.utils.state.WindowInsetsHelper
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.ext.isDarkTheme
import ir.romroid.secureboxrecorder.base.ext.logE
import ir.romroid.secureboxrecorder.utils.language.LocaleUtils


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB

    lateinit var windowInsetsHelper: WindowInsetsHelper

    @Suppress("UNCHECKED_CAST")
    val binding: VB?
        get() = _binding as VB?

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.setLocale(this)

        resetTitle()

        _binding = bindingInflater.invoke(layoutInflater)
        ViewCompat.setLayoutDirection(
            requireNotNull(_binding).root,
            ViewCompat.LAYOUT_DIRECTION_RTL
        )

        setContentView(requireNotNull(_binding).root)

        windowInsetsHelper = WindowInsetsHelper(window, binding?.root)
        configNavigationAndStatusBar()

        viewHandler(savedInstanceState)

        initObservers()
    }

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
        window.statusBarColor = Color.TRANSPARENT

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
            val lightBackground = ContextCompat.getColor(this, R.color.backgroundColorLight)
            val darkBackground = ContextCompat.getColor(this, R.color.backgroundColorDark)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = darkBackground
            } else {
                window.navigationBarColor = if (isDarkTheme()) darkBackground else lightBackground
            }

//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                window.statusBarColor = darkBackground
//            } else {
            window.statusBarColor = if (isDarkTheme()) darkBackground else lightBackground
//            }
        }


    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.setLocale(this)
    }

    abstract fun viewHandler(savedInstanceState: Bundle?)

    protected open fun initObservers() {
    }

}