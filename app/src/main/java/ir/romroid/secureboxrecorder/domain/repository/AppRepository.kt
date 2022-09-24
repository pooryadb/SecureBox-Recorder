package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.provider.AppCache
import ir.romroid.secureboxrecorder.domain.provider.FileProvider
import javax.inject.Inject

class AppRepository @Inject constructor(
    val appCache: AppCache,
    val fileProvider: FileProvider
) : BaseRepository() {

    fun userKey() = appCache.userKey
    fun encryptKey() = appCache.getEncryptKey() ?: ""
    fun userKeyTime() = appCache.userKeyTime
    fun recoveryKey() = appCache.recoveryKey

    suspend fun getSavedFiles() = fileProvider.getFiles(encryptKey())

    suspend fun saveAndEncrypt(uri: Uri) = fileProvider.saveToRepo(encryptKey(), uri)

    fun deleteFile(uri: Uri) = fileProvider.delete(uri)

}