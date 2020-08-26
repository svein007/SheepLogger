package com.example.osmdroidexample.ui.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.osmdroidexample.database.entities.Trip
import com.example.osmdroidexample.databinding.TripRecyclerviewItemBinding

class TripAdapter (val clickListener: TripListItemListener)
    : ListAdapter<Trip, TripAdapter.ViewHolder>(TripDiffCallback()) {

    class ViewHolder private constructor(val binding: TripRecyclerviewItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    TripRecyclerviewItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }

        fun bind(
            item: Trip,
            clickListener: TripListItemListener
        ) {
            binding.trip = item
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

class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
    override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
        return oldItem.tripId == newItem.tripId
    }

    override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
        return oldItem == newItem
    }
}

class TripListItemListener(val clickListener: (tripId: Long) -> Unit) {
    fun onClick(trip: Trip) = clickListener(trip.tripId)
}