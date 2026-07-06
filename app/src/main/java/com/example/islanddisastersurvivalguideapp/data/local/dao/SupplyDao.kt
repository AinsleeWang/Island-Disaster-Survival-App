package com.example.islanddisastersurvivalguideapp.data.local.dao

import androidx.room.*
import com.example.islanddisastersurvivalguideapp.data.model.SupplyItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplyDao {
    @Query("SELECT * FROM supplies")
    fun getAllSupplies(): Flow<List<SupplyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupply(supply: SupplyItem)

    @Delete
    suspend fun deleteSupply(supply: SupplyItem)

    @Query("DELETE FROM supplies WHERE id = :supplyId")
    suspend fun deleteSupplyById(supplyId: String)
}