
package com.example.islanddisastersurvivalguideapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 定義專案主色調
val IslandTeal = Color(0xFF458F81)
val IslandLightTealGradientStart = Color(0xFFD4E4E2)
val IslandLightTealGradientEnd = Color(0xFF77ACA2)
val IslandRed = Color(0xFFE57373) // 用於停止按鈕


// 共用的漸層背景容器
@Composable
fun IslandAppBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(IslandLightTealGradientStart, IslandLightTealGradientEnd)
                )
            )
            .padding(16.dp),
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    // 自定義顏色
)

private val LightColorScheme = lightColorScheme(
    // 自定義顏色
)

@Composable
fun IslandDisasterSurvivalGuideAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}