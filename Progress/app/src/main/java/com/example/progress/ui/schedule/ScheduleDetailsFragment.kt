package com.example.progress.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progress.databinding.FragmentScheduleDetailsBinding
import com.example.progress.model.ProgressResponseDto
import java.time.format.DateTimeFormatter

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
        setupRecycler()
        observe()
        viewModel.load(scheduleId)
    }

    private fun setupRecycler() {
        adapter = ProgressHistoryAdapter()
        binding.rvProgressHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProgressHistory.adapter = adapter
        binding.rvProgressHistory.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
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
            // Progress bar: percent completed entries
            val total = progresses.size
            val completed = progresses.count { it.isCompleted }
            if (total > 0) {
                val percent = (completed * 100 / total)
                binding.progressBar.progress = percent
                binding.tvProgressPercent.text = "$percent%"
            } else {
                binding.progressBar.progress = if (sched.status?.equals("completed", true) == true) 100 else 0
                binding.tvProgressPercent.text = if (sched.status?.equals("completed", true) == true) "100%" else "0%"
            }
            binding.groupNotes.visibility = if (sched.notes.isNullOrBlank()) View.GONE else View.VISIBLE
        }
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
    private val fmtDate = DateTimeFormatter.ISO_LOCAL_DATE
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

