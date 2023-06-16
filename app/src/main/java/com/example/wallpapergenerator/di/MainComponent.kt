package com.example.wallpapergenerator.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.*
import com.example.wallpapergenerator.network.ApiService
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.RepositoryImpl
import dagger.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Provider

@Component(modules = [MainModule::class])
interface MainComponent {
    fun inject(activity: GenerationActivity)
    fun inject(activity: GalleryActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: RegistrationFragment)
    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance context: Context): MainComponent
    }

    fun inject(fragment: AuthorizationFragment)
}

@Module(includes = [NetworkModule::class/*, ViewModelModule::class*/])
interface MainModule {
    @Binds
    abstract fun bindRepository(repositoryImpl: RepositoryImpl): Repository
}

@Module
class NetworkModule {
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://9b45-31-162-227-230.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}

class ViewModelFactory <T : ViewModel> @Inject constructor(private val viewModelProvider: Provider<T>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModelProvider.get() as T
    }
}
