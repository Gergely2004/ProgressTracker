package com.example.progress.ui.habit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progress.databinding.FragmentAddHabitBinding

class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddHabitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateHabit.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim().ifEmpty { null }
            val categoryText = binding.editTextCategoryId.text.toString().trim()
            val goal = binding.editTextGoal.text.toString().trim()

            if (name.isEmpty() || categoryText.isEmpty() || goal.isEmpty()) {
                Toast.makeText(requireContext(), "Name, Category ID and Goal are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = try { categoryText.toLong() } catch (_: Exception) {
                Toast.makeText(requireContext(), "Category ID must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createHabit(name, description, categoryId, goal)
        }

        viewModel.createResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { habit ->
                Toast.makeText(requireContext(), "Habit '${habit.name}' created", Toast.LENGTH_LONG).show()
                // Navigate back to previous screen
                try { findNavController().navigateUp() } catch (_: Exception) { /* no-op */ }
            }.onFailure { err ->
                Toast.makeText(requireContext(), "Failed to create habit: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
