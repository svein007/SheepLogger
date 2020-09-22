package com.example.sheeptracker.ui.observationdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sheeptracker.database.entities.ImageResource
import com.example.sheeptracker.databinding.ImageResourceRvItemBinding
import com.example.sheeptracker.utils.getDrawableFromUri

class ImageResourceAdapter(val clickListener: ImgResourceListItemListener)
    : ListAdapter<ImageResource, ImageResourceAdapter.ViewHolder>(ImageResourceDiffCallback()) {

    class ViewHolder private constructor(val binding: ImageResourceRvItemBinding): RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ImageResourceRvItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: ImageResource,
            clickListener: ImgResourceListItemListener,
            position: Int
        ) {
            binding.index = (position + 1).toString()
            binding.imageResource = item
            binding.imageDrawable = getDrawableFromUri(binding.root.context.applicationContext, item.getImgUri())
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

}

class ImageResourceDiffCallback : DiffUtil.ItemCallback<ImageResource>() {
    override fun areItemsTheSame(oldItem: ImageResource, newItem: ImageResource): Boolean {
        return oldItem.imageResourceId == newItem.imageResourceId
    }

    override fun areContentsTheSame(oldItem: ImageResource, newItem: ImageResource): Boolean {
        return oldItem == newItem
    }
}

class ImgResourceListItemListener(val clickListener: (imageResourceId: Long, imgUri: String) -> Unit) {
    fun onClick(imageResource: ImageResource) = clickListener(imageResource.imageResourceId, imageResource.imageResourceUri)
}