package com.example.wallpapergenerator.network

import android.content.ContentValues
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.wallpapergenerator.imagegeneration.GenerationType
import com.example.wallpapergenerator.parameterholders.GalleryParametersHolder
import com.example.wallpapergenerator.repository.LocalRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import javax.inject.Inject

interface NetRepository {
    fun saveImageToGallery(image: IntArray, width: Int, height: Int, generationType: GenerationType,
                           onSuccess : (imageId: Int) -> Unit, onFailed : () -> Unit)
    suspend fun fetchImage(id: Int) : Bitmap?
    suspend fun fetchCardsData(parameters: GalleryParametersHolder) : List<WallpaperTextData>?
    fun authorize(
        username: String,
        password: String,
        authMessage: MutableLiveData<String?>
    )
    fun register(
        username: String,
        email: String,
        password: String,
        regMessage: MutableLiveData<String?>
    )

    suspend fun deleteImageFromGallery(imageId: Int)
    suspend fun likeImage(imageId: Int)
    suspend fun dislikeImage(imageId: Int)
    suspend fun fetchCollection(parameters: GalleryParametersHolder): List<WallpaperTextData>?
    fun getIsNetConnection(): Boolean
}

class NetRepositoryRetrofit @Inject constructor(
    private val api: ApiService,
    private val localRepository: LocalRepository,
    private val context: Context
    ): NetRepository {
    private val TAG = "NetRepositoryRetrofit"
    override fun saveImageToGallery(image: IntArray, width: Int, height: Int, generationType: GenerationType,
                                    onSuccess : (imageId: Int) -> Unit,  onFailed : () -> Unit){
        Log.i(TAG, "send image...")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(image, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val requestBody = RequestBody.create(MediaType.parse("image/png"), byteArrayOutputStream.toByteArray())

        val imagePart = MultipartBody.Part.createFormData("imageFile", "image", requestBody)

        val params = HashMap<String, RequestBody>()
        params["length"] = RequestBody.create(MediaType.parse("text/plain"),byteArrayOutputStream.toByteArray().size.toString())
        params["hashCode"] = RequestBody.create(MediaType.parse("text/plain"), image.contentHashCode().toString())
        params["generationType"] = RequestBody.create(MediaType.parse("text/plain"), generationType.name)
        val token : String = "Bearer " + localRepository.readToken().toString()

        api.sendImage(token, params, imagePart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val body = response.body()?.string()
                if(response.isSuccessful && body != null){
                    Gson().fromJson(body.toString(), JsonObject::class.java).run {
                        onSuccess(get("id").asInt)
                    }
                }
                else {
                    onFailed()
                }

                Log.i(TAG, response.code().toString())
                Log.i(TAG, response.message())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onFailed()
                Log.i(TAG, "не удалось сохранить изображение")
            }
        })
    }

    override suspend fun deleteImageFromGallery(
        imageId: Int
    ) {
        try {
            println("Удаление изображения id: ${imageId}")
            val token : String = "Bearer " + localRepository.readToken().toString()
            val response = api.deleteImage(imageId.toString(), token)
            println("Изображение удалено id: ${imageId}")
        } catch (e: Exception) {
            println("Не удалось удалить: ${e.message}")
        }
    }

    override suspend fun likeImage(imageId: Int) {
        try {
            Log.i(TAG, "Лайк изображения")
            val token : String = "Bearer " + localRepository.readToken().toString()
            val response = api.likeImage(imageId.toString(), token)
        } catch (e: Exception) {
        }
    }

    override suspend fun dislikeImage(imageId: Int) {
        try {
            Log.i(TAG, "Лайк изображения")
            val token : String = "Bearer " + localRepository.readToken().toString()
            val response = api.dislikeImage(imageId.toString(), token)
        } catch (e: Exception) {
        }
    }

    override suspend fun fetchImage(id: Int) : Bitmap? {
        try {
            val response = api.getImage(id.toString())
            return BitmapFactory.decodeStream(response.byteStream())
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun fetchCardsData(parameters: GalleryParametersHolder) : List<WallpaperTextData>? {
        val token : String = "Bearer " + localRepository.readToken().toString()

        val params = HashMap<String, RequestBody>()
        val imageType: String = if (parameters.allGenerationTypes) "ALL" else parameters.currentGenerationType.name
        params["imageType"] = RequestBody.create(
            MediaType.parse("text/plain"),
            imageType
        )
        params["orderBy"] = RequestBody.create(
            MediaType.parse("text/plain"),
            parameters.orderBy.name
        )
        params["isLikedOnly"] = RequestBody.create(
            MediaType.parse("text/plain"),
            parameters.isLikedOnly.toString()
        )

        val response = api.getAll(token, params)
        return if (response.isSuccessful) {
            response.body()
        } else {
            Log.d(ContentValues.TAG, "Error while fetching cards: " + response.errorBody())
            null
        }
    }

    override suspend fun fetchCollection(parameters: GalleryParametersHolder) : List<WallpaperTextData>? {
        val params = HashMap<String, RequestBody>()
        val imageType: String = if (parameters.allGenerationTypes) "ALL" else parameters.currentGenerationType.name
        params["imageType"] = RequestBody.create(
            MediaType.parse("text/plain"),
            imageType
        )
        params["orderBy"] = RequestBody.create(
            MediaType.parse("text/plain"),
            parameters.orderBy.name
        )
        params["isLikedOnly"] = RequestBody.create(
            MediaType.parse("text/plain"),
            parameters.isLikedOnly.toString()
        )

        val token : String = "Bearer " + localRepository.readToken().toString()

        val response = api.getCollection(token, params)
        return if (response.isSuccessful) {
            response.body()
        } else {
            Log.d(ContentValues.TAG, response.code().toString())
            Log.d(ContentValues.TAG, "Error while fetching cards: " + response.errorBody())
            null
        }
    }

    override fun authorize(
        username: String,
        password: String,
        authMessage: MutableLiveData<String?>
    ) {
        val body = mapOf(
            "username" to username,
            "passwordHash" to password
        )
        val response = api.login(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                authMessage.value = checkResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i(TAG, "не удалось выполнить запрос")
                authMessage.value = "Нет доступа к сети"
            }
        })
    }

    override fun register(
        username: String,
        email: String,
        password: String,
        regMessage: MutableLiveData<String?>
    ) {
        val body = mapOf(
            "username" to username,
            "email" to email,
            "passwordHash" to password
        )


        val response = api.register(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                regMessage.value = checkResponse(response)
                Log.i(TAG, "сохраненные данные (token): " + localRepository.readToken())
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                regMessage.value = "не удалось выполнить запрос"
            }
        })
    }

    fun checkResponse(response: Response<ResponseBody>) : String? {
        val errorBody = response.errorBody()?.string()
        val body = response.body()?.string()
        val gson = Gson()

        if(response.isSuccessful){
            Log.i(TAG, body.toString())
            if(body != null){
                val jsonObject = gson.fromJson(body.toString(), JsonObject::class.java)
                localRepository.saveToken(jsonObject.get("token").asString)
                return null
            }
        }
        else if(response.code() == 400 || response.code() == 401){
            if(errorBody != null){
                val jsonObject = gson.fromJson(errorBody.toString(), JsonObject::class.java)
                return jsonObject.get("message").asString.toString()
            }
        }
        return "Произошла ошибка на стороне сервера"
    }

    override fun getIsNetConnection() : Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null
    }
}
