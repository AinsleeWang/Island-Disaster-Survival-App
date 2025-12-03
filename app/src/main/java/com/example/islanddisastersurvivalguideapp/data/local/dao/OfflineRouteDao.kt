package com.example.islanddisastersurvivalguideapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.islanddisastersurvivalguideapp.data.local.entity.PrecomputedRouteEntity

@Dao
interface OfflineRouteDao {
    @Query("SELECT * FROM precomputed_routes WHERE startLocationId = :locationId")
    suspend fun getRoutesForLocation(locationId: String): List<PrecomputedRouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: PrecomputedRouteEntity)

    @Query("DELETE FROM precomputed_routes WHERE lastUpdated < :timestamp")
    suspend fun deleteOldRoutes(timestamp: Long)
}