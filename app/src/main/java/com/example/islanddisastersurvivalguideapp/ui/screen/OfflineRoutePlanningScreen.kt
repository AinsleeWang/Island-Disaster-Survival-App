package com.example.islanddisastersurvivalguideapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.islanddisastersurvivalguideapp.LocationSelectionScreen
import com.example.islanddisastersurvivalguideapp.ShelterSelectionScreen
import com.example.islanddisastersurvivalguideapp.data.model.FrequentLocation
import com.example.islanddisastersurvivalguideapp.data.model.ShelterInfo
import com.example.islanddisastersurvivalguideapp.ui.theme.IslandAppBackground
import com.example.islanddisastersurvivalguideapp.ui.theme.IslandTeal
import com.example.islanddisastersurvivalguideapp.viewmodel.ShelterViewModel

// 定義流程步驟 (保持不變)
enum class RouteStep(val title: String) {
    SELECT_LOCATION("選擇起點"),
    SELECT_SHELTER("選擇避難所"),
    NAVIGATION("開始導航")
}

@Composable
fun OfflineRoutePlanningScreen(
    shelterViewModel: ShelterViewModel = viewModel(),
    onClose: () -> Unit
) {
    // 狀態管理
    var currentStep by remember { mutableStateOf(RouteStep.SELECT_LOCATION) }
    var selectedLocation by remember { mutableStateOf<FrequentLocation?>(null) }
    var selectedShelter by remember { mutableStateOf<ShelterInfo?>(null) }
    var isNavigating by remember { mutableStateOf(false) }

    // 觀察 ViewModel 的資料
    val frequentLocations by shelterViewModel.frequentLocations.collectAsState()
    val route by shelterViewModel.selectedRoute.collectAsState()
    val nearestShelters by shelterViewModel.nearestShelters.collectAsState()

    // 使用自定義的漸層背景
    IslandAppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // 頂部步驟指示器 (根據截圖新增)
            RouteStepIndicator(currentStep = currentStep)

            Spacer(modifier = Modifier.height(16.dp))

            // 內容區域，使用 Scaffold 來管理基本的 padding，但設為透明背景
            Scaffold(
                containerColor = Color.Transparent, // 重要：讓背景透明
                contentWindowInsets = WindowInsets(0.dp) // 避免不必要的 inset
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentStep) {
                        RouteStep.SELECT_LOCATION -> {
                            // 注意：這裡假設 LocationSelectionScreen 內部已經有了 Card 風格
                            // 如果沒有，需要在外部包裹 Card
                            LocationSelectionScreen(
                                locations = frequentLocations,
                                onLocationSelected = { location ->
                                    selectedLocation = location
                                    shelterViewModel.loadNearestShelters(location)
                                    currentStep = RouteStep.SELECT_SHELTER
                                },
                                onBack = onClose
                            )
                        }

                        RouteStep.SELECT_SHELTER -> {
                            // 同上，假設 ShelterSelectionScreen 內部已有 Card 風格
                            ShelterSelectionScreen(
                                shelters = nearestShelters,
                                onShelterSelected = { shelter ->
                                    selectedShelter = shelter
                                    if (selectedLocation != null) {
                                        shelterViewModel.loadRouteForShelter(
                                            selectedLocation!!,
                                            shelter
                                        )
                                        currentStep = RouteStep.NAVIGATION
                                    }
                                },
                                onBack = { currentStep = RouteStep.SELECT_LOCATION }
                            )
                        }

                        RouteStep.NAVIGATION -> {
                            if (route != null) {
                                NavigationScreen(
                                    route = route!!,
                                    isNavigating = isNavigating,
                                    onStartNavigation = { isNavigating = true },
                                    onStopNavigation = { isNavigating = false },
                                    onBack = {
                                        isNavigating = false
                                        currentStep = RouteStep.SELECT_SHELTER
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = IslandTeal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 頂部步驟指示器組件
@Composable
fun RouteStepIndicator(currentStep: RouteStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RouteStep.values().forEachIndexed { index, step ->
            val isActive = step == currentStep
            val isPassed = step.ordinal < currentStep.ordinal
            val color = if (isActive || isPassed) IslandTeal else Color.Gray

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = step.title,
                    fontSize = 12.sp,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 如果不是最後一個步驟，畫連接線
            if (index < RouteStep.values().size - 1) {
                Spacer(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(if (isPassed) IslandTeal else Color.LightGray)
                        .offset(y = (-10).dp) // 稍微向上偏移以對齊圓圈中心
                )
            }
        }
    }
}