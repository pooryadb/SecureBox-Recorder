package ir.romroid.secureboxrecorder.presentation.safe

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.repository.AppRepository
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SafeViewModel @Inject constructor(
    private val appRepository: AppRepository
) : BaseViewModel() {

    fun shouldSetUserKey(): Boolean {
        return appRepository.userKey().isEmpty()
    }

    fun shouldChangeUserKey(): Boolean {
        val lastTime = appRepository.userKeyTime()
        val maxDiffTime = 604800000 // 1 week

        val diff = abs(System.currentTimeMillis() - lastTime)

        return diff > maxDiffTime
    }

    fun saveUserKey(key: String) {
        appRepository.appCache.userKey = key
        appRepository.appCache.userKeyTime = System.currentTimeMillis()
    }

    fun saveRecoveryKey(key: String) {
        appRepository.appCache.recoveryKey = key
    }

    fun getFileNameFromCursor(context: Context, uri: Uri): String? {
        val fileCursor: Cursor? = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        var fileName: String? = null
        if (fileCursor != null && fileCursor.moveToFirst()) {
            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex)
            }
        }
        return fileName
    }

}