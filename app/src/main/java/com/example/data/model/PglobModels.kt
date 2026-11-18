package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserPrivateProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val usernameVerified: Boolean = false,
    val phone: String = "",
    val email: String = "",
    val country: String = "Nigeria",
    val countryFlag: String = "🇳🇬",
    val currency: String = "₦",
    val language: String = "English",
    val about: String = "Hey there! I am using PGLOB CHAT.",
    val bio: String = "",
    val interests: List<String> = emptyList(),
    val photoUri: String = "",
    val coins: Long = 100L,
    val diamonds: Long = 1L,
    val walletBalance: Double = 0.0,
    val payAccountNumber: String = "",
    val transactionPin: String = "",
    val vipTier: String = "FREE",
    val datingEnabled: Boolean = false,
    val fingerprintEnabled: Boolean = false,
    val isBanned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "fullName" to fullName.ifBlank { "PGLOB Member" },
        "username" to username,
        "usernameVerified" to usernameVerified,
        "phone" to phone,
        "email" to email,
        "country" to country,
        "countryFlag" to countryFlag,
        "currency" to currency,
        "language" to language,
        "about" to about,
        "bio" to bio.take(300),
        "interests" to interests.take(15),
        "photoUri" to photoUri,
        "coins" to coins,
        "diamonds" to diamonds,
        "walletBalance" to walletBalance,
        "payAccountNumber" to payAccountNumber,
        "transactionPin" to transactionPin,
        "vipTier" to vipTier,
        "datingEnabled" to datingEnabled,
        "fingerprintEnabled" to fingerprintEnabled,
        "isBanned" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val usernameVerified: Boolean = false,
    val country: String = "Nigeria",
    val countryFlag: String = "🇳🇬",
    val city: String = "Lagos",
    val language: String = "English",
    val about: String = "Hey there! I am using PGLOB CHAT.",
    val photoUri: String = "",
    val coins: Long = 100L,
    val vipTier: String = "FREE",
    val isOnline: Boolean = true,
    val isKingQueen: Boolean = false,
    val isPublic: Boolean = true,
    val datingEnabled: Boolean = false,
    val age: Int = 24,
    val gender: String = "Female",
    val lookingFor: String = "Both",
    val bio: String = "",
    val interests: List<String> = emptyList(),
    val relationshipGoal: String = "Serious",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatThread(
    @DocumentId val id: String = "",
    val title: String = "",
    val isGroup: Boolean = false,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val unreadCount: Int = 0,
    val memberUids: List<String> = emptyList(),
    val peerFlag: String = "🌍",
    val peerOnline: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val memberUids: List<String> = emptyList(),
    val text: String = "",
    val messageType: String = "TEXT",
    val mediaUri: String = "",
    val amount: Double = 0.0,
    val giftName: String = "",
    val replyToText: String = "",
    val reaction: String = "",
    val isRead: Boolean = false,
    val isPendingOffline: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PayTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val counterpartyId: String = "",
    val txType: String = "SEND",
    val amount: Double = 0.0,
    val fee: Double = 0.0,
    val currency: String = "₦",
    val method: String = "PGLOB Pay",
    val destination: String = "",
    val status: String = "PAID",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class OrderDeliveryStatus(val label: String) {
    PENDING("Pending"),
    ESCROWED("Escrowed"),
    DELIVERED("Delivered")
}

data class OrderHistoryRecord(
    val id: String,
    val orderReference: String,
    val title: String,
    val counterpartyName: String,
    val counterpartyId: String = "",
    val txType: String,
    val amount: Double,
    val fee: Double,
    val currency: String,
    val paymentMethod: String,
    val deliveryStatus: OrderDeliveryStatus,
    val rawStatus: String,
    val note: String,
    val timestampMillis: Long,
    val dateLabel: String
)

@IgnoreExtraProperties
data class MarketProduct(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerFlag: String = "🇳🇬",
    val sellerVerified: Boolean = false,
    val title: String = "",
    val priceCoins: Int = 0,
    val priceCash: Double = 0.0,
    val category: String = "Phones",
    val description: String = "",
    val location: String = "Lagos",
    val imageUri: String = "",
    val approved: Boolean = true,
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StatusPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val countryFlag: String = "🌍",
    val mediaType: String = "TEXT",
    val caption: String = "",
    val mediaUri: String = "",
    val avatarUri: String = "",
    val isPublic: Boolean = true,
    val privacyMode: String = "PUBLIC", // PUBLIC, CONTACTS, PRIVATE
    val resharedFromName: String = "",
    val resharedFromHandle: String = "",
    val originalStatusId: String = "",
    val likesCount: Int = 0,
    val reshareCount: Int = 0,
    val viewsCount: Int = 0,
    val timeLabel: String = "2m ago",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class StatusViewerRecord(
    val uid: String,
    val name: String,
    val photoUri: String = "",
    val reaction: String = "",
    val isLiked: Boolean = false,
    val viewedAt: String = "Just now"
)

@Keep
@IgnoreExtraProperties
data class StatusNoteActor(
    val uid: String = "",
    val name: String = "",
    val photoUri: String = "",
    val timestampMillis: Long = 0L
)

@Keep
@IgnoreExtraProperties
data class StatusNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUri: String = "",
    val text: String = "",
    val bgColor: String = "#00A884",
    val music: String = "",
    val mood: String = "",
    val privacy: String = "EVERYONE", // EVERYONE, CONTACTS, AJO_GROUP, CUSTOM
    @get:PropertyName("isPublic")
    @PropertyName("isPublic")
    val isPublic: Boolean = true,
    val customAllowedUids: List<String> = emptyList(),
    val durationHours: Int = 24,
    val likes: List<StatusNoteActor> = emptyList(),
    val reshares: List<StatusNoteActor> = emptyList(),
    val viewers: List<StatusNoteActor> = emptyList(),
    val resharedFromNoteId: String = "",
    val resharedFromUid: String = "",
    val resharedFromName: String = "",
    val resharedFromAvatarUri: String = "",
    val resharedFromText: String = "",
    val edited: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
) {
    fun createdAtMillisEffective(): Long = createdAt?.toDate()?.time ?: 0L
    fun updatedAtMillisEffective(): Long = updatedAt?.toDate()?.time ?: createdAtMillisEffective()
    fun expiresAtMillisEffective(): Long =
        expiresAt?.toDate()?.time ?: (createdAtMillisEffective() + durationHours * 3_600_000L)

    fun isExpiredAt(nowMillis: Long = System.currentTimeMillis()): Boolean =
        expiresAtMillisEffective() <= nowMillis
}

@IgnoreExtraProperties
data class ReelPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val caption: String = "",
    val hashtag: String = "#pglob",
    val music: String = "Original Sound",
    val likes: Int = 0,
    val views: Int = 0,
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class JobListing(
    @DocumentId val id: String = "",
    val posterId: String = "",
    val title: String = "",
    val company: String = "",
    val salary: String = "",
    val jobType: String = "Remote",
    val location: String = "Global",
    val description: String = "",
    val approved: Boolean = true,
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LiveRoom(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val title: String = "",
    val category: String = "Music",
    val mode: String = "AUDIO",
    val countryFlag: String = "🌍",
    val viewerCount: Int = 1,
    val isLive: Boolean = true,
    val isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DatingMatch(
    @DocumentId val id: String = "",
    val memberUids: List<String> = emptyList(),
    val chatId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SafetyReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "USER",
    val targetId: String = "",
    val reason: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CountryInfo(
    val name: String,
    val flag: String,
    val dialCode: String,
    val currency: String
)

data class GiftItem(
    val name: String,
    val emoji: String,
    val costCoins: Long
)

data class ChannelItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val isVoice: Boolean = false
)

data class CommunityItem(
    val id: String,
    val name: String,
    val announcement: String,
    val announcementTime: String,
    val groupName: String,
    val groupMessage: String,
    val groupTime: String,
    val hasMention: Boolean = false
)

data class CallLogItem(
    val id: String,
    val name: String,
    val flag: String,
    val isVideo: Boolean,
    val isMissed: Boolean,
    val time: String
)

data class LearnCourse(
    val id: String,
    val title: String,
    val instructor: String,
    val duration: String,
    val priceCoins: Int,
    val rewardCoins: Int,
    val lessons: List<String>,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctIndex: Int
)
