package com.example.islanddisastersurvivalguideapp.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MorseAlarmViewModel : ViewModel() {
    private val _location = MutableStateFlow(LatLng(25.0366405, 121.5132336))
    val location: StateFlow<LatLng> = _location.asStateFlow()

    fun updateLocation(newLatLng: LatLng) {
        _location.value = newLatLng
    }
}