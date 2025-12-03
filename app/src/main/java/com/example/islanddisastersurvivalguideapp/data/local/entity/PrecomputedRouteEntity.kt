package com.example.islanddisastersurvivalguideapp.data.local.entity

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.islanddisastersurvivalguideapp.data.model.PrecomputedRoute
import com.example.islanddisastersurvivalguideapp.data.model.RouteWaypoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "precomputed_routes")
data class PrecomputedRouteEntity(
    @PrimaryKey val id: String,
    val startLocationId: String,
    val shelterInfoId: String,
    val distance: Double,
    val initialBearing: Double,
    val waypointsJson: String,
    val lastUpdated: Long
){
    fun toDomainModel(gson: Gson): PrecomputedRoute {
        // 首先將 JSON 字串轉換為正確的資料結構
        val waypointsData = try {
            // 使用更明確的型別定義
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson<List<Map<String, Any>>>(waypointsJson, type)
        } catch (e: Exception) {
            // 如果解析失敗，記錄錯誤並返回空列表
            Log.e("PrecomputedRouteEntity", "JSON parsing error: ${e.message}")
            emptyList()
        }

        // 將解析後的資料轉換為 RouteWaypoint 物件
        val waypoints = waypointsData.map { data ->
            RouteWaypoint(
                // 使用安全的型別轉換
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                ordinal = (data["ordinal"] as? Number)?.toInt() ?: 0,
                distance = (data["distance"] as? Number)?.toDouble() ?: 0.0,
                bearing = (data["bearing"] as? Number)?.toDouble() ?: 0.0,
                instruction = (data["instruction"] as? String) ?: ""
            )
        }

        return PrecomputedRoute(
            id = id,
            startLocationId = startLocationId,
            shelterInfoId = shelterInfoId,
            distance = distance,
            initialBearing = initialBearing,
            waypoints = waypoints,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        fun fromDomainModel(route: PrecomputedRoute, gson: Gson): PrecomputedRouteEntity {
            val waypointsJson = gson.toJson(route.waypoints.map { waypoint ->
                mapOf(
                    "latitude" to waypoint.latitude,
                    "longitude" to waypoint.longitude,
                    "ordinal" to waypoint.ordinal,
                    "distance" to waypoint.distance,
                    "bearing" to waypoint.bearing,
                    "instruction" to waypoint.instruction
                )
            })

            return PrecomputedRouteEntity(
                id = route.id,
                startLocationId = route.startLocationId,
                shelterInfoId = route.shelterInfoId,
                distance = route.distance,
                initialBearing = route.initialBearing,
                waypointsJson = waypointsJson,
                lastUpdated = route.lastUpdated
            )
        }
    }
}