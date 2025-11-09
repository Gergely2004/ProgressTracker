package com.example.progress.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.progress.databinding.ItemHomeScheduleBinding
import com.example.progress.model.ScheduleResponseDto
import androidx.core.view.isVisible
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.google.android.material.checkbox.MaterialCheckBox

class HomeScheduleAdapter(
    private val onItemClick: (ScheduleResponseDto) -> Unit,
    private val onToggleComplete: (ScheduleResponseDto, Boolean) -> Unit
) : ListAdapter<ScheduleResponseDto, HomeScheduleAdapter.ViewHolder> (DiffCallback()) {

    private var inFlight: Set<Long> = emptySet()
    fun updateInFlight(ids: Set<Long>) {
        inFlight = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.bindToggle(item, onToggleComplete)
        holder.setEnabled(!inFlight.contains(item.id))
    }

    class ViewHolder(private val binding: ItemHomeScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleResponseDto) {
            val timeText = item.startTime?.toLocalTime()?.toString() ?: "--:--"
            binding.tvTime.text = timeText
            binding.tvTitle.text = item.habit?.name ?: "Unknown Habit"

            val status = item.status ?: "Planned"
            binding.tvStatus.text = when (status.lowercase()) {
                "completed" -> "Completed"
                "skipped" -> "Skipped"
                else -> "Planned"
            }
            val statusColor = when (status.lowercase()) {
                "completed" -> 0xFF2E7D32.toInt() // green
                "skipped" -> 0xFFC62828.toInt() // red
                else -> 0xFF616161.toInt() // grey
            }
            binding.tvStatus.setTextColor(statusColor)

            binding.tvNotes.text = item.notes ?: ""
            binding.tvNotes.isVisible = !item.notes.isNullOrEmpty()

            binding.cbDone.setOnCheckedChangeListener(null)
            binding.cbDone.isChecked = status.equals("completed", ignoreCase = true)
            // Disable during in-flight handled by adapter: set in onBind
        }
        fun bindToggle(item: ScheduleResponseDto, onToggle: (ScheduleResponseDto, Boolean) -> Unit) {
            binding.cbDone.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item, isChecked)
            }
        }
        fun setEnabled(enabled: Boolean) { binding.cbDone.isEnabled = enabled }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScheduleResponseDto>() {
        override fun areItemsTheSame(
            oldItem: ScheduleResponseDto, newItem:
            ScheduleResponseDto
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ScheduleResponseDto,
            newItem: ScheduleResponseDto
        ): Boolean {
            return oldItem == newItem
        }
    }
}
