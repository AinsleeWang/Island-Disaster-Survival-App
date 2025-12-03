package com.example.islanddisastersurvivalguideapp.data.model

import com.google.android.gms.maps.model.LatLng

data class ShelterInfo(
    val id: String,
    val name: String,
    val address: String,
    val capacity: Int,
    val latLng: LatLng,
    val disabledFacility: Boolean,
    val earthquake: Boolean,
    val flood: Boolean,
    val landslide: Boolean,
    val tsunami: Boolean,
    val wartime: Boolean,
    val number: String,
    var distance: Double? = null
) {
    // 獲取適用災害類型的列表
    fun getApplicableDisasters(): List<String> {
        val disasters = mutableListOf<String>()
        if (earthquake) disasters.add("地震")
        if (flood) disasters.add("淹水")
        if (landslide) disasters.add("土石流")
        if (tsunami) disasters.add("海嘯")
        if (wartime) disasters.add("空襲")
        return disasters
    }

    // 格式化顯示災害類型
    fun getFormattedDisasterTypes(): String {
        return getApplicableDisasters().joinToString(", ")
    }

    // 檢查是否適用於特定災害類型
    fun isApplicableFor(disasterType: String): Boolean {
        return when (disasterType.toLowerCase()) {
            "地震" -> earthquake
            "淹水" -> flood
            "土石流" -> landslide
            "海嘯" -> tsunami
            "空襲" -> wartime
            else -> false
        }
    }

    // 獲取避難所資訊摘要
    fun getSummary(): String {
        return "$name (容納人數: $capacity)"
    }
}