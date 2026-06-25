package com.example.translateapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.translateapp.R
import com.example.translateapp.model.FavoriteEntity

class FavoriteAdapter(
    private val onDeleteClick: (FavoriteEntity) -> Unit,
    private val onItemClick: (FavoriteEntity) -> Unit
    ) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    private var list = listOf<FavoriteEntity>()

    fun setData(newList: List<FavoriteEntity>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvSource =
            view.findViewById<TextView>(R.id.tvFavoriteSource)

        val tvResult =
            view.findViewById<TextView>(R.id.tvFavoriteResult)

        val tvLang =
            view.findViewById<TextView>(R.id.tvFavoriteLang)

        val btnDltFav =
            view.findViewById<ImageView>(R.id.btnDltFav)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_favorite,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val favorite = list[position]

        holder.tvSource.text =
            favorite.sourceText

        holder.tvResult.text =
            favorite.translatedText

        holder.tvLang.text =
            "${favorite.sourceLang} -> ${favorite.targetLang}"

        holder.btnDltFav.setOnClickListener {
            onDeleteClick(favorite)
        }

        holder.itemView.setOnClickListener {
            onItemClick(favorite)
        }
    }

    override fun getItemCount(): Int =
        list.size
}