package ir.romroid.secureboxrecorder.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ir.romroid.secureboxrecorder.ext.logE

object PermissionUtils {

    interface PermissionListener {
        fun observe(permissions: Map<String, Boolean>)
    }

    private lateinit var resultContract: ActivityResultLauncher<Array<String>>

    fun requestPermission(
        context: Context, permissions: Array<String>,
    ) {
        val isNotGranted = permissions.any { isGranted(context, it).not() }
        if (isNotGranted) {
            request(permissions)
        }
    }

    /**
     * should call on [AppCompatActivity.onStart]
     */
    fun register(
        activity: AppCompatActivity,
        listener: PermissionListener,
    ) {

        resultContract = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            listener.observe(permissions)
        }
    }

    /**
     * should call on [Fragment.onAttach] or [Fragment.onStart]
     */
    fun register(
        fragment: Fragment,
        listener: PermissionListener,
    ) {

        resultContract = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            listener.observe(permissions)
        }
    }

    private fun request(
        permissionList: Array<String>,
    ) {
        if (::resultContract.isInitialized)
            resultContract.launch(permissionList)
        else
            "resultContract not initialized".logE("PermissionUtils request")
    }

    fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}