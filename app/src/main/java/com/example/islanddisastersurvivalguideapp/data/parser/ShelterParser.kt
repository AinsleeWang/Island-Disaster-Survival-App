package com.example.islanddisastersurvivalguideapp.data.parser

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.example.islanddisastersurvivalguideapp.data.model.ShelterInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ShelterParser {
    fun parseGeoJson(jsonString: String): List<ShelterInfo> {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val features = json["features"]?.jsonArray ?: return emptyList()

        Log.d("ShelterParser", "開始解析避難所資料，特徵數量: ${features.size}")

        return features.mapNotNull { feature ->
            try {
                val properties = feature.jsonObject["properties"]?.jsonObject
                    ?: throw Exception("Missing properties")
                val geometry = feature.jsonObject["geometry"]?.jsonObject
                    ?: throw Exception("Missing geometry")

                val coordinates = geometry["coordinates"]?.jsonArray
                    ?: throw Exception("Missing coordinates")

                // 座標解析
                val lng = coordinates[0].jsonPrimitive.double
                val lat = coordinates[1].jsonPrimitive.double
                val latLng = LatLng(lat, lng)

                // 解析基本資訊
                val number = properties["number"]?.jsonPrimitive?.contentOrNull ?: ""
                val name = properties["name"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                val address = properties["address"]?.jsonPrimitive?.contentOrNull ?: ""
                val capacity = properties["capacity"]?.jsonPrimitive?.intOrNull ?: 0

                // 解析災害類型（將"是"/"否"轉換為布林值）
                val earthquake = properties["earthquake"]?.jsonPrimitive?.contentOrNull == "是"
                val flood = properties["flood"]?.jsonPrimitive?.contentOrNull == "是"
                val landslide = properties["landslide"]?.jsonPrimitive?.contentOrNull == "是"
                val tsunami = properties["tsunami"]?.jsonPrimitive?.contentOrNull == "是"
                val wartime = properties["wartime"]?.jsonPrimitive?.contentOrNull == "是"
                val disabledFacility = properties["disabled_facility"]?.jsonPrimitive?.contentOrNull == "是"

                // 創建避難所資訊物件
                val shelter = ShelterInfo(
                    id = number,  // 使用 number 作為 id
                    name = name,
                    address = address,
                    capacity = capacity,
                    latLng = latLng,
                    disabledFacility = disabledFacility,
                    earthquake = earthquake,
                    flood = flood,
                    landslide = landslide,
                    tsunami = tsunami,
                    wartime = wartime,
                    number = number
                )

                Log.d("ShelterParser", "成功解析避難所: ${shelter.name}, 位置: ${shelter.latLng}")
                shelter

            } catch (e: Exception) {
                Log.e("ShelterParser", "解析避難所時發生錯誤: ${e.message}")
                null
            }
        }.also { shelters ->
            Log.d("ShelterParser", "完成解析，總共解析到 ${shelters.size} 個避難所")
        }
    }
}