package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Bill
import com.example.data.repository.BillRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class BillFilter(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    PAID("Paid")
}

data class MonthlyStats(
    val totalDue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val upcomingCount: Int = 0,
    val overdueCount: Int = 0
)

class BillViewModel(
    private val repository: BillRepository,
    val currentUserId: String
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _filterStatus = MutableStateFlow(BillFilter.ALL)
    val filterStatus: StateFlow<BillFilter> = _filterStatus.asStateFlow()

    val bills: StateFlow<UiState<List<Bill>>> = repository.observeBills(currentUserId)
        .map<List<Bill>, UiState<List<Bill>>> { list ->
            UiState.Success(list.sortedBy { it.dueDateEpochDay })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w("BillVM", "Error observing bills", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load bills"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val monthlyStats: StateFlow<MonthlyStats> = combine(bills, currentMonth) { billsState, month ->
        if (billsState !is UiState.Success) {
            MonthlyStats()
        } else {
            val today = LocalDate.now()
            val monthBills = billsState.data.filter { bill ->
                val billMonth = YearMonth.from(bill.dueDate)
                billMonth == month
            }
            val totalDue = monthBills.sumOf { it.amount }
            val totalPaid = monthBills.filter { it.isPaid }.sumOf { it.amount }
            val totalPending = totalDue - totalPaid
            val upcomingCount = billsState.data.count { !it.isPaid && !it.isOverdue(today) }
            val overdueCount = billsState.data.count { it.isOverdue(today) }

            MonthlyStats(
                totalDue = totalDue,
                totalPaid = totalPaid,
                totalPending = totalPending,
                upcomingCount = upcomingCount,
                overdueCount = overdueCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MonthlyStats()
    )

    val billsOnSelectedDate: StateFlow<List<Bill>> = combine(bills, selectedDate) { billsState, date ->
        if (billsState is UiState.Success) {
            billsState.data.filter { it.dueDate == date }
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        val dateMonth = YearMonth.from(date)
        if (dateMonth != _currentMonth.value) {
            _currentMonth.value = dateMonth
        }
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.from(today)
    }

    fun setFilter(filter: BillFilter) {
        _filterStatus.value = filter
    }

    fun createBill(bill: Bill, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.createBill(currentUserId, bill)
            onComplete(result)
        }
    }

    fun updateBill(bill: Bill, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.updateBill(currentUserId, bill)
            onComplete(result)
        }
    }

    fun togglePaid(bill: Bill) {
        viewModelScope.launch {
            repository.markBillPaid(currentUserId, bill.id, !bill.isPaid)
        }
    }

    fun deleteBill(billId: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteBill(currentUserId, billId)
            onComplete(result)
        }
    }
}
