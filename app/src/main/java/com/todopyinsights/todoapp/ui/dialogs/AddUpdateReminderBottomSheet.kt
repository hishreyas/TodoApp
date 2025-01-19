import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.todopyinsights.todoapp.R
import com.todopyinsights.todoapp.databinding.BottomSheetAddUpdateReminderBinding
import com.todopyinsights.todoapp.models.RecurrenceType
import com.todopyinsights.todoapp.models.ReminderData
import java.util.*

class AddUpdateReminderBottomSheet(
    private val reminderData: ReminderData? = null,
    private val onSave: (ReminderData) -> Unit
) : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetAddUpdateReminderBinding

    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAddUpdateReminderBinding.inflate(inflater, container, false)

        // Pre-fill data if editing
        reminderData?.let {
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)
            val dateTimeParts = it.dateTime.split(" ")
            if (dateTimeParts.size == 2) {
                selectedDate = dateTimeParts[0]
                selectedTime = dateTimeParts[1]
                binding.tvDate.text = selectedDate
                binding.tvTime.text = selectedTime
            }
            binding.tvBottomsheetTitle.text = "Update ReminderData"
            binding.btnSave.text = "Update"
        }
        setupRecurrenceSpinner()
        setupListeners()
        return binding.root
    }


    private fun setupRecurrenceSpinner() {
        // Load recurrence options from resources
        val recurrenceOptions = resources.getStringArray(R.array.recurrence_options)

        // Create an ArrayAdapter
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            recurrenceOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set adapter to the Spinner
        binding.spinnerRecurrence.adapter = adapter

        // Pre-select a value if editing an existing reminderData
        reminderData?.let {
            val selectedIndex = recurrenceOptions.indexOf(it.recurrence)
            if (selectedIndex != -1) {
                binding.spinnerRecurrence.setSelection(selectedIndex)
            }
        }
    }


    private fun setupListeners() {
        // Date Picker
        binding.tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    binding.tvDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker
        binding.tvTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.tvTime.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Save Button
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            val recurrence = binding.spinnerRecurrence.selectedItem.toString()
            val dateTime = "$selectedDate $selectedTime"

            if (title.isBlank() || selectedDate.isBlank() || selectedTime.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val newReminderData = ReminderData(
                id = reminderData?.id ?: System.currentTimeMillis().toInt(),
                title = title,
                description = description,
                dateTime = dateTime,
                recurrence = recurrence,
                recurrenceType = getRecurrenceType(recurrence),
                isFromApi = reminderData?.isFromApi?:false
            )
            onSave(newReminderData)
            dismiss()
        }
    }

    fun  getRecurrenceType(recurrence:String):RecurrenceType{
        return when(recurrence){
            "Every 15 minutes"->RecurrenceType.EVERY_15_MIN
            "Hourly"->RecurrenceType.HOURLY
            "Daily"->RecurrenceType.DAILY
            else -> RecurrenceType.NONE
        }
    }
}
