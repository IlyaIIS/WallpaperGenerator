package com.example.wallpapergenerator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wallpapergenerator.databinding.ActivityMainBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.imagegeneration.GenerationType
import com.example.wallpapergenerator.network.NetRepository
import com.example.wallpapergenerator.repository.LocalRepository
import com.example.wallpapergenerator.repository.ToastMessageDrawer
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var localRepository: LocalRepository
    @Inject
    lateinit var toastMessageDrawer: ToastMessageDrawer
    @Inject
    lateinit var netRepository: NetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blurViewHeader = binding.fkBlurView
        blurViewHeader.setBlur(this, blurViewHeader)

        val blurViewMenu = binding.fkBlurViewMenu
        blurViewMenu.setBlur(this, blurViewMenu)

        binding.toCollectionButton.setOnClickListener {
            if (netRepository.getIsNetConnection()) {
                Intent(this, GalleryActivity::class.java).run { startActivity(this) }
            } else {
                toastMessageDrawer.showMessage(getString(R.string.error_no_net_connection))
            }
        }
        (application as MainApplication).appComponent.inject(this)
        fun setLoginButton() {
            binding.loginButton.text = getString(R.string.login)
            binding.loginButton.setOnClickListener {
                Intent(this, AuthActivity::class.java).run { startActivity(this) }
            }
        }
        val isAuthorized = !localRepository.readToken().isNullOrBlank()
        if (isAuthorized) {
            binding.loginButton.text = getString(R.string.logout)
            binding.loginButton.setOnClickListener {
                localRepository.logout()
                setLoginButton()
            }
        } else {
            setLoginButton()
        }

        binding.generationButtonGradients.setOnClickListener {
            goToGenerationAction(GenerationType.GRADIENTS)
        }
        binding.generationButtonShapes.setOnClickListener {
            goToGenerationAction(GenerationType.SHAPES)
        }
        binding.generationButtonInterference.setOnClickListener {
            goToGenerationAction(GenerationType.INTERFERENCE)
        }
        binding.generationButtonFractals.setOnClickListener {
            goToGenerationAction(GenerationType.FRACTALS)
        }
    }

    fun goToGenerationAction(generationType: GenerationType) {
        val intent = Intent(this, GenerationActivity::class.java)
        intent.putExtra("typeNum", generationType.ordinal)
        startActivity(intent)
    }
}