package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserPrivateProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PglobRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveProfile_authenticatedUser_createsPrivateAndPublicDocs() = runBlocking {
        val uid = signInTestUser("alice_pglob@test.com")
        val repo = PglobRepository(firestore)
        val uname = "alice_${UUID.randomUUID().toString().take(6)}"
        val result = withTimeout(5000L) {
            repo.saveCompleteProfile(
                UserPrivateProfile(
                    userId = uid,
                    fullName = "Alice Global",
                    username = uname,
                    country = "Nigeria",
                    countryFlag = "🇳🇬",
                    currency = "₦"
                )
            )
        }
        assertTrue(result.isSuccess)

        val profile = withTimeout(3000L) {
            repo.observeMyProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("Alice Global", profile?.fullName)
    }

    @Test
    fun chatAndMessages_memberCanRead_nonMemberRejected() = runBlocking {
        val aliceUid = signInTestUser("alice_chat@test.com")
        val aliceRepo = PglobRepository(firestore)
        val chatId = "chat_${UUID.randomUUID().toString().take(8)}"

        withTimeout(5000L) {
            aliceRepo.createOrOpenChat(chatId, "Alice Room", listOf(aliceUid)).getOrThrow()
            aliceRepo.sendMessage(chatId, listOf(aliceUid), "Alice", "Hello Earth!").getOrThrow()
        }

        val msgs = withTimeout(3000L) {
            aliceRepo.observeMessages(chatId, aliceUid).first { it.isNotEmpty() }
        }
        assertTrue(msgs.any { it.text == "Hello Earth!" })

        signInTestUser("bob_outsider@test.com")
        var denied = false
        try {
            withTimeout(5000L) {
                firestore.collection("chats").document(chatId).get().await()
            }
        } catch (e: Exception) {
            val fe = e.unwrapFirestoreException()
            denied = fe?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        }
        assertTrue("Expected PERMISSION_DENIED for non-member Bob", denied)
    }

    @Test
    fun observeTransactions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = PglobRepository(firestore)
        var denied = false
        try {
            withTimeout(3000L) {
                repo.observeTransactions("some_uid").first()
            }
        } catch (e: Exception) {
            val fe = e.unwrapFirestoreException()
            denied = fe?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        }
        assertTrue("Expected PERMISSION_DENIED when signed out", denied)
    }
}
