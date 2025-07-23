package com.app.smart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.smart.R
import com.app.smart.data.AlertEvent
import java.text.SimpleDateFormat
import java.util.*

class AlertHistoryAdapter(
    private val onDeleteClick: (AlertEvent) -> Unit
) : ListAdapter<AlertEvent, AlertHistoryAdapter.AlertHistoryViewHolder>(AlertHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert_history, parent, false)
        return AlertHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestampView: TextView = itemView.findViewById(R.id.tv_alert_timestamp)
        private val descriptionView: TextView = itemView.findViewById(R.id.tv_alert_description)
        private val locationView: TextView = itemView.findViewById(R.id.tv_alert_location)
        private val statusView: TextView = itemView.findViewById(R.id.tv_alert_status)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_alert)

        fun bind(alert: AlertEvent) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            timestampView.text = dateFormat.format(alert.timestamp)
            descriptionView.text = alert.description
            locationView.text = "Location: ${alert.latitude}, ${alert.longitude}"
            statusView.text = if (alert.wasCancelled) "Cancelled" else "Active"
            statusView.setTextColor(
                if (alert.wasCancelled) itemView.context.getColor(R.color.red)
                else itemView.context.getColor(R.color.green)
            )

            deleteButton.setOnClickListener {
                onDeleteClick(alert)
            }
        }
    }

    private class AlertHistoryDiffCallback : DiffUtil.ItemCallback<AlertEvent>() {
        override fun areItemsTheSame(oldItem: AlertEvent, newItem: AlertEvent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlertEvent, newItem: AlertEvent): Boolean {
            return oldItem == newItem
        }
    }
} 