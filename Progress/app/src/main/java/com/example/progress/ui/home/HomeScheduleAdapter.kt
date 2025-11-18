package com.example.progress.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.progress.databinding.ItemHomeScheduleBinding
import com.example.progress.model.ScheduleResponseDto
import androidx.core.view.isVisible

class HomeScheduleAdapter(
    private val onItemClick: (ScheduleResponseDto) -> Unit,
    private val onStatusChange: (ScheduleResponseDto, String) -> Unit
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
        holder.bindStatusButtons(item, onStatusChange)
        holder.setEnabled(!inFlight.contains(item.id))
    }

    class ViewHolder(private val binding: ItemHomeScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleResponseDto) {
            val timeText = item.startTime?.toLocalTime()?.toString() ?: "--:--"
            binding.tvTime.text = timeText
            binding.tvTitle.text = item.habit?.name ?: "Unknown Habit"

            item.habit?.category?.name?.let { categoryName ->
                binding.tvCategoryIcon.text = getCategoryEmoji(categoryName)
            } ?: run {
                binding.tvCategoryIcon.text = "✓"
            }

            val status = item.status ?: "Planned"
            binding.tvStatus.text = when (status.lowercase()) {
                "completed" -> "Completed"
                "skipped" -> "Skipped"
                else -> "Planned"
            }
            val statusColor = when (status.lowercase()) {
                "completed" -> 0xFF2E7D32.toInt()
                "skipped" -> 0xFFC62828.toInt()
                else -> 0xFF616161.toInt()
            }
            binding.tvStatus.setTextColor(statusColor)

            binding.tvNotes.text = item.notes ?: ""
            binding.tvNotes.isVisible = !item.notes.isNullOrEmpty()
            updateButtonStates(status)
        }

        private fun updateButtonStates(status: String) {
            val isCompleted = status.equals("completed", ignoreCase = true)
            val isSkipped = status.equals("skipped", ignoreCase = true)
            val isPlanned = !isCompleted && !isSkipped

            binding.btnComplete.strokeWidth = if (isCompleted) 4 else 2
            binding.btnSkip.strokeWidth = if (isSkipped) 4 else 2
            binding.btnReset.strokeWidth = if (isPlanned) 4 else 2
            binding.btnComplete.isEnabled = !isCompleted
            binding.btnSkip.isEnabled = !isSkipped
            binding.btnReset.isEnabled = !isPlanned
        }

        fun bindStatusButtons(item: ScheduleResponseDto, onStatusChange: (ScheduleResponseDto, String) -> Unit) {
            binding.btnComplete.setOnClickListener { onStatusChange(item, "Completed") }
            binding.btnSkip.setOnClickListener { onStatusChange(item, "Skipped") }
            binding.btnReset.setOnClickListener { onStatusChange(item, "Planned") }
        }

        fun setEnabled(enabled: Boolean) {
            binding.statusActions.alpha = if (enabled) 1.0f else 0.5f
            binding.btnComplete.isClickable = enabled
            binding.btnSkip.isClickable = enabled
            binding.btnReset.isClickable = enabled
        }

        private fun getCategoryEmoji(categoryName: String): String {
            return when (categoryName.lowercase()) {
                "exercise", "fitness" -> "💪"
                "reading", "study" -> "📖"
                "health", "wellness" -> "🏥"
                "work" -> "💼"
                "personal" -> "👤"
                else -> "?"
            }
        }
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
