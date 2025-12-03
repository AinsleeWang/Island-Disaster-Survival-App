package com.example.islanddisastersurvivalguideapp.data.local.dao

import androidx.room.*
import com.example.islanddisastersurvivalguideapp.data.local.entity.FrequentLocationEntity

@Dao
interface FrequentLocationDao {
    @Query("SELECT * FROM frequent_locations")
    suspend fun getAllLocations(): List<FrequentLocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FrequentLocationEntity): Long

    @Delete
    suspend fun delete(location: FrequentLocationEntity)
}