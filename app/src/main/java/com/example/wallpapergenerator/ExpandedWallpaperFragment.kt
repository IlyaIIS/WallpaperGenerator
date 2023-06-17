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
import androidx.fragment.app.FragmentContainerView
import com.example.wallpapergenerator.network.WallpaperData

class ExpandedWallpaperFragment : Fragment() {
    lateinit var exportImageFragment: ExportImageFragment
    var wallpaperData: WallpaperData? = null
    lateinit var onSaveImage: () -> Unit
    lateinit var onLikeImage: () -> Unit

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

        exportImageFragment = view.findViewById<FragmentContainerView>(R.id.exportImageFragmentContainer).getFragment()
        exportImageFragment.onSaveImageClick = {
            onSaveImage()
        }
        exportImageFragment.onLikeClick = {
            onLikeImage()
        }

        view.findViewById<ConstraintLayout>(R.id.background).setOnClickListener { }

        view.findViewById<Button>(R.id.collapseWallpaperButton).setOnClickListener {
            (activity as GalleryActivity).wallpaperFragmentContainer.isVisible = false
        }
    }

    fun setWallpaper(wallpaper: WallpaperData) {
        this.wallpaperData = wallpaper
        view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(wallpaper.image.value)
        if (wallpaper.isLiked) {
            exportImageFragment.like()
        } else {
            exportImageFragment.dislike()
        }
    }
}