package com.example.wallpapergenerator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.repository.LocalRepository
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random


class GenerationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerationBinding
    private lateinit var mainImage: ImageView
    private var isWaitForImage = false
    private var isNextImageReady = false
    private lateinit var parameters: GenerationParametersHolder
    private lateinit var settingsFragment: SettingsFragment
    
    @Inject lateinit var viewModelFactory: ViewModelFactory<GenerationActivityViewModel>
    @Inject lateinit var parameterFactory: ViewModelFactory<GenerationParametersHolder>
    lateinit var viewModel: GenerationActivityViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[GenerationActivityViewModel::class.java]
        parameters = ViewModelProvider(this, parameterFactory)[GenerationParametersHolder::class.java]
        parameters.loadParameters()
        mainImage = binding.mainImage
        viewModel.mainImage = mainImage
        viewModel.context = this
        parameters.currentGenerationType = intent.getSerializableExtra("Type") as GenerationType
        updateGenerationName()

        binding.nextImageGenerationButton.setOnTouchListener(object: OnSwipeTouchListener(this@GenerationActivity) {
            override fun onSwipeLeft() {
                setPrevGenerator()
            }
            override fun onSwipeRight() {
                setNextGenerator()
            }
            override fun onClick() {
                onNextImageGenerationClick()
            }
        })

        binding.toPrevGeneratorButton.setOnClickListener {
            setPrevGenerator()
        }
        binding.toNextGeneratorButton.setOnClickListener {
            setNextGenerator()
        }

        binding.settingsButton.setOnClickListener {
            if (binding.settingsFragmentContainer.visibility == View.GONE) {
                binding.settingsFragmentContainer.visibility = View.VISIBLE
                binding.nextImageGenerationButton.isEnabled = false
            } else {
                binding.settingsFragmentContainer.visibility = View.GONE
                binding.nextImageGenerationButton.isEnabled = true
            }
        }

        settingsFragment = supportFragmentManager.findFragmentById(R.id.settingsFragmentContainer) as SettingsFragment

        Thread {
            Thread.sleep(1000)
            returnNewImage()
        }.start()
    }

    fun setPrevGenerator() {
        parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() + 1).mod(GenerationType.values().size)).toEnum()
        updateGenerationName()
        returnNewImage()
        settingsFragment.updateParameters()
    }
    fun setNextGenerator() {
        parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() - 1).mod(GenerationType.values().size)).toEnum()
        updateGenerationName()
        returnNewImage()
        settingsFragment.updateParameters()
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
        lateinit var context : Context
        lateinit var currentImage : IntArray
        lateinit var nextImage: IntArray
        fun isNextImageInitialized() : Boolean { return ::nextImage.isInitialized }

        fun saveImage() {
            println(::currentImage.isInitialized)
            if(!::currentImage.isInitialized){
                ShowMessage("Дождитесь генерации")
                return
            }
            val bitmap = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(currentImage, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            saveMediaToStorage(bitmap)
        }

        fun addImageToGallery() {
        }

        fun shareImage() {

        }

        fun setImageAsWallpaper() {
            fun onSuccessful() = ShowMessage("Сохранено в галерею")
            fun onFailed() = ShowMessage("Не удалось сохранить")

            if(!::currentImage.isInitialized){
                ShowMessage("Дождитесь генерации")
                return
            }

            viewModelScope.launch(Dispatchers.IO) {
                repository.saveImageToGallery(
                    currentImage,
                    mainImage.width,
                    mainImage.height,
                    ::onSuccessful,
                    ::onFailed)

            }
        }

        override fun onCleared() {
            super.onCleared()
            viewModelScope.cancel()
        }

        @SuppressLint("SimpleDateFormat")
        private fun saveMediaToStorage(bitmap: Bitmap) {
            val df: DateFormat = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss")
            val date = df.format(Calendar.getInstance().time)
            val imageFileName = "wp_${date}.png"
            try {
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(storageDir, imageFileName)

                val fileOutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), null, null)
                ShowMessage("Успешно сохранено в Pictures/")
            }
            catch (e: Exception){
                ShowMessage("Ошибка! Проверьте разрешения")
            }

        }

        fun ShowMessage(text : String){
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(context, text, duration)
            toast.show()
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

    class GenerationParametersHolder @Inject constructor(private val repository: LocalRepository) : ParameterHolder() {
        lateinit var currentGenerationType: GenerationType

        var noiseParameters = NoiseParameters(
            true, 4,
            true, Color.RED,
            true, Color.BLUE
        )

        var gradientParameters = GradientParameters(
            ImageGenerator.Companion.GradientType.StrictLine,
            true,
            3,
            7,
            4,
            mutableListOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        )

        var fractalParameters = FractalParameters(
            ImageGenerator.Companion.FractalType.JuliaSet,
            ImageGenerator.Companion.FractalColoringType.MODULE,
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

        private fun saveParameters() {
            repository.saveSetting(noiseParameters::class.simpleName!!, Gson().toJson(noiseParameters))
            repository.saveSetting(gradientParameters::class.simpleName!!, Gson().toJson(gradientParameters))
            repository.saveSetting(fractalParameters::class.simpleName!!, Gson().toJson(fractalParameters))
            repository.saveSetting(shapeParameters::class.simpleName!!, Gson().toJson(shapeParameters))
        }

        fun loadParameters() {
            noiseParameters = Gson().fromJson(repository.readSettingString(noiseParameters::class.simpleName!!), NoiseParameters::class.java) ?: noiseParameters
            gradientParameters = Gson().fromJson(repository.readSettingString(gradientParameters::class.simpleName!!), GradientParameters::class.java) ?: gradientParameters
            fractalParameters = Gson().fromJson(repository.readSettingString(fractalParameters::class.simpleName!!), FractalParameters::class.java) ?: fractalParameters
            shapeParameters = Gson().fromJson(repository.readSettingString(shapeParameters::class.simpleName!!), ShapeParameters::class.java) ?: shapeParameters
        }

        override fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter> {
            lateinit var params: List<SettingsParameter>

            if (currentGenerationType == GenerationType.Noise) {
                params = mutableListOf()
                params.add(
                    CheckboxParameter(
                        "Случайное количество слоёв",
                        noiseParameters.isLevelCountRandom
                    ) { isChecked ->
                        noiseParameters.isLevelCountRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!noiseParameters.isLevelCountRandom)
                    params.add(
                        InputDigitParameter(
                            "Количество слоёв",
                            noiseParameters.levelCount,
                            1, 9999
                        ) { number ->
                            noiseParameters.levelCount = number
                            saveParameters()
                        }
                    )

                params.add(
                    CheckboxParameter(
                        "Случайный верхний цвет",
                        noiseParameters.isTopColorRandom
                    ) { isChecked ->
                        noiseParameters.isTopColorRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!noiseParameters.isTopColorRandom)
                    params.add(
                        ColorParameter(
                            "Верхний цвет",
                            noiseParameters.topColor,
                        ) { color ->
                            noiseParameters.topColor = color
                            saveParameters()
                        }
                    )

                params.add(
                    CheckboxParameter(
                        "Случайный нижний цвет",
                        noiseParameters.isBottomColorRandom
                    ) { isChecked ->
                        noiseParameters.isBottomColorRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!noiseParameters.isBottomColorRandom) {
                    params.add(
                        ColorParameter(
                            "Нижний цвет",
                            noiseParameters.bottomColor,
                        ) { color ->
                            noiseParameters.bottomColor = color
                            saveParameters()
                        }
                    )
                }

            } else if (currentGenerationType == GenerationType.Shapes) {
                params = mutableListOf<SettingsParameter>(
                    CheckboxParameter(
                        "Случайный фон",
                        shapeParameters.isBackgroundColorRandom
                    ) { isChecked ->
                        shapeParameters.isBackgroundColorRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!shapeParameters.isBackgroundColorRandom) {
                    params.add (
                        ColorParameter(
                            "Цвет заднего фона",
                            shapeParameters.backgroundColor
                        ) { color ->
                            shapeParameters.backgroundColor = color
                            saveParameters()
                        }
                    )
                }
                params.add (
                    InputDigitRangeParameter(
                        "Число фигур",
                        shapeParameters.minShapeCount,
                        shapeParameters.maxShapeCount,
                        1, 20
                    ) { numberFrom, numberTo ->
                        shapeParameters.minShapeCount = numberFrom
                        shapeParameters.maxShapeCount = numberTo
                        saveParameters()
                    }
                )
                params.add (
                    InputDigitParameter(
                        "Шанс появления границы",
                        shapeParameters.borderChance,
                        0, 100
                    ) { number ->
                        shapeParameters.borderChance = number
                        saveParameters()
                    }
                )
                params.add (
                    CheckboxParameter(
                        "Круги",
                        shapeParameters.ableCircles
                    ) { isChecked ->
                        shapeParameters.ableCircles = isChecked
                        saveParameters()
                    }
                )
                params.add (
                    CheckboxParameter(
                        "Прямоугольники",
                        shapeParameters.ableRectangles
                    ) { isChecked ->
                        shapeParameters.ableRectangles = isChecked
                        saveParameters()
                    }
                )
                params.add (
                    CheckboxParameter(
                        "Треугольники",
                        shapeParameters.ableTriangles
                    ) { isChecked ->
                        shapeParameters.ableTriangles = isChecked
                        saveParameters()
                    }
                )
                params.add (
                    CheckboxParameter(
                        "Линии",
                        shapeParameters.ableLines
                    ) { isChecked ->
                        shapeParameters.ableLines = isChecked
                        saveParameters()
                    }
                )
                params.add (
                    CheckboxParameter(
                        "Плоскости",
                        shapeParameters.ablePlanes
                    ) { isChecked ->
                        shapeParameters.ablePlanes = isChecked
                        saveParameters()
                    }
                )

            } else if (currentGenerationType == GenerationType.Gradients) {
                params = mutableListOf<SettingsParameter>(
                    DropdownParameter(
                        "Тип",
                        gradientParameters.gradientType.ordinal,
                        ImageGenerator.gradientTypeNames
                    ) { optionNum ->
                        gradientParameters.gradientType = optionNum.toEnum()
                        saveParameters()
                    }
                )
                params.add(
                    CheckboxParameter(
                        "Случайные цвета",
                        gradientParameters.isColorsRandom
                    ) { isChecked ->
                        gradientParameters.isColorsRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (gradientParameters.isColorsRandom) {
                    params.add(
                        InputDigitRangeParameter(
                            "Число цветов",
                            gradientParameters.minColorsCount,
                            gradientParameters.maxColorsCount,
                            2, 9999
                        ) { numberFrom, numberTo ->
                            gradientParameters.minColorsCount = numberFrom
                            gradientParameters.maxColorsCount = numberTo
                            saveParameters()
                        }
                    )
                } else {
                    params.add(
                        InputDigitParameter(
                            "Число цветов",
                            gradientParameters.colorsCount,
                            2, 30
                        ) { number ->
                            gradientParameters.colorsCount = number
                            for(i in gradientParameters.colors.size until number)
                                gradientParameters.colors.add(
                                    Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
                            updateParameters()
                            saveParameters()
                        }
                    )
                    for (i in 0 until gradientParameters.colorsCount) {
                        params.add(
                            ColorParameter(
                                "Цвет №$i",
                                gradientParameters.colors[i]
                            ) { color ->
                                gradientParameters.colors[i] = color
                                saveParameters()
                            }
                        )
                    }
                }

            } else if (currentGenerationType == GenerationType.Fractals) {
                params = mutableListOf<SettingsParameter>(
                    DropdownParameter(
                        "Фрактал",
                        fractalParameters.fractalType.ordinal,
                        ImageGenerator.fractalTypeNames
                    ) { optionNum ->
                        fractalParameters.fractalType = optionNum.toEnum()
                        saveParameters()
                    }
                )
                params.add(
                    DropdownParameter(
                        "Тип закраски",
                        fractalParameters.coloringType.ordinal,
                        ImageGenerator.fractalColoringTypeNames
                    ) { optionNum ->
                        fractalParameters.coloringType = optionNum.toEnum()
                        updateParameters()
                        saveParameters()
                    }
                )
                params.add(
                    InputDigitParameter(
                        "Макс. глубина",
                        fractalParameters.depth,
                        1, 300
                    ) { number ->
                        fractalParameters.depth = number
                        saveParameters()
                    }
                )
                params.add(
                    CheckboxParameter(
                        "Случайный зум",
                        fractalParameters.isZoomRandom
                    ) { isChecked ->
                        fractalParameters.isZoomRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!fractalParameters.isZoomRandom) {
                    params.add(
                        InputDigitParameter(
                            "Зум",
                            fractalParameters.zoom,
                            0, 100
                        ) { number ->
                            fractalParameters.zoom = number
                            saveParameters()
                        }
                    )
                }
                params.add(
                    CheckboxParameter(
                        "Случайный отступ",
                        fractalParameters.isOffsetRandom
                    ) { isChecked ->
                        fractalParameters.isOffsetRandom = isChecked
                        updateParameters()
                        saveParameters()
                    }
                )
                if (!fractalParameters.isOffsetRandom) {
                    params.add(
                        InputDigitParameter(
                            "X отступ",
                            fractalParameters.offsetX,
                            0, 100
                        ) { number ->
                            fractalParameters.offsetX = number
                            saveParameters()
                        }
                    )
                    params.add(
                        InputDigitParameter(
                            "Y отступ",
                            fractalParameters.offsetY,
                            0, 100
                        ) { number ->
                            fractalParameters.offsetY = number
                            saveParameters()
                        }
                    )
                }
                if (fractalParameters.coloringType == ImageGenerator.Companion.FractalColoringType.LERP) {
                    params.add(
                        CheckboxParameter(
                            "Случайный верхний цвет",
                            fractalParameters.isTopColorRandom
                        ) { isChecked ->
                            fractalParameters.isTopColorRandom = isChecked
                            updateParameters()
                            saveParameters()
                        }
                    )
                    if (!fractalParameters.isTopColorRandom)
                        params.add(
                            ColorParameter(
                                "Верхний цвет",
                                fractalParameters.topColor,
                            ) { color ->
                                fractalParameters.topColor = color
                                saveParameters()
                            }
                        )

                    params.add(
                        CheckboxParameter(
                            "Случайный нижний цвет",
                            fractalParameters.isBottomColorRandom
                        ) { isChecked ->
                            fractalParameters.isBottomColorRandom = isChecked
                            updateParameters()
                            saveParameters()
                        }
                    )
                    if (!fractalParameters.isBottomColorRandom) {
                        params.add(
                            ColorParameter(
                                "Нижний цвет",
                                fractalParameters.bottomColor,
                            ) { color ->
                                fractalParameters.bottomColor = color
                                saveParameters()
                            }
                        )
                    }
                }
            } else {
                throw NotImplementedError()
            }

            return params
        }
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