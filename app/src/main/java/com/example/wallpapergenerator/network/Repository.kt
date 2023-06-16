package com.example.wallpapergenerator.network

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.wallpapergenerator.repository.SharedPrefRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import org.xml.sax.Parser
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
    fun authorize(username: String, password: String)
    fun register(username: String, email: String, password: String)
}

class RepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val client: OkHttpClient,
    private val sharedPrefRepository: SharedPrefRepository
    ): Repository {
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
        val token : String = "Bearer " + sharedPrefRepository.readData().toString()

        api.sendImage(token, params, imagePart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println(response.code())
                println(response.message())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("не удалось выполнить запрос")
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

    override fun authorize(username: String, password: String) {
        val body = mapOf(
            "username" to username,
            "passwordHash" to password
        )
        val response = api.login(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println(response.code())
                println(response.message())
                if(response.body()?.string() != null){
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body()?.string(), JsonObject::class.java)
                    sharedPrefRepository.saveData(jsonObject.get("token").asString)
                }
                print("сохраненные данные: ")
                println(sharedPrefRepository.readData())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("не удалось выполнить запрос")
            }
        })
    }

    override fun register(username: String, email: String, password: String) {
        val body = mapOf(
            "username" to username,
            "email" to email,
            "passwordHash" to password
        )
        val response = api.register(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println(response.code())
                println(response.message())
                println(response.errorBody()?.string())
                if(response.body()?.string() != null){
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body()?.string(), JsonObject::class.java)
                    sharedPrefRepository.saveData(jsonObject.get("token").asString)
                }
                print("сохраненные данные (token): ")
                println(sharedPrefRepository.readData())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("не удалось выполнить запрос")
            }
        })
    }
}
