package com.example.wallpapergenerator

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wallpapergenerator.adapters.GalleryAdapter
import com.example.wallpapergenerator.databinding.FragmentFirstBinding
import com.example.wallpapergenerator.network.ApiServices
import com.example.wallpapergenerator.network.WallpaperData
import com.example.wallpapergenerator.network.WallpaperTextData
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    lateinit var galleryAdapter: GalleryAdapter
    private val viewModel = MainFragmentViewModel()

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class MainFragmentViewModel : ViewModel() {
    private val _viewModelScope = CoroutineScope(Dispatchers.Main)
    private val _cards = MutableLiveData<List<WallpaperData>>()
    val cards: LiveData<List<WallpaperData>> = _cards
    private val _apiServices = ApiServices.create()

    fun loadData() {
        _viewModelScope.launch {
            val cardData: MutableList<WallpaperData> = mutableListOf()
            val cardTextData = fetchCardsData()
            if (cardTextData != null) {
                for(item in cardTextData){
                    val cardImage = fetchImage(item.id)
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

    suspend fun fetchImage(id: Int) : Bitmap? {
        try {
            val response = _apiServices.getImages(id.toString())
            return BitmapFactory.decodeStream(response.byteStream())
        } catch (e: Exception) {
            return null
        }

    }
    suspend fun fetchCardsData() : List<WallpaperTextData>? {
        val response = _apiServices.getAll()
        return if (response.isSuccessful) {
            response.body()
        } else {
            Log.d(ContentValues.TAG, "Error while fetching cards: " + response.errorBody())
            null
        }
    }
}