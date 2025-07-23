package com.app.smart.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.smart.R
import com.app.smart.databinding.DialogPasswordConfirmBinding

class PasswordConfirmDialog(
    private val onPasswordConfirmed: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogPasswordConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val password = binding.etPassword.text.toString()
            if (password.isBlank()) {
                Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onPasswordConfirmed(password)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PasswordConfirmDialog"
    }
} 