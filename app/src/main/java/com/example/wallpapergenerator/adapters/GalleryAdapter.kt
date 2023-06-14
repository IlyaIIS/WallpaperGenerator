package com.example.wallpapergenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpapergenerator.network.WallpaperData

class GalleryAdapter() : androidx.recyclerview.widget.ListAdapter<WallpaperData,
        GalleryAdapter.MyViewHolder>(MyDiffUtil())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(com.example.wallpapergenerator.R.layout.card, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.cardText.text = currentList.get(position).likes.toString()
        holder.cardImage.setImageBitmap(currentList.get(position).image)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardText: TextView = itemView.findViewById(com.example.wallpapergenerator.R.id.cardText)
        val cardImage: ImageView = itemView.findViewById(com.example.wallpapergenerator.R.id.cardImage)
    }

    class MyDiffUtil() : DiffUtil.ItemCallback<WallpaperData>() {
        override fun areItemsTheSame(oldItem: WallpaperData, newItem: WallpaperData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WallpaperData, newItem: WallpaperData): Boolean {
            return oldItem.id == newItem.id
        }
    }
}