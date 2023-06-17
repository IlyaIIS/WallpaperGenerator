package com.example.wallpapergenerator.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @JvmSuppressWildcards
    @POST("/wallpaper/all")
    suspend fun getAll(@Header("Authorization") token : String,
                       @PartMap params: Map<String, RequestBody>): Response<List<WallpaperTextData>>

    @Multipart
    @JvmSuppressWildcards
    @POST("/wallpaper/collection")
    suspend fun getCollection(@Header("Authorization") token : String,
                              @PartMap params: Map<String, RequestBody>): Response<List<WallpaperTextData>>

    @GET("/wallpaper/{id}")
    @Streaming
    suspend fun getImage(@Path("id") id: String): ResponseBody

    @Multipart
    @JvmSuppressWildcards
    @POST("/wallpaper/image")
    fun sendImage(@Header("Authorization") token : String,
                  @PartMap params: Map<String, RequestBody>,
                  @Part image:MultipartBody.Part):Call<ResponseBody>

    @DELETE("/wallpaper/image/{id}")
    suspend fun deleteImage(@Path("id") id: String,
                            @Header("Authorization") token : String): ResponseBody

    @POST("/wallpaper/{id}/like")
    suspend fun likeImage(@Path("id") id: String,
                          @Header("Authorization") token : String): ResponseBody

    @DELETE("/wallpaper/{id}/like")
    suspend fun dislikeImage(@Path("id") id: String,
                             @Header("Authorization") token : String): ResponseBody

    @JvmSuppressWildcards
    @POST("/account/authorize")
    fun login(@Body body: Map<String, Any>):Call<ResponseBody>

    @JvmSuppressWildcards
    @POST("/account/register")
    fun register(@Body body: Map<String, Any>):Call<ResponseBody>

    data class IntArrayRequest(val numbers: List<Int>)
}