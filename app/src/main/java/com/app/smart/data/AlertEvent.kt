package com.app.smart.data

import java.util.Date

data class AlertEvent(
    val id: String = "",
    val timestamp: Date = Date(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val description: String = "",
    val wasCancelled: Boolean = false,
    val notifiedContacts: List<String> = emptyList()
) 