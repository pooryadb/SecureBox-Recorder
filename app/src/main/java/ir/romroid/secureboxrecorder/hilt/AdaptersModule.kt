package ir.romroid.secureboxrecorder.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import ir.romroid.secureboxrecorder.presentation.fileManager.FileManagerAdapter
import ir.romroid.secureboxrecorder.presentation.recorder.list.AudioAdapter

@Module
@InstallIn(FragmentComponent::class)
object AdaptersModule {

    @FragmentScoped
    @Provides
    fun provideNewsAdapter() = AudioAdapter()

    @FragmentScoped
    @Provides
    fun provideFileManagerAdapter() = FileManagerAdapter()

}