package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import androidx.core.net.toUri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.model.FileModel
import ir.romroid.secureboxrecorder.domain.model.FileType
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.BoxProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BoxRepository @Inject constructor(
    private val appCache: AppCache,
    private val boxProvider: BoxProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository() {

    fun saveUserKey(key: String) {
        appCache.userKey = key
    }

    fun userKey() = appCache.userKey
    fun encryptKey() = appCache.getEncryptKey() ?: ""

    suspend fun getSavedFiles(): List<FileModel> = boxProvider.getFiles(encryptKey())
        .map {
            val fileSuffix = it.first.substringAfterLast(".")

            FileModel(
                it.first.hashCode().toLong(),
                name = it.first,
                type = FileType.getType(fileSuffix),
                uri = it.second.toUri()
            )
        }

    suspend fun saveAndEncrypt(uri: Uri) = boxProvider.saveToBox(encryptKey(), uri)

    fun deleteFile(uri: Uri) = boxProvider.delete(uri)

    suspend fun getFile(uri: Uri): Result<FileModel> = withContext(ioDispatcher) {
        val file = boxProvider.decryptToTemp(encryptKey(), uri)

        return@withContext if (file != null) {
            Result.Success(
                FileModel(
                    file.name.hashCode().toLong(),
                    name = file.name,
                    type = FileType.getType(file.extension),
                    uri = file.toUri()
                )
            )
        } else
            Result.Error(Exception("can't decrypt"))
    }

    fun clearTemp() = boxProvider.clearTemp()

    suspend fun exportFiles() = boxProvider.zipToExportFolder()

    /**
     * @return success
     */
    suspend fun extractBackup(file: Uri): Boolean = withContext(ioDispatcher) {
        val fileTemp = boxProvider.copyToTemp(file)

        return@withContext if (fileTemp != null) {
            boxProvider.unzipToSaveFolder(fileTemp)
                .let {
                    when (it) {
                        is Result.Error -> {
                            fileTemp.delete()
                            false
                        }
                        is Result.Success -> {
                            fileTemp.delete()
                            true
                        }
                    }
                }
        } else {
            false
        }

    }

}