package com.example.wallpapergenerator

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGalleryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.WallpaperData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]
        parameters = ViewModelProvider(this)[GalleryParametersHolder::class.java]
        viewModel.parameters = parameters
        wallpaperFragmentContainer = binding.wallpaperFragmentContainer
        viewModel.onWallpaperClicked = { wallpaper ->
            wallpaperFragmentContainer.isVisible = true
            wallpaperFragmentContainer.getFragment<ExpandedWallpaperFragment>().setWallpaper(wallpaper)
        }

        binding.toGalleryButton.setOnClickListener {
            toggleGalleryAndCollection()
        }
        binding.toCollectionButton.setOnClickListener {
            toggleGalleryAndCollection()
        }
        binding.settingsButton.setOnClickListener {
            binding.settingsFragmentContainer.isVisible = !binding.settingsFragmentContainer.isVisible
        }
        binding.toMenuButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
        if (viewModel.isInGallery) {
            viewModel.isInGallery = false
            binding.toGalleryButton.text = "галерея"
            binding.toCollectionButton.text = "КОЛЛЕКЦИЯ"
        } else {
            viewModel.isInGallery = true
            binding.toGalleryButton.text = "ГАЛЕРЕЯ"
            binding.toCollectionButton.text = "коллекция"
        }
        viewModel.loadData()
    }

    class GalleryViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
        var isInGallery: Boolean = true
        private val _viewModelScope = CoroutineScope(Dispatchers.Main)
        private val _cards = MutableLiveData<List<WallpaperData>>()
        val cards: LiveData<List<WallpaperData>> = _cards
        lateinit var parameters: GalleryParametersHolder
        lateinit var onWallpaperClicked: (self: WallpaperData) -> Unit

        fun loadData() {
            _viewModelScope.launch {
                val cardData: MutableList<WallpaperData> = mutableListOf()
                val cardTextData = repository.fetchCardsData(parameters)
                if (cardTextData != null) {
                    for(item in cardTextData) {
                        cardData.add(WallpaperData(item.id, item.likes, onWallpaperClicked, ::onWallpaperInScreen))
                    }
                }
                _cards.value = cardData
            }
        }

        fun onWallpaperInScreen(wallpaper: WallpaperData) {
            _viewModelScope.launch {
                var image: Bitmap?
                withContext(Dispatchers.IO) {
                    image = repository.fetchImage(wallpaper.id)
                }
                wallpaper.image.value = image
            }
        }
    }

    class GalleryParametersHolder : ParameterHolder() {

        lateinit var currentGenerationType: GenerationType

        var orderBy: GalleryOrderType = GalleryOrderType.NONE

        override fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter> {
            lateinit var params: List<SettingsParameter>
            params = mutableListOf<SettingsParameter>()

            params.add(
                DropdownParameter(
                    "Тип генератора",
                    0,
                    GenerationTypeNames
                ) {optionNum -> currentGenerationType = optionNum.toEnum() }
            )

            params.add(
                DropdownParameter(
                    "Сортировка",
                    0,
                    GalleryOrderTypeNames
                ) {optionNum -> orderBy = optionNum.toEnum() }
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
            "Время уб.",
            "Лайки",
            "Лайки уб."
        )
    }
}