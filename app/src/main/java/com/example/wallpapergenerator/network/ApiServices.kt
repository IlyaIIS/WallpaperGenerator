package com.example.wallpapergenerator.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.stream.Stream

interface ApiServices {
    @GET("/all")
    suspend fun getAll(): Response<List<WallpaperTextData>>

    @GET("/{id}")
    @Streaming
    suspend fun getImages(@Path("id") id: String): ResponseBody

    @Multipart
    @POST("/pro")
    fun sendIntArray( @Part image:MultipartBody.Part):Call<ResponseBody>

    companion object {
        private const val BASE_URL = "https://014b-31-162-227-230.eu.ngrok.io/"
        fun create(): ApiServices {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiServices::class.java)
        }
    }

    data class IntArrayRequest(val numbers: List<Int>)
}