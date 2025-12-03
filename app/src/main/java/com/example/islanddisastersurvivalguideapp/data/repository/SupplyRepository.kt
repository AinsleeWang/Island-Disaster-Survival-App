package com.example.disasterpreparednessapp.data.repository


import android.util.Log
import com.example.islanddisastersurvivalguideapp.data.model.SupplyItem
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SupplyRepository {
    private val database: FirebaseDatabase by lazy {
        Firebase.database("https://islanddisastersurvivalguideapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }
    private val suggestionChecksRef by lazy { database.reference.child("suggestionChecks") }
    private val suppliesRef by lazy { database.reference.child("supplies") }

    suspend fun addSupply(supply: SupplyItem) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "開始新增物資: ${supply.id}")
                val supplyData = supply.toMap()
                suppliesRef.child(supply.id).setValue(supplyData).await()
                Log.d(TAG, "物資新增成功")
            } catch (e: Exception) {
                Log.e(TAG, "新增物資失敗", e)
                throw e
            }
        }
    }

    suspend fun getAllSupplies(): List<SupplyItem> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = suppliesRef.get().await()
                Log.d(TAG, "成功獲取所有物資")
                snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(SupplyItem::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "解析物資數據失敗: ${child.key}", e)
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "獲取物資列表失敗", e)
                emptyList()
            }
        }
    }

    suspend fun updateSupply(supply: SupplyItem) {
        withContext(Dispatchers.IO) {
            try {
                val updates = supply.toMap()
                suppliesRef.child(supply.id).setValue(updates).await()
                Log.d(TAG, "更新物資成功: ${supply.id}")
            } catch (e: Exception) {
                Log.e(TAG, "更新物資失敗", e)
                throw e
            }
        }
    }

    suspend fun deleteSupply(supplyId: String) {
        withContext(Dispatchers.IO) {
            try {
                suppliesRef.child(supplyId).removeValue().await()
                Log.d(TAG, "刪除物資成功: $supplyId")
            } catch (e: Exception) {
                Log.e(TAG, "刪除物資失敗", e)
                throw e
            }
        }
    }

    companion object {
        private const val TAG = "SupplyRepository"
    }


}