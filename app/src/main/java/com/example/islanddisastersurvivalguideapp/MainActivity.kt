package com.example.islanddisastersurvivalguideapp

import android.Manifest
import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.islanddisastersurvivalguideapp.ui.screen.OfflineRoutePlanningScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.example.islanddisastersurvivalguideapp.data.repository.SupplyRepository
import com.example.islanddisastersurvivalguideapp.data.local.AppDatabase
import com.example.islanddisastersurvivalguideapp.data.model.FrequentLocation
import com.example.islanddisastersurvivalguideapp.data.model.LocationType
import com.example.islanddisastersurvivalguideapp.data.model.PrecomputedRoute
import com.example.islanddisastersurvivalguideapp.data.model.ShelterInfo
import com.example.islanddisastersurvivalguideapp.data.model.SupplyItem
import com.example.islanddisastersurvivalguideapp.data.repository.MedicalCardRepository
import com.example.islanddisastersurvivalguideapp.ui.screen.DisasterResponseScreen
import com.example.islanddisastersurvivalguideapp.ui.screen.EmergencyContactScreen
import com.example.islanddisastersurvivalguideapp.ui.screen.MedicalCardScreen
import com.example.islanddisastersurvivalguideapp.ui.screen.MorseAlarmScreen
import com.example.islanddisastersurvivalguideapp.viewmodel.MedicalCardViewModel
import com.example.islanddisastersurvivalguideapp.viewmodel.factory.MedicalCardViewModelFactory
import com.example.islanddisastersurvivalguideapp.viewmodel.ShelterViewModel
import com.example.islanddisastersurvivalguideapp.viewmodel.SupplyViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Calendar
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.flowOf


class MyApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        database = AppDatabase.getInstance(this)
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var supplyRepository: SupplyRepository
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback


    private val database: AppDatabase by lazy {
        (application as MyApplication).database
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                Log.d("Permission", "精確位置權限已授予")
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                Log.d("Permission", "粗略位置權限已授予")
            }
            else -> {
                Log.e("Permission", "位置權限被拒絕")
            }
        }
    }

    private fun requestContactPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach { entry ->
                Log.d("Permission", "${entry.key} = ${entry.value}")
            }
        }.launch(permissions)
    }

    private fun setupNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)
                Log.d("Network", "網路連線已建立")
            }

            override fun onLost(network: Network) {
                connectivityManager.bindProcessToNetwork(null)
                Log.d("Network", "網路連線已中斷")
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supplyDao = database.supplyDao()
        supplyRepository = SupplyRepository(supplyDao)
        setupNetworkCallback()
        requestLocationPermission()

        setContent {
            MaterialTheme {
                var showError by remember { mutableStateOf(false) }
                val errorMessage by remember { mutableStateOf("") }

                // 錯誤對話框
                if (showError) {
                    AlertDialog(
                        onDismissRequest = { showError = false },
                        title = { Text("錯誤") },
                        text = { Text(errorMessage) },
                        confirmButton = {
                            TextButton(onClick = { showError = false }) {
                                Text("確定")
                            }
                        }
                    )
                }

                val supplyViewModel: SupplyViewModel = viewModel(
                    factory = SupplyViewModelFactory(supplyRepository, applicationContext)
                )
                val shelterViewModel: ShelterViewModel = viewModel()

                MainScreen(
                    supplyViewModel = supplyViewModel,
                    shelterViewModel = shelterViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        connectivityManager.bindProcessToNetwork(null)
    }
}

@Composable
fun MainScreen(
    supplyViewModel: SupplyViewModel,
    shelterViewModel: ShelterViewModel

) {
    val navController = rememberNavController()
    var currentPage by remember { mutableStateOf(0) }
    val context = LocalContext.current.applicationContext as MyApplication

    LocalContext.current

    LaunchedEffect(Unit) {
        shelterViewModel.loadShelters()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentPage = currentPage,
                onPageChange = { page ->
                    currentPage = page
                    when (page) {
                        0 -> navController.navigate("EvacuationPlanningScreen") {
                            // 清除回退堆疊
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        1 -> navController.navigate("SupplyPreparationScreen") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        2 -> navController.navigate("EmergencyFunctionsScreen") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFD4E4E2), Color(0xFF77ACA2))
                    )
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = "EvacuationPlanningScreen"
            ) {
                composable("EvacuationPlanningScreen") {
                    EvacuationPlanningScreen(shelterViewModel, navController)
                }
                composable("SupplyPreparationScreen") {
                    SupplyPreparationScreen(navController, supplyViewModel)
                }
                composable("AddSupplyScreen") {
                    AddSupplyScreen(supplyViewModel, navController)
                }
                composable("EmergencyFunctionsScreen") {
                    EmergencyFunctionsScreen(
                        navController = navController,
                        onNavigateToMedicalCard = { navController.navigate("medical_card") },
                        onNavigateToDisasterResponse = { navController.navigate("disaster_response") },
                        onNavigateToMorseAlarm = { navController.navigate("morse_alarm") }
                    )
                }
                composable("SupplySuggestionScreen") {
                    SupplySuggestionScreen(navController = navController)
                }
                composable("offline_route_planning") {
                    OfflineRoutePlanningScreen(
                        shelterViewModel = shelterViewModel,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable("medical_card") {
                    val context = LocalContext.current.applicationContext as MyApplication
                    val medicalCardDao = remember { context.database.medicalCardDao() }
                    val repository = remember { MedicalCardRepository(medicalCardDao) }
                    val viewModel = viewModel<MedicalCardViewModel>(
                        factory = MedicalCardViewModelFactory(repository)
                    )

                    MedicalCardScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("morse_alarm") {
                    MorseAlarmScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("emergency_contact") {
                    val context = LocalContext.current.applicationContext as MyApplication
                    val medicalCardDao = remember { context.database.medicalCardDao() }
                    val repository = remember { MedicalCardRepository(medicalCardDao) }
                    val medicalCardViewModel = viewModel<MedicalCardViewModel>(
                        factory = MedicalCardViewModelFactory(repository)
                    )

                    EmergencyContactScreen(
                        medicalCardViewModel = medicalCardViewModel,
                        supplyViewModel = supplyViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("disaster_response") {
                    DisasterResponseScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}


// 按鍵列
@Composable
fun BottomNavigationBar(currentPage: Int, onPageChange: (Int) -> Unit) {
    NavigationBar {
        val items = listOf(
            Triple(Icons.Default.Place, "避難規劃", 0),
            Triple(Icons.Default.Menu, "物資準備", 1),
            Triple(Icons.Default.Warning, "緊急功能", 2)
        )
        items.forEach { (icon, label, page) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentPage == page,
                onClick = { onPageChange(page) }
            )
        }
    }
}

@Composable
fun OptionButton(title: String, description: String, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MapOptionButton(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun EvacuationPlanningScreen(shelterViewModel: ShelterViewModel, navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var hasLocationPermission by remember { mutableStateOf(false) }

    var selectedShelter by remember { mutableStateOf<ShelterInfo?>(null) }
    var selectedLocation by remember { mutableStateOf<FrequentLocation?>(null) }
    var showFrequentLocationsDialog by remember { mutableStateOf(false) }
    var showOfflineRoutePlanning by remember { mutableStateOf(false) }
    val isDownloadingMap by remember { mutableStateOf(false) } // 標記地圖是否正在下載
    // 檢查位置權限
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("EvacuationPlanningScreen", "Location permission: $hasLocationPermission")
    }

    // 獲取位置
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                Log.d("EvacuationPlanningScreen", "Attempting to get location")
                val locationResult = fusedLocationClient.lastLocation.await()
                if (locationResult != null) {
                    Log.d("EvacuationPlanningScreen", "Location received: ${locationResult.latitude}, ${locationResult.longitude}")
                    shelterViewModel.updateUserLocation(LatLng(locationResult.latitude, locationResult.longitude))
                } else {
                    Log.d("EvacuationPlanningScreen", "Location is null")
                }
            } catch (e: SecurityException) {
                Log.e("EvacuationPlanningScreen", "Location permission denied", e)
            } catch (e: Exception) {
                Log.e("EvacuationPlanningScreen", "Error getting location", e)
            }
        } else {
            Log.d("EvacuationPlanningScreen", "No location permission")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 使整個分頁可以捲動
    ) {
        ScreenTitle(title = "路線規劃")

        MapOptionButton(
            title = "避難所查詢",
            description = "避難所資訊與周遭路況",
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            content = {
                GoogleMapView(
                    shelterViewModel = shelterViewModel,
                    onMarkerClick = { shelter -> selectedShelter = shelter },
                    hasLocationPermission = hasLocationPermission,
                    selectedLocation = selectedLocation
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OptionButton(
            title = "常用地點",
            description = "建立或編輯您的常用地點",
            onClick = { showFrequentLocationsDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 更新為導航至獨立的離線路線規劃頁面
        OptionButton(
            title = "離線規劃",
            description = "無網路時進行路線規劃",
            onClick = {
                navController.navigate("offline_route_planning")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        /*
        // 離線地圖下載按鈕
        OptionButton(
            title = "離線地圖下載",
            description = "無網路時可用之地圖",
            onClick = {
                isDownloadingMap = true
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = downloadMapImage(context, 25.0330, 121.5654, 14)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            saveBitmapToLocalStorage(context, bitmap)
                        }
                        isDownloadingMap = false
                    }
                }
            }
        )
        */

        // 顯示下載進度指示器
        if (isDownloadingMap) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "地圖下載中，請稍候...")
        }
    }

    if (showFrequentLocationsDialog) {
        FrequentLocationsDialog(
            shelterViewModel = shelterViewModel,
            onDismiss = { showFrequentLocationsDialog = false },
            onLocationSelected = { location ->
                selectedLocation = location
                showFrequentLocationsDialog = false
            }
        )
    }

    if (showOfflineRoutePlanning) {
        Dialog(onDismissRequest = { showOfflineRoutePlanning = false }) {
            OfflineRoutePlanningScreen(shelterViewModel = shelterViewModel, onClose = { showOfflineRoutePlanning = false })
        }
    }
}

@Composable
fun GoogleMapView(
    shelterViewModel: ShelterViewModel,
    onMarkerClick: (ShelterInfo) -> Unit,
    hasLocationPermission: Boolean,
    selectedLocation: FrequentLocation?
) {
    val shelters by shelterViewModel.shelters.collectAsState()
    val userLocation by shelterViewModel.userLocation.collectAsState()
    val frequentLocations by shelterViewModel.frequentLocations.collectAsState()

    val defaultLocation = LatLng(25.0329, 121.5025) // 台北市立大學博愛校區

    val cameraPositionState = rememberCameraPositionState {
        position = when {
            // 如果有 GPS 位置，使用 GPS 位置
            userLocation != null -> CameraPosition.fromLatLngZoom(userLocation!!, 15f)
            // 如果有選定的位置，使用選定的位置
            selectedLocation != null -> CameraPosition.fromLatLngZoom(selectedLocation.latLng, 15f)
            // 如果都沒有，使用預設位置
            else -> CameraPosition.fromLatLngZoom(defaultLocation, 15f)
        }
    }

// 當位置更新時，更新攝影機位置
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            Log.d("GoogleMapView", "Updating camera to user location: $location")
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 15f),
                durationMs = 1000
            )
        }
    }


    // 計算最近的三個避難所
    val nearestShelters = remember(shelters, userLocation, selectedLocation) {
        val referenceLocation = selectedLocation?.latLng ?: userLocation ?: defaultLocation
        shelters.map { shelter ->
            val distance = calculateDistance(
                referenceLocation.latitude,
                referenceLocation.longitude,
                shelter.latLng.latitude,
                shelter.latLng.longitude
            )
            shelter to distance
        }.sortedBy { it.second }
            .take(3)
            .map { it.first }
    }

    // 添加日誌以驗證數據
    LaunchedEffect(Unit) {
        Log.d("GoogleMapView", "Shelters count: ${shelters.size}")
        Log.d("GoogleMapView", "User location: $userLocation")
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location.latLng, 14f),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    ) {

        // 只顯示最近的三個避難所
        nearestShelters.forEach { shelter ->
            Marker(
                state = MarkerState(position = shelter.latLng),
                title = shelter.name,
                snippet = "容量: ${shelter.capacity}人 | 適用災害: ${shelter.getDisasterTypes()}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = {
                    onMarkerClick(shelter)
                    false
                }
            )
        }

        // 顯示已儲存的常用地點
        frequentLocations.forEach { location ->
            Marker(
                state = MarkerState(position = location.latLng),
                title = location.name,
                snippet = location.address,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }
    }
}

// 計算兩點之間距離的函數（使用 Haversine 公式）
private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val R = 6371 // 地球半徑（公里）
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}

fun ShelterInfo.getDisasterTypes(): String {
    val disasters = mutableListOf<String>()
    if (earthquake) disasters.add("地震")
    if (flood) disasters.add("水災")
    if (landslide) disasters.add("土石流")
    if (tsunami) disasters.add("海嘯")
    if (wartime) disasters.add("空襲")
    return disasters.joinToString(", ")
}

@Composable
fun FrequentLocationItem(
    location: FrequentLocation,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = location.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = location.address, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "刪除地點")
        }
    }
}

@Composable
fun FrequentLocationsDialog(
    shelterViewModel: ShelterViewModel,
    onDismiss: () -> Unit,
    onLocationSelected: (FrequentLocation) -> Unit
) {
    var showAddLocationDialog by remember { mutableStateOf(false) }
    val frequentLocations by shelterViewModel.frequentLocations.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("常用地點", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                frequentLocations.forEach { location ->
                    FrequentLocationItem(
                        location = location,
                        onSelect = {
                            onLocationSelected(location)
                            onDismiss()
                        },
                        onDelete = { shelterViewModel.removeFrequentLocation(location) }
                    )
                }

                if (frequentLocations.size < 3) {
                    Button(
                        onClick = { showAddLocationDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF458F81)), // 綠色
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("新增常用地點")
                    }
                }
            }
        }

        if (showAddLocationDialog) {
            AddLocationDialog(
                shelterViewModel = shelterViewModel,
                onDismiss = { showAddLocationDialog = false },
                onLocationAdded = {
                    showAddLocationDialog = false
                }
            )
        }
    }
}


@Composable
fun AddLocationDialog(
    shelterViewModel: ShelterViewModel,
    onDismiss: () -> Unit,
    onLocationAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var locationType by remember { mutableStateOf(LocationType.其他) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("常用地點", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("地點名稱") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || address.isBlank()) {
                                showError = true
                                errorMessage = "請填寫所有欄位"
                                return@Button
                            }
                            try {
                                shelterViewModel.addFrequentLocation(name, address, locationType)
                                onLocationAdded()
                                onDismiss()
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = "新增地點失敗：${e.message}"
                            }
                        },
                        enabled = name.isNotBlank() && address.isNotBlank()
                    ) {
                        Text("新增")
                    }
                }
            }
        }
    }

}

@Composable
fun CompassCalibrationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要校準羅盤") },
        text = { Text("請按照8字形移動手機來校準羅盤") },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("確定")
            }
        }
    )
}

// 首先定義方向列舉
enum class NavigationDirection(val degrees: Float, val label: String) {
    FORWARD(0f, "向前"),
    RIGHT(90f, "向右"),
    BACKWARD(180f, "向後"),
    LEFT(270f, "向左")
}

// 根據進度獲取目標方向
private fun getTargetDirectionForProgress(route: PrecomputedRoute, progress: Float): Float {
    if (route.waypoints.isEmpty()) {
        return route.initialBearing.toFloat() // 明確的型別轉換
    }

    val totalDistance = route.distance
    val targetDistance = totalDistance * progress
    var accumulatedDistance = 0.0

    for (i in 0 until route.waypoints.size - 1) {
        val currentPoint = route.waypoints[i]
        val nextPoint = route.waypoints[i + 1]
        val segmentDistance = calculateDistance(
            currentPoint.latitude,
            currentPoint.longitude,
            nextPoint.latitude,
            nextPoint.longitude
        )

        if (accumulatedDistance + segmentDistance >= targetDistance) {
            return calculateBearing(
                currentPoint.latitude,
                currentPoint.longitude,
                nextPoint.latitude,
                nextPoint.longitude
            ).toFloat()
        }

        accumulatedDistance += segmentDistance
    }

    return route.initialBearing.toFloat()
}

@Composable
private fun DirectionIndicator(
    deviceDirection: Float,
    targetDirection: Float,
    difference: Float
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 方向箭頭
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = "方向指示",
            modifier = Modifier
                .size(120.dp)
                .rotate(deviceDirection),
            tint = when {
                difference.absoluteValue <= 15 -> Color(0xFF458F81) // 正確方向
                else -> Color.Red // 需要調整方向
            }
        )
    }
}


@Composable
private fun NavigationContent(
    route: PrecomputedRoute,
    isNavigating: Boolean,
    onStartNavigation: () -> Unit,
    onStopNavigation: () -> Unit
) {
    val currentWaypoint = remember { mutableStateOf(0) }
    val remainingDistance = remember { mutableStateOf(route.distance) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 導航信息顯示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "剩餘距離: ${remainingDistance.value.toInt()}米",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "預計時間: ${calculateWalkingTime(remainingDistance.value)}分鐘",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = if (isNavigating) onStopNavigation else onStartNavigation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isNavigating) Color.Red else Color(0xFF458F81)
            )
        ) {
            Text(if (isNavigating) "停止導航" else "開始導航")
        }
    }
}

@Composable
fun StepIndicator(
    number: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(
                    when {
                        isCompleted -> Color(0xFF458F81)
                        isActive -> Color(0xFF77ACA2)
                        else -> Color.LightGray
                    },
                    shape = CircleShape
                )
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isActive) Color(0xFF458F81) else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LocationSelectionScreen(
    locations: List<FrequentLocation>,
    onLocationSelected: (FrequentLocation) -> Unit,
    onBack: () -> Unit  // 新增返回按鈕的回調函數
) {
    Column {
        // 標題列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
            Text(
                text = "選擇起點位置",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (locations.isEmpty()) {
            Text(
                text = "尚未新增常用地點",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        locations.forEach { location ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onLocationSelected(location) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ShelterSelectionScreen(
    shelters: List<ShelterInfo>,
    onShelterSelected: (ShelterInfo) -> Unit,
    onBack: () -> Unit
) {
    var showError by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
            Text(
                text = "選擇避難所",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (shelters.isEmpty()) {
            Text(
                text = "找不到附近的避難所",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        shelters.forEach { shelter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        try {
                            onShelterSelected(shelter)
                        } catch (e: Exception) {
                            showError = true
                        }
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = shelter.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "容納人數: ${shelter.capacity}人",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "適用災害: ${shelter.getDisasterTypes()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // 使用安全調用運算符來處理可能為空的 distance
                    shelter.distance?.let { distance ->
                        Text(
                            text = "預計步行時間: ${calculateWalkingTime(distance)}分鐘",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

        }
    }
}

private fun calculateWalkingTime(distance: Double): Int {
    val averageWalkingSpeed = 1.4 // 平均步行速度 (米/秒)
    return (distance / averageWalkingSpeed / 60).toInt() // 轉換為分鐘
}

private fun calculateBearing(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)

    val y = Math.sin(Δλ) * Math.cos(φ2)
    val x = Math.cos(φ1) * Math.sin(φ2) -
            Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ)

    return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
}

private const val STEP_THRESHOLD = 15.0f // 步數檢測的加速度閥值

// 更新使用者位置的函數，並使用回呼更新位置狀態
private fun updateUserLocation(stepCount: Int, direction: Float, currentLocation: LatLng, onLocationUpdated: (LatLng) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        val stepLength = 0.8 // 平均步長，以公尺為單位（可以個性化）
        val distance = stepLength * stepCount // 計算總移動距離
        // 根據方向計算位移量
        val deltaX = distance * cos(Math.toRadians(direction.toDouble())).toFloat()
        val deltaY = distance * sin(Math.toRadians(direction.toDouble())).toFloat()
        // 更新使用者的緯度和經度
        val newLat = currentLocation.latitude + deltaY * 0.00001 // 緯度的轉換因子
        val newLng = currentLocation.longitude + deltaX * 0.00001 // 經度的轉換因子
        onLocationUpdated(LatLng(newLat, newLng))
    }
}



@Composable
fun SupplyPreparationScreen(navController: NavController, supplyViewModel: SupplyViewModel) {
    val supplies by supplyViewModel.supplies.collectAsState()
    var selectedSupply by remember { mutableStateOf<SupplyItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // 所有可用的分類
    val categories = listOf("全部", "食品飲水", "醫療衛生", "衣物寢具", "通訊與照明設備", "其他")

    // 根據選擇的分類過濾物資
    val filteredSupplies = remember(supplies, selectedCategory) {
        when (selectedCategory) {
            null, "全部" -> supplies
            else -> supplies.filter { it.category == selectedCategory }
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        //ScreenTitle(title = "物資準備")

        // 標題區域
        Text(
            text = "物資準備",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            textAlign = TextAlign.Center
        )

        // 功能按鈕列
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { navController.navigate("AddSupplyScreen") },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF458F81), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Supply",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { navController.navigate("SupplySuggestionScreen") },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF458F81), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Supply Suggestions",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 分類下拉選單
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = selectedCategory ?: "全部",
                onValueChange = { },
                readOnly = true,
                label = { Text("使用分類") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "選擇分類",
                        modifier = Modifier.clickable { showCategoryDropdown = true }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }

        // 物資列表
        LazyColumn {
            items(
                items = filteredSupplies.distinctBy { it.id },
                key = { it.id }
            ) { supplyItem ->
                SupplyCard(
                    supplyItem = supplyItem,
                    onClick = { selectedSupply = supplyItem }
                )
            }
        }
    }

    // 對話框處理
    selectedSupply?.let { supply ->
        SupplyDetailDialog(
            supply = supply,
            onDismiss = { selectedSupply = null },
            onEdit = {
                showEditDialog = true
                selectedSupply = it
            },
            onDelete = {
                supplyViewModel.deleteSupply(it)
                selectedSupply = null
            }
        )
    }

    if (showEditDialog) {
        EditSupplyDialog(
            supply = selectedSupply!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedSupply ->
                supplyViewModel.updateSupply(updatedSupply)
                showEditDialog = false
                selectedSupply = null
            }
        )
    }
}

data class Suggestion(
    val title: String,
    val items: List<String>
)



@Composable
fun SupplySuggestionScreen(navController: NavController){
    val suggestions = listOf(
        Suggestion(
            "如果是一人家庭，應該如何準備？",
            listOf("瓶裝水和食物餅乾", "罐頭食品", "重要文件", "身份證", "健保卡", "其他", "一些現金", "急救用品", "藥物", "厚棉手套", "手電筒", "收音機", "電池", "保暖衣物", "內衣褲", "小毛毯", "雨衣", "衛生紙", "筆記本和筆", "備用鑰匙", "瑞士軍刀", "哨子")
        ),
        Suggestion(
            "如果家中有嬰幼兒，應該如何準備？",
            listOf("尿布", "嬰兒奶粉", "嬰兒食品", "濕紙巾", "毛毯", "嬰兒衣物", "嬰兒適用藥物")
        ),
        Suggestion(
            "如果家中有老年人，應該如何準備？",
            listOf("處方藥物（三個月以上）", "老花眼鏡", "助聽器和額外電池", "拐杖/助行器", "成人尿布", "舒適衣物", "緊急聯絡清單")
        ),
        Suggestion(
            "如果家中有寵物，應該如何準備？",
            listOf("寵物食品", "裝水容器", "寵物藥物", "寵物籠", "寵物牽繩", "寵物玩具", "寵物毯")
        ),
        Suggestion(
            "如果家中有糖尿病患者，應該如何準備？",
            listOf("胰島素和注射器", "血糖儀（離電池款）與額外電池", "葡萄糖片或凝膠", "低血糖用零食", "胰高血糖緊急套件", "糖尿病醫療用品", "糖尿病管理計劃副本")
        )
    )
    Column(modifier = Modifier.fillMaxSize()) {
        // 標題區域
        Text(
            text = "物資建議",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            textAlign = TextAlign.Center
        )



        // 返回按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF458F81)
                )
            }
        }

        // 建議列表
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionCard(suggestion)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SuggestionCard(suggestion: Suggestion) {
    var expanded by remember { mutableStateOf(false) }

    // 添加動畫相關的狀態
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    val expandTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        )
    }

    val collapseTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = 600)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 600)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = animateDpAsState(
            if (expanded) 8.dp else 4.dp,
            label = "elevation"
        ).value,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                expanded = !expanded
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起" else "展開",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState),
                    tint = Color(0xFF458F81)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandTransition,
                exit = collapseTransition
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    suggestion.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = Color(0xFF458F81),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
fun
        Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
    ) {
        ScreenTitle(title = "災害應變")
        Text(
            text = "請點擊以下圖示，進入防災資訊！",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) {
                EmergencyTypeCard()
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "更多急救方法與資訊",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EmergencyInfoCard()
            EmergencyInfoCard()
        }
    }*/


@Composable
fun ScreenTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}




@Composable
fun SupplyCard(supplyItem: SupplyItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = supplyItem.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "分類: ${supplyItem.category} | 災害類別: ${supplyItem.disasterType}", fontSize = 18.sp, fontWeight = FontWeight.Light)
            Text(text = "有效期限: ${supplyItem.date}", fontSize = 18.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun SupplyDetailDialog(
    supply: SupplyItem,
    onDismiss: () -> Unit,
    onEdit: (SupplyItem) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember {
        supply.imageUriString?.let { uriString ->
            runCatching {
                val uri = Uri.parse(uriString)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }.getOrNull()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = supply.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 顯示圖片
                SupplyImage(imageUriString = supply.imageUriString)

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem("物資分類", supply.category)
                DetailItem("物資數量", supply.number)
                DetailItem("有效期限", supply.date)
                DetailItem("災害分類", supply.disasterType)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onDelete(supply.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x8076AEA3)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("刪除", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onEdit(supply) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF458F81)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("設定", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplyImage(imageUriString: String?) {
    val context = LocalContext.current

    imageUriString?.let { uriString ->
        val imageFile = File(uriString)
        val painter = rememberImagePainter(
            data = imageFile,
            builder = {
                crossfade(true)
                transformations(RoundedCornersTransformation(8f))
            }
        )

        Image(
            painter = painter,
            contentDescription = "Supply image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 16.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

//調整物資資訊
@Composable
fun EditSupplyDialog(
    supply: SupplyItem,
    onDismiss: () -> Unit,
    onSave: (SupplyItem) -> Unit
) {
    var supplyName by remember { mutableStateOf(TextFieldValue(supply.name)) }
    var supplyCategory by remember { mutableStateOf(supply.category) }
    var supplyNumber by remember { mutableStateOf(TextFieldValue(supply.number)) }
    var expirationDate by remember { mutableStateOf(supply.date) }
    var disasterCategory by remember { mutableStateOf(supply.disasterType) }
    var selectedImageUriString by remember { mutableStateOf(supply.imageUriString) }

    val supplyCategories = listOf("食品飲水", "醫療衛生", "衣物寢具", "通訊與照明設備", "其他")
    val disasterCategories = listOf("地震", "火災", "水災", "空襲")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUriString = uri?.toString()
    }

    var supplyCategoryExpanded by remember { mutableStateOf(false) }
    var disasterCategoryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            expirationDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
        },
        year,
        month,
        day
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(450.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("編輯物資", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF063049)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "由相簿上傳物資圖片", fontSize = 15.sp)
                }


                // 顯示當前圖片
                selectedImageUriString?.let { uriString ->
                    SupplyImage(imageUriString = uriString)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = supplyName,
                    onValueChange = { supplyName = it },
                    label = { Text("物資名稱") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = supplyCategory,
                        onValueChange = { },
                        label = { Text(text = "物資分類", fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { supplyCategoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "展開分類選單")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = supplyCategoryExpanded,
                        onDismissRequest = { supplyCategoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)  // 設定下拉選單寬度
                    ) {
                        supplyCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    supplyCategory = category
                                    supplyCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    value = supplyNumber,
                    onValueChange = { supplyNumber = it },
                    label = { Text(text = "物資數量", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { },
                    label = { Text(text = "有效期限", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Filled.DateRange, "date picker")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = disasterCategory,
                        onValueChange = { },
                        label = { Text(text = "災害分類", fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { disasterCategoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "展開災害類型選單")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = disasterCategoryExpanded,
                        onDismissRequest = { disasterCategoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)  // 設定下拉選單寬度
                    ) {
                        disasterCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    disasterCategory = category
                                    disasterCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x8076AEA3))
                    ) {
                        Text("取消", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val updatedSupply = supply.copy(
                                name = supplyName.text,
                                category = supplyCategory,
                                date = expirationDate,
                                disasterType = disasterCategory,
                                imageUriString = selectedImageUriString,
                            )
                            onSave(updatedSupply)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF458F81))
                    ) {
                        Text("保存", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun EmergencyTypeCard() {
    Button(
        onClick = { /* TODO: handle click */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        // 這裡可以添加具體的災害類型圖標
    }
}



@Composable
fun AddSupplyScreen(supplyViewModel: SupplyViewModel, navController: NavController) {
    var supplyName by remember { mutableStateOf(TextFieldValue("")) }
    var supplyCategory by remember { mutableStateOf("食物") }
    var supplyNumber by remember { mutableStateOf(TextFieldValue("")) }
    var expirationDate by remember { mutableStateOf("") }
    var disasterCategory by remember { mutableStateOf("地震") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val supplyCategories = listOf("食品飲水", "醫療衛生", "衣物寢具", "通訊與照明設備", "其他")

    val disasterCategories = listOf("地震", "火災", "洪水", "空襲")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var supplyCategoryExpanded by remember { mutableStateOf(false) }
    var disasterCategoryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            expirationDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
        },
        year,
        month,
        day
    )

    val saveStatus by supplyViewModel.saveStatus.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD4E4E2), Color(0xFF77ACA2))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "新增物資",
                    fontSize = 24.sp,
                    color = Color(0xFF063049),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF063049)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "由相簿上傳物資圖片", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = supplyName,
                    onValueChange = { supplyName = it },
                    label = { Text(text = "物資名稱", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = supplyCategory,
                        onValueChange = { },
                        label = { Text(text = "物資分類", fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { supplyCategoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "展開分類選單")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = supplyCategoryExpanded,
                        onDismissRequest = { supplyCategoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)  // 設定下拉選單寬度
                    ) {
                        supplyCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    supplyCategory = category
                                    supplyCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = supplyNumber,
                    onValueChange = { supplyNumber = it },
                    label = { Text(text = "物資數量", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { },
                    label = { Text(text = "有效期限", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Filled.DateRange, "date picker")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = disasterCategory,
                        onValueChange = { },
                        label = { Text(text = "災害分類", fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { disasterCategoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "展開災害類型選單")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = disasterCategoryExpanded,
                        onDismissRequest = { disasterCategoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)  // 設定下拉選單寬度
                    ) {
                        disasterCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    disasterCategory = category
                                    disasterCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x8076AEA3))
                    ) {
                        Text(text = "取消", fontSize = 15.sp)
                    }

                    Button(
                        onClick = {
                            if (supplyName.text.isNotBlank() &&
                                supplyNumber.text.isNotBlank() &&
                                expirationDate.isNotBlank()) {

                                val newSupply = SupplyItem(
                                    id = UUID.randomUUID().toString(),
                                    name = supplyName.text,
                                    category = supplyCategory,
                                    number = supplyNumber.text,
                                    date = expirationDate,
                                    disasterType = disasterCategory,
                                    imageUriString = selectedImageUri?.toString()
                                )

                                try {
                                    Log.d("AddSupplyScreen", "呼叫 addSupply")
                                    supplyViewModel.addSupply(newSupply)
                                } catch (e: Exception) {
                                    Log.e("AddSupplyScreen", "儲存失敗", e)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF458F81)),
                        enabled = supplyName.text.isNotBlank() && supplyNumber.text.isNotBlank() && expirationDate.isNotBlank()
                    ) {
                        Text(text = "儲存物資", fontSize = 15.sp)
                    }
                }

                if (saveStatus is SaveStatus.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    LaunchedEffect(saveStatus) {
        when (saveStatus) {
            is SaveStatus.Success -> {
                navController.popBackStack()
                supplyViewModel.resetSaveStatus()  // 重置狀態
            }
            is SaveStatus.Error -> {
                // 處理錯誤...
            }
            else -> {} // 處理其他狀態
        }
    }
}



@Composable
fun EmergencyFunctionsScreen(
    navController: NavController,
    onNavigateToMedicalCard: () -> Unit,
    onNavigateToDisasterResponse: () -> Unit,
    onNavigateToMorseAlarm: () -> Unit
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitle(title = "緊急功能")

        // 個人醫療卡
        OptionButton(
            title = "個人醫療卡",
            description = "建立或編輯醫療卡",
            onClick = onNavigateToMedicalCard
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 災害應變
        OptionButton(
            title = "災害應變",
            description = "面對各式災難的生存法則",
            onClick = onNavigateToDisasterResponse
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 緊急警鈴
        OptionButton(
            title = "緊急警鈴",
            description = "經緯度轉摩斯密碼警鈴撥出",
            onClick = onNavigateToMorseAlarm
        )

        Spacer(modifier = Modifier.height(16.dp))

        OptionButton(
            title = "119 緊急報案",
            description = "電話報案或訊息推播",
            onClick = { navController.navigate("emergency_contact") }  // 更改這裡
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要通話權限") },
            text = { Text("為了能夠撥打緊急電話，需要獲取通話權限。") },
            confirmButton = {
                TextButton(onClick = {
                    requestCallPermission(context)
                    showPermissionDialog = false
                }) {
                    Text("確認")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun makeEmergencyCall(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:119")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun checkCallPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requestCallPermission(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activity = context as? android.app.Activity
        activity?.requestPermissions(
            arrayOf(Manifest.permission.CALL_PHONE),
            REQUEST_CALL_PERMISSION
        )
    }
}

private const val REQUEST_CALL_PERMISSION = 1001

sealed class SaveStatus {
    object Idle : SaveStatus()
    object Saving : SaveStatus()
    object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}


class SupplyViewModelFactory(private val supplyRepository: SupplyRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupplyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SupplyViewModel(supplyRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    val fakeSupplyDao = object : com.example.islanddisastersurvivalguideapp.data.local.dao.SupplyDao {
        override fun getAllSupplies(): kotlinx.coroutines.flow.Flow<List<SupplyItem>> {
            // 回傳一個空的清單，讓 UI 顯示空白狀態
            return flowOf(emptyList())
        }
        override suspend fun insertSupply(supply: SupplyItem) {
            // 假的，不做事
        }
        override suspend fun deleteSupply(supply: SupplyItem) {
            // 假的，不做事
        }
        override suspend fun deleteSupplyById(supplyId: String) {
            // 假的，不做事
        }
    }

    // 2. 把假的 DAO 塞給 Repository
    val supplyRepository = SupplyRepository(fakeSupplyDao)
    val supplyViewModel = SupplyViewModel(supplyRepository, context)
    val shelterViewModel = ShelterViewModel(context.applicationContext as Application)

    MaterialTheme {
        MainScreen(
            supplyViewModel = supplyViewModel,
            shelterViewModel = shelterViewModel
        )
    }
}