package com.example.wallpapergenerator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wallpapergenerator.databinding.ActivityMainBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.repository.LocalRepository
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var localRepository: LocalRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toCollectionButton.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }
        (application as MainApplication).appComponent.inject(this)
        val isAuthorized = !localRepository.readSettingString(LocalRepository.SHARED_PREF_NAME).isNullOrBlank()
        if (isAuthorized) {
            binding.loginButton.text = "Выйти"
            binding.loginButton.setOnClickListener {
                localRepository.logout()
            }
        } else {
            binding.loginButton.text = "Войти"
            binding.loginButton.setOnClickListener {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
        }



        //generation buttons listeners
        binding.generationButtonGradients.setOnClickListener {
            goToGenerationAction(GenerationType.Gradients)
        }
        binding.generationButtonShapes.setOnClickListener {
            goToGenerationAction(GenerationType.Shapes)
        }
        binding.generationButtonNoise.setOnClickListener {
            goToGenerationAction(GenerationType.Noise)
        }
        binding.generationButtonFractals.setOnClickListener {
            goToGenerationAction(GenerationType.Fractals)
        }
    }

    fun goToGenerationAction(generationType: GenerationType) {
        val intent = Intent(this, GenerationActivity::class.java)
        intent.putExtra("Type", generationType)
        startActivity(intent)
    }
}