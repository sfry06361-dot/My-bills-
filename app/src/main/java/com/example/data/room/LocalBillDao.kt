package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalBillDao {
    @Query("SELECT * FROM local_bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<LocalBillEntity>>

    @Query("SELECT * FROM local_bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): LocalBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: LocalBillEntity): Long

    @Update
    suspend fun updateBill(bill: LocalBillEntity)

    @Query("DELETE FROM local_bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)

    @Query("DELETE FROM local_bills")
    suspend fun deleteAllBills()
}
