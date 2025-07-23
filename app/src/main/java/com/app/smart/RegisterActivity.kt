package com.app.smart

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.smart.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    
    // Email pattern for validation
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    private val termsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.cbTerms.isChecked = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Set up click listeners
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Register button click listener
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                showLoading(true)
                registerUser()
            }
        }
        
        // Login link click listener
        binding.tvLogin.setOnClickListener {
            // Simply finish this activity to go back to login
            finish()
        }

        // Terms and conditions click listener
        binding.tvTermsLink.setOnClickListener {
            termsLauncher.launch(Intent(this, TermsActivity::class.java))
        }
    }
    
    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val fullName = binding.etFullName.text.toString().trim()
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update user profile with full name
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            showLoading(false)
                            if (profileTask.isSuccessful) {
                                // Save terms acceptance
                                getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("terms_accepted", true)
                                    .apply()

                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            } else {
                                Toast.makeText(this, "Failed to update profile: ${profileTask.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    showLoading(false)
                    // If registration fails, display a message to the user
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun validateForm(): Boolean {
        var valid = true
        
        // Validate full name
        val fullName = binding.etFullName.text.toString().trim()
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.error = getString(R.string.error_field_required)
            valid = false
        } else {
            binding.tilFullName.error = null
        }
        
        // Validate email
        val email = binding.etEmail.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            valid = false
        } else if (!isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            valid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Validate password
        val password = binding.etPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            valid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            valid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Validate confirm password
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.error = getString(R.string.error_field_required)
            valid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            valid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        // Validate terms acceptance
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, getString(R.string.error_terms_not_accepted), Toast.LENGTH_SHORT).show()
            valid = false
        }
        
        return valid
    }
    
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }
    
    private fun showLoading(show: Boolean) {
        binding.btnRegister.isEnabled = !show
        binding.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 