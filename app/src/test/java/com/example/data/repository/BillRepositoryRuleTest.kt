package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Bill
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BillRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBill_validData_succeedsAndReturnsId(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BillRepository(firestore)

        val bill = Bill(
            userId = aliceUid,
            title = "Electric Utility",
            amount = 95.0,
            dueDateEpochDay = 20050,
            dueDayOfMonth = 15,
            isRecurring = true,
            frequency = "MONTHLY",
            isPaid = false,
            reminderDaysBefore = 3,
            reminderEnabled = true,
            notes = "Test note"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBill(aliceUid, bill)
        }
        assertTrue("Expected createBill to succeed", result.isSuccess)
        val billId = result.getOrThrow()
        assertTrue("Expected non-empty bill ID", billId.isNotEmpty())
    }

    @Test
    fun observeBills_authenticatedOwner_emitsBills(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BillRepository(firestore)

        val uniqueTitle = "Internet Bill ${UUID.randomUUID()}"
        val bill = Bill(
            userId = aliceUid,
            title = uniqueTitle,
            amount = 65.0,
            dueDateEpochDay = 20055,
            isPaid = false,
            reminderDaysBefore = 2
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBill(aliceUid, bill)
        }
        assertTrue(createResult.isSuccess)
        val createdId = createResult.getOrThrow()

        val emittedBills = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBills(aliceUid).first { list ->
                list.any { it.id == createdId }
            }
        }
        assertTrue(emittedBills.any { it.id == createdId })
    }

    @Test
    fun markBillPaid_authenticatedOwner_updatesSuccessfully(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BillRepository(firestore)

        val bill = Bill(
            userId = aliceUid,
            title = "Water Utility",
            amount = 40.0,
            dueDateEpochDay = 20060,
            isPaid = false,
            reminderDaysBefore = 1
        )

        val createdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBill(aliceUid, bill).getOrThrow()
        }

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.markBillPaid(aliceUid, createdId, true)
        }
        assertTrue("Update failed: ${updateResult.exceptionOrNull()?.message}", updateResult.isSuccess)

        val doc = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("users").document(aliceUid).collection("bills").document(createdId).get().await()
        }
        val updatedBill = doc.toObject(Bill::class.java)
        assertNotNull(updatedBill)
        assertTrue(updatedBill!!.isPaid)
    }

    @Test
    fun crossUserAccess_bobCannotDeleteAliceBill(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BillRepository(firestore)

        val bill = Bill(
            userId = aliceUid,
            title = "Alice Secret Bill",
            amount = 120.0,
            dueDateEpochDay = 20070,
            isPaid = false,
            reminderDaysBefore = 5
        )

        val billId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createBill(aliceUid, bill).getOrThrow()
        }

        // Bob signs in and attempts to delete Alice's bill
        signInTestUser(BOB_EMAIL)
        val bobRepo = BillRepository(firestore)

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.deleteBill(aliceUid, billId)
        }
        assertTrue("Expected delete from another user to fail", deleteResult.isFailure)
    }

    @Test
    fun observeBills_unauthenticated_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        val repository = BillRepository(firestore)

        val exception = assertThrows(Exception::class.java) {
            runBlocking {
                withTimeout(FLOW_TIMEOUT_MS) {
                    repository.observeBills("fake_unauth_uid").first()
                }
            }
        }
        val firestoreException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
