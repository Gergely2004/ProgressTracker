package com.example.progress.ui.schedule

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.progress.R
import com.example.progress.databinding.FragmentCreateScheduleBinding
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.HabitCategory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class CreateScheduleFragment : Fragment() {

    private var _binding: FragmentCreateScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreateScheduleViewModel

    private var pickedDate: LocalDate? = null
    private var pickedStartHour: Int? = null
    private var pickedStartMinute: Int? = null

    private var categories: List<HabitCategory> = emptyList()
    private var selectedCategoryId: Long? = null

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
        viewModel.loadCategories()
    }

    private fun isRecurringSelected(): Boolean = binding.scheduleTypeGroup.checkedButtonId == binding.btnTypeRecurring.id

    private fun setupUi() {
        val repeatOptions: List<String> = listOf("none", "daily", "weekdays", "weekends")
        binding.actRepeat.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, repeatOptions))

        binding.switchUseExisting.setOnCheckedChangeListener { _, isChecked ->
            binding.tilHabit.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.groupNewHabit.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        binding.scheduleTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isRecurring = checkedId == binding.btnTypeRecurring.id
            binding.tilRepeat.visibility = if (isRecurring) View.VISIBLE else View.GONE
            binding.tilDate.visibility = if (isRecurring) View.GONE else View.VISIBLE
            binding.tilStartTimeCustom.visibility = if (isRecurring) View.GONE else View.VISIBLE
            binding.tilStartTimeRecurring.visibility = if (isRecurring) View.VISIBLE else View.GONE
            binding.weekdayContainer.visibility = if (isRecurring) View.VISIBLE else View.GONE
            binding.tilNumberOfWeeks.visibility = if (isRecurring) View.VISIBLE else View.GONE
            updateTimeFieldDisplay()
        }

        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }

        binding.btnCreate.setOnClickListener { onCreateClicked() }
    }

    private fun onCreateClicked() {
        val duration = binding.etDuration.text?.toString()?.toIntOrNull()
        val notes = binding.etNotes.text?.toString()?.trim()?.ifEmpty { null }
        val selectedIsRecurring = isRecurringSelected()

        if (selectedIsRecurring) {
            val timeText = binding.etStartTimeRecurring.text?.toString()?.trim()
            if (timeText.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Pick recurring start time", Toast.LENGTH_SHORT).show()
                return
            }
            val today = LocalDate.now()
            val parts = timeText.split(":")
            if (parts.size < 2) {
                Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show()
                return
            }
            val hour = parts[0].toIntOrNull()
            val minute = parts[1].toIntOrNull()
            if (hour == null || minute == null) {
                Toast.makeText(requireContext(), "Invalid time", Toast.LENGTH_SHORT).show()
                return
            }
            val startIso = LocalDateTime.of(today, LocalTime.of(hour, minute, 0)).format(isoDateTimeFormatter)

            val daysOfWeek = collectSelectedWeekdays()
            val numberOfWeeks = binding.etNumberOfWeeks.text?.toString()?.toIntOrNull() ?: 4
            if (daysOfWeek.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one weekday", Toast.LENGTH_SHORT).show()
                return
            }

            val useExisting = binding.switchUseExisting.isChecked
            if (useExisting) {
                val habitId = viewModel.resolveSelectedHabitId(binding.actHabit.text?.toString())
                if (habitId == null) {
                    Toast.makeText(requireContext(), "Select a habit", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.createWeekdayRecurringSchedule(
                    habitId = habitId,
                    startTime = startIso,
                    daysOfWeek = daysOfWeek,
                    numberOfWeeks = numberOfWeeks,
                    duration = duration,
                    notes = notes
                )
            } else {
                val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
                val description = binding.etHabitDescription.text?.toString()?.trim()?.ifEmpty { null }
                val goal = binding.etHabitGoal.text?.toString()?.trim().orEmpty()

                if (selectedCategoryId == null) {
                    val typed = binding.actCategory.text?.toString()?.trim().orEmpty()
                    if (typed.isNotEmpty()) {
                        selectedCategoryId = categories.firstOrNull { it.name == typed }?.id
                    }
                }

                val categoryId = selectedCategoryId
                if (name.isEmpty() || categoryId == null || goal.isEmpty()) {
                    if (categoryId == null) binding.tilCategory.error = "Select a category"
                    Toast.makeText(requireContext(), "Name, Category, Goal required", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.createHabitThenWeekdayRecurringSchedule(
                    name = name,
                    description = description,
                    categoryId = categoryId,
                    goal = goal,
                    startTime = startIso,
                    daysOfWeek = daysOfWeek,
                    numberOfWeeks = numberOfWeeks,
                    duration = duration,
                    notes = notes
                )
            }
        } else {
            val dateText = binding.etDate.text?.toString()?.trim().orEmpty()
            val startIso = binding.etStartTimeCustom.text?.toString()?.trim().orEmpty()
            if (dateText.isEmpty() || pickedDate == null) {
                Toast.makeText(requireContext(), "Pick a date", Toast.LENGTH_SHORT).show()
                return
            }
            if (startIso.isEmpty()) {
                Toast.makeText(requireContext(), "Pick start time", Toast.LENGTH_SHORT).show()
                return
            }

            val useExisting = binding.switchUseExisting.isChecked
            if (useExisting) {
                val habitId = viewModel.resolveSelectedHabitId(binding.actHabit.text?.toString())
                if (habitId == null) {
                    Toast.makeText(requireContext(), "Select a habit", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.createScheduleCustom(
                    date = pickedDate!!.atStartOfDay().format(isoDateTimeFormatter),
                    startTime = startIso,
                    notes = notes,
                    duration = duration,
                    habitId = habitId
                )
            } else {
                val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
                val description = binding.etHabitDescription.text?.toString()?.trim()?.ifEmpty { null }
                val goal = binding.etHabitGoal.text?.toString()?.trim().orEmpty()
                if (selectedCategoryId == null) {
                    val typed = binding.actCategory.text?.toString()?.trim().orEmpty()
                    if (typed.isNotEmpty()) {
                        selectedCategoryId = categories.firstOrNull { it.name == typed }?.id
                    }
                }

                val categoryId = selectedCategoryId
                if (name.isEmpty() || categoryId == null || goal.isEmpty()) {
                    if (categoryId == null) binding.tilCategory.error = "Select a category"
                    Toast.makeText(requireContext(), "Name, Category, Goal required", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.createHabitThenCustomSchedule(
                    name = name,
                    description = description,
                    categoryId = categoryId,
                    goal = goal,
                    date = pickedDate!!.atStartOfDay().format(isoDateTimeFormatter),
                    startTime = startIso,
                    notes = notes,
                    duration = duration
                )
            }
        }
    }

    private fun collectSelectedWeekdays(): List<Int> {
        val ids = listOf(
            binding.chipMon, binding.chipTue, binding.chipWed,
            binding.chipThu, binding.chipFri, binding.chipSat, binding.chipSun
        )
        val selected = mutableListOf<Int>()
        ids.forEach { chip ->
            if (chip.isChecked) {
                val tag = (chip.tag as? String)?.toIntOrNull() ?: (chip.tag as? Int)
                tag?.let { selected.add(it) }
            }
        }
        return selected
    }

    private fun setupPickers() {
        binding.etDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker() }
        }
        binding.etStartTimeCustom.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePicker(forRecurring = false) }
        }
        binding.etStartTimeRecurring.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showTimePicker(forRecurring = true) }
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
            updateTimeFieldDisplay()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun showTimePicker(forRecurring: Boolean) {
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
            if (forRecurring) {
                val t = String.format(Locale.getDefault(), "%02d:%02d", pickedStartHour, pickedStartMinute)
                binding.etStartTimeRecurring.setText(t)
            } else {
                val date = pickedDate
                val dt = if (date != null) LocalDateTime.of(date, LocalTime.of(pickedStartHour!!, pickedStartMinute!!, 0)) else null
                if (dt != null) binding.etStartTimeCustom.setText(dt.format(isoDateTimeFormatter)) else binding.etStartTimeCustom.setText(
                    String.format(Locale.getDefault(), "%02d:%02d", pickedStartHour, pickedStartMinute)
                )
            }
        }
        timePicker.show(childFragmentManager, if (forRecurring) "recurring_time_picker" else "custom_time_picker")
    }

    private fun updateTimeFieldDisplay() {
        val h = pickedStartHour
        val m = pickedStartMinute
        if (h == null || m == null) return
        val isRecurring = isRecurringSelected()
        if (isRecurring) {
            binding.etStartTimeRecurring.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
        } else {
            val d = pickedDate
            if (d != null) {
                val dt = LocalDateTime.of(d, LocalTime.of(h, m, 0))
                binding.etStartTimeCustom.setText(dt.format(isoDateTimeFormatter))
            } else {
                binding.etStartTimeCustom.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }
        }
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { list: List<HabitResponseDto> ->
            val names: List<String> = list.map { it.name }
            binding.actHabit.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names))
        }

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
                binding.actCategory.setText(item.name, false)
                binding.tilCategory.error = null
            }
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
