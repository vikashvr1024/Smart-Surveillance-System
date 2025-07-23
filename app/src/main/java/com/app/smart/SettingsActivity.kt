package com.app.smart

import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.app.smart.databinding.ActivitySettingsBinding
import com.google.android.material.slider.Slider
import android.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun setupUI() {
        // Initialize dark mode switch
        val savedMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        
        // Set initial state based on saved preference or current mode
        binding.switchDarkMode.isChecked = when {
            savedMode == AppCompatDelegate.MODE_NIGHT_YES -> true
            savedMode == AppCompatDelegate.MODE_NIGHT_NO -> false
            currentNightMode == AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> false
        }

        binding.switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                val mode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                prefs.edit().putInt("night_mode", mode).apply()
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }

        // Initialize auto SMS and call switches
        binding.switchAutoSms.isChecked = prefs.getBoolean("auto_sms", true)
        binding.switchAutoCall.isChecked = prefs.getBoolean("auto_call", true)

        binding.switchAutoSms.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_sms", isChecked).apply()
        }

        binding.switchAutoCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_call", isChecked).apply()
        }

        // Initialize sensitivity sliders with very low ranges for testing
        binding.sliderAcceleration.apply {
            valueFrom = 2f
            valueTo = 10f
            stepSize = 0.5f
            value = prefs.getFloat("acceleration_threshold", 5f)
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    prefs.edit().putFloat("acceleration_threshold", value).apply()
                    Toast.makeText(this@SettingsActivity, 
                        "Acceleration threshold set to: $value", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.sliderRotation.apply {
            valueFrom = 1f
            valueTo = 5f
            stepSize = 0.2f
            value = prefs.getFloat("rotation_threshold", 2f)
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    prefs.edit().putFloat("rotation_threshold", value).apply()
                    Toast.makeText(this@SettingsActivity, 
                        "Rotation threshold set to: $value", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Initialize impact sensitivity selector with Very Low option
        val sensitivityOptions = arrayOf("Very Low", "Low", "Medium", "High")
        val currentSensitivity = prefs.getString("impact_sensitivity", "VERY_LOW") ?: "VERY_LOW"
        val currentIndex = sensitivityOptions.indexOf(currentSensitivity.replace("_", " ").capitalize())
        
        binding.spinnerSensitivity.apply {
            adapter = ArrayAdapter(this@SettingsActivity, 
                android.R.layout.simple_spinner_item, 
                sensitivityOptions).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            setSelection(currentIndex)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedSensitivity = sensitivityOptions[position].replace(" ", "_").toUpperCase()
                    prefs.edit().putString("impact_sensitivity", selectedSensitivity).apply()
                    Toast.makeText(this@SettingsActivity, 
                        "Impact sensitivity set to: ${sensitivityOptions[position]}", 
                        Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Setup clear history button
        binding.btnClearHistory.setOnClickListener {
            showClearHistoryDialog()
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Alert History")
            .setMessage("Are you sure you want to delete all alert history? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearAlertHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAlertHistory() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("alert_history")
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    snapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Alert history cleared", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to clear history", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to clear history", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}