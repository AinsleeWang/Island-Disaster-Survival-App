package com.example.islanddisastersurvivalguideapp.data.model

import com.google.android.gms.maps.model.LatLng

data class RouteWaypoint(
    val latitude: Double,
    val longitude: Double,
    val ordinal: Int,
    val distance: Double,
    val bearing: Double,
    val instruction: String
) {
    // 保留現有的 LatLng 轉換功能
    val latLng: LatLng
        get() = LatLng(latitude, longitude)

    // 添加序列化支援
    fun toJson(): Map<String, Any> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "ordinal" to ordinal,
            "distance" to distance,
            "bearing" to bearing,
            "instruction" to instruction
        )
    }

    companion object {
        // 保留現有的 fromLatLng 方法
        fun fromLatLng(
            latLng: LatLng,
            ordinal: Int = 0,
            distance: Double = 0.0,
            bearing: Double = 0.0,
            instruction: String = ""
        ): RouteWaypoint {
            return RouteWaypoint(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                ordinal = ordinal,
                distance = distance,
                bearing = bearing,
                instruction = instruction
            )
        }

        // 添加從 JSON 創建實例的方法
        fun fromJson(json: Map<String, Any>): RouteWaypoint {
            return RouteWaypoint(
                latitude = (json["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (json["longitude"] as? Number)?.toDouble() ?: 0.0,
                ordinal = (json["ordinal"] as? Number)?.toInt() ?: 0,
                distance = (json["distance"] as? Number)?.toDouble() ?: 0.0,
                bearing = (json["bearing"] as? Number)?.toDouble() ?: 0.0,
                instruction = json["instruction"] as? String ?: ""
            )
        }
    }
}
