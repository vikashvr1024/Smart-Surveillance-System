package com.app.smart.viewmodels

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.app.smart.data.AlertEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun deleteAlert(alert: AlertEvent) {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("alert_history")
                .document(alert.id)
                .delete()
                .addOnSuccessListener {
                    // Alert deleted successfully
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }
    }
} 