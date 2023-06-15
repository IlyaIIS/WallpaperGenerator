package com.example.wallpapergenerator.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("/all")
    suspend fun getAll(): Response<List<WallpaperTextData>>

    @GET("/{id}")
    @Streaming
    suspend fun getImages(@Path("id") id: String): ResponseBody

    @Multipart
    @POST("/pro")
    fun sendIntArray( @Part image:MultipartBody.Part):Call<ResponseBody>

    data class IntArrayRequest(val numbers: List<Int>)
}