package com.example.islanddisastersurvivalguideapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "precomputed_routes")
data class PrecomputedRouteEntity(
    @PrimaryKey val id: String,
    val startLocationId: String,
    val shelterInfoId: String,
    val distance: Double,
    val initialBearing: Double,
    val waypointsJson: String,    // 序列化的路徑點列表
    val lastUpdated: Long
)