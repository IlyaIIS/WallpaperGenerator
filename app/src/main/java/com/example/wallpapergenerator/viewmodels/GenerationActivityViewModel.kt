package com.example.wallpapergenerator.viewmodels

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.example.wallpapergenerator.ExportImageFragment
import com.example.wallpapergenerator.R
import com.example.wallpapergenerator.network.NetRepository
import com.example.wallpapergenerator.parameterholders.GenerationParametersHolder
import com.example.wallpapergenerator.repository.*
import kotlinx.coroutines.*
import javax.inject.Inject

class GenerationActivityViewModel @Inject constructor(
    private val netRepository: NetRepository,
    private val localRepository: LocalRepository,
    private val fileRepository: FileRepository,
    private val toastMessageDrawer: ToastMessageDrawer,
    private val resourceRepository: ResourceRepository
) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    lateinit var mainImage: ImageView
    lateinit var currentImage : IntArray
    lateinit var exportImageFragment: ExportImageFragment
    lateinit var parematers: GenerationParametersHolder

    var maxImageCount = 4
    val nextImages: ArrayDeque<IntArray> = ArrayDeque()
    val isNextImagePoolFull get() = nextImages.count() >= maxImageCount

    fun saveImage() {
        println(::currentImage.isInitialized)
        if(!::currentImage.isInitialized){
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.warning_wait_for_generation))
            return
        }
        val bitmap = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(currentImage, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val message = fileRepository.saveMediaToStorage(bitmap)
        toastMessageDrawer.showMessage(message)
    }

    var isImageSaved = false
    var savedImageId = -1
    var isWaiting = false
    fun likeImage() {
        fun onSuccessful(imageId: Int) {
            isImageSaved = true
            exportImageFragment.like()
            savedImageId = imageId
            isWaiting = false
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.message_saved_to_gallery))
        }
        fun onFailed() {
            isWaiting = false
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.error_not_saved))
        }

        if(!::currentImage.isInitialized){
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.warning_wait_for_generation))
            return
        }

        if (!isWaiting) {
            viewModelScope.launch(Dispatchers.IO) {
                isWaiting = true
                if (isImageSaved) {
                    netRepository.deleteImageFromGallery(savedImageId)
                    withContext(Dispatchers.Main) {
                        exportImageFragment.dislike()
                        toastMessageDrawer.showMessage(resourceRepository.getString(R.string.message_removed_from_gallery))
                    }
                    isImageSaved = false
                    isWaiting = false
                } else {
                    netRepository.saveImageToGallery(
                        currentImage,
                        mainImage.width,
                        mainImage.height,
                        parematers.currentGenerationType,
                        ::onSuccessful,
                        ::onFailed)
                }
            }
        } else {
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.warning_wait))
        }
    }

    fun getIsUserAuthorized() = localRepository.getIsUserAuthorized()

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}