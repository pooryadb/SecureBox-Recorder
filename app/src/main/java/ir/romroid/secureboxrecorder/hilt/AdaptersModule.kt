package ir.romroid.secureboxrecorder.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import ir.romroid.secureboxrecorder.presentation.recordList.AudioAdapter

@Module
@InstallIn(FragmentComponent::class)
object AdaptersModule {

    @FragmentScoped
    @Provides
    fun provideNewsAdapter() = AudioAdapter()

}