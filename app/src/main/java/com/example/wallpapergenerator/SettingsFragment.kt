package com.example.wallpapergenerator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wallpapergenerator.databinding.FragmentGenerationParametersBinding


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentGenerationParametersBinding
    private lateinit var parametersHolder: ParameterHolder
    private lateinit var adapter: GenerationSettingsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGenerationParametersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parametersHolder = when (activity) {
            is GenerationActivity -> ViewModelProvider(requireActivity())[GenerationActivity.GenerationParametersHolder::class.java]
            is GalleryActivity -> ViewModelProvider(requireActivity())[GalleryActivity.GalleryParametersHolder::class.java]
            else -> throw NotImplementedError()
        }


        adapter = GenerationSettingsRecyclerViewAdapter()
        binding.paramsList.adapter = adapter

        updateParameters()
    }

    fun updateParameters() {
        adapter.submitList(parametersHolder.getParameters(::updateParameters))
    }
}