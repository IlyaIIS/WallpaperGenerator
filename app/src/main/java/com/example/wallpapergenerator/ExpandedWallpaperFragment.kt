package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.example.wallpapergenerator.network.WallpaperData

class ExpandedWallpaperFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_expanded_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ConstraintLayout>(R.id.background).setOnClickListener { }

        view.findViewById<Button>(R.id.collapseWallpaperButton).setOnClickListener {
            (activity as GalleryActivity).wallpaperFragmentContainer.isVisible = false
        }
    }

    fun setWallpaper(wallpaper: WallpaperData) {
        view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(wallpaper.image.value)
    }
}