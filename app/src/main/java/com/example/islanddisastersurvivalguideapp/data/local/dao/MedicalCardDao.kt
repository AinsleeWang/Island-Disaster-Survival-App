package com.example.islanddisastersurvivalguideapp.data.local.dao

import androidx.room.*
import com.example.islanddisastersurvivalguideapp.data.local.MedicalCardEntity

@Dao
interface MedicalCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicalCard: MedicalCardEntity)

    @Query("SELECT * FROM medical_cards ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCard(): MedicalCardEntity?

    @Query("SELECT * FROM medical_cards ORDER BY createdAt DESC")
    suspend fun getAllCards(): List<MedicalCardEntity>

    @Query("DELETE FROM medical_cards WHERE id = :cardId")
    suspend fun deleteCard(cardId: String)

    @Update
    suspend fun updateCard(medicalCard: MedicalCardEntity)
}