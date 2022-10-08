package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.BoxProvider
import javax.inject.Inject

class BoxRepository @Inject constructor(
    val appCache: AppCache,
    val boxProvider: BoxProvider
) : BaseRepository() {

    fun userKey() = appCache.userKey
    fun encryptKey() = appCache.getEncryptKey() ?: ""

    suspend fun getSavedFiles() = boxProvider.getFiles(encryptKey())

    suspend fun saveAndEncrypt(uri: Uri) = boxProvider.saveToBox(encryptKey(), uri)

    fun deleteFile(uri: Uri) = boxProvider.delete(uri)

    suspend fun copyToShare(uri: Uri) = boxProvider.restoreFromBox(encryptKey(), uri)

    suspend fun copyToTemp(uri: Uri) = boxProvider.restoreFromBox(encryptKey(), uri)

    fun clearTemp() = boxProvider.clearTemp()

    // TODO: use Flow
    suspend fun exportFiles(listener: ((Result<String>) -> Unit)) =
        boxProvider.zipFilesToExportFolder(listener)

}