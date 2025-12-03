package com.example.islanddisastersurvivalguideapp.data.model

import java.util.UUID

data class MedicalCard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val birthDate: String,
    val bloodType: String,
    val medicalHistory: String,
    val medications: String,
    val emergencyContact: String
)