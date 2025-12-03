package com.example.islanddisastersurvivalguideapp.data.model

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class FrequentLocation(
    val id: Long = 0L,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val type: LocationType
) {
    fun validate() {
        require(name.isNotBlank()) { "名稱不能為空" }
        require(address.isNotBlank()) { "地址不能為空" }
        require(latLng.latitude in -90.0..90.0) { "緯度超出範圍" }
        require(latLng.longitude in -180.0..180.0) { "經度超出範圍" }
    }
}

enum class LocationType {
    住家, 公司, 學校, 其他
}