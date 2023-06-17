package com.example.wallpapergenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LifecycleOwner
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
        val data = currentList[position]
        holder.cardText.text = data.likes.toString()
        data.image.observe(holder.itemView.context as LifecycleOwner) {
            holder.cardImage.setImageBitmap(it)
        }
        data.onInScreen(data)
        holder.cardImage.setOnClickListener {
            data.onClick(data)
        }
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