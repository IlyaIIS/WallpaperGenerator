package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.databinding.FragmentExportImageBinding


class ExportImageFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentExportImageBinding
    private lateinit var viewModel: GenerationActivity.GenerationActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExportImageBinding.inflate(layoutInflater)

        binding.expandButton.setOnClickListener(this)

        viewModel = ViewModelProvider(requireActivity())[GenerationActivity.GenerationActivityViewModel::class.java]

        return inflater.inflate(R.layout.fragment_export_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.saveImageButton).setOnClickListener {
            viewModel.saveImage()
        }
        view.findViewById<ImageButton>(R.id.setIamgeAsWallpaperButton).setOnClickListener {
            viewModel.setImageAsWallpaper()
        }
        view.findViewById<ImageButton>(R.id.addImageToGalleryButton).setOnClickListener {
            viewModel.addImageToGallery()
        }
    }

    fun onExpandClick(view: View) {
        println("Click there")
    }

    override fun onClick(view: View?) {
        println("Redirect")
    }
}