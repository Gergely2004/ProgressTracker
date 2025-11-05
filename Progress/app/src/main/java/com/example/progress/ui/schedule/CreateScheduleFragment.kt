package com.example.progress.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.progress.databinding.FragmentCreateScheduleBinding
import com.example.progress.model.HabitResponse
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateScheduleFragment : Fragment() {

    private var _binding: FragmentCreateScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreateScheduleViewModel

    // Hold picked values to build proper ISO strings
    private var pickedDate: LocalDate? = null
    private var pickedStartHour: Int? = null
    private var pickedStartMinute: Int? = null

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[CreateScheduleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupPickers()
        setupObservers()
        viewModel.loadHabits()
    }

    private fun isRecurringSelected(): Boolean = binding.scheduleTypeGroup.checkedButtonId == binding.btnTypeRecurring.id

    private fun setupUi() {
        val repeatOptions: List<String> = listOf("none", "daily", "weekdays", "weekends")
        binding.actRepeat.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, repeatOptions))

        binding.switchUseExisting.setOnCheckedChangeListener { _, isChecked ->
            binding.tilHabit.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.groupNewHabit.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Schedule type toggle behavior
        binding.scheduleTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isRecurring = checkedId == binding.btnTypeRecurring.id
            binding.tilRepeat.visibility = if (isRecurring) View.VISIBLE else View.GONE
            binding.tilDate.visibility = if (isRecurring) View.GONE else View.VISIBLE
            // Refresh the displayed start time format on toggle
            updateStartTimeField()
        }

        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }

        binding.btnCreate.setOnClickListener {
            val dateText = binding.etDate.text?.toString()?.trim().orEmpty()
            val duration = binding.etDuration.text?.toString()?.toIntOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()?.ifEmpty { null }
            val repeatPattern = binding.actRepeat.text?.toString()?.trim()?.lowercase().orEmpty()

            val selectedIsRecurring = isRecurringSelected()

            // Build ISO start time based on mode and picked values
            val isoStart: String? = if (pickedStartHour != null && pickedStartMinute != null) {
                val dateForStart = if (selectedIsRecurring) LocalDate.now() else pickedDate
                if (dateForStart != null) {
                    LocalDateTime.of(dateForStart, LocalTime.of(pickedStartHour!!, pickedStartMinute!!, 0))
                        .format(isoDateTimeFormatter)
                } else null
            } else null

            if (isoStart.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please pick a start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIsRecurring) {
                val useExisting = binding.switchUseExisting.isChecked
                val rp = when (repeatPattern) {
                    "daily", "weekdays", "weekends" -> repeatPattern
                    else -> "daily" // sensible default
                }
                if (useExisting) {
                    val habitId = viewModel.resolveSelectedHabitId(binding.actHabit.text?.toString())
                    if (habitId == null) {
                        Toast.makeText(requireContext(), "Please select a habit", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.createRecurringSchedule(
                        habitId = habitId,
                        startTime = isoStart,
                        repeatPattern = rp,
                        duration = duration,
                        notes = notes
                    )
                } else {
                    val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
                    val description = binding.etHabitDescription.text?.toString()?.trim()?.ifEmpty { null }
                    val categoryId = binding.etCategoryId.text?.toString()?.toLongOrNull()
                    val goal = binding.etHabitGoal.text?.toString()?.trim().orEmpty()
                    if (name.isEmpty() || categoryId == null || goal.isEmpty()) {
                        Toast.makeText(requireContext(), "Name, Category ID, and Goal are required for new habit", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.createHabitThenRecurringSchedule(
                        name = name,
                        description = description,
                        categoryId = categoryId,
                        goal = goal,
                        startTime = isoStart,
                        repeatPattern = rp,
                        duration = duration,
                        notes = notes
                    )
                }
            } else {
                // Custom schedule requires a date
                val isoDate: String = pickedDate?.atStartOfDay()?.format(isoDateTimeFormatter)
                    ?: if (dateText.isNotEmpty()) "${dateText}T00:00:00" else ""
                if (isoDate.isEmpty()) {
                    Toast.makeText(requireContext(), "Date is required for custom schedule", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val useExisting = binding.switchUseExisting.isChecked
                if (useExisting) {
                    val habitId = viewModel.resolveSelectedHabitId(binding.actHabit.text?.toString())
                    if (habitId == null) {
                        Toast.makeText(requireContext(), "Please select a habit", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.createScheduleCustom(
                        date = isoDate,
                        startTime = isoStart,
                        notes = notes,
                        duration = duration,
                        habitId = habitId
                    )
                } else {
                    val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
                    val description = binding.etHabitDescription.text?.toString()?.trim()?.ifEmpty { null }
                    val categoryId = binding.etCategoryId.text?.toString()?.toLongOrNull()
                    val goal = binding.etHabitGoal.text?.toString()?.trim().orEmpty()
                    if (name.isEmpty() || categoryId == null || goal.isEmpty()) {
                        Toast.makeText(requireContext(), "Name, Category ID, and Goal are required for new habit", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.createHabitThenCustomSchedule(
                        name = name,
                        description = description,
                        categoryId = categoryId,
                        goal = goal,
                        date = isoDate,
                        startTime = isoStart,
                        notes = notes,
                        duration = duration
                    )
                }
            }
        }
    }

    private fun setupPickers() {
        binding.etDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker() }
        }

        binding.etStartTime.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePicker() }
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val zone = ZoneId.systemDefault()
            val localDate = Instant.ofEpochMilli(selection).atZone(zone).toLocalDate()
            pickedDate = localDate
            binding.etDate.setText(localDate.format(isoDateFormatter))

            updateStartTimeField()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val hour = pickedStartHour ?: 8
        val minute = pickedStartMinute ?: 0
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select start time")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            pickedStartHour = timePicker.hour
            pickedStartMinute = timePicker.minute
            updateStartTimeField()
        }
        timePicker.show(childFragmentManager, "start_time_picker")
    }

    private fun updateStartTimeField() {
        val h = pickedStartHour
        val m = pickedStartMinute
        if (h == null || m == null) return

        val timeOnly = String.format(Locale.getDefault(), "%02d:%02d", h, m)
        if (isRecurringSelected()) {
            binding.etStartTime.setText(timeOnly)
        } else {
            val d = pickedDate
            if (d != null) {
                val dt = LocalDateTime.of(d, LocalTime.of(h, m, 0))
                binding.etStartTime.setText(dt.format(isoDateTimeFormatter))
            } else {
                binding.etStartTime.setText(timeOnly)
            }
        }
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { list: List<HabitResponse> ->
            val names: List<String> = list.map { it.name }
            binding.actHabit.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, names))
        }
        viewModel.createResult.observe(viewLifecycleOwner) { res: Result<Unit> ->
            res.onSuccess {
                Toast.makeText(requireContext(), "Schedule created", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
