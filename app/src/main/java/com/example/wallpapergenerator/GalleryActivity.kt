package com.example.wallpapergenerator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGalleryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.WallpaperData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var parameters: GalleryParametersHolder
    lateinit var viewModel: GalleryViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GalleryViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]
        parameters = ViewModelProvider(this)[GalleryParametersHolder::class.java]
        viewModel.parameters = parameters

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

        fun loadData() {
            _viewModelScope.launch {
                val cardData: MutableList<WallpaperData> = mutableListOf()
                val cardTextData = repository.fetchCardsData()
                if (cardTextData != null) {
                    for(item in cardTextData){
                        val cardImage = repository.fetchImage(item.id)
                        if(cardImage == null){
                            continue
                        }
                        cardData.add(WallpaperData(item.id, cardImage, item.likes))
                    }
                }
                _cards.value = cardData!!
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