package com.example.wallpapergenerator.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiServices {
    @GET("https://4189-31-162-227-230.eu.ngrok.io/all")
    suspend fun getAll(): Response<List<WallpaperTextData>>

    @GET("https://4189-31-162-227-230.eu.ngrok.io/{id}")
    @Streaming
    suspend fun getImages(@Path("id") id: String): ResponseBody
}