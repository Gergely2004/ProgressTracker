package com.example.progress.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progress.R
import com.example.progress.databinding.FragmentHomeBinding
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.model.UpdateScheduleDto
import com.example.progress.repository.ProgressRepository
import com.example.progress.repository.ScheduleRepository
import com.example.progress.utils.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repository = ScheduleRepository(context)
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeScheduleAdapter
    private lateinit var progressRepo: ProgressRepository
    private lateinit var scheduleRepo: ScheduleRepository

    private var currentSchedules: List<ScheduleResponseDto> = emptyList()
    private val togglingIds = mutableSetOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = HomeViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory) [HomeViewModel::class.java]
        progressRepo = ProgressRepository(requireContext())
        scheduleRepo = ScheduleRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
    }
    private fun setupUi() {

        binding.fabCreateSchedule.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_createScheduleFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Navigation failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = HomeScheduleAdapter(
            onItemClick = { sched ->
                try {
                    findNavController().navigate(R.id.action_homeFragment_to_scheduleDetailsFragment, bundleOf("scheduleId" to sched.id))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Navigation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onStatusChange = { sched, newStatus ->
                if (togglingIds.contains(sched.id)) return@HomeScheduleAdapter
                togglingIds.add(sched.id)
                adapter.updateInFlight(togglingIds)

                val prevList = currentSchedules
                val idx = prevList.indexOfFirst { it.id == sched.id }
                var previousStatus = sched.status
                if (idx >= 0) {
                    previousStatus = prevList[idx].status
                    val updated = prevList[idx].copy(status = newStatus)
                    val newList = prevList.toMutableList().apply { set(idx, updated) }
                    currentSchedules = newList
                    adapter.submitList(newList.toList())
                }

                lifecycleScope.launch {
                    try {
                        val resp = scheduleRepo.updateSchedule(sched.id, UpdateScheduleDto(status = newStatus))
                        if (!resp.isSuccessful) {
                            if (idx >= 0) {
                                val rollback = currentSchedules.toMutableList().apply { set(idx, currentSchedules[idx].copy(status = previousStatus)) }
                                currentSchedules = rollback
                                adapter.submitList(rollback.toList())
                            }
                            Toast.makeText(requireContext(), "Update failed ${resp.code()}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                            viewModel.getScheduleByDay(LocalDate.now().toString())
                        }
                    } catch (e: Exception) {
                        if (idx >= 0) {
                            val rollback = currentSchedules.toMutableList().apply { set(idx, currentSchedules[idx].copy(status = previousStatus)) }
                            currentSchedules = rollback
                            adapter.submitList(rollback.toList())
                        }
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        togglingIds.remove(sched.id)
                        adapter.updateInFlight(togglingIds)
                    }
                }
            }
        )
        binding.rvSchedules.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvSchedules.adapter = adapter

        binding.rvSchedules.addItemDecoration(DividerItemDecoration(requireContext
            (), LinearLayoutManager.VERTICAL))

        val today = try { LocalDate.now().toString() } catch (_: Exception) { "2025-10-26" }
        viewModel.getScheduleByDay(today)
    }
    private fun setupObservers() {
        viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
            currentSchedules = schedules ?: emptyList()
            if (!schedules.isNullOrEmpty()) {
                adapter.submitList(schedules)
                binding.rvSchedules.visibility = View.VISIBLE
            } else {
                adapter.submitList(emptyList())
                binding.rvSchedules.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sessionManager = SessionManager(requireContext())

        if (!sessionManager.isLoggedIn()) {
            currentSchedules = emptyList()
            adapter.submitList(emptyList())
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            try {
                findNavController().navigate(R.id.loginFragment)
            } catch (_: Exception) {
            }
            return
        }
        val today = try { LocalDate.now().toString() } catch (_: Exception) { "2025-10-26" }
        viewModel.getScheduleByDay(today)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}