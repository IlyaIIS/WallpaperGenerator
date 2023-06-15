package com.example.wallpapergenerator.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.FirstFragment
import com.example.wallpapergenerator.GenerationActivity
import com.example.wallpapergenerator.MainActivity
import com.example.wallpapergenerator.MainFragmentViewModel
import com.example.wallpapergenerator.network.ApiService
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.RepositoryImpl
import dagger.*
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Component(modules = [MainModule::class])
interface MainComponent {
    fun inject(activity: GenerationActivity)
    fun inject(fragment: FirstFragment)
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
            .baseUrl("https://747e-31-162-227-230.eu.ngrok.io/")
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