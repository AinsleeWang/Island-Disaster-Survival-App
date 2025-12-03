package com.example.islanddisastersurvivalguideapp.data.model

import com.example.islanddisastersurvivalguideapp.data.local.entity.PrecomputedRouteEntity
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

data class PrecomputedRoute(
    val id: String,
    val startLocationId: String,
    val shelterInfoId: String,
    val distance: Double,
    val initialBearing: Double,
    val waypoints: List<RouteWaypoint> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toEntity(gson: Gson): PrecomputedRouteEntity {
        // 現在可以安全地使用 toJson 方法
        return PrecomputedRouteEntity(
            id = id,
            startLocationId = startLocationId,
            shelterInfoId = shelterInfoId,
            distance = distance,
            initialBearing = initialBearing,
            waypointsJson = gson.toJson(waypoints.map { it.toJson() }),
            lastUpdated = lastUpdated
        )
    }
}