package com.example.islanddisastersurvivalguideapp.data.repository

import android.util.Log
import com.example.islanddisastersurvivalguideapp.data.local.MedicalCardEntity
import com.example.islanddisastersurvivalguideapp.data.local.dao.MedicalCardDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MedicalCardRepository(private val medicalCardDao: MedicalCardDao) {

    suspend fun saveMedicalCard(medicalCard: MedicalCardEntity) = withContext(Dispatchers.IO) {
        try {
            Log.d("MedicalCardRepository", "Saving medical card to database")
            medicalCardDao.insert(medicalCard)
            Log.d("MedicalCardRepository", "Medical card saved successfully")
        } catch (e: Exception) {
            Log.e("MedicalCardRepository", "Error saving medical card", e)
            throw e
        }
    }

    suspend fun getLatestMedicalCard(): MedicalCardEntity? = withContext(Dispatchers.IO) {
        medicalCardDao.getLatestCard()
    }

    suspend fun getAllMedicalCards(): List<MedicalCardEntity> = withContext(Dispatchers.IO) {
        medicalCardDao.getAllCards()
    }

    suspend fun deleteMedicalCard(id: String) = withContext(Dispatchers.IO) {
        medicalCardDao.deleteCard(id)
    }
}