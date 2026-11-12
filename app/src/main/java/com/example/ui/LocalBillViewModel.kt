package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.room.AppDatabase
import com.example.data.room.LocalBillDao
import com.example.data.room.LocalBillEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalBillViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: LocalBillDao = AppDatabase.getDatabase(application).localBillDao()

    val allLocalBills: StateFlow<List<LocalBillEntity>> = dao.getAllBills()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun insertBill(name: String, amount: Double, dueDate: String, category: String = "Utilities", reminderDays: Int = 3) {
        viewModelScope.launch {
            val entity = LocalBillEntity(
                name = name.trim(),
                amount = amount,
                dueDate = dueDate,
                category = category,
                reminderDaysBefore = reminderDays
            )
            dao.insertBill(entity)
        }
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            dao.deleteBillById(billId)
        }
    }

    fun togglePaid(bill: LocalBillEntity) {
        viewModelScope.launch {
            dao.updateBill(bill.copy(isPaid = !bill.isPaid))
        }
    }
}
