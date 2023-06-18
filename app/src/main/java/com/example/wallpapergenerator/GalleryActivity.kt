package com.example.wallpapergenerator

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.databinding.ActivityGalleryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.parameterholders.GalleryParametersHolder
import com.example.wallpapergenerator.viewmodels.GalleryViewModel
import javax.inject.Inject

class GalleryActivity : AppCompatActivity() {
    lateinit var galleryAdapter: GalleryAdapter

    private lateinit var binding: ActivityGalleryBinding

    private lateinit var parameters: GalleryParametersHolder
    lateinit var viewModel: GalleryViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory<GalleryViewModel>
    lateinit var expandedWallpaperFragment: ExpandedWallpaperFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        injectViewModels()

        createFragments()

        initView()

        initGalleryAdapter()

        askPermissions()

        viewModel.loadData()
    }

    private fun initView() {
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
    }

    private fun initGalleryAdapter() {
        val galleryList = binding.galleryRecyclerView
        val layoutManager = GridLayoutManager(this, 2)
        galleryList.layoutManager = layoutManager

        galleryAdapter = GalleryAdapter()

        galleryList.adapter = galleryAdapter

        viewModel.cards.observe(this) { cards ->
            galleryAdapter.submitList(cards)
        }
    }

    private fun createFragments() {
        supportFragmentManager.beginTransaction().replace(R.id.settingsFragmentContainer, SettingsFragment(parameters)).commit()

        fun onExportImageFragmentCreated(exportImageFragment: ExportImageFragment) {
            if (viewModel.getIsUserAuthorized()) {
                exportImageFragment.onLikeClick = viewModel::likeImage
            } else {
                exportImageFragment.hideLike()
            }
            exportImageFragment.onSaveImageClick = viewModel::saveImage
        }
        fun onCollapse() {
            binding.wallpaperFragmentContainer.isVisible = false
        }
        expandedWallpaperFragment = ExpandedWallpaperFragment(::onExportImageFragmentCreated, ::onCollapse)
        supportFragmentManager.beginTransaction().replace(R.id.wallpaperFragmentContainer, expandedWallpaperFragment).commit()
        viewModel.expandedWallpaperFragment = expandedWallpaperFragment
    }

    private fun injectViewModels() {
        (application as MainApplication).appComponent.inject(this)

        parameters = ViewModelProvider(this)[GalleryParametersHolder::class.java]

        viewModel = ViewModelProvider(this, viewModelFactory)[GalleryViewModel::class.java]
        parameters.onParameterChanged = viewModel::loadData
        viewModel.parameters = parameters
        viewModel.onWallpaperClicked = { wallpaper ->
            if (wallpaper.image.value != null) {
                binding.wallpaperFragmentContainer.isVisible = true
                binding.wallpaperFragmentContainer.getFragment<ExpandedWallpaperFragment>().setWallpaper(wallpaper)
            }
        }
    }

    private fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 123)
        }
    }

    private fun toggleGalleryAndCollection(){
        viewModel.isInGallery = !viewModel.isInGallery
        binding.toGalleryButton.isAllCaps = viewModel.isInGallery
        binding.toCollectionButton.isAllCaps = !viewModel.isInGallery

        viewModel.loadData()
    }

    private fun hideCollection() {
        binding.toCollectionButton.visibility = View.GONE
    }
}