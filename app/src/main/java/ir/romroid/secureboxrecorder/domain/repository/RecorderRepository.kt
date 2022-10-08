package ir.romroid.secureboxrecorder.domain.repository


import android.net.Uri
import androidx.core.net.toUri
import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.RecorderProvider
import javax.inject.Inject

class RecorderRepository @Inject constructor(
    private val appCache: AppCache,
    private val recorderProvider: RecorderProvider
) : BaseRepository() {

    fun getRecords(): List<AudioModel> = recorderProvider.getRecords().map {
        AudioModel(
            name = it.name,
            uri = it.toUri()
        ).apply {
            id = it.name.hashCode().toLong()
        }
    }

    fun deleteFile(uri: Uri) = recorderProvider.delete(uri)

}