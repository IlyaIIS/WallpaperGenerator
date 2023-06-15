package com.example.wallpapergenerator

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import kotlinx.coroutines.*
import javax.inject.Inject


class GenerationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerationBinding
    private lateinit var mainImage: ImageView
    private var isWaitForImage = false
    private var isNextImageReady = false
    private lateinit var parameters: ParametersHolder
    private lateinit var settingsFragment: GenerationSettingsFragment
    
    @Inject lateinit var viewModelFactory: ViewModelFactory<GenerationActivityViewModel>
    lateinit var viewModel: GenerationActivityViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GenerationActivityViewModel::class.java]

        parameters = ViewModelProvider(this)[ParametersHolder::class.java]
        mainImage = binding.mainImage
        viewModel.mainImage = mainImage
        parameters.currentGenerationType = intent.getSerializableExtra("Type") as GenerationType
        updateGenerationName()

        binding.nextImageGenerationButton.setOnTouchListener(object: OnSwipeTouchListener(this@GenerationActivity) {
            override fun onSwipeLeft() {
                println("Left")
                parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() + 1).mod(GenerationType.values().size)).toEnum()
                updateGenerationName()
                returnNewImage()
                settingsFragment.updateParameters()
            }
            override fun onSwipeRight() {
                println("Right")
                parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() - 1).mod(GenerationType.values().size)).toEnum()
                updateGenerationName()
                returnNewImage()
                settingsFragment.updateParameters()
            }
            override fun onClick() {
                onNextImageGenerationClick()
            }
        })

        binding.settingsButton.setOnClickListener {
            if (binding.settingsFragmentContainer.visibility == View.GONE) {
                binding.settingsFragmentContainer.visibility = View.VISIBLE
                binding.nextImageGenerationButton.isEnabled = false
            } else {
                binding.settingsFragmentContainer.visibility = View.GONE
                binding.nextImageGenerationButton.isEnabled = true
            }
        }

        settingsFragment = supportFragmentManager.findFragmentById(R.id.settingsFragmentContainer) as GenerationSettingsFragment

        Thread {
            Thread.sleep(1000)
            returnNewImage()
        }.start()
    }

    fun updateGenerationName() {
        binding.generationName.text = when (parameters.currentGenerationType) {
            GenerationType.Gradients -> "Градиент"
            GenerationType.Shapes -> "Фигуры"
            GenerationType.Noise -> "Шум"
            GenerationType.Fractals -> "Фракталы"
            else -> throw NotImplementedError()
        }
    }

    inline fun <reified T : Enum<T>> Int.toEnum(): T {
        return enumValues<T>().first { it.ordinal == this }
    }

    inline fun <reified T : Enum<T>> T.toInt(): Int {
        return this.ordinal
    }

/*    fun onClickMenu(view: View) { //замена фрагмента
        val fragmentMenu = PopupMenuFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (isMenuOpened) {
            fragmentTransaction.remove(fragmentMenu)
        } else {
            //fragmentTransaction.add(fragmentMenu, "menuFragment")
            fragmentTransaction.replace(binding.menuContainer.id, fragmentMenu)
        }
        fragmentTransaction.commit()
    }*/

    fun onNextImageGenerationClick() {
        println("Generate next image")
        returnNewImage()
    }

    fun onClickMenu(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onExpandClick(view: View) {
        println("Click no there")
    }

    private fun returnNewImage() {
        fun getPixels(): IntArray {
            return when (parameters.currentGenerationType) {
                GenerationType.Gradients -> ImageGenerator.generateGradient(mainImage.width, mainImage.height, parameters.gradientParameters)
                GenerationType.Shapes -> ImageGenerator.generateShapes(mainImage.width, mainImage.height, parameters.shapeParameters)
                GenerationType.Noise -> ImageGenerator.generateSinNoise(mainImage.width, mainImage.height, parameters.noiseParameters)
                GenerationType.Fractals -> ImageGenerator.generateFractal(mainImage.width, mainImage.height, parameters.fractalParameters)
                else -> throw NotImplementedError()
            }
        }

        fun startGeneration() {
            val pixels = getPixels()

            if (isWaitForImage) {
                drawHourglassIndicator(IndicatorState.WAITING_FOR_NEXT_IMAGE)
                isWaitForImage = false
                runOnUiThread {
                    drawImage(pixels)
                }
                startGeneration()
            } else {
                drawHourglassIndicator(IndicatorState.OFF)
                isNextImageReady = true
                viewModel.nextImage = pixels
            }
        }

        if (!viewModel.isNextImageInitialized()) {
            lifecycleScope.launch(Dispatchers.IO) {
                println("INTERNAL generation started")
                drawHourglassIndicator(IndicatorState.WAITING_FOR_CHANGE_IMAGE)
                viewModel.nextImage = getPixels()
                isNextImageReady = true
                println("INTERNAL generation ended")
                startGeneration()
            }
        }

        if (isNextImageReady) {
            println("NEXT IMAGE READY")
            drawImage(viewModel.nextImage)
            isNextImageReady = false
            drawHourglassIndicator(IndicatorState.WAITING_FOR_NEXT_IMAGE)
            lifecycleScope.launch(Dispatchers.IO) {
                println("generation started")
                startGeneration()
                println("generation ended")
            }
        } else {
            isWaitForImage = true
            drawHourglassIndicator(IndicatorState.WAITING_FOR_CHANGE_IMAGE)
        }
    }

    class GenerationActivityViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
        private val viewModelScope = CoroutineScope(Dispatchers.Main)
        lateinit var mainImage: ImageView
        lateinit var currentImage : IntArray
        lateinit var nextImage: IntArray
        fun isNextImageInitialized() : Boolean { return ::nextImage.isInitialized }

        fun saveImage() {
            println(::currentImage.isInitialized)
            if (::currentImage.isInitialized)
                println(currentImage)
        }

        fun addImageToGallery() {

        }

        fun shareImage() {

        }

        fun setImageAsWallpaper() {
            if(!::currentImage.isInitialized)
                return

            viewModelScope.launch(Dispatchers.IO) {
                repository.saveImageToGallery(currentImage, mainImage.width, mainImage.height)
            }
        }

        override fun onCleared() {
            super.onCleared()
            viewModelScope.cancel()
        }
    }

    private fun drawImage(image: IntArray) {
        println("DRAWING STARTED")
        viewModel.currentImage = image
        val bitmap = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(image, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        mainImage.setImageBitmap(bitmap)
        println("DRAWING ENDED")
    }

    data class NoiseParameters(
        var isLevelCountRandom: Boolean,
        var levelCount: Int,
        var isTopColorRandom: Boolean,
        var topColor: Int,
        var isBottomColorRandom: Boolean,
        var bottomColor: Int,
    )

    data class GradientParameters(
        var gradientType: ImageGenerator.Companion.GradientType,
        var isColorsRandom: Boolean,
        var minColorsCount: Int,
        var maxColorsCount: Int,
        var colorsCount: Int,
        var colors: MutableList<Int>
    )

    data class FractalParameters(
        var fractalType: ImageGenerator.Companion.FractalType,
        var coloringType: ImageGenerator.Companion.FractalColoringType,
        var depth: Int,
        var isOffsetRandom: Boolean,
        var offsetX: Int,
        var offsetY: Int,
        var isZoomRandom: Boolean,
        var zoom: Int,
        var isTopColorRandom: Boolean,
        var topColor: Int,
        var isBottomColorRandom: Boolean,
        var bottomColor: Int,
    )

    data class ShapeParameters(
        var isBackgroundColorRandom: Boolean,
        var backgroundColor: Int,
        var minShapeCount: Int,
        var maxShapeCount: Int,
        var borderChance: Int,
        var ableCircles: Boolean,
        var ableRectangles: Boolean,
        var ableTriangles: Boolean,
        var ableLines: Boolean,
        var ablePlanes: Boolean,
    )

    class ParametersHolder : ViewModel() {
        lateinit var currentGenerationType: GenerationType
        val noiseParameters = NoiseParameters(
            true, 4,
            true, Color.RED,
            true, Color.BLUE)
        val gradientParameters = GradientParameters(
            ImageGenerator.Companion.GradientType.StrictLine,
            true,
            3,
            7,
            4,
            mutableListOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        )
        var fractalParameters = FractalParameters(
            ImageGenerator.Companion.FractalType.JuliaSet,
            ImageGenerator.Companion.FractalColoringType.Module,
            32,
            true,
            50,
            50,
            true,
            0,
            true,
            Color.rgb(0, 200, 255),
            true,
            Color.GRAY
        )
        var shapeParameters = ShapeParameters(
            false,
            Color.WHITE,
            5,
            9,
            50,
            true, true, true, true, true,
        )
    }

    enum class IndicatorState {
        OFF,
        WAITING_FOR_MAX_REACHED,
        WAITING_FOR_NEXT_IMAGE,
        WAITING_FOR_CHANGE_IMAGE
    }

    fun drawHourglassIndicator(state: IndicatorState) {
        binding.generationIndicator.imageAlpha = 255
        when (state) {
            IndicatorState.OFF -> binding.generationIndicator.imageAlpha = 0
            IndicatorState.WAITING_FOR_MAX_REACHED -> binding.generationIndicator.setColorFilter(Color.GREEN)
            IndicatorState.WAITING_FOR_NEXT_IMAGE -> binding.generationIndicator.setColorFilter(Color.rgb(255, 105, 0))
            IndicatorState.WAITING_FOR_CHANGE_IMAGE -> binding.generationIndicator.setColorFilter(Color.RED)
        }
    }
}