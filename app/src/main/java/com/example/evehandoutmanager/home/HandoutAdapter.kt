package com.example.evehandoutmanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.databinding.ListItemHandoutBinding

class HandoutAdapter (private val removeListener: HandoutRemoveListener) :
    ListAdapter<Handout, HandoutAdapter.HandoutViewHolder>(HandoutDiffCallback()) {

    class HandoutViewHolder private constructor(private val binding: ListItemHandoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Handout, clickListener: HandoutRemoveListener) {
            binding.handout = item
            binding.removeListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): HandoutViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemHandoutBinding.inflate(inflater, parent, false)
                return HandoutViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandoutViewHolder {
        return HandoutViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HandoutViewHolder, position: Int) {
        holder.bind(getItem(position), removeListener)
    }
}

class HandoutDiffCallback : DiffUtil.ItemCallback<Handout>() {
    override fun areItemsTheSame(oldItem: Handout, newItem: Handout): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Handout, newItem: Handout): Boolean {
        return oldItem == newItem
    }
}

class HandoutRemoveListener(val clickListener: (Handout) -> Unit){
    fun onClick(handout: Handout) = clickListener(handout)
}