package com.example.wallpapergenerator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.databinding.FragmentOutsideGallaryBinding
import com.example.wallpapergenerator.di.MainApplication
import com.example.wallpapergenerator.di.ViewModelFactory
import com.example.wallpapergenerator.network.Repository
import com.example.wallpapergenerator.network.WallpaperData
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OutsideGalleryFragment : Fragment() {
    lateinit var galleryAdapter: GalleryAdapter

    @Inject lateinit var viewModelFactory: ViewModelFactory<MainFragmentViewModel>

    private lateinit var viewModel: MainFragmentViewModel

    private lateinit var binding: FragmentOutsideGallaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOutsideGallaryBinding.inflate(inflater, container, false)

        (activity?.application as MainApplication).appComponent.inject(this)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[MainFragmentViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = Bitmap.createBitmap(1, 2,Bitmap.Config.ARGB_8888)

        val galleryList = binding.galleryRecyclerView
        val layoutManager = GridLayoutManager(requireActivity(), 2)
        galleryList.layoutManager = layoutManager

        galleryAdapter = GalleryAdapter()

        galleryList.adapter = galleryAdapter

        viewModel.cards.observe(this.viewLifecycleOwner) { cards ->
            galleryAdapter.submitList(cards)
        }

        viewModel.loadData()

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

class MainFragmentViewModel @Inject constructor(private val repository: Repository): ViewModel() {
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