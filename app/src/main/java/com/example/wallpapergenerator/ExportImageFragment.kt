package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView


class ExportImageFragment : Fragment() {
    var onSaveImageClick: () -> Unit = { }
    var onLikeClick: () -> Unit = { }
    var isLikeHidden = false

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
        if (isLikeHidden) {
            hideLike()
        } else {
            view.findViewById<ImageButton>(R.id.likeButton).setOnClickListener {
                onLikeClick()
            }
        }
    }

    fun like() {
        view?.findViewById<ImageView>(R.id.likeButton)?.setImageResource(R.drawable.heart)
    }
    fun dislike() {
        view?.findViewById<ImageView>(R.id.likeButton)?.setImageResource(R.drawable.heart_void)
    }
    fun hideLike() {
        view?.findViewById<ImageView>(R.id.likeButton)?.visibility = View.GONE
        isLikeHidden = true
    }
}