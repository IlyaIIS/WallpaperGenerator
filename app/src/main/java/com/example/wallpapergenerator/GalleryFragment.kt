package com.example.wallpapergenerator

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.databinding.FragmentGallaryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.WallpaperData
import kotlinx.coroutines.*
import javax.inject.Inject

class OutsideGalleryFragment : Fragment() {
    lateinit var galleryAdapter: GalleryAdapter

    @Inject lateinit var viewModelFactory: ViewModelFactory<OutsideGalleryFragmentViewModel>

    private lateinit var viewModel: OutsideGalleryFragmentViewModel

    private lateinit var binding: FragmentGallaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGallaryBinding.inflate(inflater, container, false)

        (activity?.application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[OutsideGalleryFragmentViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val galleryList = binding.galleryRecyclerView
        val layoutManager = GridLayoutManager(requireActivity(), 2)
        galleryList.layoutManager = layoutManager

        galleryAdapter = GalleryAdapter()

        galleryList.adapter = galleryAdapter

        viewModel.cards.observe(this.viewLifecycleOwner) { cards ->
            galleryAdapter.submitList(cards)
        }

        viewModel.loadData()

        binding.toCollectionButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.menuButton.setOnClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
        }

/*        binding.settingsButton.setOnClickListener {
            binding.settingsFragmentContainer.isVisible = !binding.settingsFragmentContainer.isVisible
        }*/
    }
}

class OutsideGalleryFragmentViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private val _viewModelScope = CoroutineScope(Dispatchers.Main)
    private val _cards = MutableLiveData<List<WallpaperData>>()
    val cards: LiveData<List<WallpaperData>> = _cards

    fun loadData() {
        _viewModelScope.launch {
            val cardData: MutableList<WallpaperData> = mutableListOf()
            val cardTextData = repository.fetchCardsData()
            if (cardTextData != null) {
                for(item in cardTextData){
                    val cardImage = repository.fetchImage(item.id)
                    if(cardImage == null){
                        continue
                    }
                    cardData.add(WallpaperData(item.id, cardImage, item.likes))
                }
            }
            _cards.value = cardData!!
        }
    }

    override fun onCleared() {
        super.onCleared()
        _viewModelScope.cancel()
    }
}