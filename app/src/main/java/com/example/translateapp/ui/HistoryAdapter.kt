package com.example.translateapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.translateapp.R
import com.example.translateapp.model.HistoryEntity

class HistoryAdapter(
            private val onItemClick: (HistoryEntity) -> Unit
    ) : ListAdapter<HistoryEntity, HistoryAdapter.HistoryViewHolder>(
            HistoryDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

        val history = getItem(position)

        holder.bind(history)

        holder.itemView.setOnClickListener {
            onItemClick(history)
        }
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSource: TextView = itemView.findViewById(R.id.tvHistorySource)
        private val tvResult: TextView = itemView.findViewById(R.id.tvHistoryResult)
        private val tvLang: TextView = itemView.findViewById(R.id.tvHistoryLang)

        fun bind(history: HistoryEntity) {
            tvSource.text = history.sourceText
            tvResult.text = history.translatedText
            tvLang.text = "${history.sourceLang} -> ${history.targetLang}"
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryEntity>() {
        override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
