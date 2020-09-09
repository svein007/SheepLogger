package com.example.sheeptracker.ui.addtrip

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sheeptracker.database.entities.MapArea
import com.example.sheeptracker.databinding.SelectableMapAreaRvItemBinding

class SelectableMapAreaAdapter (val clickListener: SelectableMapAreaListItemListener)
    : ListAdapter<MapArea, SelectableMapAreaAdapter.ViewHolder>(MapAreaDiffCallback()) {

    private var selectedPosition: Int = -1

    class ViewHolder private constructor(val binding: SelectableMapAreaRvItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    SelectableMapAreaRvItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }

        fun bind(
            item: MapArea,
            clickListener: SelectableMapAreaListItemListener
        ) {
            binding.mapArea = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item, SelectableMapAreaListItemListener {
            clickListener.clickListener(it)

            val oldPos = selectedPosition

            selectedPosition = position
            Log.d("######", "Selected pos: $selectedPosition")

            if (oldPos != -1) {
                notifyItemChanged(oldPos)
            }
            notifyItemChanged(selectedPosition)
        })

        holder.binding.isSelected = selectedPosition != -1 && selectedPosition == position
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

class SelectableMapAreaListItemListener(val clickListener: (mapAreaId: Long) -> Unit) {
    fun onClick(mapArea: MapArea) = clickListener(mapArea.mapAreaId)
}