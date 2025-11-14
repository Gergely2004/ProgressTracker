package com.example.progress.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progress.R
import com.example.progress.databinding.FragmentScheduleDetailsBinding
import com.example.progress.model.ProgressResponseDto
import com.google.android.material.snackbar.Snackbar
import java.util.*

class ScheduleDetailsFragment: Fragment() {
    private var _binding: FragmentScheduleDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScheduleDetailsViewModel
    private lateinit var adapter: ProgressHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[ScheduleDetailsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scheduleId = arguments?.getLong("scheduleId") ?: return
        binding.btnEditSchedule.setOnClickListener {
            val bundle = Bundle().apply { putLong("scheduleId", scheduleId) }
            findNavController().navigate(R.id.action_scheduleDetailsFragment_to_editScheduleFragment, bundle)
        }
        binding.btnAddProgress.setOnClickListener {
            val bundle = Bundle().apply { putLong("scheduleId", scheduleId) }
            findNavController().navigate(R.id.addProgressFragment, bundle)
        }
        binding.btnDeleteSchedule.setOnClickListener {
            showDeleteConfirmationDialog(scheduleId)
        }
        setupRecycler()
        setupNotesEditing(scheduleId)
        observe()
        viewModel.load(scheduleId)

    }

    private fun setupRecycler() {
        adapter = ProgressHistoryAdapter()
        binding.rvProgressHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProgressHistory.adapter = adapter
        binding.rvProgressHistory.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }

    private fun setupNotesEditing(scheduleId: Long) {
        binding.btnEditNotes.setOnClickListener {
            binding.tilNotes.visibility = View.VISIBLE
            binding.notesEditActions.visibility = View.VISIBLE
            binding.tvNotes.visibility = View.GONE
            binding.btnEditNotes.visibility = View.GONE
            binding.etNotes.setText(binding.tvNotes.text ?: "")
        }
        binding.btnCancelNotes.setOnClickListener {
            binding.tilNotes.visibility = View.GONE
            binding.notesEditActions.visibility = View.GONE
            binding.tvNotes.visibility = View.VISIBLE
            binding.btnEditNotes.visibility = View.VISIBLE
        }
        binding.btnSaveNotes.setOnClickListener {
            val text = binding.etNotes.text?.toString() ?: ""
            viewModel.updateNotes(scheduleId, text)
            binding.tvNotes.text = text
            binding.tilNotes.visibility = View.GONE
            binding.notesEditActions.visibility = View.GONE
            binding.tvNotes.visibility = View.VISIBLE
            binding.btnEditNotes.visibility = View.VISIBLE
        }
    }

    private fun observe() {
        viewModel.schedule.observe(viewLifecycleOwner) { sched ->
            if (sched == null) return@observe
            binding.tvHabitName.text = sched.habit?.name ?: "(No habit)"
            binding.tvGoal.text = sched.habit?.goal ?: ""
            binding.tvDescription.text = sched.habit?.description ?: ""
            binding.tvStatus.text = sched.status ?: "Planned"
            binding.tvNotes.text = sched.notes ?: ""
            val progresses = sched.progress ?: emptyList()
            adapter.submitList(progresses.sortedBy { it.date })
            val total = progresses.size
            val completed = progresses.count { it.isCompleted }
            if (total > 0) {
                val percent = (completed * 100 / total)
                binding.progressBar.progress = percent
                binding.tvProgressPercent.text = String.format(Locale.getDefault(), "%d%%", percent)
            } else {
                binding.progressBar.progress = if (sched.status?.equals("completed", true) == true) 100 else 0
                binding.tvProgressPercent.text = if (sched.status?.equals("completed", true) == true) "100%" else "0%"
            }
            binding.groupNotes.visibility = View.VISIBLE
        }

        viewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, "Schedule deleted successfully", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(scheduleId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_schedule_title)
            .setMessage(R.string.delete_schedule_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteSchedule(scheduleId)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class ProgressHistoryAdapter: androidx.recyclerview.widget.ListAdapter<ProgressResponseDto, ProgressVH>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = com.example.progress.databinding.ItemProgressEntryBinding.inflate(inflater, parent, false)
        return ProgressVH(view)
    }
    override fun onBindViewHolder(holder: ProgressVH, position: Int) { holder.bind(getItem(position)) }
}
private class ProgressVH(private val binding: com.example.progress.databinding.ItemProgressEntryBinding): androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ProgressResponseDto) {
        binding.tvDate.text = item.date
        binding.tvLoggedTime.text = item.loggedTime.toString()
        binding.tvIsCompleted.text = if (item.isCompleted) "Completed" else "Planned"
        binding.tvNotes.text = item.notes ?: ""
        binding.tvNotes.visibility = if (item.notes.isNullOrBlank()) View.GONE else View.VISIBLE
    }
}
private class Diff: androidx.recyclerview.widget.DiffUtil.ItemCallback<ProgressResponseDto>() {
    override fun areItemsTheSame(oldItem: ProgressResponseDto, newItem: ProgressResponseDto) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ProgressResponseDto, newItem: ProgressResponseDto) = oldItem == newItem
}
