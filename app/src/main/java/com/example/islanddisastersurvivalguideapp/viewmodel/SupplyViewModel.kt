package com.example.islanddisastersurvivalguideapp.viewmodel;

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList;
import java.io.File
import java.io.IOException
import java.util.UUID

import android.content.Context;

import com.example.islanddisastersurvivalguideapp.data.repository.SupplyRepository;
import com.example.islanddisastersurvivalguideapp.SaveStatus;
import com.example.islanddisastersurvivalguideapp.data.model.SupplyItem;

public class SupplyViewModel(
        private val repository:SupplyRepository,
        private val context:Context) : ViewModel() {
            val supplies: StateFlow<List<SupplyItem>> = repository.getAllSupplies()
                .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000), // 停止訂閱 5秒後才停止 Flow，節省資源
                        initialValue = emptyList()
                )

            private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
            val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    fun addSupply(supply: SupplyItem) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving
                Log.d("SupplyViewModel", "開始儲存物資: ${supply.name}")

                // 處理圖片
                val imagePath = supply.imageUriString?.let { uriString ->
                    try {
                        val uri = Uri.parse(uriString)
                        saveImageToInternalStorage(uri)
                    } catch (e: Exception) {
                        Log.e("SupplyViewModel", "圖片處理失敗", e)
                        null
                    }
                }

                // 建立新的 SupplyItem，確保有正確的 ID 和圖片路徑
                val newSupply = supply.copy(
                        id = supply.id.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString(),
                        imageUriString = imagePath
                )

                // 儲存到 Repository
                repository.addSupply(newSupply)

                Log.d("SupplyViewModel", "物資儲存成功")
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                Log.e("SupplyViewModel", "儲存物資失敗", e)
                _saveStatus.value = SaveStatus.Error(e.message ?: "未知錯誤")
            }
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("無法打開圖片來源")

            val imageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "supply_images").apply {
                if (!exists()) mkdirs()
            }

            val imageFile = File(imageDir, "IMG_${System.currentTimeMillis()}.jpg")

            inputStream.use { input ->
                    imageFile.outputStream().use { output ->
                    input.copyTo(output)
            }
            }

            Log.d("SupplyViewModel", "圖片保存成功: ${imageFile.absolutePath}")
            return@withContext imageFile.absolutePath
        } catch (e: Exception) {
            Log.e("SupplyViewModel", "保存圖片失敗", e)
            throw e
        }
    }

    fun updateSupply(supply: SupplyItem) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving
                repository.updateSupply(supply)
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "更新失敗")
            }
        }
    }

    fun deleteSupply(supplyId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSupply(supplyId)
            } catch (e: Exception) {
                Log.e("SupplyViewModel", "刪除失敗", e)
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = SaveStatus.Idle
    }

    fun getEmergencySupplyInfo(): String {
        return buildString {
            val urgentSupplies = supplies.value.filter { supply ->
                try {
                    val count = supply.number.toIntOrNull() ?: 0
                    count <= 3  // 數量小於等於3的視為緊急物資
                } catch (e: NumberFormatException) {
                    false
                }
            }

            if (urgentSupplies.isEmpty()) {
                appendLine("目前無緊急物資需求")
            } else {
                appendLine("緊急物資需求：")
                urgentSupplies.forEach { supply ->
                        appendLine("- ${supply.name}: 剩餘 ${supply.number} 個")
                }
            }
        }
    }

}
