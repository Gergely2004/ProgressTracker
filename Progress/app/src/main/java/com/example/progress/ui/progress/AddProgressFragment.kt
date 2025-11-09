package com.example.progress.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.progress.databinding.FragmentAddProgressBinding
import com.example.progress.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddProgressFragment: Fragment() {
    private var _binding: FragmentAddProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProgressViewModel by viewModels()
    private lateinit var scheduleRepo: ScheduleRepository

    private var todaySchedules: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleRepo = ScheduleRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passedId = arguments?.getLong("scheduleId", 0L) ?: 0L
        if (passedId > 0) {
            binding.actScheduleId.setText(passedId.toString(), false)
            binding.actScheduleId.isEnabled = false
        }
        loadTodaySchedules()
        binding.btnSubmit.setOnClickListener { submit() }
        viewModel.createResult.observe(viewLifecycleOwner) { res ->
            if (res.isSuccess) {
                Toast.makeText(requireContext(), "Progress saved", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadTodaySchedules() {
        val today = LocalDate.now().toString()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = scheduleRepo.getScheduleByDay(today)
                todaySchedules = list.map { it.id }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, todaySchedules.map { it.toString() })
                binding.actScheduleId.setAdapter(adapter)
            } catch (_: Exception) { }
        }
    }

    private fun submit() {
        val scheduleIdText = binding.actScheduleId.text?.toString()?.trim().orEmpty()
        val scheduleId = scheduleIdText.toLongOrNull()
        val logged = binding.etLoggedTime.text?.toString()?.toDoubleOrNull() ?: 0.0
        val notes = binding.etNotes.text?.toString()?.trim()?.ifEmpty { null }
        val completed = binding.cbCompleted.isChecked
        val dateOverride = binding.etDate.text?.toString()?.trim()?.ifEmpty { null }

        if (scheduleId == null) {
            Toast.makeText(requireContext(), "Select a scheduleId", Toast.LENGTH_SHORT).show(); return
        }
        viewModel.submitProgress(scheduleId, logged, notes, completed, dateOverride)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
