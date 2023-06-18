package com.example.wallpapergenerator

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGalleryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.NetRepository
import com.example.wallpapergenerator.network.WallpaperData
import com.example.wallpapergenerator.repository.FileRepository
import com.example.wallpapergenerator.repository.LocalRepository
import com.example.wallpapergenerator.repository.ToastMessageDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GalleryActivity : AppCompatActivity() {
    lateinit var galleryAdapter: GalleryAdapter

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var parameters: GalleryParametersHolder
    lateinit var viewModel: GalleryViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GalleryViewModel>

    lateinit var wallpaperFragmentContainer :FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 123)
        }

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]
        parameters = ViewModelProvider(this)[GalleryParametersHolder::class.java]
        viewModel.parameters = parameters
        wallpaperFragmentContainer = binding.wallpaperFragmentContainer
        viewModel.onWallpaperClicked = { wallpaper ->
            if (wallpaper.image.value != null) {
                wallpaperFragmentContainer.isVisible = true
                wallpaperFragmentContainer.getFragment<ExpandedWallpaperFragment>().setWallpaper(wallpaper)
            }
        }
        parameters.onParameterChanged = {
            viewModel.loadData()
        }

        if (viewModel.getIsUserAuthorized()) {
            binding.toCollectionButton.setOnClickListener {
                toggleGalleryAndCollection()
            }
            binding.toGalleryButton.setOnClickListener {
                toggleGalleryAndCollection()
            }
        } else {
            hideCollection()
        }
        binding.settingsButton.setOnClickListener {
            binding.settingsFragmentContainer.isVisible = !binding.settingsFragmentContainer.isVisible
        }
        binding.toMenuButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        viewModel.expandedWallpaperFragment = binding.wallpaperFragmentContainer.getFragment()
        viewModel.expandedWallpaperFragment.onExportImageFragmentCreated = { exportImageFragment ->
            if (viewModel.getIsUserAuthorized()) {
                exportImageFragment.onLikeClick = viewModel::likeImage
            } else {
                exportImageFragment.hideLike()
            }
            exportImageFragment.onSaveImageClick = viewModel::saveImage
        }

        val galleryList = binding.galleryRecyclerView
        val layoutManager = GridLayoutManager(this, 2)
        galleryList.layoutManager = layoutManager

        galleryAdapter = GalleryAdapter()

        galleryList.adapter = galleryAdapter

        viewModel.cards.observe(this) { cards ->
            galleryAdapter.submitList(cards)
        }

        viewModel.loadData()
    }

    fun toggleGalleryAndCollection(){
        viewModel.isInGallery = !viewModel.isInGallery
        binding.toGalleryButton.isAllCaps = viewModel.isInGallery
        binding.toCollectionButton.isAllCaps = !viewModel.isInGallery

        viewModel.loadData()
    }

    class GalleryViewModel @Inject constructor(
            private val netRepository: NetRepository,
            private val localRepository: LocalRepository,
            private val fileRepository: FileRepository,
            private val toastMessageDrawer: ToastMessageDrawer
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
                        cardData.add(WallpaperData(item.id, item.likes, item.isLiked, onWallpaperClicked, ::onWallpaperInScreen))
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
            if(expandedWallpaperFragment.wallpaperData == null){
                toastMessageDrawer.showMessage("Картинка не загружена!")
            } else {
                val message = fileRepository.saveMediaToStorage(expandedWallpaperFragment.wallpaperData!!.image.value!!)
                toastMessageDrawer.showMessage(message)
            }
        }

        var isWaiting = false
        fun likeImage() {
            if(expandedWallpaperFragment.wallpaperData == null){
                toastMessageDrawer.showMessage("Картинка не загружена!")
            } else {
                if (!isWaiting) {
                    if (expandedWallpaperFragment.wallpaperData!!.isLiked) {
                        isWaiting = true
                        viewModelScope.launch(Dispatchers.IO) {
                            netRepository.dislikeImage(expandedWallpaperFragment.wallpaperData!!.id)
                            isWaiting = false
                        }
                        expandedWallpaperFragment.wallpaperData!!.isLiked = false
                        expandedWallpaperFragment.wallpaperData!!.likes --
                        expandedWallpaperFragment.wallpaperData!!.run { onLiked(this) }
                        expandedWallpaperFragment.exportImageFragment.dislike()
                        toastMessageDrawer.showMessage("Удалено из галереи")
                    }else {
                        isWaiting = true
                        viewModelScope.launch(Dispatchers.IO) {
                            netRepository.likeImage(expandedWallpaperFragment.wallpaperData!!.id)
                            isWaiting = false
                        }
                        expandedWallpaperFragment.wallpaperData!!.isLiked = true
                        expandedWallpaperFragment.wallpaperData!!.likes ++
                        expandedWallpaperFragment.wallpaperData!!.run { onLiked(this) }
                        expandedWallpaperFragment.exportImageFragment.like()
                        toastMessageDrawer.showMessage("Сохранено в галерею")
                    }
                }
            }
        }

        fun getIsUserAuthorized() = localRepository.getIsUserAuthorized()
    }

    fun hideCollection() {
        binding.toCollectionButton.visibility = View.GONE
    }

    class GalleryParametersHolder : ParameterHolder() {
        var allGenerationTypes = true
        var currentGenerationType: GenerationType = 0.toEnum()

        var orderBy: GalleryOrderType = GalleryOrderType.TIME

        var isLikedOnly = false

        var onParameterChanged: () -> Unit = { }

        override fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter> {
            lateinit var params: List<SettingsParameter>
            params = mutableListOf<SettingsParameter>()

            params.add(
                CheckboxParameter(
                    "Все типы",
                    allGenerationTypes,
                ) { value ->
                    if (allGenerationTypes != value) {
                        allGenerationTypes = value
                        updateParameters()
                        onParameterChanged()
                    }
                }
            )

            if (!allGenerationTypes) {
                params.add(
                    DropdownParameter(
                        "Тип генератора",
                        0,
                        GenerationTypeNames
                    ) { optionNum ->
                        currentGenerationType = optionNum.toEnum()
                        onParameterChanged()
                    }
                )
            }

            params.add(
                DropdownParameter(
                    "Сортировка",
                    orderBy.ordinal,
                    GalleryOrderTypeNames
                ) {optionNum ->
                    orderBy = optionNum.toEnum()
                    onParameterChanged()
                }
            )

            params.add(
                CheckboxParameter(
                    "Только лайкнутые",
                    isLikedOnly,
                ) {value ->
                    isLikedOnly = value
                    onParameterChanged()
                }
            )

            return params
        }

        enum class GalleryOrderType {
            NONE,
            TIME,
            TIME_ASCENDING,
            LIKE,
            LIKE_ASCENDING
        }

        val GalleryOrderTypeNames = arrayOf(
            "Нет",
            "Время",
            "Время воз.",
            "Лайки",
            "Лайки воз."
        )
    }
}