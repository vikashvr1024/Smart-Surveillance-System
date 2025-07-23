package com.app.smart

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.smart.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.terms_title)

        binding.btnAccept.setOnClickListener {
            // Save that user has accepted terms
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("terms_accepted", true)
                .apply()

            // Return to previous screen
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val REQUEST_CODE = 100
    }
} 