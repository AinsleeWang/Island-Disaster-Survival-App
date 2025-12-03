package com.example.islanddisastersurvivalguideapp.data.model

import androidx.annotation.DrawableRes

data class DisasterType(
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int
)