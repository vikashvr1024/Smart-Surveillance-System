package com.app.smart

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.smart.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Check if user is already signed in
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }
        
        // Set up click listeners
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Login button click listener
        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                showLoading(true)
                loginUser()
            }
        }
        
        // Forgot password click listener
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmailUsername.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email)) {
                sendPasswordResetEmail(email)
            } else {
                binding.tilEmailUsername.error = "Please enter a valid email to reset password"
            }
        }
        
        // Sign up link click listener
        binding.tvSignUp.setOnClickListener {
            navigateToRegister()
        }
    }
    
    private fun loginUser() {
        val email = binding.etEmailUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun validateForm(): Boolean {
        var valid = true
        
        // Validate email/username
        val emailUsername = binding.etEmailUsername.text.toString().trim()
        if (TextUtils.isEmpty(emailUsername)) {
            binding.tilEmailUsername.error = getString(R.string.error_field_required)
            valid = false
        } else if (!isValidEmail(emailUsername)) {
            binding.tilEmailUsername.error = getString(R.string.error_invalid_email)
            valid = false
        } else {
            binding.tilEmailUsername.error = null
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
        
        return valid
    }
    
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }
    
    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled = !show
        binding.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 