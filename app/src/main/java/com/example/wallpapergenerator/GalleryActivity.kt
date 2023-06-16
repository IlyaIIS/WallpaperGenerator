package com.example.wallpapergenerator

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGalleryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import javax.inject.Inject
import kotlin.random.Random

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    lateinit var viewModel: GalleryViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GalleryViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]

        val navController = findNavController(R.id.nav_host_fragment_content_gallery)
    }

    class GalleryViewModel @Inject constructor(repository: Repository) : ViewModel() {
        fun loadData() {

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