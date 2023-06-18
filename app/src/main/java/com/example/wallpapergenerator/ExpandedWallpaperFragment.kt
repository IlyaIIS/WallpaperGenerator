package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import com.example.wallpapergenerator.network.WallpaperData

class ExpandedWallpaperFragment(
    private val onExportImageFragmentCreated: (ExportImageFragment) -> Unit,
    private val onCollapse: () -> Unit
) : Fragment() {
    private lateinit var _exportImageFragment: ExportImageFragment
    val exportImageFragment: ExportImageFragment get() = _exportImageFragment
    private var _wallpaperData: WallpaperData? = null
    val wallpaperData: WallpaperData? get() = _wallpaperData

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

        _exportImageFragment = view.findViewById<FragmentContainerView>(R.id.exportImageFragmentContainer).getFragment()
        onExportImageFragmentCreated(exportImageFragment)

        view.findViewById<ConstraintLayout>(R.id.background).setOnClickListener { }

        view.findViewById<AppCompatImageButton>(R.id.collapseWallpaperButton).setOnClickListener {
            onCollapse()
        }
    }

    fun setWallpaper(wallpaper: WallpaperData) {
        _wallpaperData = wallpaper
        view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(wallpaper.image.value)
        if (wallpaper.isLiked) {
            exportImageFragment.like()
        } else {
            exportImageFragment.dislike()
        }
    }
}