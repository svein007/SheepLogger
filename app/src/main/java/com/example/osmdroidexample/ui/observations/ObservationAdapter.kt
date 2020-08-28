package com.example.osmdroidexample.ui.observations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.osmdroidexample.database.entities.Observation
import com.example.osmdroidexample.databinding.ObservationRvItemBinding

class ObservationAdapter (val clickListener: ObservationListItemListener)
    : ListAdapter<Observation, ObservationAdapter.ViewHolder>(ObservationDiffCallback()) {

    class ViewHolder private constructor(val binding: ObservationRvItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ObservationRvItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }

        fun bind(
            item: Observation,
            clickListener: ObservationListItemListener) {
            binding.observation = item
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

class ObservationDiffCallback : DiffUtil.ItemCallback<Observation>() {
    override fun areContentsTheSame(oldItem: Observation, newItem: Observation): Boolean {
        return oldItem.observationId == newItem.observationId
    }

    override fun areItemsTheSame(oldItem: Observation, newItem: Observation): Boolean {
        return oldItem == newItem
    }
}

class ObservationListItemListener(val clickListener: (observationId: Long) -> Unit) {
    fun onClick(observation: Observation) = clickListener(observation.observationId)
}
