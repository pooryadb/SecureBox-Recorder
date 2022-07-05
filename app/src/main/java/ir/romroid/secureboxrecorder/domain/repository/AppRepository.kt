package ir.romroid.secureboxrecorder.domain.repository


import ir.romroid.secureboxrecorder.base.architecture.BaseRepository
import ir.romroid.secureboxrecorder.domain.provider.AppCache
import javax.inject.Inject

class AppRepository @Inject constructor(
    val appCache: AppCache,
) : BaseRepository() {

    fun userKey() = appCache.userKey
    fun userKeyTime() = appCache.userKeyTime
    fun recoveryKey() = appCache.recoveryKey

}