package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.DatingMatch
import com.example.data.model.JobListing
import com.example.data.model.LiveRoom
import com.example.data.model.MarketProduct
import com.example.data.model.PayTransaction
import com.example.data.model.PublicProfile
import com.example.data.model.ReelPost
import com.example.data.model.SafetyReport
import com.example.data.model.StatusNote
import com.example.data.model.StatusNoteActor
import com.example.data.model.StatusPost
import com.example.data.model.UserPrivateProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class PglobRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeMyProfile(uid: String): Flow<UserPrivateProfile?> = flow {
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(
                            UserPrivateProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    logFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observePublicProfiles(): Flow<List<PublicProfile>> = flow {
        val uid = requireUserId()
        val path = "publicProfiles"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        PublicProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeChats(uid: String): Flow<List<ChatThread>> = flow {
        val path = "chats"
        emitAll(
            db.collection(path)
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ChatThread::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<ChatThread> { it.isPinned }
                            .thenByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                    )
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMessages(chatId: String, uid: String): Flow<List<ChatMessage>> = flow {
        val path = "chats/$chatId/messages"
        emitAll(
            db.collection("chats").document(chatId).collection("messages")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeTransactions(uid: String): Flow<List<PayTransaction>> = flow {
        val path = "transactions"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        PayTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun fetchTransactionsFromFirestore(uid: String): Result<List<PayTransaction>> {
        val activeUid = auth.currentUser?.uid ?: uid
        val path = "transactions"
        return try {
            val snap = db.collection(path)
                .whereEqualTo("userId", activeUid)
                .get()
                .await()
            val list = snap.toObjects(
                PayTransaction::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    fun observeMarketProducts(): Flow<List<MarketProduct>> = flow {
        val uid = requireUserId()
        val path = "marketProducts"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        MarketProduct::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeStatuses(): Flow<List<StatusPost>> = flow {
        val uid = requireUserId()
        val path = "statuses"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        StatusPost::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeStatusNotes(uid: String): Flow<List<StatusNote>> = flow {
        val activeUid = auth.currentUser?.uid ?: uid
        val path = "statusNotes"
        val publicFlow = db.collection(path)
            .whereEqualTo("isPublic", true)
            .snapshots()
            .map { snap ->
                snap.toObjects(StatusNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logFlowError(error, OperationType.LIST, path, activeUid)
                emit(emptyList())
            }
        val ownFlow = db.collection(path)
            .whereEqualTo("userId", activeUid)
            .snapshots()
            .map { snap ->
                snap.toObjects(StatusNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logFlowError(error, OperationType.LIST, path, activeUid)
                emit(emptyList())
            }
        val customFlow = db.collection(path)
            .whereArrayContains("customAllowedUids", activeUid)
            .snapshots()
            .map { snap ->
                snap.toObjects(StatusNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logFlowError(error, OperationType.LIST, path, activeUid)
                emit(emptyList())
            }
        emitAll(
            combine(publicFlow, ownFlow, customFlow) { pub, own, custom ->
                (own + custom + pub)
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAtMillisEffective() }
            }
        )
    }

    fun observeReels(): Flow<List<ReelPost>> = flow {
        val uid = requireUserId()
        val path = "reels"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ReelPost::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeJobs(): Flow<List<JobListing>> = flow {
        val uid = requireUserId()
        val path = "jobs"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        JobListing::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeLiveRooms(): Flow<List<LiveRoom>> = flow {
        val uid = requireUserId()
        val path = "liveRooms"
        emitAll(
            db.collection(path)
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        LiveRoom::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.viewerCount }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeDatingMatches(uid: String): Flow<List<DatingMatch>> = flow {
        val path = "datingMatches"
        emitAll(
            db.collection(path)
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        DatingMatch::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMyReports(uid: String): Flow<List<SafetyReport>> = flow {
        val path = "reports"
        emitAll(
            db.collection(path)
                .whereEqualTo("reporterId", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        SafetyReport::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun checkUsernameAvailable(username: String): Boolean {
        val clean = username.trim().lowercase()
        if (clean.length < 3) return false
        val docRef = db.collection("usernames").document(clean)
        return try {
            val snap = docRef.get().await()
            !snap.exists() || snap.getString("userId") == auth.currentUser?.uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            true
        }
    }

    suspend fun saveCompleteProfile(
        profile: UserPrivateProfile,
        age: Int = 24,
        gender: String = "Female",
        lookingFor: String = "Both",
        bio: String = "",
        interests: List<String> = emptyList(),
        city: String = "Lagos",
        goal: String = "Serious"
    ): Result<Unit> {
        val uid = requireUserId()
        val cleanUsername = profile.username.trim().lowercase().replace(Regex("[^a-z0-9_]"), "").ifBlank { "user_${uid.take(6)}" }
        val accountNum = profile.payAccountNumber.ifBlank { (1000000000L + (uid.hashCode().toLong() and 0x7FFFFFFFL) % 8999999999L).toString() }
        val resolvedBio = (bio.ifBlank { profile.bio }).trim().take(300)
        val resolvedInterests = (if (interests.isNotEmpty()) interests else profile.interests).take(15)
        val finalProfile = profile.copy(
            userId = uid,
            username = cleanUsername,
            bio = resolvedBio,
            interests = resolvedInterests,
            payAccountNumber = accountNum
        )

        return try {
            val userRef = db.collection("users").document(uid)
            val existingUser = userRef.get().await()
            if (existingUser.exists()) {
                val updateMap = mapOf(
                    "fullName" to finalProfile.fullName.ifBlank { "PGLOB Member" },
                    "username" to cleanUsername,
                    "usernameVerified" to finalProfile.usernameVerified,
                    "phone" to finalProfile.phone,
                    "email" to finalProfile.email,
                    "country" to finalProfile.country,
                    "countryFlag" to finalProfile.countryFlag,
                    "currency" to finalProfile.currency,
                    "language" to finalProfile.language,
                    "about" to finalProfile.about,
                    "bio" to resolvedBio,
                    "interests" to resolvedInterests,
                    "photoUri" to finalProfile.photoUri.take(60000),
                    "coins" to finalProfile.coins,
                    "diamonds" to finalProfile.diamonds,
                    "walletBalance" to finalProfile.walletBalance,
                    "payAccountNumber" to accountNum,
                    "transactionPin" to finalProfile.transactionPin,
                    "vipTier" to finalProfile.vipTier,
                    "datingEnabled" to finalProfile.datingEnabled,
                    "fingerprintEnabled" to finalProfile.fingerprintEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(updateMap, SetOptions.merge()).await()
            } else {
                userRef.set(finalProfile.toCreateMap()).await()
            }

            val pubRef = db.collection("publicProfiles").document(uid)
            val existingPub = pubRef.get().await()
            val pubMap = mutableMapOf<String, Any>(
                "userId" to uid,
                "displayName" to finalProfile.fullName.ifBlank { "PGLOB Member" },
                "username" to cleanUsername,
                "usernameVerified" to finalProfile.usernameVerified,
                "country" to finalProfile.country,
                "countryFlag" to finalProfile.countryFlag,
                "city" to city,
                "language" to finalProfile.language,
                "about" to finalProfile.about,
                "photoUri" to finalProfile.photoUri.take(60000),
                "coins" to finalProfile.coins,
                "vipTier" to finalProfile.vipTier,
                "isOnline" to true,
                "isKingQueen" to false,
                "isPublic" to true,
                "datingEnabled" to finalProfile.datingEnabled,
                "age" to age.coerceIn(18, 120),
                "gender" to gender,
                "lookingFor" to lookingFor,
                "bio" to resolvedBio.ifBlank { finalProfile.about },
                "interests" to resolvedInterests,
                "relationshipGoal" to goal,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existingPub.exists()) {
                pubMap["createdAt"] = FieldValue.serverTimestamp()
            }
            pubRef.set(pubMap, SetOptions.merge()).await()

            val unameRef = db.collection("usernames").document(cleanUsername)
            if (!unameRef.get().await().exists()) {
                unameRef.set(
                    mapOf(
                        "userId" to uid,
                        "username" to cleanUsername,
                        "isPublic" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun updateCoinsAndWallet(
        coinsDelta: Long = 0L,
        diamondsDelta: Long = 0L,
        walletDelta: Double = 0.0,
        vipTier: String? = null
    ): Result<Unit> {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid)
        return try {
            val snap = ref.get().await()
            val currentCoins = snap.getLong("coins") ?: 100L
            val currentDiamonds = snap.getLong("diamonds") ?: 1L
            val currentWallet = snap.getDouble("walletBalance") ?: 0.0
            val newCoins = (currentCoins + coinsDelta).coerceAtLeast(0L)
            val newDiamonds = (currentDiamonds + diamondsDelta).coerceAtLeast(0L)
            val newWallet = (currentWallet + walletDelta).coerceAtLeast(0.0)
            val update = mutableMapOf<String, Any>(
                "coins" to newCoins,
                "diamonds" to newDiamonds,
                "walletBalance" to newWallet,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (vipTier != null) update["vipTier"] = vipTier
            ref.set(update, SetOptions.merge()).await()

            val pubUpdate = mutableMapOf<String, Any>(
                "coins" to newCoins,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (vipTier != null) pubUpdate["vipTier"] = vipTier
            db.collection("publicProfiles").document(uid).set(pubUpdate, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun createOrOpenChat(
        chatId: String,
        title: String,
        memberUids: List<String>,
        isGroup: Boolean = false,
        peerFlag: String = "🌍"
    ): Result<String> {
        val uid = requireUserId()
        val finalMembers = (memberUids + uid).distinct()
        val docRef = db.collection("chats").document(chatId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "title" to title.take(100),
                        "isGroup" to isGroup,
                        "isArchived" to false,
                        "isPinned" to false,
                        "isMuted" to false,
                        "lastMessage" to "Tap to start chatting on PGLOB CHAT",
                        "lastSenderId" to uid,
                        "unreadCount" to 0,
                        "memberUids" to finalMembers,
                        "peerFlag" to peerFlag,
                        "peerOnline" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(chatId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateChatFlags(
        chatId: String,
        isArchived: Boolean? = null,
        isPinned: Boolean? = null,
        isMuted: Boolean? = null
    ): Result<Unit> {
        requireUserId()
        val docRef = db.collection("chats").document(chatId)
        return try {
            val map = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            if (isArchived != null) map["isArchived"] = isArchived
            if (isPinned != null) map["isPinned"] = isPinned
            if (isMuted != null) map["isMuted"] = isMuted
            docRef.update(map).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("chats").document(chatId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        chatId: String,
        memberUids: List<String>,
        senderName: String,
        text: String,
        messageType: String = "TEXT",
        mediaUri: String = "",
        amount: Double = 0.0,
        giftName: String = "",
        replyToText: String = "",
        messageId: String = "msg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    ): Result<Unit> {
        val uid = requireUserId()
        val finalMembers = (memberUids + uid).distinct()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        val chatRef = db.collection("chats").document(chatId)
        return try {
            val payload = mutableMapOf<String, Any>(
                "chatId" to chatId,
                "senderId" to uid,
                "senderName" to senderName.take(80),
                "memberUids" to finalMembers,
                "text" to text.take(2000),
                "messageType" to messageType,
                "mediaUri" to mediaUri.take(500),
                "amount" to amount,
                "giftName" to giftName.take(50),
                "replyToText" to replyToText.take(500),
                "reaction" to "",
                "isRead" to true,
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(payload).await()
            chatRef.update(
                mapOf(
                    "lastMessage" to text.take(400),
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun reactToMessage(chatId: String, messageId: String, reaction: String): Result<Unit> {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        return try {
            msgRef.update("reaction", reaction).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        requireUserId()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        return try {
            msgRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordTransaction(
        txType: String,
        amount: Double,
        fee: Double,
        currency: String,
        method: String,
        destination: String,
        status: String,
        note: String,
        counterpartyId: String = ""
    ): Result<String> {
        val uid = requireUserId()
        val txId = "tx_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val ref = db.collection("transactions").document(txId)
        return try {
            val map = mutableMapOf<String, Any>(
                "userId" to uid,
                "counterpartyId" to counterpartyId,
                "txType" to txType,
                "amount" to amount,
                "fee" to fee,
                "currency" to currency,
                "method" to method.take(60),
                "destination" to destination.take(120),
                "status" to status,
                "note" to note.take(200),
                "createdAt" to FieldValue.serverTimestamp()
            )
            ref.set(map).await()
            Result.success(txId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun postOrReplaceStatusNote(note: StatusNote): Result<StatusNote> {
        val uid = requireUserId()
        val noteId = note.id.ifBlank { "note_${UUID.randomUUID().toString().replace("-", "").take(14)}" }
        val ref = db.collection("statusNotes").document(noteId)
        val hours = if (note.durationHours == 48) 48 else 24
        val nowMs = System.currentTimeMillis()
        val expTs = note.expiresAt ?: Timestamp(Date(nowMs + hours * 3_600_000L))
        return try {
            // 1 active Note per user rule: delete any prior active/expired note(s) owned by uid
            val existing = db.collection("statusNotes")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            existing.documents.forEach { doc ->
                if (doc.id != noteId) {
                    doc.reference.delete().await()
                }
            }
            val payload = mapOf(
                "userId" to uid,
                "authorName" to note.authorName.ifBlank { "PGLOB Member" }.take(80),
                "authorAvatarUri" to note.authorAvatarUri.take(500),
                "text" to note.text.take(100),
                "bgColor" to note.bgColor,
                "music" to note.music.take(80),
                "mood" to note.mood.take(40),
                "privacy" to note.privacy,
                "isPublic" to (note.privacy != "CUSTOM"),
                "customAllowedUids" to note.customAllowedUids.take(100),
                "durationHours" to hours,
                "likes" to emptyList<Map<String, Any>>(),
                "reshares" to emptyList<Map<String, Any>>(),
                "viewers" to emptyList<Map<String, Any>>(),
                "resharedFromNoteId" to note.resharedFromNoteId.take(128),
                "resharedFromUid" to note.resharedFromUid.take(128),
                "resharedFromName" to note.resharedFromName.take(80),
                "resharedFromAvatarUri" to note.resharedFromAvatarUri.take(500),
                "resharedFromText" to note.resharedFromText.take(120),
                "edited" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "expiresAt" to expTs
            )
            ref.set(payload).await()
            Result.success(note.copy(id = noteId, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun editStatusNote(
        noteId: String,
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int,
        expiresAt: Timestamp
    ): Result<Unit> {
        requireUserId()
        val ref = db.collection("statusNotes").document(noteId)
        val hours = if (durationHours == 48) 48 else 24
        return try {
            ref.update(
                mapOf(
                    "text" to text.take(100),
                    "bgColor" to bgColor,
                    "music" to music.take(80),
                    "mood" to mood.take(40),
                    "privacy" to privacy,
                    "isPublic" to (privacy != "CUSTOM"),
                    "customAllowedUids" to customAllowedUids.take(100),
                    "durationHours" to hours,
                    "edited" to true,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "expiresAt" to expiresAt
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun updateStatusNoteInteractions(
        noteId: String,
        likes: List<StatusNoteActor>? = null,
        reshares: List<StatusNoteActor>? = null,
        viewers: List<StatusNoteActor>? = null
    ): Result<Unit> {
        requireUserId()
        val ref = db.collection("statusNotes").document(noteId)
        val map = mutableMapOf<String, Any>()
        if (likes != null) map["likes"] = likes.take(500).map { actorToMap(it) }
        if (reshares != null) map["reshares"] = reshares.take(500).map { actorToMap(it) }
        if (viewers != null) map["viewers"] = viewers.take(500).map { actorToMap(it) }
        if (map.isEmpty()) return Result.success(Unit)
        return try {
            ref.update(map).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStatusNote(noteId: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("statusNotes").document(noteId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    private fun actorToMap(actor: StatusNoteActor): Map<String, Any> = mapOf(
        "uid" to actor.uid,
        "name" to actor.name.take(80),
        "photoUri" to actor.photoUri.take(500),
        "timestampMillis" to actor.timestampMillis
    )

    suspend fun postStatus(
        displayName: String,
        username: String = "",
        countryFlag: String,
        mediaType: String,
        caption: String,
        mediaUri: String = "",
        isPublic: Boolean = true,
        privacyMode: String = "PUBLIC",
        resharedFromName: String = "",
        resharedFromHandle: String = "",
        originalStatusId: String = ""
    ): Result<String> {
        val uid = requireUserId()
        val id = "st_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("statuses").document(id)
        return try {
            ref.set(
                mapOf(
                    "userId" to uid,
                    "displayName" to displayName.take(80),
                    "username" to username.take(30),
                    "countryFlag" to countryFlag,
                    "mediaType" to mediaType.take(20),
                    "caption" to caption.take(300),
                    "mediaUri" to mediaUri.take(500),
                    "isPublic" to isPublic,
                    "privacyMode" to privacyMode,
                    "resharedFromName" to resharedFromName.take(80),
                    "resharedFromHandle" to resharedFromHandle.take(40),
                    "originalStatusId" to originalStatusId.take(128),
                    "likesCount" to 0,
                    "reshareCount" to 0,
                    "viewsCount" to 1,
                    "timeLabel" to "Just now",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun postMarketProduct(
        sellerName: String,
        sellerFlag: String,
        sellerVerified: Boolean,
        title: String,
        priceCoins: Int,
        priceCash: Double,
        category: String,
        description: String,
        location: String,
        imageUri: String = ""
    ): Result<Unit> {
        val uid = requireUserId()
        val id = "prod_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("marketProducts").document(id)
        return try {
            ref.set(
                mapOf(
                    "sellerId" to uid,
                    "sellerName" to sellerName.take(80),
                    "sellerFlag" to sellerFlag,
                    "sellerVerified" to sellerVerified,
                    "title" to title.take(120),
                    "priceCoins" to priceCoins,
                    "priceCash" to priceCash,
                    "category" to category.take(50),
                    "description" to description.take(500),
                    "location" to location.take(80),
                    "imageUri" to imageUri.take(500),
                    "approved" to true,
                    "isPublic" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun postReel(authorName: String, caption: String, hashtag: String, music: String): Result<Unit> {
        val uid = requireUserId()
        val id = "reel_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("reels").document(id)
        return try {
            ref.set(
                mapOf(
                    "authorId" to uid,
                    "authorName" to authorName.take(80),
                    "caption" to caption.take(300),
                    "hashtag" to hashtag.take(80),
                    "music" to music.take(80),
                    "likes" to 1,
                    "views" to 1,
                    "isPublic" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun likeReel(reel: ReelPost): Result<Unit> {
        requireUserId()
        val ref = db.collection("reels").document(reel.id)
        return try {
            ref.update("likes", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun postJob(title: String, company: String, salary: String, jobType: String, location: String, description: String): Result<Unit> {
        val uid = requireUserId()
        val id = "job_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("jobs").document(id)
        return try {
            ref.set(
                mapOf(
                    "posterId" to uid,
                    "title" to title.take(120),
                    "company" to company.take(100),
                    "salary" to salary.take(60),
                    "jobType" to jobType.take(40),
                    "location" to location.take(80),
                    "description" to description.take(500),
                    "approved" to true,
                    "isPublic" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun createLiveRoom(hostName: String, title: String, category: String, mode: String, countryFlag: String): Result<String> {
        val uid = requireUserId()
        val id = "room_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("liveRooms").document(id)
        return try {
            ref.set(
                mapOf(
                    "hostId" to uid,
                    "hostName" to hostName.take(80),
                    "title" to title.take(120),
                    "category" to category.take(50),
                    "mode" to mode,
                    "countryFlag" to countryFlag,
                    "viewerCount" to 1,
                    "isLive" to true,
                    "isPublic" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun swipeDatingProfile(targetProfile: PublicProfile, action: String): Result<Boolean> {
        val uid = requireUserId()
        val swipeId = "${uid}_${targetProfile.userId}".take(120)
        val swipeRef = db.collection("datingSwipes").document(swipeId)
        return try {
            swipeRef.set(
                mapOf(
                    "fromUid" to uid,
                    "toUid" to targetProfile.userId,
                    "action" to action,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (action == "LIKE" || action == "SUPER_LIKE") {
                val matchId = listOf(uid, targetProfile.userId).sorted().joinToString("_").take(120)
                val chatId = "match_$matchId".take(120)
                db.collection("datingMatches").document(matchId).set(
                    mapOf(
                        "memberUids" to listOf(uid, targetProfile.userId),
                        "chatId" to chatId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                createOrOpenChat(
                    chatId = chatId,
                    title = "${targetProfile.displayName} ${targetProfile.countryFlag}",
                    memberUids = listOf(uid, targetProfile.userId),
                    peerFlag = targetProfile.countryFlag
                )
                return Result.success(true)
            }
            Result.success(false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, swipeRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(targetType: String, targetId: String, reason: String): Result<Unit> {
        val uid = requireUserId()
        val id = "rep_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val ref = db.collection("reports").document(id)
        return try {
            ref.set(
                mapOf(
                    "reporterId" to uid,
                    "targetType" to targetType.take(40),
                    "targetId" to targetId.take(128),
                    "reason" to reason.take(300),
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun seedInitialEcosystemIfNeeded(uid: String, displayName: String) {
        try {
            val welcomeChatRef = db.collection("chats").document("welcome_$uid")
            if (!welcomeChatRef.get().await().exists()) {
                createOrOpenChat(
                    chatId = "welcome_$uid",
                    title = "PGLOB Global Concierge 🌍",
                    memberUids = listOf(uid, "pglob_concierge"),
                    isGroup = false,
                    peerFlag = "🌍"
                )
                sendMessage(
                    chatId = "welcome_$uid",
                    memberUids = listOf(uid, "pglob_concierge"),
                    senderName = "PGLOB Concierge",
                    text = "Welcome to PGLOB APP, $displayName! You earned +100 coins for joining. Tap the 3-dot menu for Chat Wallpapers or PGLOB AI!"
                )
            }

            val seedChats = listOf(
                Triple("lounge_global_$uid", "World Connect Lounge 🌍", "Amina: Hello from Lagos! Anyone live?"),
                Triple("pay_support_$uid", "PGLOB Pay Alerts 💳", "Your PGLOB Virtual Account is ready for instant transfers.")
            )
            seedChats.forEach { (id, title, msg) ->
                val ref = db.collection("chats").document(id)
                if (!ref.get().await().exists()) {
                    createOrOpenChat(id, title, listOf(uid, "pglob_system"), isGroup = id.startsWith("lounge"), peerFlag = "🌍")
                    sendMessage(id, listOf(uid, "pglob_system"), "PGLOB System", msg)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seedInitialEcosystem")
        }
    }

    suspend fun fetchAdminUsers(): List<UserPrivateProfile> {
        val uid = requireUserId()
        return try {
            db.collection("users").get().await()
                .toObjects(UserPrivateProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "users")
            emptyList()
        }
    }

    suspend fun adminSetUserBanned(targetUid: String, banned: Boolean): Result<Unit> {
        requireUserId()
        val ref = db.collection("users").document(targetUid)
        return try {
            ref.update(mapOf("isBanned" to banned, "updatedAt" to FieldValue.serverTimestamp())).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun adminGrantCoins(targetUid: String, deltaCoins: Long): Result<Unit> {
        requireUserId()
        val ref = db.collection("users").document(targetUid)
        return try {
            val snap = ref.get().await()
            val cur = snap.getLong("coins") ?: 0L
            val next = (cur + deltaCoins).coerceAtLeast(0L)
            ref.update(mapOf("coins" to next, "updatedAt" to FieldValue.serverTimestamp())).await()
            db.collection("publicProfiles").document(targetUid)
                .set(mapOf("coins" to next, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun fetchAdminTransactions(): List<PayTransaction> {
        requireUserId()
        return try {
            db.collection("transactions").get().await()
                .toObjects(PayTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "transactions")
            emptyList()
        }
    }

    suspend fun adminUpdateTransactionStatus(txId: String, status: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("transactions").document(txId)
        return try {
            ref.update("status", status).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun fetchAdminReports(): List<SafetyReport> {
        requireUserId()
        return try {
            db.collection("reports").get().await()
                .toObjects(SafetyReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "reports")
            emptyList()
        }
    }

    suspend fun adminUpdateReportStatus(reportId: String, status: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("reports").document(reportId)
        return try {
            ref.update("status", status).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMarketProduct(productId: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("marketProducts").document(productId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteJobListing(jobId: String): Result<Unit> {
        requireUserId()
        val ref = db.collection("jobs").document(jobId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
