package com.example.progress.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.progress.databinding.ItemHabitBinding
import com.example.progress.model.HabitResponseDto

class HabitAdapter(
    private var habits: List<HabitResponseDto> = emptyList()
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: HabitResponseDto) {
            binding.habitNameTextView.text = habit.name
            binding.habitGoalTextView.text = "Goal: ${habit.goal}"
            binding.habitDescriptionTextView.text = habit.description ?: "No description"

            // Set category icon if available
            habit.category?.name?.let {
                binding.habitCategoryIcon.text = getCategoryEmoji(it)
            }
        }

        private fun getCategoryEmoji(categoryName: String): String {
            return when (categoryName.lowercase()) {
                "exercise", "fitness" -> "💪"
                "reading", "study" -> "📖"
                "health", "wellness" -> "🏥"
                "work" -> "💼"
                "personal" -> "👤"
                else -> "✓"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<HabitResponseDto>) {
        habits = newHabits
        notifyDataSetChanged()
    }
}

