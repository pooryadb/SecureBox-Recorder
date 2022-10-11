package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import androidx.core.net.toUri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.RecorderProvider
import java.io.File
import javax.inject.Inject

class RecorderRepository @Inject constructor(
    private val appCache: AppCache,
    private val recorderProvider: RecorderProvider
) : BaseRepository() {

    fun getRecords(): List<AudioModel> = recorderProvider.getRecords()
        .sortedBy { it.lastModified() }
        .map {
            AudioModel(
                it.name.hashCode().toLong(),
                name = it.name,
                uri = it.toUri()
            )
        }

    fun deleteFile(uri: Uri) = recorderProvider.delete(uri)

    fun prepareTempFile() = recorderProvider.prepareTempFile()

    suspend fun saveRecord(file: File, name: String): File? =
        recorderProvider.saveToRecords(file, name)

}