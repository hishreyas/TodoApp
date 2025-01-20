package com.todopyinsights.todoapp.ui
import AddUpdateReminderBottomSheet
import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.todopyinsights.todoapp.broadcast.NotificationReceiver
import com.todopyinsights.todoapp.databinding.ActivityMainBinding
import com.todopyinsights.todoapp.models.ReminderData
import com.todopyinsights.todoapp.ui.adapters.ReminderAdapter
import com.todopyinsights.todoapp.utils.NotificationManager
import com.todopyinsights.todoapp.utils.ReminderAccessibilityService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() ,ReminderAdapter.OnReminderActionListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderDataList = mutableListOf<ReminderData>()

    private  val reminderViewModel:ReminderViewModel by viewModels()


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this@MainActivity, "Notifications enabled! You’ll never miss a reminder.",Toast.LENGTH_SHORT).show()
            } else {
                showPermissionDeniedDialog()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        reminderAdapter = ReminderAdapter(reminderDataList,this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = reminderAdapter
        }

        // Floating Action Button to add a new reminder
        binding.fab.setOnClickListener {
            openAddReminderBottomSheet()
        }

        checkAndRequestNotificationPermission()
        observe()
        if (!isAccessibilityServiceEnabled(ReminderAccessibilityService::class.java)) {
            showAccessibilityDialog()
        }
    }

    private fun observe(){
        CoroutineScope(Dispatchers.IO).launch {
            reminderViewModel.reminders.collectLatest { reminders ->
                withContext(Dispatchers.Main) {
                    reminderAdapter.submitData(reminders)
                }
            }
        }
       reminderViewModel.uiState.observe(this) { uiState ->
           if(uiState.isLoading){
               binding.progressBar.visibility = View.VISIBLE
           }else {
               binding.progressBar.visibility =  View.GONE
           }
           if(!uiState.success && uiState.errorMessage?.isNotEmpty()==true){
               Toast.makeText(this, uiState.errorMessage, Toast.LENGTH_SHORT).show()
           }
        }

    }

    private fun openAddReminderBottomSheet(reminderData: ReminderData? = null) {
        val bottomSheet = AddUpdateReminderBottomSheet(reminderData) { newReminder ->
            // Add new reminderData to the list and refresh the adapter
            if(reminderData==null) {
                NotificationManager.scheduleNotification(this,newReminder)
                Toast.makeText(this, "ReminderData Added!", Toast.LENGTH_SHORT).show()
            }else{
                NotificationManager.cancelNotification(this,newReminder)
                NotificationManager.scheduleNotification(this,newReminder)
                Toast.makeText(this, "ReminderData Updated!", Toast.LENGTH_SHORT).show()
            }
            reminderViewModel.addReminder(newReminder.toReminderData())
        }
        bottomSheet.show(supportFragmentManager, "AddUpdateReminderBottomSheet")
    }


    override fun onReminderClicked(reminderData: ReminderData) {
        openAddReminderBottomSheet(reminderData)
    }

    override fun onDeleteReminderClicked(reminderData: ReminderData) {
        showDeleteConfirmationDialog(reminderData){
            reminderViewModel.deleteReminder(reminderData.toReminderData())
            NotificationManager.cancelNotification(this,reminderData)
        }
    }

    private fun showDeleteConfirmationDialog(reminderData: ReminderData, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete ReminderData")
            .setMessage("Are you sure you want to delete this reminderData?")
            .setPositiveButton("Delete") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                  Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Notifications help you stay on track by reminding you at the right time.")
            .setPositiveButton("Settings") { _, _ ->
                openAppNotificationSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>): Boolean {
        val expectedComponentName = ComponentName(this, serviceClass)
        val enabledServicesSetting =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val accessibilityEnabled =
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)

        if (accessibilityEnabled == 1 && enabledServicesSetting != null) {
            val colonSplitter = enabledServicesSetting.split(":")
            return colonSplitter.any {
                ComponentName.unflattenFromString(it)?.equals(expectedComponentName) == true
            }
        }

        return false
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Accessibility Service")
            .setMessage("To enable the Text-to-Speech feature, please enable the Accessibility Service for this app in your device's settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAccessibilitySettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }
}
