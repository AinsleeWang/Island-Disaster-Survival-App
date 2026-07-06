package com.example.islanddisastersurvivalguideapp.data.repository

import android.content.Context
import android.util.Log
import com.example.islanddisastersurvivalguideapp.data.model.ShelterInfo
import com.example.islanddisastersurvivalguideapp.data.parser.ShelterParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShelterRepository(private val context: Context) {

    suspend fun getShelters(): List<ShelterInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d("ShelterRepository", "開始從本地資源載入避難所資料")

            // 從 assets 資料夾讀取 JSON 檔案
            val jsonString = context.assets.open("ShelterInTainan.geojson")
                .bufferedReader()
                .use { it.readText() }

            Log.d("ShelterRepository", "成功讀取 GeoJSON 檔案")

            // 解析資料
            val shelters = ShelterParser.parseGeoJson(jsonString)
            Log.d("ShelterRepository", "成功解析避難所資料，共 ${shelters.size} 個避難所")

            shelters

        } catch (e: Exception) {
            Log.e("ShelterRepository", "載入避難所資料時發生錯誤", e)
            emptyList()
        }
    }

    // 獲取指定位置附近的避難所
    suspend fun getNearestShelters(lat: Double, lng: Double, limit: Int = 3): List<ShelterInfo> = withContext(Dispatchers.IO) {
        try {
            val allShelters = getShelters()
            val currentLocation = com.google.android.gms.maps.model.LatLng(lat, lng)

            // 計算並排序最近的避難所
            allShelters.sortedBy { shelter ->
                calculateDistance(currentLocation, shelter.latLng)
            }.take(limit)

        } catch (e: Exception) {
            Log.e("ShelterRepository", "獲取最近避難所時發生錯誤", e)
            emptyList()
        }
    }

    // 計算兩點之間的距離
    private fun calculateDistance(loc1: com.google.android.gms.maps.model.LatLng, loc2: com.google.android.gms.maps.model.LatLng): Double {
        val R = 6371 // 地球半徑（公里）
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(loc1.latitude)) * Math.cos(Math.toRadians(loc2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}