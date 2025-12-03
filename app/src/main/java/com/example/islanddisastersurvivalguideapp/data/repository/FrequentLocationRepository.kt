package com.example.islanddisastersurvivalguideapp.data.repository

import android.util.Log
import com.example.islanddisastersurvivalguideapp.data.local.dao.FrequentLocationDao
import com.example.islanddisastersurvivalguideapp.data.local.entity.FrequentLocationEntity
import com.example.islanddisastersurvivalguideapp.data.model.FrequentLocation
import com.example.islanddisastersurvivalguideapp.data.model.LocationType
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrequentLocationRepository(private val frequentLocationDao: FrequentLocationDao) {
    suspend fun getAllLocations(): List<FrequentLocation> = withContext(Dispatchers.IO) {
        try {
            frequentLocationDao.getAllLocations().map { entity ->
                FrequentLocation(
                    id = entity.id,
                    name = entity.name,
                    address = entity.address,
                    latLng = LatLng(entity.latitude, entity.longitude),
                    type = LocationType.valueOf(entity.type)
                )
            }
        } catch (e: Exception) {
            Log.e("FrequentLocationRepo", "取得所有地點失敗: ${e.message}")
            emptyList()
        }
    }

    suspend fun addLocation(name: String, address: String, latLng: LatLng, type: LocationType): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 檢查是否已存在相同地址
                val existingLocations = frequentLocationDao.getAllLocations()
                if (existingLocations.any { it.address == address }) {
                    Log.w("FrequentLocationRepo", "地址已存在: $address")
                    return@withContext false
                }

                val entity = FrequentLocationEntity(
                    name = name,
                    address = address,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    type = type.name
                )

                val id = frequentLocationDao.insert(entity)
                if (id <= 0) {
                    Log.e("FrequentLocationRepo", "插入失敗，返回的 ID 無效: $id")
                    return@withContext false
                }

                true
            } catch (e: Exception) {
                Log.e("FrequentLocationRepo", "新增地點時發生錯誤", e)
                false
            }
        }
    }

    suspend fun deleteLocation(location: FrequentLocation) {
        try {
            withContext(Dispatchers.IO) {
                val entity = FrequentLocationEntity(
                    id = location.id,
                    name = location.name,
                    address = location.address,
                    latitude = location.latLng.latitude,
                    longitude = location.latLng.longitude,
                    type = location.type.name
                )
                frequentLocationDao.delete(entity)
                Log.d("FrequentLocationRepo", "成功刪除地點: ${location.name}")
            }
        } catch (e: Exception) {
            Log.e("FrequentLocationRepo", "刪除地點失敗: ${e.message}")
            throw e
        }
    }
}