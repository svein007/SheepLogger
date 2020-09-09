package com.example.sheeptracker.ui.mapareas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.databinding.MapAreaRecyclerviewItemBinding

class MapAreaAdapter (val clickListener: MapAreaListItemListener)
    : ListAdapter<MapArea, MapAreaAdapter.ViewHolder>(MapAreaDiffCallback()) {

    class ViewHolder private constructor(val binding: MapAreaRecyclerviewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    MapAreaRecyclerviewItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }

        fun bind(
            item: MapArea,
            clickListener: MapAreaListItemListener
        ) {
            binding.mapArea = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

}

class MapAreaDiffCallback : DiffUtil.ItemCallback<MapArea>() {
    override fun areItemsTheSame(oldItem: MapArea, newItem: MapArea): Boolean {
        return oldItem.mapAreaId == newItem.mapAreaId
    }

    override fun areContentsTheSame(oldItem: MapArea, newItem: MapArea): Boolean {
        return oldItem == newItem
    }
}

class MapAreaListItemListener(val clickListener: (mapAreaId: Long) -> Unit) {
    fun onClick(mapArea: MapArea) = clickListener(mapArea.mapAreaId)
}