package com.example.sheeptracker.ui.herdobservationdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.databinding.CounterRvItemBinding

class CounterAdapter(
    val incClickListener: CounterListItemListener,
    val decClickListener: CounterListItemListener,
    val onCounterEnterListener: CounterListItemListener
)
    : ListAdapter<Counter, CounterAdapter.ViewHolder>(CounterDiffCallback()) {

    class ViewHolder private constructor(val binding: CounterRvItemBinding):
            RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CounterRvItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: Counter,
            countTypeName: String,
            incClickListener: CounterListItemListener,
            decClickListener: CounterListItemListener,
            onCounterEnterListener: CounterListItemListener
        ) {
            binding.countEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    item.counterValue = binding.countEditText.text.toString().toInt()
                    onCounterEnterListener.onClick(item)
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            binding.counter = item
            binding.countTypeName = countTypeName
            binding.incClickListener = incClickListener
            binding.decClickListener = decClickListener
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            item.getStr(holder.itemView.context),
            incClickListener,
            decClickListener,
            onCounterEnterListener
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

}

class CounterDiffCallback : DiffUtil.ItemCallback<Counter>() {
    override fun areItemsTheSame(oldItem: Counter, newItem: Counter): Boolean {
        return oldItem.counterId == newItem.counterId
    }

    override fun areContentsTheSame(oldItem: Counter, newItem: Counter): Boolean {
        return oldItem == newItem
    }
}

class CounterListItemListener(val clickListener: (counter: Counter) -> Unit) {
    fun onClick(counter: Counter) = clickListener(counter)
}