package com.example.wallpapergenerator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wallpapergenerator.databinding.FragmentSettingsBinding
import com.example.wallpapergenerator.parameterholders.ParameterHolder


class SettingsFragment(private val parametersHolder: ParameterHolder) : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var adapter: GenerationSettingsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GenerationSettingsRecyclerViewAdapter()
        binding.paramsList.adapter = adapter

        updateParameters()
    }

    fun updateParameters() {
        adapter.submitList(parametersHolder.getParameters(::updateParameters))
    }
}