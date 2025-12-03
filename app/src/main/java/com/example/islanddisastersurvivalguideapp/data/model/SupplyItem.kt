package com.example.islanddisastersurvivalguideapp.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SupplyItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val number: String = "",
    val date: String = "",
    val disasterType: String = "",
    val imageUriString: String? = null
) {
    // 用於 Firebase 序列化的無參數構造函數
    constructor() : this("", "", "", "", "", "", null)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category,
            "number" to number,
            "date" to date,
            "disasterType" to disasterType,
            "imageUriString" to imageUriString
        )
    }
}