package com.example.islanddisastersurvivalguideapp.utils

sealed interface SaveStatus {
    object Idle : SaveStatus
    object Saving : SaveStatus
    object Success : SaveStatus
    data class Error(val message: String) : SaveStatus
}