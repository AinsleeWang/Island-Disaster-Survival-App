package com.example.islanddisastersurvivalguideapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "supplies")
@IgnoreExtraProperties
data class SupplyItem(
    @PrimaryKey
    val id: String = "",

    val name: String = "",
    val category: String = "",
    val number: String = "",
    val date: String = "",
    val disasterType: String = "",
    val imageUriString: String? = null
) {
    // 空建構子保留給 Firebase 或 Room 使用皆可
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