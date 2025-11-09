package com.example.progress.ui.habit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.progress.R
import com.example.progress.databinding.FragmentAddHabitBinding
import com.example.progress.model.HabitCategory

class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddHabitViewModel by viewModels()

    private var categories: List<HabitCategory> = emptyList()
    private var selectedCategoryId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load categories immediately
        viewModel.loadCategories()

        binding.btnCreateHabit.setOnClickListener { onCreateHabitClicked() }

        viewModel.categories.observe(viewLifecycleOwner) { list: List<HabitCategory> ->
            categories = list
            if (list.isEmpty()) {
                binding.tilCategory.error = "No categories available"
            } else {
                binding.tilCategory.error = null
            }
            val adapter = object : ArrayAdapter<HabitCategory>(requireContext(), R.layout.item_category_dropdown, list) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_category_dropdown, parent, false)
                    val item = getItem(position)
                    val icon: ImageView = v.findViewById(R.id.imgIcon)
                    val nameTv: TextView = v.findViewById(R.id.tvName)
                    nameTv.text = item?.name ?: ""
                    val url = item?.iconUrl
                    if (!url.isNullOrBlank()) icon.load(url) { crossfade(true) } else icon.setImageResource(android.R.drawable.ic_menu_help)
                    return v
                }
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = getView(position, convertView, parent)
            }
            binding.actCategory.setAdapter(adapter)
            binding.actCategory.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent.getItemAtPosition(position) as HabitCategory
                selectedCategoryId = item.id
                // Ensure the text field shows only the name, not data class toString
                binding.actCategory.setText(item.name, false)
                binding.tilCategory.error = null
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading: Boolean? ->
            binding.progressBar.visibility = if (loading == true) View.VISIBLE else View.GONE
            binding.btnCreateHabit.isEnabled = loading != true
        }

        viewModel.createResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { habit ->
                Toast.makeText(requireContext(), "Habit '${habit.name}' created", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed to create habit: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onCreateHabitClicked() {
        val name = binding.editTextName.text?.toString()?.trim().orEmpty()
        val description = binding.editTextDescription.text?.toString()?.trim()?.ifEmpty { null }
        val goal = binding.editTextGoal.text?.toString()?.trim().orEmpty()

        binding.tilCategory.error = null

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Habit name required", Toast.LENGTH_SHORT).show(); return
        }
        if (goal.isEmpty()) {
            Toast.makeText(requireContext(), "Goal required", Toast.LENGTH_SHORT).show(); return
        }
        // If user typed the category name manually without selecting, try to resolve once
        if (selectedCategoryId == null) {
            val typed = binding.actCategory.text?.toString()?.trim().orEmpty()
            if (typed.isNotEmpty()) {
                selectedCategoryId = categories.firstOrNull { it.name == typed }?.id
            }
        }
        val catId = selectedCategoryId
        if (catId == null) {
            binding.tilCategory.error = "Select a category"; return
        }
        viewModel.createHabit(name, description, catId, goal)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
