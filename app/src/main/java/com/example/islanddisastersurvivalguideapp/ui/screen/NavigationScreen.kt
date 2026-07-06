package com.example.islanddisastersurvivalguideapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.islanddisastersurvivalguideapp.data.model.PrecomputedRoute
import com.example.islanddisastersurvivalguideapp.sensor.NavigationSensorManager
import com.example.islanddisastersurvivalguideapp.ui.theme.IslandRed
import com.example.islanddisastersurvivalguideapp.ui.theme.IslandTeal
import kotlin.math.absoluteValue

@Composable
fun NavigationScreen(
    route: PrecomputedRoute,
    isNavigating: Boolean,
    onStartNavigation: () -> Unit,
    onStopNavigation: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // 使用 Sensor Manager (邏輯保持不變)
    val navigationSensorManager = remember { NavigationSensorManager(context) }

    var deviceDirection by remember { mutableStateOf(0f) }
    val targetDirection = remember { route.initialBearing.toFloat() }

    // 處理運動記錄權限請求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "已取得活動權限，可以開始計步", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "請允許權限以使用計步功能", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        // Android 10 (Q) 以上需要動態申請 ACTIVITY_RECOGNITION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    // 計算方向差異
    val directionDifference = remember(deviceDirection, targetDirection) {
        var diff = targetDirection - deviceDirection
        while (diff > 180) diff -= 360
        while (diff < -180) diff += 360
        diff
    }

    // 計算步數 (邏輯保持不變)
    val totalSteps = remember(route.distance) { (route.distance / 0.7).toInt() }
    var stepsTaken by remember { mutableStateOf(0) }
    val remainingSteps = (totalSteps - stepsTaken).coerceAtLeast(0)

    // 監聽感測器 (邏輯保持不變)
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            navigationSensorManager.apply {
                setStepListener { steps -> stepsTaken = steps }
                setDirectionListener { direction -> deviceDirection = direction }
                startListening()
            }
        } else {
            navigationSensorManager.stopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { navigationSensorManager.stopListening() }
    }

    // UI 重新佈局，參考截圖風格
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 標題列 (參考 MedicalCardScreen 風格)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Color.Black)
            }
            Text(
                text = "離線導航",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(48.dp)) // 平衡佈局用
        }

        // 2. 主要內容卡片 (白色背景，大圓角)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 佔據剩餘空間
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // 內容上下分佈
            ) {
                // 2.1 頂部：超大步數顯示
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$remainingSteps",
                        fontSize = 80.sp, // 根據截圖設定超大字體
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "步",
                        fontSize = 24.sp,
                        color = Color.Gray
                    )
                }

                // 2.2 中間：簡化版指南針視覺化 (參考截圖)
                // 截圖顯示的是兩條線的相對角度，而非複雜的羅盤
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val lineLength = size.width / 2f * 0.8f

                        // 畫基準線 (例如：手機正前方)，使用紅色，稍微細一點
                        drawLine(
                            color = IslandRed.copy(alpha = 0.7f),
                            start = center,
                            end = Offset(center.x, center.y - lineLength),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        // 畫目標方向線，使用主色調青色，旋轉角度為 directionDifference
                        rotate(degrees = directionDifference, pivot = center) {
                            drawLine(
                                color = IslandTeal,
                                start = center,
                                end = Offset(center.x, center.y - lineLength),
                                strokeWidth = 10f, // 目標線稍微粗一點
                                cap = StrokeCap.Round
                            )
                        }

                        // 中心點
                        drawCircle(color = Color.Gray, radius = 8f, center = center)
                    }
                }

                // 2.3 底部：文字提示與按鈕
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 方向提示文字
                    val isCloseEnough = directionDifference.absoluteValue <= 15
                    Text(
                        text = when {
                            isCloseEnough -> "方向正確 直直走"
                            directionDifference > 0 -> "向右轉 ${directionDifference.toInt()}°"
                            else -> "向左轉 ${directionDifference.absoluteValue.toInt()}°"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCloseEnough) IslandTeal else Color.Black
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 控制按鈕 (參考 MorseAlarmScreen 風格)
                    Button(
                        onClick = if (isNavigating) onStopNavigation else onStartNavigation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNavigating) IslandRed else IslandTeal
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isNavigating) "停止導航" else "開始導航",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}