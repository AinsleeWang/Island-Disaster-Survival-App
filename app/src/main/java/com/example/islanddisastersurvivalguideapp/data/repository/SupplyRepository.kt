package com.example.disasterpreparednessapp.data.repository


import android.util.Log
import com.example.islanddisastersurvivalguideapp.data.model.SupplyItem
import com.example.islanddisastersurvivalguideapp.data.local.dao.SupplyDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SupplyRepository(private val supplyDao: SupplyDao) {

    companion object {
        private const val TAG = "SupplyRepository"
    }

    fun getAllSupplies(): Flow<List<SupplyItem>> {
        // 直接回傳 DAO 的 Flow，不需要 suspend，因為 Flow 本身是冷的 (Cold Stream)
        return supplyDao.getAllSupplies()
    }

    /**
     * 新增或更新物資
     *
     * 確保在 IO 執行緒執行，避免阻塞主執行緒。
     */
    suspend fun addSupply(supply: SupplyItem) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "開始新增/更新物資至本地資料庫: ${supply.name}")
                supplyDao.insertSupply(supply)
                Log.d(TAG, "物資儲存成功")
            } catch (e: Exception) {
                Log.e(TAG, "儲存物資失敗", e)
                throw e
            }
        }
    }

    // 更新邏輯與新增相同 (Room 的 OnConflictStrategy.REPLACE 處理了)
    suspend fun updateSupply(supply: SupplyItem) = addSupply(supply)

    suspend fun deleteSupply(supplyId: String) {
        withContext(Dispatchers.IO) {
            try {
                supplyDao.deleteSupplyById(supplyId)
                Log.d(TAG, "刪除物資成功: $supplyId")
            } catch (e: Exception) {
                Log.e(TAG, "刪除物資失敗", e)
                throw e
            }
        }
    }


}