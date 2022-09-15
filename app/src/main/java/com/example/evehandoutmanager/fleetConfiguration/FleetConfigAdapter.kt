package com.example.evehandoutmanager.fleetConfiguration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.databinding.ListItemFleetConfigSelectorBinding


class FleetConfigAdapter(private val removeListener: FleetRemoveListener) :
    ListAdapter<FleetConfigItem, FleetConfigAdapter.FleetViewHolder>(FleetDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FleetViewHolder {
        return FleetViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FleetViewHolder, position: Int) {
        holder.bind(getItem(position), removeListener)
    }

    class FleetViewHolder private constructor(private val binding: ListItemFleetConfigSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FleetConfigItem, clickListener: FleetRemoveListener){
            binding.fleetConfigItem = item
            binding.removeListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup) : FleetViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemFleetConfigSelectorBinding.inflate(inflater, parent, false)
                return FleetViewHolder(binding)
            }
        }
    }
}

class FleetDiffCallback : DiffUtil.ItemCallback<FleetConfigItem>() {
    override fun areItemsTheSame(oldItem: FleetConfigItem, newItem: FleetConfigItem): Boolean {
        return oldItem.iskValue == newItem.iskValue
    }

    override fun areContentsTheSame(oldItem: FleetConfigItem, newItem: FleetConfigItem): Boolean {
        return oldItem == newItem
    }
}

class FleetRemoveListener(val clickListener: (FleetConfigItem) -> Unit){
    fun onClick(item: FleetConfigItem) = clickListener(item)
}