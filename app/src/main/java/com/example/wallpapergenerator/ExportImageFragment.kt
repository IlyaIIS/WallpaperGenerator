package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.databinding.FragmentExportImageBinding


class ExportImageFragment : Fragment() {
    var onSaveImageClick: () -> Unit = { }
    var onLikeClick: () -> Unit = { }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.saveImageButton).setOnClickListener {
            onSaveImageClick()
        }
        view.findViewById<ImageButton>(R.id.likeButton).setOnClickListener {
            onLikeClick()
        }
    }

    fun like() {
        view?.findViewById<ImageView>(R.id.likeButton)?.setImageResource(R.drawable.heart)
    }
    fun dislike() {
        view?.findViewById<ImageView>(R.id.likeButton)?.setImageResource(R.drawable.heart_void)
    }
    fun hideLike() {
        view?.findViewById<ImageView>(R.id.likeButton)?.isVisible = false
    }
}