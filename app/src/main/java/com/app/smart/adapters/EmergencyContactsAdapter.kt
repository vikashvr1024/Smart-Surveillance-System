package com.app.smart.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.smart.data.EmergencyContact
import com.app.smart.databinding.ItemEmergencyContactBinding

class EmergencyContactsAdapter(
    private val onDelete: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EmergencyContactsAdapter.ViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemEmergencyContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.apply {
                tvContactName.text = contact.name
                tvContactPhone.text = contact.phone
                tvContactRelationship.text = contact.relationship

                btnDelete.setOnClickListener { onDelete(contact) }
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
} 