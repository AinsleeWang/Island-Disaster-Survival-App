// app/src/main/java/com/example/islanddisastersurvivalguideapp/data/local/converter/Converters.kt

package com.example.islanddisastersurvivalguideapp.data.local.converter

import androidx.room.TypeConverter
import com.example.islanddisastersurvivalguideapp.data.model.RouteWaypoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<RouteWaypoint> {
        val listType = object : TypeToken<List<RouteWaypoint>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<RouteWaypoint>): String {
        return gson.toJson(list)
    }
}