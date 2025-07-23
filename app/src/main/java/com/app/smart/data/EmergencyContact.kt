package com.app.smart.data

data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val sendSms: Boolean = true,
    val makeCall: Boolean = true
) 