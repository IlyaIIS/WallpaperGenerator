package com.example.wallpapergenerator.adapters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpapergenerator.R
import com.example.wallpapergenerator.network.WallpaperData

class GalleryAdapter() : androidx.recyclerview.widget.ListAdapter<WallpaperData,
        GalleryAdapter.MyViewHolder>(MyDiffUtil())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.initialize(currentList[position])
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardText: TextView = itemView.findViewById(com.example.wallpapergenerator.R.id.cardText)
        private val cardImage: ImageView = itemView.findViewById(com.example.wallpapergenerator.R.id.cardImage)

        fun initialize(data: WallpaperData) {
            var colorHex = "#8050A2"
            if(data.mainColor.isNotEmpty())
                colorHex = data.mainColor
            val colorInt = Color.parseColor(colorHex)
            val bitmap = Bitmap.createBitmap(1024, 1280, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(colorInt)

            cardImage.setImageBitmap(bitmap)

            data.image.observe(itemView.context as LifecycleOwner) {
                cardImage.setImageBitmap(it)
            }
            data.onInScreen(data)

            cardImage.setOnClickListener {
                data.onClick(data)
            }

            data.onLiked = {
                val notActiveColor = ContextCompat.getColor(itemView.context, R.color.red_like_not_active)
                val activeColor = ContextCompat.getColor(itemView.context, R.color.red_like)
                if (data.isLiked) {
                    itemView.findViewById<ImageView>(R.id.heart)
                        .setColorFilter(activeColor)
                    itemView.findViewById<TextView>(R.id.cardText).setTextColor(Color.WHITE)
                } else {
                    itemView.findViewById<ImageView>(R.id.heart)
                        .setColorFilter(notActiveColor)
                    itemView.findViewById<TextView>(R.id.cardText).setTextColor(Color.BLACK)
                }
                cardText.text = data.likes.toString()
            }

            data.onLiked(data)
        }
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