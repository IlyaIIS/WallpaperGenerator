package com.example.wallpapergenerator.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpapergenerator.ExpandedWallpaperFragment
import com.example.wallpapergenerator.R
import com.example.wallpapergenerator.network.NetRepository
import com.example.wallpapergenerator.network.WallpaperData
import com.example.wallpapergenerator.parameterholders.GalleryParametersHolder
import com.example.wallpapergenerator.repository.FileRepository
import com.example.wallpapergenerator.repository.LocalRepository
import com.example.wallpapergenerator.repository.ResourceRepository
import com.example.wallpapergenerator.repository.ToastMessageDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GalleryViewModel @Inject constructor(
    private val netRepository: NetRepository,
    private val localRepository: LocalRepository,
    private val fileRepository: FileRepository,
    private val toastMessageDrawer: ToastMessageDrawer,
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    var isInGallery: Boolean = true
    private val _viewModelScope = CoroutineScope(Dispatchers.Main)
    private val _cards = MutableLiveData<List<WallpaperData>>()
    val cards: LiveData<List<WallpaperData>> = _cards
    lateinit var parameters: GalleryParametersHolder
    lateinit var onWallpaperClicked: (self: WallpaperData) -> Unit
    lateinit var expandedWallpaperFragment: ExpandedWallpaperFragment

    fun loadData() {
        _viewModelScope.launch {
            val cardData: MutableList<WallpaperData> = mutableListOf()
            val cardTextData = if (isInGallery) netRepository.fetchCardsData(parameters) else netRepository.fetchCollection(parameters)
            if (cardTextData != null) {
                for(item in cardTextData) {
                    cardData.add(WallpaperData(item.id, item.mainColor, item.likes, item.isLiked, onWallpaperClicked, ::onWallpaperInScreen))
                }
            }
            _cards.value = cardData
        }
    }

    fun onWallpaperInScreen(wallpaper: WallpaperData) {
        if (wallpaper.image.value == null) {
            _viewModelScope.launch {
                var image: Bitmap?
                withContext(Dispatchers.IO) {
                    image = netRepository.fetchImage(wallpaper.id)
                }
                wallpaper.image.value = image
            }
        }
    }

    fun saveImage() {
        val message = fileRepository.saveMediaToStorage(expandedWallpaperFragment.wallpaperData!!.image.value!!)
        toastMessageDrawer.showMessage(message)
    }

    var isWaiting = false
    fun likeImage() {
        if (netRepository.getIsNetConnection()){
            if (!isWaiting) {
                if (expandedWallpaperFragment.wallpaperData!!.isLiked) {
                    fun onDeleted(isSuccess : Boolean) {
                        if (isSuccess) {
                            expandedWallpaperFragment.wallpaperData!!.isLiked = false
                            expandedWallpaperFragment.wallpaperData!!.likes--
                            expandedWallpaperFragment.wallpaperData!!.run { onLiked(this) }
                            expandedWallpaperFragment.exportImageFragment.dislike()
                            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.message_removed_from_gallery))
                        } else {
                            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.error_not_removed))
                        }
                    }
                    isWaiting = true
                    viewModelScope.launch(Dispatchers.IO) {
                        val isSuccess = netRepository.dislikeImage(expandedWallpaperFragment.wallpaperData!!.id)

                        withContext(Dispatchers.Main) {
                            onDeleted(isSuccess)
                        }

                        isWaiting = false
                    }

                } else {
                    fun onSaved(isSuccess : Boolean) {
                        if (isSuccess) {
                            expandedWallpaperFragment.wallpaperData!!.isLiked = true
                            expandedWallpaperFragment.wallpaperData!!.likes ++
                            expandedWallpaperFragment.wallpaperData!!.run { onLiked(this) }
                            expandedWallpaperFragment.exportImageFragment.like()
                            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.message_saved_to_gallery))
                        } else {
                            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.error_not_saved))
                        }
                    }

                    isWaiting = true
                    viewModelScope.launch(Dispatchers.IO) {
                        val isSuccess = netRepository.likeImage(expandedWallpaperFragment.wallpaperData!!.id)

                        withContext(Dispatchers.Main) {
                            onSaved(isSuccess)
                        }

                        isWaiting = false
                    }
                }
            }
        } else {
            toastMessageDrawer.showMessage(resourceRepository.getString(R.string.error_no_net_connection))
        }

    }

    fun getIsUserAuthorized() = localRepository.getIsUserAuthorized()
}