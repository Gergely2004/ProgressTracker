package com.example.progress.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progress.databinding.FragmentAddProgressBinding
import com.example.progress.repository.ScheduleRepository

class AddProgressFragment: Fragment() {
    private var _binding: FragmentAddProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProgressViewModel by viewModels()
    private lateinit var scheduleRepo: ScheduleRepository

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
        if (passedId <= 0L) {
            Toast.makeText(requireContext(), "Missing schedule id", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }
        binding.btnSubmit.setOnClickListener { submit(passedId) }
        viewModel.createResult.observe(viewLifecycleOwner) { res ->
            if (res.isSuccess) {
                Toast.makeText(requireContext(), "Progress saved", Toast.LENGTH_LONG).show()
                // notify previous screen
                setFragmentResult("progress_updated", Bundle.EMPTY)
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun submit(scheduleId: Long) {
        val logged = binding.etLoggedTime.text?.toString()?.toDoubleOrNull() ?: 0.0
        val notes = binding.etNotes.text?.toString()?.trim()?.ifEmpty { null }
        val completed = binding.cbCompleted.isChecked
        val dateOverride = binding.etDate.text?.toString()?.trim()?.ifEmpty { null }
        viewModel.submitProgress(scheduleId, logged, notes, completed, dateOverride)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
