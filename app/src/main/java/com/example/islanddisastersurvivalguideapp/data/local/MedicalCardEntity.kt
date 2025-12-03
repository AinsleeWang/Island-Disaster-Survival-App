package com.example.islanddisastersurvivalguideapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medical_cards")
data class MedicalCardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val birthDate: String,
    val bloodType: String,
    val medicalHistory: String,
    val medications: String,
    val emergencyContact: String,
    val createdAt: Long = System.currentTimeMillis()
)