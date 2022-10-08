package ir.romroid.secureboxrecorder.hilt

import android.app.Application
import android.content.Context
import com.aaaamirabbas.reactor.handler.Reactor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.romroid.secureboxrecorder.domain.provider.local.AppCache
import ir.romroid.secureboxrecorder.domain.provider.local.BoxProvider
import ir.romroid.secureboxrecorder.domain.provider.local.RecorderProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Singleton
    @Provides
    @Named("AES")
    fun provideReactorAES(context: Context) = Reactor(context, true)

    @Singleton
    @Provides
    @Named("Base64")
    fun provideReactorBase64(context: Context) = Reactor(context, false)

    @Singleton
    @Provides
    fun provideAppCache(
        @Named("AES") reactorAES: Reactor,
        @Named("Base64") reactorBase64: Reactor,
    ) = AppCache(reactorAES, reactorBase64)

    @Singleton
    @Provides
    fun provideBoxProvider(
        context: Context,
        ioDispatcher: CoroutineDispatcher
    ) = BoxProvider(context, ioDispatcher)

    @Singleton
    @Provides
    fun provideRecorderProvider(
        context: Context,
        ioDispatcher: CoroutineDispatcher
    ) = RecorderProvider(context, ioDispatcher)

}