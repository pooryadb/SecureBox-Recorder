package ir.romroid.secureboxrecorder.domain.repository


import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.provider.AppCache
import ir.romroid.secureboxrecorder.domain.provider.FileProvider
import javax.inject.Inject

class AppRepository @Inject constructor(
    val appCache: AppCache,
    val fileProvider: FileProvider
) : BaseRepository() {

    fun userKey() = appCache.userKey
    fun userKeyTime() = appCache.userKeyTime
    fun recoveryKey() = appCache.recoveryKey

}