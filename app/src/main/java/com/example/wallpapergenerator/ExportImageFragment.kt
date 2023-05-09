package com.example.wallpapergenerator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wallpapergenerator.databinding.ActivityGenerationBinding
import com.example.wallpapergenerator.databinding.FragmentExportImageBinding


class ExportImageFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentExportImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExportImageBinding.inflate(layoutInflater)

        binding.expandButton.setOnClickListener(this)

        return inflater.inflate(R.layout.fragment_export_image, container, false)
    }

    fun onExpandClick(view: View) {
        println("Click there")
    }

    override fun onClick(view: View?) {
        println("Redirect")
    }
}