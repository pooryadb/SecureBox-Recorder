package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.provider.AppCache
import ir.romroid.secureboxrecorder.domain.provider.FileProvider
import ir.romroid.secureboxrecorder.domain.provider.FileProviderListener
import javax.inject.Inject

class AppRepository @Inject constructor(
    val appCache: AppCache,
    val fileProvider: FileProvider
) : BaseRepository() {

    fun userKey() = appCache.userKey
    fun encryptKey() = appCache.getEncryptKey() ?: ""

    suspend fun getSavedFiles() = fileProvider.getFiles(encryptKey())

    suspend fun saveAndEncrypt(uri: Uri) = fileProvider.saveToBox(encryptKey(), uri)

    fun deleteFile(uri: Uri) = fileProvider.delete(uri)

    suspend fun copyToShare(uri: Uri) = fileProvider.restoreFromBox(encryptKey(), uri)

    suspend fun copyToTemp(uri: Uri) = fileProvider.restoreFromBox(encryptKey(), uri)

    fun clearTemp() = fileProvider.clearTemp()

    suspend fun exportFiles(listener: FileProviderListener) =
        fileProvider.zipFilesToExportFolder(listener)

}