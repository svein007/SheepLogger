package com.example.sheeptracker.ui.observations

import android.app.Application
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.database.entities.Observation
import com.example.sheeptracker.databinding.ObservationRvItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObservationAdapter (
    val application: Application,
    val clickListener: ObservationListItemListener
)
    : ListAdapter<Observation, ObservationAdapter.ViewHolder>(ObservationDiffCallback()) {

    class ViewHolder private constructor(val binding: ObservationRvItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        val appDao = AppDatabase.getInstance(binding.root.context.applicationContext).appDatabaseDao

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
            icon: Drawable,
            clickListener: ObservationListItemListener) {
            binding.observation = item
            binding.observationTypeIcon = icon
            binding.clickListener = clickListener
            CoroutineScope(Dispatchers.Main).launch {
                val observationShortDescription = when (item.observationType) {
                    Observation.ObservationType.COUNT -> getCountersDesc(item)
                    Observation.ObservationType.DEAD -> "DEAD #${getAnimalRegisterNumber(item)}"
                    Observation.ObservationType.INJURED -> "INJURED #${getAnimalRegisterNumber(item)}"
                }
                binding.obsShortDesc = observationShortDescription
            }
            binding.executePendingBindings()
        }

        private suspend fun getCountersDesc(observation: Observation): String {
            return withContext(Dispatchers.IO) {

                "${appDao.getCounter(observation.observationId, Counter.CountType.SHEEP)?.counterValue} sheep" + ", " +
                        "${appDao.getCounter(observation.observationId, Counter.CountType.LAMB)?.counterValue} lamb"
            }
        }

        private suspend fun getAnimalRegisterNumber(observation: Observation): String {
            return withContext(Dispatchers.IO) {
                appDao.getAnimalRegistrationForObservation(observation.observationId)?.let {
                    return@withContext it.animalNumber
                }
                return@withContext ""
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val icon = item.observationType.getDrawable(application.resources)
        holder.bind(item, icon!!, clickListener)
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

class ObservationListItemListener(val clickListener: (observationId: Long, observationType: Observation.ObservationType) -> Unit) {
    fun onClick(observation: Observation) = clickListener(observation.observationId, observation.observationType)
}
