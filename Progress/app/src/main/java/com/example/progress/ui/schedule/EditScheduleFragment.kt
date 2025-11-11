package com.example.progress.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.progress.R
import com.example.progress.model.UpdateScheduleDto
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EditScheduleFragment: Fragment() {
    private lateinit var viewModel: EditScheduleViewModel

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private var scheduleId: Long = 0

    private var actStatus: AutoCompleteTextView? = null
    private var etStartTime: EditText? = null
    private var etEndTime: EditText? = null
    private var etDuration: EditText? = null
    private var etNotes: EditText? = null
    private var etParticipants: EditText? = null
    private var btnSave: Button? = null
    private var btnCancel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[EditScheduleViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_edit_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleId = arguments?.getLong("scheduleId") ?: 0
        if (scheduleId == 0L) {
            Toast.makeText(requireContext(), "Missing scheduleId", Toast.LENGTH_LONG).show()
            findNavController().navigateUp(); return
        }
        bindViews(view)
        setupUi()
        observe()
        viewModel.load(scheduleId)
    }

    private fun bindViews(root: View) {
        actStatus = root.findViewById(R.id.actStatus)
        etStartTime = root.findViewById(R.id.etStartTime)
        etEndTime = root.findViewById(R.id.etEndTime)
        etDuration = root.findViewById(R.id.etDuration)
        etNotes = root.findViewById(R.id.etNotes)
        etParticipants = root.findViewById(R.id.etParticipants)
        btnSave = root.findViewById(R.id.btnSave)
        btnCancel = root.findViewById(R.id.btnCancel)
    }

    private fun setupUi() {
        val statusOptions = listOf("Planned", "Completed", "Skipped")
        actStatus?.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, statusOptions))
        etStartTime?.setOnClickListener { showTimePicker(isStart = true) }
        etEndTime?.setOnClickListener { showTimePicker(isStart = false) }
        btnSave?.setOnClickListener { submit() }
        btnCancel?.setOnClickListener { findNavController().navigateUp() }
    }

    private fun observe() {
        viewModel.schedule.observe(viewLifecycleOwner) { sched ->
            if (sched == null) return@observe
            actStatus?.setText(sched.status ?: "Planned", false)
            sched.startTime?.let { etStartTime?.setText(it.format(dateTimeFormatter)) }
            sched.endTime?.let { etEndTime?.setText(it.format(dateTimeFormatter)) }
            etDuration?.setText(sched.durationMinutes?.toString() ?: "")
            etNotes?.setText(sched.notes ?: "")
            val participants = sched.participants?.map { it.id.toString() } ?: emptyList()
            etParticipants?.setText(participants.joinToString(","))
        }
        viewModel.updateResult.observe(viewLifecycleOwner) { res ->
            res.onSuccess {
                Toast.makeText(requireContext(), "Updated", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTimePicker(isStart: Boolean) {
        val existingText = if (isStart) etStartTime?.text?.toString() else etEndTime?.text?.toString()
        val base = try { existingText?.let { LocalDateTime.parse(it, dateTimeFormatter) } } catch (_: Exception) { null }
        val hour = base?.hour ?: 8
        val minute = base?.minute ?: 0
        val picker = MaterialTimePicker.Builder()
            .setTitleText(if (isStart) "Select start" else "Select end")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
        picker.addOnPositiveButtonClickListener {
            val h = picker.hour
            val m = picker.minute
            val date = base?.toLocalDate() ?: LocalDateTime.now().toLocalDate()
            val dt = LocalDateTime.of(date, java.time.LocalTime.of(h, m))
            if (isStart) etStartTime?.setText(dt.format(dateTimeFormatter)) else etEndTime?.setText(dt.format(dateTimeFormatter))
        }
        picker.show(childFragmentManager, if (isStart) "start_time" else "end_time")
    }

    private fun submit() {
        val start = etStartTime?.text?.toString()?.trim()?.ifEmpty { null }
        val end = etEndTime?.text?.toString()?.trim()?.ifEmpty { null }
        val duration = etDuration?.text?.toString()?.toIntOrNull()
        val status = actStatus?.text?.toString()?.trim()?.ifEmpty { null }
        val notes = etNotes?.text?.toString()?.trim()?.ifEmpty { null }
        val participantsRaw = etParticipants?.text?.toString()?.trim()?.ifEmpty { null }
        val participantIds = participantsRaw?.split(',')?.mapNotNull { it.trim().toLongOrNull() }?.takeIf { it.isNotEmpty() }
        if (start == null && end == null && duration == null && status == null && notes == null && participantIds == null) {
            Toast.makeText(requireContext(), "Nothing to update", Toast.LENGTH_SHORT).show(); return
        }
        val dto = UpdateScheduleDto(
            startTime = start,
            endTime = end,
            durationMinutes = duration,
            status = status,
            notes = notes,
            participantIds = participantIds
        )
        viewModel.update(scheduleId, dto)
    }
}
