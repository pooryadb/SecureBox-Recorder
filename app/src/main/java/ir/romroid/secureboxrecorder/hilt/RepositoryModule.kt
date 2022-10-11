package ir.romroid.secureboxrecorder.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.BoxProvider
import ir.romroid.secureboxrecorder.domain.provider.local.RecorderProvider
import ir.romroid.secureboxrecorder.domain.repository.BoxRepository
import ir.romroid.secureboxrecorder.domain.repository.RecorderRepository
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideBoxRepository(
        appCache: AppCache,
        boxProvider: BoxProvider,
        ioDispatcher: CoroutineDispatcher
    ) = BoxRepository(appCache, boxProvider, ioDispatcher)

    @ViewModelScoped
    @Provides
    fun provideRecorderRepository(
        appCache: AppCache,
        recorderProvider: RecorderProvider
    ) = RecorderRepository(appCache, recorderProvider)

}