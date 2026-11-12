package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Bill
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BillRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private val guestBills = kotlinx.coroutines.flow.MutableStateFlow<List<Bill>>(
        listOf(
            Bill(
                id = "guest_sample_1",
                userId = "guest_admin_uid",
                title = "Electricity Utility",
                amount = 65.50,
                currency = "$",
                category = "Utilities",
                dueDateEpochDay = java.time.LocalDate.now().plusDays(2).toEpochDay(),
                dueDayOfMonth = java.time.LocalDate.now().plusDays(2).dayOfMonth,
                isRecurring = true,
                frequency = "MONTHLY",
                isPaid = false,
                reminderDaysBefore = 3,
                reminderEnabled = true,
                notes = "Sample bill for Guest mode"
            )
        )
    )

    private fun isGuestMode(userId: String): Boolean {
        return userId.startsWith("guest") || auth.currentUser == null
    }

    private fun requireUserId(expectedUserId: String): String {
        val current = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated with Google")
        if (current != expectedUserId) {
            throw IllegalStateException("User ID mismatch: authenticated as $current, requesting $expectedUserId")
        }
        return current
    }

    fun observeBills(userId: String): Flow<List<Bill>> {
        if (isGuestMode(userId)) {
            return guestBills
        }
        val path = "users/$userId/bills"
        return db.collection("users").document(userId).collection("bills")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Bill::class.java)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createBill(userId: String, bill: Bill): Result<String> {
        if (isGuestMode(userId)) {
            val newId = if (bill.id.isNotBlank()) bill.id else java.util.UUID.randomUUID().toString()
            val newBill = bill.copy(id = newId, userId = userId)
            guestBills.value = guestBills.value + newBill
            return Result.success(newId)
        }
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid).collection("bills").document()
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "title" to bill.title.trim(),
                "amount" to bill.amount,
                "currency" to bill.currency,
                "category" to bill.category,
                "dueDateEpochDay" to bill.dueDateEpochDay,
                "dueDayOfMonth" to bill.dueDayOfMonth,
                "isRecurring" to bill.isRecurring,
                "frequency" to bill.frequency,
                "isPaid" to bill.isPaid,
                "paidAtEpochMillis" to bill.paidAtEpochMillis,
                "reminderDaysBefore" to bill.reminderDaysBefore,
                "reminderEnabled" to bill.reminderEnabled,
                "notes" to bill.notes.trim(),
                "paymentUrlOrMethod" to bill.paymentUrlOrMethod.trim(),
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$userId/bills")
            Result.failure(e)
        }
    }

    suspend fun updateBill(userId: String, bill: Bill): Result<Unit> {
        if (isGuestMode(userId)) {
            guestBills.value = guestBills.value.map { if (it.id == bill.id) bill else it }
            return Result.success(Unit)
        }
        return try {
            val uid = requireUserId(userId)
            require(bill.id.isNotBlank()) { "Bill ID must not be blank for update" }
            val docRef = db.collection("users").document(uid).collection("bills").document(bill.id)

            val updatePayload = mutableMapOf<String, Any?>(
                "title" to bill.title.trim(),
                "amount" to bill.amount,
                "currency" to bill.currency,
                "category" to bill.category,
                "dueDateEpochDay" to bill.dueDateEpochDay,
                "dueDayOfMonth" to bill.dueDayOfMonth,
                "isRecurring" to bill.isRecurring,
                "frequency" to bill.frequency,
                "isPaid" to bill.isPaid,
                "paidAtEpochMillis" to bill.paidAtEpochMillis,
                "reminderDaysBefore" to bill.reminderDaysBefore,
                "reminderEnabled" to bill.reminderEnabled,
                "notes" to bill.notes.trim(),
                "paymentUrlOrMethod" to bill.paymentUrlOrMethod.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.update(updatePayload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$userId/bills/${bill.id}")
            Result.failure(e)
        }
    }

    suspend fun markBillPaid(userId: String, billId: String, isPaid: Boolean): Result<Unit> {
        if (isGuestMode(userId)) {
            guestBills.value = guestBills.value.map {
                if (it.id == billId) {
                    it.copy(
                        isPaid = isPaid,
                        paidAtEpochMillis = if (isPaid) System.currentTimeMillis() else null
                    )
                } else it
            }
            return Result.success(Unit)
        }
        return try {
            val uid = requireUserId(userId)
            require(billId.isNotBlank()) { "Bill ID must not be blank" }
            val docRef = db.collection("users").document(uid).collection("bills").document(billId)
            val updates = mapOf<String, Any?>(
                "isPaid" to isPaid,
                "paidAtEpochMillis" to if (isPaid) System.currentTimeMillis() else null,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$userId/bills/$billId")
            Result.failure(e)
        }
    }

    suspend fun deleteBill(userId: String, billId: String): Result<Unit> {
        if (isGuestMode(userId)) {
            guestBills.value = guestBills.value.filterNot { it.id == billId }
            return Result.success(Unit)
        }
        return try {
            val uid = requireUserId(userId)
            require(billId.isNotBlank()) { "Bill ID must not be blank" }
            val docRef = db.collection("users").document(uid).collection("bills").document(billId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId/bills/$billId")
            Result.failure(e)
        }
    }
}
