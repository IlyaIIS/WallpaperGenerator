package com.example.wallpapergenerator.network

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.nio.IntBuffer
import javax.inject.Inject

interface Repository {
    fun saveImageToGallery(image: IntArray, width: Int, height: Int)
    suspend fun fetchImage(id: Int) : Bitmap?
    suspend fun fetchCardsData() : List<WallpaperTextData>?
}

class RepositoryImpl @Inject constructor(private val api: ApiService, private val client: OkHttpClient): Repository {
    override fun saveImageToGallery(image: IntArray, width: Int, height: Int) {
        println("send image...")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(image))
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArrayOutputStream.toByteArray())

        val imagePart = MultipartBody.Part.createFormData("imageFile", "image", requestBody)

        val params = HashMap<String, RequestBody>()
        params["length"] = RequestBody.create(MediaType.parse("text/plain"),byteArrayOutputStream.toByteArray().size.toString())

        api.sendImage(params, imagePart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println("11111111111111111")// Обработка успешного ответа сервера
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("0000000000000000000000000")// Обработка неудачного ответа сервера
            }
        })
    }

    override suspend fun fetchImage(id: Int) : Bitmap? {
        try {
            val response = api.getImages(id.toString())
            return BitmapFactory.decodeStream(response.byteStream())
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun fetchCardsData() : List<WallpaperTextData>? {
        val response = api.getAll()
        return if (response.isSuccessful) {
            response.body()
        } else {
            Log.d(ContentValues.TAG, "Error while fetching cards: " + response.errorBody())
            null
        }
    }
}
