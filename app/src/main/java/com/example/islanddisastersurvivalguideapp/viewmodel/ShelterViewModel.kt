package com.example.islanddisastersurvivalguideapp.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.islanddisastersurvivalguideapp.data.local.AppDatabase
import com.example.islanddisastersurvivalguideapp.data.model.FrequentLocation
import com.example.islanddisastersurvivalguideapp.data.model.LocationType
import com.example.islanddisastersurvivalguideapp.data.model.PrecomputedRoute
import com.example.islanddisastersurvivalguideapp.data.model.RouteWaypoint
import com.example.islanddisastersurvivalguideapp.data.model.ShelterInfo
import com.example.islanddisastersurvivalguideapp.data.parser.ShelterParser
import com.example.islanddisastersurvivalguideapp.data.repository.FrequentLocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ShelterViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val frequentLocationRepository = FrequentLocationRepository(database.frequentLocationDao())
    private val _shelters = MutableStateFlow<List<ShelterInfo>>(emptyList())
    val shelters: StateFlow<List<ShelterInfo>> = _shelters
    private val _selectedRoute = MutableStateFlow<PrecomputedRoute?>(null)
    val selectedRoute: StateFlow<PrecomputedRoute?> = _selectedRoute
    private val _isLoading = MutableStateFlow(false)

    // 改為可空的初始值
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _frequentLocations = MutableStateFlow<List<FrequentLocation>>(emptyList())
    val frequentLocations: StateFlow<List<FrequentLocation>> = _frequentLocations

    // 最近的避難所
    private val _nearestShelters = MutableStateFlow<List<ShelterInfo>>(emptyList())
    val nearestShelters: StateFlow<List<ShelterInfo>> = _nearestShelters

    private val geocoder = Geocoder(application)



    init {
        viewModelScope.launch {
            loadShelters()
            loadFrequentLocations()
        }
    }


    fun updateUserLocation(location: LatLng) {
        Log.d("ShelterViewModel", "Updating user location: $location")
        _userLocation.value = location
        updateNearestShelters()
    }

    private fun updateNearestShelters() {
        viewModelScope.launch {
            val currentLocation = userLocation.value ?: return@launch
            Log.d("ShelterViewModel", "Updating nearest shelters for location: $currentLocation")

            val sortedShelters = sortSheltersByDistance(_shelters.value, currentLocation)
                .take(3)
                .also {
                    Log.d("ShelterViewModel", "Found ${it.size} nearest shelters")
                }

            _nearestShelters.value = sortedShelters
        }
    }

    fun loadShelters() {
        viewModelScope.launch {
            try {
                Log.d("ShelterViewModel", "Starting to load shelters")
                val jsonString = readGeoJsonFromAssets("ShelterInTaipei.geojson")
                Log.d("ShelterViewModel", "GeoJSON content length: ${jsonString.length}")

                val parsedShelters = ShelterParser.parseGeoJson(jsonString)
                Log.d("ShelterViewModel", "Parsed shelters count: ${parsedShelters.size}")

                _shelters.value = parsedShelters
                updateNearestShelters() // 確保更新最近的避難所

                Log.d("ShelterViewModel", "Nearest shelters count: ${_nearestShelters.value.size}")
            } catch (e: Exception) {
                Log.e("ShelterViewModel", "Error loading shelters", e)
                e.printStackTrace()
            }
        }
    }

    private fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
        return calculateDistance(
            loc1.latitude,
            loc1.longitude,
            loc2.latitude,
            loc2.longitude
        )
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // 地球半徑（公尺）
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(Δφ/2) * kotlin.math.sin(Δφ/2) +
                kotlin.math.cos(φ1) * kotlin.math.cos(φ2) *
                kotlin.math.sin(Δλ/2) * kotlin.math.sin(Δλ/2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1-a))

        return R * c // 返回公尺為單位的距離
    }

    fun getNearestShelters(location: LatLng): List<ShelterInfo> {
        return sortSheltersByDistance(_shelters.value, location)
            .take(3)
    }

    private fun sortSheltersByDistance(
        shelters: List<ShelterInfo>,
        referenceLocation: LatLng
    ): List<ShelterInfo> {
        return try {
            shelters.map { shelter ->
                shelter.copy(
                    distance = calculateDistance(referenceLocation, shelter.latLng)
                )
            }.sortedBy { it.distance }
        } catch (e: Exception) {
            Log.e("ShelterViewModel", "計算避難所距離失敗", e)
            shelters
        }
    }

    fun loadNearestShelters(location: FrequentLocation) {
        viewModelScope.launch {
            try {
                val sortedShelters = withContext(Dispatchers.Default) {
                    sortSheltersByDistance(
                        shelters = _shelters.value,
                        referenceLocation = location.latLng
                    ).take(3)
                }
                _nearestShelters.value = sortedShelters
            } catch (e: Exception) {
                Log.e("ShelterViewModel", "載入最近避難所失敗", e)
                _nearestShelters.value = emptyList()
            }
        }
    }

    private suspend fun readGeoJsonFromAssets(fileName: String): String =
        withContext(Dispatchers.IO) {
            try {
                getApplication<Application>().assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                Log.e("ShelterViewModel", "Error reading file: $fileName", e)
                throw e
            }
        }

    fun loadFrequentLocations() {
        viewModelScope.launch {
            try {
                // 使用 repository 來取得資料
                val locations = frequentLocationRepository.getAllLocations()
                _frequentLocations.value = locations
                Log.d("ShelterViewModel", "成功載入 ${locations.size} 個常用地點")
            } catch (e: Exception) {
                Log.e("ShelterViewModel", "載入常用地點失敗", e)
                _frequentLocations.value = emptyList()
            }
        }
    }


    fun addFrequentLocation(name: String, address: String, type: LocationType) {
        viewModelScope.launch {
            try {
                // 輸入驗證
                if (name.isBlank()) throw IllegalArgumentException("名稱不能為空")
                if (address.isBlank()) throw IllegalArgumentException("地址不能為空")

                // 解析地址
                val latLng = getLatLngFromAddress(address)
                    ?: throw IllegalStateException("無法解析地址: $address")

                // 新增地點
                val success = frequentLocationRepository.addLocation(name, address, latLng, type)
                if (!success) {
                    throw IllegalStateException("儲存地點失敗")
                }

                // 重新載入地點列表
                loadFrequentLocations()

            } catch (e: Exception) {
                // 記錄詳細錯誤
                Log.e("ShelterViewModel", "新增地點失敗: ${e.message}", e)
                // 這裡可以加入通知 UI 層的機制
            }
        }
    }

    private suspend fun getLatLngFromAddress(address: String): LatLng? =
        withContext(Dispatchers.IO) {
            try {

                // 添加地區限制以提高準確度
                val results = geocoder.getFromLocationName("$address, 台灣", 1)

                if (results.isNullOrEmpty()) {
                    Log.e("Geocoding", "無法找到地址: $address")
                    return@withContext null
                }

                val location = results[0]
                return@withContext LatLng(location.latitude, location.longitude)

            } catch (e: IOException) {
                Log.e("Geocoding", "Geocoding失敗: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("Geocoding", "未預期的錯誤: ${e.message}")
                null
            }
        }

    fun removeFrequentLocation(location: FrequentLocation) {
        viewModelScope.launch {
            try {
                frequentLocationRepository.deleteLocation(location)
                _frequentLocations.value = _frequentLocations.value - location
                Log.d("ShelterViewModel", "成功刪除常用地點: ${location.name}")
            } catch (e: Exception) {
                Log.e("ShelterViewModel", "刪除常用地點失敗", e)
            }
        }
    }

    suspend fun getPrecomputedRoutes(locationId: String): List<PrecomputedRoute> {
        return try {
            // 這裡應該從本地資料庫獲取預計算的路線
            emptyList() // 暫時返回空列表
        } catch (e: Exception) {
            Log.e("ShelterViewModel", "Error loading precomputed routes", e)
            emptyList()
        }
    }

    fun loadRouteForShelter(location: FrequentLocation, shelter: ShelterInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 計算兩點間的直線距離
                val directDistance = calculateDistance(
                    location.latLng.latitude,
                    location.latLng.longitude,
                    shelter.latLng.latitude,
                    shelter.latLng.longitude
                )

                // 創建起點的路徑點
                val startWaypoint = RouteWaypoint(
                    latitude = location.latLng.latitude,
                    longitude = location.latLng.longitude,
                    ordinal = 0,
                    distance = 0.0,
                    bearing = calculateInitialBearing(location.latLng, shelter.latLng),
                    instruction = "從起點出發"
                )

                // 使用改進後的 generateDetailedWaypoints 生成路徑點
                val waypoints = generateDetailedWaypoints(
                    location.latLng,
                    shelter.latLng,
                    50.0 // 每50米一個檢查點
                )

                // 計算初始方位角並規範化到 0-360 度範圍
                val initialBearing = calculateInitialBearing(
                    location.latLng,
                    shelter.latLng
                ).let { bearing ->
                    when {
                        bearing < 0 -> bearing + 360
                        bearing > 360 -> bearing - 360
                        else -> bearing
                    }
                }

                // 創建路線物件
                val route = PrecomputedRoute(
                    id = "${location.id}_${shelter.id}",
                    startLocationId = location.id.toString(),
                    shelterInfoId = shelter.id,
                    distance = directDistance,
                    initialBearing = initialBearing,
                    waypoints = waypoints,
                    lastUpdated = System.currentTimeMillis()
                )

                // 如果需要儲存到資料庫，添加這部分
                val gson = Gson()
                val routeEntity = route.toEntity(gson)
                // 在這裡添加資料庫儲存邏輯，如果需要的話

                // 更新選定的路線
                _selectedRoute.value = route

                // 記錄詳細的路線資訊
                Log.d("ShelterViewModel", """
                路線生成成功:
                - ID: ${route.id}
                - 距離: ${route.distance} 公尺
                - 初始方位: ${route.initialBearing} 度
                - 檢查點數量: ${route.waypoints.size}
                - 第一個檢查點: ${waypoints.firstOrNull()?.latitude}, ${waypoints.firstOrNull()?.longitude}
            """.trimIndent())

            } catch (e: Exception) {
                Log.e("ShelterViewModel", "路線計算失敗: ${e.message}", e)
                _selectedRoute.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateDetailedWaypoints(
        start: LatLng,
        end: LatLng,
        intervalDistance: Double
    ): List<RouteWaypoint> {
        val totalDistance = calculateDistance(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude
        )

        val numberOfPoints = (totalDistance / intervalDistance).toInt()

        return buildList {
            // 修改建構函式的呼叫方式
            add(RouteWaypoint(
                latitude = start.latitude,
                longitude = start.longitude,
                ordinal = 0,
                distance = 0.0,
                bearing = calculateInitialBearing(start, end),
                instruction = "從起點出發"
            ))

            // 生成中間檢查點
            for (i in 1..numberOfPoints) {
                val fraction = i.toDouble() / (numberOfPoints + 1)
                val lat = start.latitude + (end.latitude - start.latitude) * fraction
                val lng = start.longitude + (end.longitude - start.longitude) * fraction

                val currentDistance = calculateDistance(
                    start.latitude,
                    start.longitude,
                    lat,
                    lng
                )

                add(RouteWaypoint(
                    latitude = lat,
                    longitude = lng,
                    ordinal = i,
                    distance = currentDistance,
                    bearing = calculateInitialBearing(
                        LatLng(lat, lng),
                        end
                    ),
                    instruction = "繼續前進"
                ))
            }

            // 添加終點
            add(RouteWaypoint(
                latitude = end.latitude,
                longitude = end.longitude,
                ordinal = numberOfPoints + 1,
                distance = totalDistance,
                bearing = calculateInitialBearing(start, end),
                instruction = "抵達目的地"
            ))
        }
    }

    private fun calculateInitialBearing(start: LatLng, end: LatLng): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
    }

    fun clearResources() {
        viewModelScope.launch {
            clearResourcesImpl()
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            clearResourcesImpl()
        }
    }

    private fun calculateWaypoints(start: LatLng, end: LatLng): List<RouteWaypoint> {
        val waypoints = mutableListOf<RouteWaypoint>()
        val totalDistance = calculateDistance(start, end)

        // 修改起點的建構方式
        waypoints.add(RouteWaypoint(
            latitude = start.latitude,  // 直接使用 latitude
            longitude = start.longitude,  // 直接使用 longitude
            ordinal = 0,
            distance = 0.0,
            bearing = calculateInitialBearing(start, end),
            instruction = "從起點出發"
        ))

        // 生成中間點時的建構方式也需要修改
        for (i in 1..3) {
            val ratio = i / 4.0
            val lat = start.latitude + (end.latitude - start.latitude) * ratio
            val lng = start.longitude + (end.longitude - start.longitude) * ratio

            waypoints.add(RouteWaypoint(
                latitude = lat,  // 使用計算出的 latitude
                longitude = lng,  // 使用計算出的 longitude
                ordinal = i,
                distance = calculateDistance(
                    LatLng(start.latitude, start.longitude),
                    LatLng(lat, lng)
                ),
                bearing = calculateInitialBearing(
                    LatLng(lat, lng),
                    end
                ),
                instruction = "繼續前進"
            ))
        }

        // 修改終點的建構方式
        waypoints.add(RouteWaypoint(
            latitude = end.latitude,  // 直接使用 latitude
            longitude = end.longitude,  // 直接使用 longitude
            ordinal = 4,
            distance = totalDistance,
            bearing = calculateInitialBearing(start, end),
            instruction = "抵達目的地"
        ))

        return waypoints
    }

    private suspend fun clearResourcesImpl() {
        try {
            _selectedRoute.value = null
            _userLocation.value = null
            _isLoading.value = false
            // 清理感應器和其他資源的邏輯可以放在這裡
            Log.d("ShelterViewModel", "資源清理成功")
        } catch (e: Exception) {
            Log.e("ShelterViewModel", "清理資源失敗", e)
        }
    }
}