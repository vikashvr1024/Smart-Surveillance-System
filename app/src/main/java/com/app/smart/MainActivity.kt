package com.app.smart

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smart.adapters.AlertHistoryAdapter
import com.app.smart.data.AlertEvent
import com.app.smart.data.EmergencyContact
import com.app.smart.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.app.smart.dialogs.PasswordConfirmDialog
import com.app.smart.viewmodels.MainViewModel
import java.util.Date
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var alertHistoryAdapter: AlertHistoryAdapter
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    private lateinit var viewModel: MainViewModel

    private var isMonitoring = false
    private var lastLocation: Location? = null
    private var emergencyContacts: List<EmergencyContact> = emptyList()
    private var countDownTimer: CountDownTimer? = null
    private var lastAlertTime = 0L
    private var isInCooldown = false
    private var monitoringHandler = Handler(Looper.getMainLooper())

    // Sensitivity levels and their corresponding thresholds
    enum class ImpactSensitivity {
        VERY_LOW {
            override val accelerationThreshold = 5f // m/s²
            override val rotationThreshold = 2f // rad/s
        },
        LOW {
            override val accelerationThreshold = 15f // m/s²
            override val rotationThreshold = 8f // rad/s
        },
        MEDIUM {
            override val accelerationThreshold = 28f // m/s²
            override val rotationThreshold = 14f // rad/s
        },
        HIGH {
            override val accelerationThreshold = 40f // m/s²
            override val rotationThreshold = 20f // rad/s
        };

        abstract val accelerationThreshold: Float
        abstract val rotationThreshold: Float
    }

    private var currentSensitivity = ImpactSensitivity.MEDIUM

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
        private const val COUNTDOWN_DURATION = 15000L // 15 seconds
        private const val ALERT_COLLECTION = "alert_history"
        private const val MONITORING_START_DELAY = 3000L // 3 seconds delay before monitoring starts
        private const val ALERT_COOLDOWN_PERIOD = 30000L // 30 seconds between alerts
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupAlertHistory()
        loadSettings()
        loadEmergencyContacts()
        setupUI()
    }

    private fun loadSettings() {
        // Load sensitivity level
        val sensitivityLevel = prefs.getString("impact_sensitivity", "MEDIUM") ?: "MEDIUM"
        currentSensitivity = ImpactSensitivity.valueOf(sensitivityLevel)
        
        // Restore monitoring state
        isMonitoring = prefs.getBoolean("is_monitoring", false)
        binding.switchMonitoring.isChecked = isMonitoring
        updateMonitoringStatus()
        
        // Start monitoring if it was enabled
        if (isMonitoring && checkPermissions()) {
            startMonitoring()
        }
    }

    private fun setupAlertHistory() {
        alertHistoryAdapter = AlertHistoryAdapter { alert: AlertEvent ->
            PasswordConfirmDialog { password ->
                // TODO: Verify password and delete alert
                viewModel.deleteAlert(alert)
            }.show(supportFragmentManager, PasswordConfirmDialog.TAG)
        }
        binding.rvAlertHistory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alertHistoryAdapter
        }
        loadAlertHistory()
    }

    private fun loadAlertHistory() {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection(ALERT_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error loading alert history", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        val alerts = it.documents.mapNotNull { doc ->
                            doc.toObject(AlertEvent::class.java)?.copy(id = doc.id)
                        }
                        alertHistoryAdapter.submitList(alerts)
                        binding.cardAlertHistory.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
        }
    }

    private fun loadEmergencyContacts() {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("emergency_contacts")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error loading contacts", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        emergencyContacts = it.documents.mapNotNull { doc ->
                            doc.toObject(EmergencyContact::class.java)
                        }
                    }
                }
        }
    }

    private fun setupUI() {
        // Set up monitoring switch
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            isMonitoring = isChecked
            updateMonitoringStatus()
            
            // Save monitoring state
            prefs.edit()
                .putBoolean("is_monitoring", isChecked)
                .apply()
            
            if (isChecked) {
                if (checkPermissions()) {
                    startMonitoring()
                }
            } else {
                stopMonitoring()
            }
        }

        // Set up emergency contacts button
        binding.btnManageContacts.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        // Set up share location button
        binding.btnShareLocation.setOnClickListener {
            shareLocation()
        }

        // Set up settings FAB
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateMonitoringStatus() {
        binding.tvMonitoringStatus.text = "Currently: ${if (isMonitoring) "ON" else "OFF"}"
        binding.tvMonitoringStatus.setTextColor(
            ContextCompat.getColor(this, 
                if (isMonitoring) android.R.color.holo_green_dark 
                else R.color.error_color
            )
        )
    }

    private fun startMonitoring() {
        // Add a delay before starting the monitoring
        monitoringHandler.postDelayed({
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
            startLocationUpdates()
            Toast.makeText(this, "Accident detection activated", Toast.LENGTH_SHORT).show()
        }, MONITORING_START_DELAY)
    }

    private fun stopMonitoring() {
        monitoringHandler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (checkPermissions()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        lastLocation = it
                        updateLocationDisplay(it)
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopLocationUpdates() {
        // Implement if needed
    }

    private fun updateLocationDisplay(location: Location) {
        binding.tvCurrentLocation.text = "Lat: ${location.latitude}, Long: ${location.longitude}"
    }

    private fun shareLocation() {
        lastLocation?.let { location ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, 
                    "My current location: https://maps.google.com/?q=${location.latitude},${location.longitude}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share location via"))
        } ?: Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isMonitoring || isInCooldown) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime < ALERT_COOLDOWN_PERIOD) {
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val acceleration = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                // Subtract gravity (9.81 m/s²) to get actual acceleration
                val actualAcceleration = abs(acceleration - 9.81f)
                if (actualAcceleration > currentSensitivity.accelerationThreshold) {
                    possibleAccidentDetected("High impact detected (${currentSensitivity.name} sensitivity)")
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rotation = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                if (rotation > currentSensitivity.rotationThreshold) {
                    possibleAccidentDetected("Abnormal rotation detected (${currentSensitivity.name} sensitivity)")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    private fun possibleAccidentDetected(reason: String) {
        // Prevent multiple dialogs and implement cooldown
        if (countDownTimer != null || isInCooldown) return

        lastAlertTime = System.currentTimeMillis()
        isInCooldown = true

        val dialog = AlertDialog.Builder(this)
            .setTitle("Possible Accident Detected")
            .setMessage("Are you okay? Emergency contacts will be notified in 15 seconds if you don't respond.")
            .setCancelable(false)
            .setPositiveButton("I'm OK") { dialog, _ ->
                dialog.dismiss()
                countDownTimer?.cancel()
                countDownTimer = null
                
                // Save cancelled alert
                saveAlertEvent(reason, true)
            }
            .create()

        dialog.show()

        countDownTimer = object : CountDownTimer(COUNTDOWN_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                dialog.setMessage("Are you okay? Emergency contacts will be notified in ${millisUntilFinished / 1000} seconds if you don't respond.")
            }

            override fun onFinish() {
                dialog.dismiss()
                notifyEmergencyContacts(reason)
                countDownTimer = null
            }
        }.start()

        // Reset cooldown after the alert period
        monitoringHandler.postDelayed({
            isInCooldown = false
        }, ALERT_COOLDOWN_PERIOD)
    }

    private fun saveAlertEvent(reason: String, wasCancelled: Boolean) {
        auth.currentUser?.let { user ->
            lastLocation?.let { location ->
                val alert = AlertEvent(
                    timestamp = Date(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    type = "accident",
                    description = reason,
                    wasCancelled = wasCancelled,
                    notifiedContacts = if (wasCancelled) emptyList() else emergencyContacts.map { it.name }
                )

                db.collection("users")
                    .document(user.uid)
                    .collection(ALERT_COLLECTION)
                    .add(alert)
                    .addOnSuccessListener { documentReference ->
                        // Update the alert with its ID
                        documentReference.update("id", documentReference.id)
                            .addOnSuccessListener {
                                // Refresh the alert history
                                loadAlertHistory()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update alert: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save alert: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun notifyEmergencyContacts(reason: String) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val autoSms = prefs.getBoolean("auto_sms", true)
        val autoCall = prefs.getBoolean("auto_call", true)

        lastLocation?.let { location ->
            val message = "EMERGENCY ALERT: Possible accident detected ($reason) at " +
                "https://maps.google.com/?q=${location.latitude},${location.longitude}"

            for (contact in emergencyContacts) {
                if (autoSms) {
                    sendSms(contact.phone, message)
                }
                if (autoCall) {
                    makeEmergencyCall(contact.phone)
                    break // Only call the first contact
                }
            }

            // Save alert event
            saveAlertEvent(reason, false)
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeEmergencyCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to make emergency call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionsToRequest = mutableListOf<String>()
        
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, 
                    "Permissions required for full functionality", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettings() // This will restore monitoring state and other settings
    }

    override fun onPause() {
        super.onPause()
        // Only stop the sensors, but don't change the monitoring state
        if (isMonitoring) {
            sensorManager.unregisterListener(this)
            stopLocationUpdates()
        }
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringHandler.removeCallbacksAndMessages(null)
        countDownTimer?.cancel()
        countDownTimer = null
    }
} 