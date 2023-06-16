package com.example.wallpapergenerator.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("/wallpaper/all")
    suspend fun getAll(): Response<List<WallpaperTextData>>

    @GET("/wallpaper/{id}")
    @Streaming
    suspend fun getImages(@Path("id") id: String): ResponseBody

    @Multipart
    @JvmSuppressWildcards
    @POST("/wallpaper/image")
    fun sendImage(@Header("Authorization") token : String,
                  @PartMap params: Map<String, RequestBody>,
                  @Part image:MultipartBody.Part):Call<ResponseBody>

    @JvmSuppressWildcards
    @POST("/account/authorize")
    fun login(@Body body: Map<String, Any>):Call<ResponseBody>

    @JvmSuppressWildcards
    @POST("/account/register")
    fun register(@Body body: Map<String, Any>):Call<ResponseBody>

    data class IntArrayRequest(val numbers: List<Int>)
}