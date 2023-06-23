package com.example.wallpapergenerator

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wallpapergenerator.SupportTools.Companion.toEnum
import com.example.wallpapergenerator.SupportTools.Companion.toInt
import com.example.wallpapergenerator.adapters.generationsettingsadapter.*
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.imagegeneration.*
import com.example.wallpapergenerator.parameterholders.GenerationParametersHolder
import com.example.wallpapergenerator.viewmodels.GenerationActivityViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis


class GenerationActivity : AppCompatActivity() {
    private val TAG = "GenerationActivity"
    private lateinit var binding: ActivityGenerationBinding
    private lateinit var mainImage: ImageView
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

        mainImage = binding.mainImage
        injectViewModels()

        createFragments()

        updateGenerationName()

        initView()

        askPermissions()

        Thread {
            Thread.sleep(1000)
            requireImage()
        }.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.nextImageGenerationButton.setOnTouchListener(object : OnSwipeTouchListener(this@GenerationActivity) {
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

        if (viewModel.getIsUserAuthorized()) {
            viewModel.exportImageFragment.onLikeClick = viewModel::likeImage
        } else {
            viewModel.exportImageFragment.hideLike()
        }
        viewModel.exportImageFragment.onSaveImageClick = viewModel::saveImage
    }

    private fun injectViewModels() {
        (application as MainApplication).appComponent.inject(this)

        parameters = ViewModelProvider(this, parameterFactory)[GenerationParametersHolder::class.java]
        parameters.loadParameters()
        parameters.currentGenerationType = intent.getIntExtra("typeNum", 0).toEnum()
        parameters.onParameterChanged = {
            restartGeneration()
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[GenerationActivityViewModel::class.java]
        viewModel.parematers = parameters
        viewModel.mainImage = mainImage
        viewModel.exportImageFragment = binding.exportImageFragmentContainer.getFragment()
    }

    private fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 123)
        }
    }

    private fun createFragments() {
        settingsFragment = SettingsFragment(parameters)
        supportFragmentManager.beginTransaction().replace(R.id.settingsFragmentContainer, settingsFragment).commit()
    }

    private fun setPrevGenerator() {
        parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() + 1).mod(GenerationType.values().size)).toEnum()
        onGeneratorChanged()
    }
    private fun setNextGenerator() {
        parameters.currentGenerationType = ((parameters.currentGenerationType.toInt() - 1).mod(GenerationType.values().size)).toEnum()
        onGeneratorChanged()
    }
    private fun onGeneratorChanged() {
        updateGenerationName()
        settingsFragment.updateParameters()
        restartGeneration()
    }

    private fun updateGenerationName() {
        binding.generationName.text = when (parameters.currentGenerationType) {
            GenerationType.GRADIENTS -> getString(R.string.generator_gradients)
            GenerationType.SHAPES -> getString(R.string.generator_shapes)
            GenerationType.INTERFERENCE -> getString(R.string.generator_interference)
            GenerationType.FRACTALS -> getString(R.string.generator_fractals)
            //GenerationType.NOISE -> "Шум"
            GenerationType.POLYGONS -> "Полигоны"
            else -> throw NotImplementedError()
        }
    }

    fun onNextImageGenerationClick() {
        Log.i(TAG, "Generate next image")
        requireImage()
    }

    fun onClickMenu(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private var isWaitForImage = false
    private var isGenerating = false
    private var shouldStopGeneration = false
    private fun requireImage() {
        fun getPixels(): IntArray {
            return when (parameters.currentGenerationType) {
                GenerationType.GRADIENTS -> GradientImageGenerator.generateImage(mainImage.width, mainImage.height, parameters.gradientParameters)
                GenerationType.SHAPES -> ShapeImageGenerator.generateImage(mainImage.width, mainImage.height, parameters.shapeParameters)
                GenerationType.INTERFERENCE -> InterferenceImageGenerator.generateImage(mainImage.width, mainImage.height, parameters.interferenceParameters)
                GenerationType.FRACTALS -> FractalImageGenerator.generateImage(mainImage.width, mainImage.height, parameters.fractalParameters)
                //GenerationType.NOISE -> NoiseImageGenerator(mainImage.width, mainImage.height).generateImage()
                GenerationType.POLYGONS -> PolygonImageGenerator(mainImage.width, mainImage.height).generateImage(parameters.polygonParameters)
                else -> throw NotImplementedError()
            }
        }

        fun returnImage() {
            val pixels = viewModel.nextImages.removeFirst()
            val result = measureTimeMillis {
                runOnUiThread {
                    drawImage(pixels)
                }
            }
            Log.i(TAG, "DRAWING TIME: $result")
        }

        fun defineHourglassIndicator() {
            if (isWaitForImage) {
                drawHourglassIndicator(IndicatorState.WAITING_FOR_CHANGE_IMAGE)
            } else if (viewModel.nextImages.count() == 0) {
                drawHourglassIndicator(IndicatorState.WAITING_FOR_NEXT_IMAGE)
            } else if (viewModel.isNextImagePoolFull) {
                drawHourglassIndicator(IndicatorState.OFF)
            } else {
                drawHourglassIndicator(IndicatorState.WAITING_FOR_MAX_REACHED)
            }
        }

        fun startGeneration() {
            isGenerating = true

            lifecycleScope.launch(Dispatchers.IO) {
                while (true) {
                    val result = measureTimeMillis {
                        viewModel.nextImages.addLast(getPixels())
                    }
                    Log.i(TAG, "GENERATION TIME: $result")

                    if (shouldStopGeneration) {
                        shouldStopGeneration = false
                        isGenerating = false
                        viewModel.nextImages.clear()
                        requireImage()
                        break
                    }

                    if (isWaitForImage) {
                        isWaitForImage = false
                        returnImage()
                    }

                    defineHourglassIndicator()

                    if (viewModel.isNextImagePoolFull) {
                        isGenerating = false
                        break
                    }
                }
            }
        }

        if (!viewModel.nextImages.isEmpty()) {
            returnImage()
        } else {
            isWaitForImage = true
        }
        if (!isGenerating) {
            startGeneration()
        }

        defineHourglassIndicator()
    }

    private fun restartGeneration() {
        viewModel.nextImages.clear()
        requireImage()
        if (isGenerating) {
            shouldStopGeneration = true
        }
    }

    private fun drawImage(image: IntArray) {
        Log.i(TAG, "DRAWING STARTED")
        viewModel.isImageSaved = false
        viewModel.exportImageFragment.dislike()
        viewModel.currentImage = image
        val bitmap = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(image, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        mainImage.setImageBitmap(bitmap)
        Log.i(TAG, "DRAWING ENDED")
    }

    enum class IndicatorState {
        OFF,
        WAITING_FOR_MAX_REACHED,
        WAITING_FOR_NEXT_IMAGE,
        WAITING_FOR_CHANGE_IMAGE
    }

    private fun drawHourglassIndicator(state: IndicatorState) {
        binding.generationIndicator.imageAlpha = 255
        when (state) {
            IndicatorState.OFF -> binding.generationIndicator.imageAlpha = 0
            IndicatorState.WAITING_FOR_MAX_REACHED -> binding.generationIndicator.setColorFilter(Color.GREEN)
            IndicatorState.WAITING_FOR_NEXT_IMAGE -> binding.generationIndicator.setColorFilter(Color.rgb(255, 105, 0))
            IndicatorState.WAITING_FOR_CHANGE_IMAGE -> binding.generationIndicator.setColorFilter(Color.RED)
        }
    }
}

