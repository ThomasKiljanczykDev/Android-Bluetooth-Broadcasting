package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Qualifier

/** Cancelled when the owning ViewModel is cleared; transports release all resources on cancellation. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TransportScope

@Module
@InstallIn(ViewModelComponent::class)
object TransportScopeModule {

    @Provides
    @ViewModelScoped
    @TransportScope
    fun provideTransportScope(lifecycle: ViewModelLifecycle): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { scope ->
            lifecycle.addOnClearedListener { scope.cancel() }
        }
}
