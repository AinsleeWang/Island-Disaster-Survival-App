package com.example.islanddisastersurvivalguideapp.ui.screen

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.islanddisastersurvivalguideapp.SupplyViewModel
import com.example.islanddisastersurvivalguideapp.viewmodel.MedicalCardViewModel
import com.google.android.gms.location.LocationServices

@Composable
fun EmergencyContactScreen(
    medicalCardViewModel: MedicalCardViewModel,
    supplyViewModel: SupplyViewModel,
    onBack: () -> Unit
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var showSmsPreview by remember { mutableStateOf(false) }
    var smsContent by remember { mutableStateOf("") }
    var showSmsStatus by remember { mutableStateOf(false) }
    var smsStatusMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            showPermissionDialog = true
            permissionMessage = "需要必要權限才能使用緊急聯絡功能"
        }
    }

    // 監聽位置更新
    LaunchedEffect(Unit) {
        if (checkLocationPermission(context)) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    currentLocation = location
                }
            } catch (e: SecurityException) {
                Log.e("EmergencyContact", "Location permission denied", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 標題列
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
            Text(
                text = "緊急聯絡",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 緊急電話按鈕
        EmergencyButton(
            text = "119 語音報案",
            description = "直接撥打119進行語音報案",
            icon = Icons.Default.Call,
            onClick = {
                if (checkCallPermission(context)) {
                    makeEmergencyCall(context, currentLocation)
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 簡訊報案按鈕
        EmergencyButton(
            text = "簡訊推播報案",
            description = "傳送包含位置和醫療資訊的緊急簡訊",
            icon = Icons.AutoMirrored.Filled.Message,
            onClick = {
                if (checkSmsPermission(context)) {
                    // 預覽簡訊內容
                    smsContent = buildEmergencyMessage(
                        context = context,
                        location = currentLocation,
                        medicalInfo = medicalCardViewModel.getMedicalCardInfo(),
                        supplyInfo = supplyViewModel.getEmergencySupplyInfo()
                    )
                    showSmsPreview = true
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS))
                }
            }
        )
    }

    // 簡訊預覽對話框
    if (showSmsPreview) {
        AlertDialog(
            onDismissRequest = { showSmsPreview = false },
            title = { Text("確認發送緊急簡訊") },
            text = {
                Column {
                    Text("以下內容將發送至119：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = smsContent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sendEmergencySms(
                            context = context,
                            message = smsContent,
                            onSuccess = {
                                showSmsStatus = true
                                smsStatusMessage = "緊急簡訊已發送成功"
                            },
                            onError = { error ->
                                showSmsStatus = true
                                smsStatusMessage = "發送失敗： $error"
                            }
                        )
                        showSmsPreview = false
                    }
                ) {
                    Text("發送")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsPreview = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 簡訊狀態對話框
    if (showSmsStatus) {
        AlertDialog(
            onDismissRequest = { showSmsStatus = false },
            title = { Text(if (smsStatusMessage.contains("成功")) "發送成功" else "發送失敗") },
            text = { Text(smsStatusMessage) },
            confirmButton = {
                TextButton(onClick = { showSmsStatus = false }) {
                    Text("確定")
                }
            }
        )
    }

    // 權限請求對話框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要權限") },
            text = { Text(permissionMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }) {
                    Text("確定")
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


@Composable
private fun EmergencyButton(
    text: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (text.contains("119")) Color.Red else Color(0xFF458F81)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun checkCallPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED

private fun checkSmsPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

private fun checkLocationPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

private fun makeEmergencyCall(context: Context) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:119")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun sendEmergencySms(
    context: Context,
    location: Location?,
    medicalInfo: String?,
    supplyInfo: String?
) {
    val locationText = location?.let {
        "位置: 緯度=${it.latitude}, 經度=${it.longitude}"
    } ?: "無法獲取位置"

    val message = buildString {
        appendLine("緊急求助!")
        appendLine(locationText)
        if (!medicalInfo.isNullOrBlank()) {
            appendLine("\n醫療資訊：")
            appendLine(medicalInfo)
        }
        if (!supplyInfo.isNullOrBlank()) {
            appendLine("\n物資需求：")
            appendLine(supplyInfo)
        }
    }

    try {
        SmsManager.getDefault().sendTextMessage(
            "119",
            null,
            message,
            null,
            null
        )
        Toast.makeText(context, "緊急簡訊已發送", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "發送簡訊失敗： ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
private fun makeEmergencyCall(context: Context, location: Location?) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:119")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // 添加位置資訊到意圖中
        location?.let {
            putExtra("emergency_location_lat", it.latitude)
            putExtra("emergency_location_lng", it.longitude)
        }

        // 標記為緊急電話
        putExtra("android.telecom.extra.IS_EMERGENCY_CALL", true)
    }
    context.startActivity(intent)
}

private fun buildEmergencyMessage(
    context: Context,
    location: Location?,
    medicalInfo: String?,
    supplyInfo: String?
): String {
    return buildString {
        appendLine("119緊急求助！")
        appendLine()

        // 位置信息
        location?.let {
            appendLine("位置資訊：")
            appendLine("緯度： ${it.latitude}")
            appendLine("經度： ${it.longitude}")
            try {
                val addresses = Geocoder(context).getFromLocation(it.latitude, it.longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    appendLine("地址： ${address.getAddressLine(0)}")
                }
            } catch (e: Exception) {
                Log.e("Emergency", "無法取得地址", e)
            }
        } ?: appendLine("無法獲取位置")

        appendLine()

        // 醫療資訊
        if (!medicalInfo.isNullOrBlank()) {
            appendLine("醫療資訊：")
            appendLine(medicalInfo)
            appendLine()
        }

        // 物資需求
        if (!supplyInfo.isNullOrBlank()) {
            appendLine("物資需求：")
            appendLine(supplyInfo)
        }
    }
}

private fun sendEmergencySms(
    context: Context,
    message: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        // 分割長簡訊
        val parts = smsManager.divideMessage(message)

        // 發送狀態追蹤
        val sentIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("SMS_SENT"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // 接收狀態追蹤
        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // 註冊廣播接收器
        context.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> onSuccess()
                        else -> onError("發送失敗: $resultCode")
                    }
                    context?.unregisterReceiver(this)
                }
            },
            IntentFilter("SMS_SENT")
        )

        // 發送簡訊
        if (parts.size > 1) {
            val sentIntents = ArrayList<PendingIntent>()
            val deliveredIntents = ArrayList<PendingIntent>()
            repeat(parts.size) {
                sentIntents.add(sentIntent)
                deliveredIntents.add(deliveredIntent)
            }
            smsManager.sendMultipartTextMessage(
                "119",
                null,
                parts,
                sentIntents,
                deliveredIntents
            )
        } else {
            smsManager.sendTextMessage(
                "119",
                null,
                message,
                sentIntent,
                deliveredIntent
            )
        }
    } catch (e: Exception) {
        onError(e.message ?: "未知錯誤")
    }
}