package com.example.islanddisastersurvivalguideapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islanddisastersurvivalguideapp.utils.MorseCodePlayer

@Composable
fun MorseAlarmScreen(
    latitude: Double = 25.0366405,
    longitude: Double = 121.5132336,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val morsePlayer = remember { MorseCodePlayer(context) }
    var isPlaying by remember { mutableStateOf(false) }
    val morseCode = remember(latitude, longitude) {
        morsePlayer.convertCoordinateToMorse(latitude, longitude)
    }

    DisposableEffect(Unit) {
        onDispose {
            morsePlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 標題區域
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
                text = "警急警鈴",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("當前位置",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp)
                Text("緯度：$latitude°N")
                Text("經度：$longitude°E")
                Spacer(modifier = Modifier.height(16.dp))
                Text("摩斯密碼",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp)
                Text(morseCode, modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        Button(
            onClick = {
                isPlaying = !isPlaying
                if (isPlaying) {
                    morsePlayer.playMorseCode(morseCode)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) Color.Red else Color(0xFF458F81)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(if (isPlaying) "停止警鈴" else "開始警鈴")
        }

        // 說明卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "使用說明",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 點擊「開始警鈴」按鈕開始播放位置的摩斯密碼。\n" +
                            "2. 聲音將循環播放直到點擊「停止警鈴」。\n" +
                            "3. 短聲(.)表示「點」，長聲(-)表示「劃」。\n" +
                            "4. 建議在安全且音量適中的情況下使用。",
                    color = Color(0xFF666666)
                )
            }
        }
    }
}