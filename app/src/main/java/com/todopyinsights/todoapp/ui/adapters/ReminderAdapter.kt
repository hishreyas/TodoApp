package com.todopyinsights.todoapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.todopyinsights.todoapp.R
import com.todopyinsights.todoapp.database.models.Reminder
import com.todopyinsights.todoapp.databinding.ItemReminderBinding
import com.todopyinsights.todoapp.models.ReminderData

class ReminderAdapter(
    private val reminderData: List<ReminderData>,
    private val listener: OnReminderActionListener
) : PagingDataAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(DIFF_CALLBACK) {

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminderData: ReminderData) {
            binding.tvTitle.text = reminderData.title
            binding.tvDescription.text = reminderData.description
            binding.tvDateTime.text = reminderData.dateTime
            binding.tvRecurrence.text = reminderData.recurrence

            binding.root.contentDescription = "Reminder title: ${reminderData.title}, Description: ${reminderData.description}"


            // Differentiate API reminderData with a label or icon
            if (reminderData.isFromApi) {
                binding.tvSource.text = "Remote"
                binding.tvSource.setBackgroundResource(R.drawable.api_reminder_background)
            } else {
                binding.tvSource.text = "Local"
                binding.tvSource.setBackgroundResource(R.drawable.local_reminder_background)
            }

            binding.btnDelete.setOnClickListener {
                listener.onDeleteReminderClicked(reminderData)
            }
            binding.root.setOnClickListener {
                if ( it.id != binding.btnDelete.id) {
                    listener.onReminderClicked(reminderData)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        reminder?.let { holder.bind(it.toReminderData()) }
    }

    interface OnReminderActionListener {
        fun onReminderClicked(reminderData: ReminderData)
        fun onDeleteReminderClicked(reminderData: ReminderData)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
                return oldItem == newItem
            }
        }
    }
}
