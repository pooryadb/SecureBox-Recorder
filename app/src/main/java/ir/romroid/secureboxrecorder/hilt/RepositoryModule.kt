package ir.romroid.secureboxrecorder.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import ir.romroid.secureboxrecorder.domain.provider.AppCache
import ir.romroid.secureboxrecorder.domain.provider.FileProvider
import ir.romroid.secureboxrecorder.domain.repository.AppRepository

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideAppRepository(
        appCache: AppCache,
        fileProvider: FileProvider
    ) = AppRepository(appCache, fileProvider)
}