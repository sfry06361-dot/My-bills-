package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_bills")
data class LocalBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val amount: Double,
    val dueDate: String, // format yyyy-MM-dd
    val category: String = "Utilities",
    val isPaid: Boolean = false,
    val reminderDaysBefore: Int = 3,
    val timestamp: Long = System.currentTimeMillis()
)
