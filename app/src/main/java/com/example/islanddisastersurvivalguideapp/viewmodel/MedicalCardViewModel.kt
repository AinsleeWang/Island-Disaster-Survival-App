package com.example.islanddisastersurvivalguideapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islanddisastersurvivalguideapp.data.local.MedicalCardEntity
import com.example.islanddisastersurvivalguideapp.data.repository.MedicalCardRepository
import com.example.islanddisastersurvivalguideapp.utils.SaveStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MedicalCardViewModel(
    private val repository: MedicalCardRepository
) : ViewModel() {
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus

    private val _currentCard = MutableStateFlow<MedicalCardEntity?>(null)
    val currentCard: StateFlow<MedicalCardEntity?> = _currentCard

    init {
        loadCurrentCard()
    }

    fun loadCurrentCard() {
        viewModelScope.launch {
            try {
                _currentCard.value = repository.getLatestMedicalCard()
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error("載入醫療卡失敗: ${e.message}")
            }
        }
    }

    fun saveMedicalCard(
        name: String,
        birthDate: String,
        bloodType: String,
        medicalHistory: String,
        medications: String,
        emergencyContact: String
    ) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving

                // 資料驗證
                if (name.isBlank() || birthDate.isBlank() || bloodType.isBlank()) {
                    _saveStatus.value = SaveStatus.Error("請填寫必要欄位")
                    return@launch
                }

                val medicalCard = MedicalCardEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    birthDate = birthDate,
                    bloodType = bloodType,
                    medicalHistory = medicalHistory,
                    medications = medications,
                    emergencyContact = emergencyContact,
                    createdAt = System.currentTimeMillis()
                )

                // 添加日誌來追蹤保存過程
                Log.d("MedicalCardViewModel", "Saving medical card: $medicalCard")
                repository.saveMedicalCard(medicalCard)
                Log.d("MedicalCardViewModel", "Medical card saved successfully")

                _saveStatus.value = SaveStatus.Success
                loadCurrentCard() // 重新載入資料

            } catch (e: Exception) {
                Log.e("MedicalCardViewModel", "Error saving medical card", e)
                _saveStatus.value = SaveStatus.Error(e.message ?: "保存失敗")
            }
        }
    }

    // 用於清除保存狀態
    fun resetSaveStatus() {
        _saveStatus.value = SaveStatus.Idle
    }

    // 用於更新現有醫療卡
    fun updateMedicalCard(card: MedicalCardEntity) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving
                repository.saveMedicalCard(card)
                _saveStatus.value = SaveStatus.Success
                loadCurrentCard()
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error("更新失敗: ${e.message}")
            }
        }
    }

    fun getMedicalCardInfo(): String {
        return buildString {
            currentCard.value?.let { card ->
                appendLine("個人醫療資訊：")
                appendLine("姓名: ${card.name}")
                appendLine("出生日期: ${card.birthDate}")
                appendLine("血型: ${card.bloodType}")
                if (!card.medicalHistory.isBlank()) {
                    appendLine("病史: ${card.medicalHistory}")
                }
                if (!card.medications.isBlank()) {
                    appendLine("服用藥物: ${card.medications}")
                }
                if (!card.emergencyContact.isBlank()) {
                    appendLine("緊急聯絡人: ${card.emergencyContact}")
                }
            } ?: appendLine("尚未建立醫療卡")
        }
    }
}