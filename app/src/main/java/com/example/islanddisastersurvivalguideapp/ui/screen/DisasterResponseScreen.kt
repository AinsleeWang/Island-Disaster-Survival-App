package com.example.islanddisastersurvivalguideapp.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.islanddisastersurvivalguideapp.R
import com.example.islanddisastersurvivalguideapp.components.DisasterTypeCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.os.Build
import android.provider.Settings
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL



@Composable
fun DisasterResponseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showDownloadProgress by remember { mutableStateOf(false) }
    var currentDownloadProgress by remember { mutableStateOf(0f) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "已取得儲存權限", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "需要儲存權限才能下載手冊，請到設定中開啟",
                Toast.LENGTH_LONG
            ).show()
            // 導向應用程式設定頁面
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    // 多個權限請求
    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(context, "已取得必要權限", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要儲存權限才能下載手冊", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndRequestStoragePermission(onGranted: () -> Unit) {
        when {
            // Android 11 (R)及以上
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    onGranted()
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    context.startActivity(intent)
                }
            }
            // Android 10 (Q)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                onGranted()
            }
            // Android 9 (P)及以下
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        onGranted()
                    }
                    else -> {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }



    // 修改下載函數
    fun downloadHandbook(url: String, fileName: String) {
        checkAndRequestStoragePermission {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    showDownloadProgress = true
                    Log.d("DownloadHandbook", "開始下載: $fileName")

                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val outputDir = File(downloadsDir, "防災手冊").apply {
                        if (!exists()) {
                            mkdirs()
                        }
                    }

                    val outputFile = File(outputDir, fileName)

                    withContext(Dispatchers.IO) {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()

                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            throw IOException("Server returned HTTP ${connection.responseCode}")
                        }

                        val fileSize = connection.contentLength.toLong()
                        var totalBytesRead = 0L

                        connection.inputStream.use { input ->
                            outputFile.outputStream().buffered().use { output ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead

                                    withContext(Dispatchers.Main) {
                                        currentDownloadProgress = if (fileSize > 0) {
                                            totalBytesRead.toFloat() / fileSize
                                        } else {
                                            0f
                                        }
                                    }
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "手冊已下載至「下載/防災手冊」資料夾",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("DownloadHandbook", "下載失敗", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "下載失敗：${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    showDownloadProgress = false
                }
            }
        }
    }

    fun openManualInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "無法開啟瀏覽器：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 權限請求的啟動器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(context, "已取得儲存權限", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要儲存權限才能下載手冊", Toast.LENGTH_SHORT).show()
        }
    }


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
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 標題區域在 Card 外
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
                    text = "災害應變",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                // 用於平衡左側返回按鈕的空間
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Card 內容
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 主要災害類型
                    DisasterTypeCard(
                        iconRes = R.drawable.ic_earthquake2,
                        title = "地震",
                        description = "地震避難與應變指南",
                        onClick = {
                            openManualInBrowser(context, "https://drive.google.com/uc?export=download&id=1JuFuj296JwW1lU1pGf5gLB2ClwDaySG-")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DisasterTypeCard(
                        iconRes = R.drawable.ic_fire2,
                        title = "火災",
                        description = "火災逃生要領",
                        onClick = {
                            openManualInBrowser(context, "https://drive.google.com/uc?export=download&id=1u5rrJQ_WEKMKtihGHHSmoXDBctxnMJVp")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DisasterTypeCard(
                        iconRes = R.drawable.ic_bombing2,
                        title = "空襲",
                        description = "空襲防災準備",
                        onClick = { openManualInBrowser(context, "https://drive.google.com/uc?export=download&id=1iGWBVeYpFci83NFn969Wkhh6g6DPAsSL")}
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    DisasterTypeCard(
                        iconRes = R.drawable.ic_flood2,
                        title = "水災",
                        description = "水災防災準備",
                        onClick = { openManualInBrowser(context, "https://drive.google.com/uc?export=download&id=1uSv1UsLJXyahBhljvu8Fsmby0Cz2secc")}
                    )
/*
                    // 緊急救護資訊
                    Text(
                        text = "緊急救護指南",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EmergencyInfoCard(
                            title = "CPR",
                            description = "心肺復甦術步驟"
                        )
                        EmergencyInfoCard(
                            title = "包紮",
                            description = "基本傷口處理"
                        )
                    }

 */// 下載進度指示器
                    if (showDownloadProgress) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("下載中") },
                            text = {
                                Column {
                                    LinearProgressIndicator(
                                        progress = currentDownloadProgress,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "${(currentDownloadProgress * 100).toInt()}%",
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            },
                            confirmButton = { }
                        )
                    }

                }
            }
        }
    }
}