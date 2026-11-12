package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@IgnoreExtraProperties
data class Bill(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val currency: String = "$",
    val category: String = "Utilities",
    val dueDateEpochDay: Long = 0L,
    val dueDayOfMonth: Int = 1,
    @get:PropertyName("isRecurring")
    @set:PropertyName("isRecurring")
    var isRecurring: Boolean = false,
    val frequency: String = "MONTHLY",
    @get:PropertyName("isPaid")
    @set:PropertyName("isPaid")
    var isPaid: Boolean = false,
    val paidAtEpochMillis: Long? = null,
    val reminderDaysBefore: Int = 3,
    val reminderEnabled: Boolean = true,
    val notes: String = "",
    val paymentUrlOrMethod: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    val dueDate: LocalDate
        get() = if (dueDateEpochDay > 0) LocalDate.ofEpochDay(dueDateEpochDay) else LocalDate.now()

    fun daysUntilDue(today: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(today, dueDate)
    }

    fun isOverdue(today: LocalDate = LocalDate.now()): Boolean {
        return !isPaid && dueDate.isBefore(today)
    }

    fun isDueSoon(today: LocalDate = LocalDate.now(), daysThreshold: Long = 3): Boolean {
        if (isPaid) return false
        val days = daysUntilDue(today)
        return days in 0..daysThreshold
    }

    fun reminderDate(): LocalDate {
        return dueDate.minusDays(reminderDaysBefore.toLong())
    }
}
