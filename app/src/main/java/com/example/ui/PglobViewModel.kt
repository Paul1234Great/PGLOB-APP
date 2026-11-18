package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.LocalAppSettings
import com.example.data.local.LocalSettingsStore
import com.example.data.local.PendingMessageDao
import com.example.data.local.PendingMessageEntity
import com.example.data.local.PglobDatabase
import com.example.data.model.AdminActivityLog
import com.example.data.model.AppAdminConfig
import com.example.data.model.AppAdminPermission
import com.example.data.model.AppSubAdmin
import com.example.data.model.BroadcastAnnouncement
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.ContributionAuditEntry
import com.example.data.model.ContributionGroup
import com.example.data.model.ContributionMember
import com.example.data.model.ContributionRoundHistory
import com.example.data.model.ContributionStatus
import com.example.data.model.ContributionType
import com.example.data.model.DatingMatch
import com.example.data.model.GroupSubAdminPermissions
import com.example.data.model.JobListing
import com.example.data.model.LiveRoom
import com.example.data.model.MarketProduct
import com.example.data.model.OrderDeliveryStatus
import com.example.data.model.OrderHistoryRecord
import com.example.data.model.PayTransaction
import com.example.data.model.PayoutOrderType
import com.example.data.model.PublicProfile
import com.example.data.model.ReelPost
import com.example.data.model.SafetyReport
import com.example.data.model.StatusInteractionTracker
import com.example.data.model.StatusNote
import com.example.data.model.StatusNoteActor
import com.example.data.model.StatusPost
import com.example.data.model.StatusViewerRecord
import com.example.data.model.UserPrivateProfile
import com.example.data.repository.AiRepository
import com.example.data.repository.PglobRepository
import com.example.util.ContributionCodec
import com.example.util.InAppNotificationItem
import com.example.util.LinkedDeviceSession
import com.example.util.NetworkMonitor
import com.example.util.PdfReceiptGenerator
import com.example.util.PglobNotificationHelper
import com.example.util.ProfileCodec
import com.example.util.SecurityAndNumberCodec
import com.example.util.StatusNoteCodec
import com.example.util.StatusQuoteCodec
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val text: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

sealed interface MarketCheckoutOutcome {
    data object Idle : MarketCheckoutOutcome
    data class Success(
        val orderNumber: String,
        val productTitle: String,
        val sellerName: String,
        val quantity: Int,
        val deliveryOption: String,
        val paymentMethod: String,
        val paidSummary: String,
        val escrowHoldSummary: String
    ) : MarketCheckoutOutcome

    data class Failure(
        val reason: String,
        val paymentMethod: String
    ) : MarketCheckoutOutcome
}

class PglobViewModel(
    private val appContext: Context,
    private val repository: PglobRepository,
    private val aiRepository: AiRepository,
    private val settingsStore: LocalSettingsStore,
    private val pendingDao: PendingMessageDao,
    private val chatMessageDao: ChatMessageDao? = null,
    networkFlow: Flow<Boolean>,
    val currentUserId: String,
    private val initialEmail: String,
    private val initialDisplayName: String
) : ViewModel() {

    val localSettings: StateFlow<LocalAppSettings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LocalAppSettings())

    val isOnline: StateFlow<Boolean> = networkFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    private val hasFirebaseAuth: Boolean
        get() = Firebase.auth.currentUser != null

    private val _profileResolved = MutableStateFlow(false)
    val profileResolved: StateFlow<Boolean> = _profileResolved.asStateFlow()

    private val _localChats = MutableStateFlow<List<ChatThread>>(emptyList())
    private val _localMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    private val _localTransactions = MutableStateFlow<List<PayTransaction>>(emptyList())

    private fun synthesizeLocalProfile(settings: LocalAppSettings): UserPrivateProfile? {
        if (!settings.isLoggedInSession && !settings.hasAccountOnDevice) return null
        val resolvedPhone = settings.chattingPhoneNumber.ifBlank { settings.savedPhoneNumber }
        val resolvedAccount = settings.pglobAccountNumber.ifBlank {
            SecurityAndNumberCodec.generateDistinctAccountNumber(resolvedPhone, currentUserId)
        }
        val resolvedName = settings.savedDisplayName.ifBlank { initialDisplayName.ifBlank { "PGLOB Member" } }
        val resolvedUsername = settings.savedUsername.ifBlank {
            val base = resolvedName.lowercase().filter { it.isLetterOrDigit() }.take(10).ifBlank { "pglobuser" }
            "${base}_${currentUserId.takeLast(4)}"
        }
        return UserPrivateProfile(
            id = currentUserId,
            userId = currentUserId,
            fullName = resolvedName,
            username = resolvedUsername,
            usernameVerified = true,
            phone = resolvedPhone,
            email = settings.savedEmail.ifBlank { initialEmail },
            country = settings.savedCountryName.ifBlank { "Nigeria" },
            countryFlag = settings.savedCountryFlag.ifBlank { "🇳🇬" },
            currency = settings.savedCurrency.ifBlank { "₦" },
            language = settings.appLanguage.ifBlank { "English" },
            about = settings.localAbout.ifBlank { "Hey there! I am using PGLOB APP." },
            bio = settings.localBio,
            interests = settings.localInterests,
            photoUri = settings.localPhotoUri,
            coins = settings.localCoins,
            diamonds = settings.localDiamonds,
            walletBalance = settings.localWalletBalance,
            payAccountNumber = resolvedAccount,
            transactionPin = settings.localTransactionPin,
            vipTier = settings.localVipTier,
            datingEnabled = false,
            fingerprintEnabled = settings.biometricEnabled
        )
    }

    private val remoteProfileFlow: Flow<UserPrivateProfile?> = repository.observeMyProfile(currentUserId)
        .catch { e ->
            handleStreamError("myProfile", e)
            emit(null)
        }

    val myProfile: StateFlow<UserPrivateProfile?> = combine(remoteProfileFlow, localSettings) { remote, settings ->
        _profileResolved.value = true
        if (remote != null) {
            val resolvedAccount = remote.payAccountNumber.ifBlank {
                settings.pglobAccountNumber.ifBlank {
                    SecurityAndNumberCodec.generateDistinctAccountNumber(remote.phone.ifBlank { settings.savedPhoneNumber }, currentUserId)
                }
            }
            val resolvedPhone = remote.phone.ifBlank { settings.chattingPhoneNumber.ifBlank { settings.savedPhoneNumber } }
            remote.copy(
                phone = resolvedPhone,
                payAccountNumber = resolvedAccount,
                bio = remote.bio.ifBlank { settings.localBio },
                interests = remote.interests.ifEmpty { settings.localInterests },
                photoUri = remote.photoUri.ifBlank { settings.localPhotoUri }
            )
        } else {
            synthesizeLocalProfile(settings)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val publicProfiles: StateFlow<List<PublicProfile>> = combine(
        repository.observePublicProfiles()
            .catch { e -> handleStreamError("publicProfiles", e); emit(defaultPublicProfiles()) },
        myProfile
    ) { remoteList, mine ->
        val seedByUid = defaultPublicProfiles().associateBy { it.userId }
        val base = remoteList.ifEmpty { defaultPublicProfiles() }.map { p ->
            val seed = seedByUid[p.userId]
            if (seed != null) {
                p.copy(
                    bio = p.bio.ifBlank { seed.bio },
                    interests = p.interests.ifEmpty { seed.interests }
                )
            } else p
        }
        if (mine == null || mine.userId.isBlank()) base
        else {
            val myPublic = PublicProfile(
                id = mine.userId,
                userId = mine.userId,
                displayName = mine.fullName.ifBlank { "PGLOB Member" },
                username = mine.username,
                usernameVerified = mine.usernameVerified,
                country = mine.country,
                countryFlag = mine.countryFlag,
                language = mine.language,
                about = mine.about,
                photoUri = mine.photoUri,
                coins = mine.coins,
                vipTier = mine.vipTier,
                isOnline = true,
                isPublic = true,
                datingEnabled = mine.datingEnabled,
                bio = mine.bio.ifBlank { mine.about },
                interests = mine.interests
            )
            listOf(myPublic) + base.filterNot { it.userId == mine.userId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), defaultPublicProfiles())

    private val remoteChatsFlow: Flow<List<ChatThread>> = repository.observeChats(currentUserId)
        .catch { e -> handleStreamError("chats", e); emit(emptyList()) }

    private val _localMarketProducts = MutableStateFlow<List<MarketProduct>>(emptyList())

    val chats: StateFlow<List<ChatThread>> = combine(remoteChatsFlow, _localChats) { remote, local ->
        (local + remote).distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _fetchedFirestoreTransactions = MutableStateFlow<List<PayTransaction>>(emptyList())
    private val _isRefreshingOrders = MutableStateFlow(false)
    val isRefreshingOrders: StateFlow<Boolean> = _isRefreshingOrders.asStateFlow()
    private val _ordersSyncError = MutableStateFlow<String?>(null)
    val ordersSyncError: StateFlow<String?> = _ordersSyncError.asStateFlow()

    private val remoteTransactionsFlow: Flow<List<PayTransaction>> = repository.observeTransactions(currentUserId)
        .catch { e -> handleStreamError("transactions", e); emit(emptyList()) }

    val transactions: StateFlow<List<PayTransaction>> = combine(
        remoteTransactionsFlow,
        _fetchedFirestoreTransactions,
        _localTransactions
    ) { remote, fetched, local ->
        val firestoreMerged = (remote + fetched).distinctBy { it.id }
        val localFiltered = if (firestoreMerged.isNotEmpty()) {
            // Avoid duplicate local shadow entries once Firestore returns matching order ref / note
            val remoteNotes = firestoreMerged.map { it.note.trim() }.toSet()
            local.filterNot { it.note.trim().isNotBlank() && it.note.trim() in remoteNotes }
        } else {
            local
        }
        (firestoreMerged + localFiltered)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val orderHistoryRecords: StateFlow<List<OrderHistoryRecord>> = combine(
        transactions,
        localSettings
    ) { txList, settings ->
        txList.map { tx ->
            OrderHistoryMapper.mapTransactionToOrderRecord(
                tx = tx,
                deliveredOrderIds = settings.deliveredOrderIds
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val marketProducts: StateFlow<List<MarketProduct>> = combine(
        repository.observeMarketProducts().catch { e -> handleStreamError("marketProducts", e); emit(emptyList()) },
        _localMarketProducts
    ) { remote, local ->
        (local + remote).distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val remoteStatuses: Flow<List<StatusPost>> = repository.observeStatuses()
        .catch { e -> handleStreamError("statuses", e); emit(emptyList()) }

    val statusReactions: StateFlow<Map<String, String>> = localSettings
        .map { parseReactionsMap(it.statusReactionsJson) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val statuses: StateFlow<List<StatusPost>> = combine(remoteStatuses, localSettings) { remote, settings ->
        val reactions = parseReactionsMap(settings.statusReactionsJson)
        val reshareDeltas = parseIntMap(settings.statusReshareCountsJson)
        val localList = parseLocalStatuses(settings.localStatusesJson)
        val mergedRaw = (localList + remote + defaultGlobalStatuses())
            .distinctBy { it.id.ifBlank { "${it.userId}_${it.caption}" } }
        mergedRaw.map { st ->
            val reacted = reactions.containsKey(st.id)
            val reshareAdd = reshareDeltas[st.id] ?: 0
            val viewedAdd = if (settings.viewedStatusIds.contains(st.id)) 1 else 0
            st.copy(
                likesCount = st.likesCount + if (reacted) 1 else 0,
                reshareCount = st.reshareCount + reshareAdd,
                viewsCount = (st.viewsCount + viewedAdd).coerceAtLeast(1)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), defaultGlobalStatuses())

    private val _deletedStatusNoteIds = MutableStateFlow<Set<String>>(emptySet())
    private val _statusNoteSeedAnchorMs = System.currentTimeMillis()
    private val remoteStatusNotes: Flow<List<StatusNote>> = if (hasFirebaseAuth) {
        repository.observeStatusNotes(currentUserId)
            .catch { e -> handleStreamError("statusNotes", e); emit(emptyList()) }
    } else {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    val reels: StateFlow<List<ReelPost>> = repository.observeReels()
        .catch { e -> handleStreamError("reels", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val jobs: StateFlow<List<JobListing>> = repository.observeJobs()
        .catch { e -> handleStreamError("jobs", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val liveRooms: StateFlow<List<LiveRoom>> = repository.observeLiveRooms()
        .catch { e -> handleStreamError("liveRooms", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val datingMatches: StateFlow<List<DatingMatch>> = repository.observeDatingMatches(currentUserId)
        .catch { e -> handleStreamError("datingMatches", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val myReports: StateFlow<List<SafetyReport>> = repository.observeMyReports(currentUserId)
        .catch { e -> handleStreamError("reports", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // v15 StateFlows: Group Contributions (Ajo), Admin/Sub-Admins, Activity Logs, Broadcasts, Config, Status Insights
    val contributions: StateFlow<List<ContributionGroup>> = combine(localSettings, myProfile) { settings, profile ->
        ContributionAdminCodec.parseContributions(settings.contributionsJson)
            ?: ContributionAdminCodec.defaultContributions(
                currentUserId = currentUserId,
                currentUserName = profile?.fullName?.ifBlank { initialDisplayName } ?: initialDisplayName,
                currentUserHandle = profile?.username?.ifBlank { "paulodogun" } ?: "paulodogun"
            )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        ContributionAdminCodec.defaultContributions(currentUserId, initialDisplayName, "paulodogun")
    )

    val appSubAdmins: StateFlow<List<AppSubAdmin>> = localSettings
        .map { ContributionAdminCodec.parseAppSubAdmins(it.appSubAdminsJson) ?: ContributionAdminCodec.defaultAppSubAdmins() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ContributionAdminCodec.defaultAppSubAdmins())

    val groupSubAdmins: StateFlow<List<GroupSubAdminPermissions>> = localSettings
        .map { ContributionAdminCodec.parseGroupSubAdmins(it.groupSubAdminsJson) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminActivityLogs: StateFlow<List<AdminActivityLog>> = localSettings
        .map { ContributionAdminCodec.parseAdminLogs(it.adminLogsJson) ?: ContributionAdminCodec.defaultAdminLogs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ContributionAdminCodec.defaultAdminLogs())

    val broadcasts: StateFlow<List<BroadcastAnnouncement>> = localSettings
        .map { ContributionAdminCodec.parseBroadcasts(it.broadcastsJson) ?: ContributionAdminCodec.defaultBroadcasts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ContributionAdminCodec.defaultBroadcasts())

    val appAdminConfig: StateFlow<AppAdminConfig> = localSettings
        .map { ContributionAdminCodec.parseAppAdminConfig(it.appAdminConfigJson) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppAdminConfig())

    val statusTrackers: StateFlow<List<StatusInteractionTracker>> = localSettings
        .map { ContributionAdminCodec.parseStatusTrackers(it.statusTrackersJson) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _noteAudienceSnapshot: Flow<Pair<Set<String>, Set<String>>> = combine(
        chats,
        publicProfiles,
        contributions
    ) { chatList, pubProfiles, contribList ->
        val contacts = buildSet {
            chatList.forEach { addAll(it.memberUids) }
            pubProfiles.forEach { add(it.userId) }
        }
        val ajoMembers = buildSet {
            contribList.forEach { g ->
                if (g.members.any { it.uid == currentUserId }) {
                    g.members.forEach { add(it.uid) }
                }
            }
        }
        contacts to ajoMembers
    }

    private val _mergedNotesRaw: Flow<List<StatusNote>> = combine(
        remoteStatusNotes,
        localSettings,
        _deletedStatusNoteIds
    ) { remote, settings, deletedIds ->
        val localSaved = StatusNoteCodec.parseNotesJson(settings.statusNotesJson)
        val seedNotes = StatusNoteCodec.defaultFriendStatusNotes(_statusNoteSeedAnchorMs)
        // Prefer local updated copy for seeded or offline notes, remote copy for synced notes when richer
        val byId = LinkedHashMap<String, StatusNote>()
        (localSaved + remote + seedNotes).forEach { note ->
            if (note.id.isNotBlank() && !deletedIds.contains(note.id)) {
                val existing = byId[note.id]
                if (existing == null) {
                    byId[note.id] = note
                } else {
                    // Merge likes/reshares/viewers union + latest edit
                    val useRemote = note.updatedAtMillisEffective() > existing.updatedAtMillisEffective()
                    val base = if (useRemote) note else existing
                    byId[note.id] = base.copy(
                        likes = (existing.likes + note.likes).distinctBy { it.uid },
                        reshares = (existing.reshares + note.reshares).distinctBy { "${it.uid}_${it.timestampMillis}" },
                        viewers = (existing.viewers + note.viewers).distinctBy { it.uid }
                    )
                }
            }
        }
        byId.values.toList()
    }

    val statusNotes: StateFlow<List<StatusNote>> = combine(
        _mergedNotesRaw,
        localSettings,
        _noteAudienceSnapshot
    ) { merged, settings, (contactUids, ajoMemberUids) ->
        val now = System.currentTimeMillis()
        merged.filter { note ->
            StatusNoteCodec.isNoteVisibleToUser(
                note = note,
                viewerUid = currentUserId,
                contactUids = contactUids,
                ajoMemberUids = ajoMemberUids,
                blockedUids = settings.blockedUserIds,
                nowMillis = now
            )
        }.sortedWith(
            compareByDescending<StatusNote> { it.userId == currentUserId }
                .thenByDescending { it.createdAtMillisEffective() }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StatusNoteCodec.defaultFriendStatusNotes())

    val myActiveStatusNote: StateFlow<StatusNote?> = statusNotes
        .map { list -> list.firstOrNull { it.userId == currentUserId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val myExpiredStatusNote: StateFlow<StatusNote?> = combine(
        _mergedNotesRaw,
        myActiveStatusNote,
        localSettings
    ) { allNotes, activeMine, settings ->
        if (activeMine != null) null
        else {
            val now = System.currentTimeMillis()
            allNotes
                .filter { it.userId == currentUserId && it.isExpiredAt(now) }
                .maxByOrNull { it.createdAtMillisEffective() }
                ?.takeIf { it.id != settings.dismissedExpiredNoteId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _showWelcomeConfetti = MutableStateFlow(false)
    val showWelcomeConfetti: StateFlow<Boolean> = _showWelcomeConfetti.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            InAppNotificationItem(
                id = "n_welcome",
                title = "Welcome to PGLOB APP 🌍",
                body = "Strong Security, App Lock, Instant Translate & PGLOB Pay are active.",
                category = "SECURITY",
                timeLabel = "Now",
                isUnread = true
            ),
            InAppNotificationItem(
                id = "n_msg",
                title = "Amina Yusuf 🇳🇬",
                body = "Sent you a new message: Ready for the global meetup?",
                category = "MESSAGE",
                timeLabel = "2m ago",
                isUnread = true
            ),
            InAppNotificationItem(
                id = "n_req",
                title = "Money Request from Marcus Vance 🇺🇸",
                body = "Requested $25 for Split Event Ticket",
                category = "REQUEST",
                timeLabel = "15m ago",
                isUnread = true,
                requestAmount = 2500.0,
                requestFrom = "Marcus Vance 🇺🇸"
            ),
            InAppNotificationItem(
                id = "n_call",
                title = "Missed Video Call • Kwame Mensah 🇬🇭",
                body = "Tap to call back over encrypted PGLOB Audio/Video",
                category = "CALL",
                timeLabel = "1h ago",
                isUnread = false
            )
        )
    )
    val notifications: StateFlow<List<InAppNotificationItem>> = _notifications.asStateFlow()

    private val _linkedDevices = MutableStateFlow(
        listOf(
            LinkedDeviceSession(
                id = "dev_1",
                deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                location = "This Device • Active Now",
                lastActive = "Online",
                isCurrent = true
            ),
            LinkedDeviceSession(
                id = "dev_2",
                deviceName = "PGLOB Web • Chrome Desktop",
                location = "Lagos, NG",
                lastActive = "Active 2h ago",
                isCurrent = false
            )
        )
    )
    val linkedDevices: StateFlow<List<LinkedDeviceSession>> = _linkedDevices.asStateFlow()

    private val _lastCompletedReceipt = MutableStateFlow<PayTransaction?>(null)
    val lastCompletedReceipt: StateFlow<PayTransaction?> = _lastCompletedReceipt.asStateFlow()

    private var failedPinAttempts = 0

    init {
        PglobNotificationHelper.ensureChannel(appContext)
        seedLocalThreadsIfNeeded()
        viewModelScope.launch {
            if (hasFirebaseAuth) {
                repository.seedInitialEcosystemIfNeeded(
                    uid = currentUserId,
                    displayName = initialDisplayName.ifBlank { "Global Explorer" }
                )
            }
        }
        viewModelScope.launch {
            myProfile.collect { profile ->
                if (profile != null) {
                    if (profile.username.isNotBlank()) {
                        settingsStore.saveKnownAccountOnDevice(profile.fullName, profile.username)
                    }
                    if (localSettings.value.pglobAccountNumber.isBlank() ||
                        localSettings.value.pglobAccountNumber == localSettings.value.chattingPhoneNumber
                    ) {
                        val generated = SecurityAndNumberCodec.generateDistinctAccountNumber(
                            profile.phone.ifBlank { localSettings.value.savedPhoneNumber },
                            currentUserId
                        )
                        settingsStore.updateNumberPrivacy(
                            pglobAccountNumber = generated,
                            chattingPhoneNumber = profile.phone.ifBlank { localSettings.value.savedPhoneNumber }
                        )
                    }
                }
            }
        }
        // Auto-send queued offline messages when connectivity returns
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online && hasFirebaseAuth) {
                    flushPendingMessages()
                }
            }
        }
    }

    private fun seedLocalThreadsIfNeeded() {
        if (_localChats.value.isNotEmpty()) return
        val loungeId = "lounge_global_pglob"
        val conciergeId = "concierge_$currentUserId"
        val ajoChatId = "grp_ajo_benin_$currentUserId"
        _localChats.value = listOf(
            ChatThread(
                id = conciergeId,
                title = "PGLOB Concierge & Wallet Bot",
                isGroup = false,
                isPinned = true,
                lastMessage = "Welcome to PGLOB APP! Your Chatting Number & PGLOB Account Number are strictly separated.",
                lastSenderId = "pglob_bot",
                unreadCount = 1,
                memberUids = listOf(currentUserId, "pglob_bot"),
                peerFlag = "🌍",
                peerOnline = true
            ),
            ChatThread(
                id = loungeId,
                title = "PGLOB Global World Lounge",
                isGroup = true,
                lastMessage = "Amina Yusuf: Welcome everyone from Lagos, Accra, London & New York!",
                lastSenderId = "amina_ng",
                unreadCount = 2,
                memberUids = listOf(currentUserId, "amina_ng", "marcus_us"),
                peerFlag = "🌎",
                peerOnline = true
            ),
            ChatThread(
                id = ajoChatId,
                title = "Benin Ladies Ajo (Escrow)",
                isGroup = true,
                lastMessage = "🔒 PGLOB Escrow: Weekly ₦10,000 round active",
                lastSenderId = "m_blessing",
                unreadCount = 1,
                memberUids = listOf(currentUserId, "m_blessing", "m_amina"),
                peerFlag = "💎",
                peerOnline = true
            )
        )
        _localMessages.value = mapOf(
            conciergeId to listOf(
                ChatMessage(
                    id = "m_welcome_1",
                    chatId = conciergeId,
                    senderId = "pglob_bot",
                    senderName = "PGLOB Security",
                    memberUids = listOf(currentUserId, "pglob_bot"),
                    text = "Welcome to PGLOB! Chat with your Chatting Number and send money safely using PGLOB Account Numbers only.",
                    isRead = true
                )
            ),
            loungeId to listOf(
                ChatMessage(
                    id = "m_lounge_1",
                    chatId = loungeId,
                    senderId = "amina_ng",
                    senderName = "Amina Yusuf 🇳🇬",
                    memberUids = listOf(currentUserId, "amina_ng"),
                    text = "Happy new week PGLOB family! Instant translate works in 50+ languages 🌍",
                    isRead = true
                )
            ),
            ajoChatId to listOf(
                ChatMessage(
                    id = "m_ajo_1",
                    chatId = ajoChatId,
                    senderId = "m_blessing",
                    senderName = "Blessing Gold 🇳🇬",
                    memberUids = listOf(currentUserId, "m_blessing"),
                    text = "🔒 All contributions stay locked inside PGLOB Escrow until Round payout release.",
                    isRead = true
                )
            )
        )
    }

    private fun defaultPublicProfiles(): List<PublicProfile> = listOf(
        PublicProfile(
            id = "amina_ng",
            userId = "amina_ng",
            displayName = "Amina Yusuf",
            username = "amina_lagos",
            usernameVerified = true,
            country = "Nigeria",
            countryFlag = "🇳🇬",
            city = "Lagos",
            language = "English",
            about = "Founder & Ajo Organizer",
            bio = "Lagos community builder running transparent Ajo circles and supporting women-led businesses.",
            interests = listOf("Ajo Savings", "Business", "Fashion", "Afrobeats"),
            coins = 12400L,
            vipTier = "GOLD",
            isOnline = true,
            isKingQueen = true,
            isPublic = true,
            datingEnabled = true,
            age = 26,
            gender = "Female"
        ),
        PublicProfile(
            id = "blessing_ng",
            userId = "blessing_ng",
            displayName = "Blessing Gold",
            username = "blessing_gold",
            usernameVerified = true,
            country = "Nigeria",
            countryFlag = "🇳🇬",
            city = "Benin City",
            language = "Pidgin",
            about = "Building trusted Ajo circles",
            bio = "Verified escrow organizer and fashion entrepreneur connecting friends globally.",
            interests = listOf("Ajo Savings", "Food & Cooking", "Fashion", "Faith & Community"),
            coins = 9400L,
            vipTier = "GOLD",
            isOnline = true,
            isKingQueen = false,
            isPublic = true,
            datingEnabled = false,
            age = 28,
            gender = "Female"
        ),
        PublicProfile(
            id = "marcus_us",
            userId = "marcus_us",
            displayName = "Marcus Vance",
            username = "marcus_nyc",
            usernameVerified = true,
            country = "United States",
            countryFlag = "🇺🇸",
            city = "New York",
            language = "English",
            about = "Music & Global Live host",
            bio = "Hosting weekly global audio rooms, DJ sessions, and cross-border creator collabs.",
            interests = listOf("Music", "Afrobeats", "Travel", "Photography"),
            coins = 9800L,
            vipTier = "DIAMOND",
            isOnline = true,
            isKingQueen = false,
            isPublic = true,
            datingEnabled = false,
            age = 29,
            gender = "Male"
        ),
        PublicProfile(
            id = "kwame_gh",
            userId = "kwame_gh",
            displayName = "Kwame Mensah",
            username = "kwame_gh",
            usernameVerified = true,
            country = "Ghana",
            countryFlag = "🇬🇭",
            city = "Accra",
            language = "English",
            about = "Fintech & PGLOB Pay trader",
            bio = "Cross-border payments specialist, football fan, and marketplace seller in Accra.",
            interests = listOf("Fintech & Pay", "Football", "Startups", "Gaming"),
            coins = 5200L,
            vipTier = "FREE",
            isOnline = true,
            isKingQueen = false,
            isPublic = true,
            datingEnabled = false,
            age = 27,
            gender = "Male"
        ),
        PublicProfile(
            id = "priya_in",
            userId = "priya_in",
            displayName = "Priya Sharma",
            username = "priya_tech",
            usernameVerified = true,
            country = "India",
            countryFlag = "🇮🇳",
            city = "Bengaluru",
            language = "Hindi",
            about = "Product designer on PGLOB",
            bio = "Designing crisp mobile experiences, learning global languages, and exploring street food.",
            interests = listOf("Design", "Tech & AI", "Travel", "Reading"),
            coins = 7500L,
            vipTier = "GOLD",
            isOnline = true,
            isKingQueen = false,
            isPublic = true,
            datingEnabled = false,
            age = 25,
            gender = "Female"
        )
    )

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun dismissWelcomeConfetti() {
        _showWelcomeConfetti.value = false
    }

    fun observeMessagesWithPending(chatId: String): Flow<List<ChatMessage>> {
        val remoteFlow = if (hasFirebaseAuth) {
            repository.observeMessages(chatId, currentUserId)
                .catch { e -> handleStreamError("messages:$chatId", e); emit(emptyList()) }
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val localMsgFlow = _localMessages.map { it[chatId].orEmpty() }
        val roomCachedFlow = (chatMessageDao?.observeMessagesForChat(chatId)
            ?: kotlinx.coroutines.flow.flowOf(emptyList())).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    chatId = entity.chatId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    memberUids = entity.memberUidsJoined.split(",").filter { it.isNotBlank() },
                    text = entity.messageContent,
                    messageType = entity.messageType,
                    mediaUri = entity.mediaUri,
                    amount = entity.amount,
                    giftName = entity.giftName,
                    replyToText = entity.replyToText,
                    reaction = entity.reaction,
                    isRead = entity.isRead,
                    isPendingOffline = entity.isPendingOffline
                )
            }
        }
        val localPendingFlow = pendingDao.observeForChat(chatId).map { list ->
            list.map { pending ->
                ChatMessage(
                    id = pending.id,
                    chatId = pending.chatId,
                    senderId = pending.senderId,
                    senderName = pending.senderName,
                    memberUids = pending.memberUidsJoined.split(",").filter { it.isNotBlank() },
                    text = pending.text,
                    messageType = pending.messageType,
                    mediaUri = pending.mediaUri,
                    amount = pending.amount,
                    giftName = pending.giftName,
                    replyToText = pending.replyToText,
                    isRead = false,
                    isPendingOffline = true
                )
            }
        }
        return combine(remoteFlow, roomCachedFlow, localMsgFlow, localPendingFlow) { remote, roomMsgs, localMsgs, pending ->
            val merged = (remote + localMsgs + roomMsgs).distinctBy { it.id }
            val existingIds = merged.map { it.id }.toSet()
            merged + pending.filterNot { it.id in existingIds }
        }
    }

    suspend fun checkUsernameAvailable(username: String): Boolean {
        return repository.checkUsernameAvailable(username)
    }

    fun completeProfileSetup(
        fullName: String,
        username: String,
        usernameVerified: Boolean,
        phone: String,
        email: String,
        country: String,
        countryFlag: String,
        currency: String,
        language: String,
        about: String,
        photoUri: String,
        bio: String? = null,
        interests: List<String>? = null,
        isInitialSignup: Boolean = true
    ) {
        viewModelScope.launch {
            val current = myProfile.value
            val resolvedAccount = current?.payAccountNumber?.ifBlank {
                localSettings.value.pglobAccountNumber.ifBlank {
                    SecurityAndNumberCodec.generateDistinctAccountNumber(phone, currentUserId)
                }
            } ?: localSettings.value.pglobAccountNumber.ifBlank {
                SecurityAndNumberCodec.generateDistinctAccountNumber(phone, currentUserId)
            }
            val cleanBio = ProfileCodec.sanitizeBio(bio ?: current?.bio ?: localSettings.value.localBio)
            val cleanInterests = (interests ?: current?.interests ?: localSettings.value.localInterests)
                .mapNotNull { ProfileCodec.normalizeInterestTag(it).takeIf { t -> t.isNotBlank() } }
                .distinctBy { it.lowercase() }
                .take(ProfileCodec.MAX_INTERESTS_COUNT)

            val profile = UserPrivateProfile(
                userId = currentUserId,
                fullName = fullName.ifBlank { initialDisplayName.ifBlank { "PGLOB Member" } },
                username = username,
                usernameVerified = usernameVerified,
                phone = phone,
                email = email.ifBlank { initialEmail },
                country = country,
                countryFlag = countryFlag,
                currency = currency,
                language = language,
                about = about.ifBlank { "Hey there! I am using PGLOB APP." },
                bio = cleanBio,
                interests = cleanInterests,
                photoUri = photoUri,
                coins = current?.coins ?: localSettings.value.localCoins,
                diamonds = current?.diamonds ?: localSettings.value.localDiamonds,
                walletBalance = current?.walletBalance ?: localSettings.value.localWalletBalance,
                payAccountNumber = resolvedAccount,
                transactionPin = current?.transactionPin ?: localSettings.value.localTransactionPin,
                vipTier = current?.vipTier ?: localSettings.value.localVipTier,
                datingEnabled = current?.datingEnabled ?: false,
                fingerprintEnabled = current?.fingerprintEnabled ?: localSettings.value.biometricEnabled
            )
            settingsStore.saveKnownAccountOnDevice(profile.fullName, profile.username)
            settingsStore.saveLocalProfileExtras(
                displayName = profile.fullName,
                phone = profile.phone,
                email = profile.email,
                countryName = profile.country,
                countryFlag = profile.countryFlag,
                currency = profile.currency,
                language = language,
                about = profile.about,
                bio = profile.bio,
                interests = profile.interests,
                photoUri = profile.photoUri
            )
            settingsStore.updateNumberPrivacy(
                pglobAccountNumber = resolvedAccount,
                chattingPhoneNumber = profile.phone
            )
            settingsStore.setAppLanguage(language)

            if (hasFirebaseAuth) {
                val res = repository.saveCompleteProfile(profile, bio = profile.bio, interests = profile.interests)
                if (res.isFailure) {
                    _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Saved locally"
                }
            }
            if (isInitialSignup) {
                _showWelcomeConfetti.value = true
            }
            _toastMessage.value = if (isInitialSignup) {
                "Welcome to PGLOB APP! Your account @${profile.username} is live!"
            } else {
                "Profile updated & visible to others!"
            }
        }
    }

    fun uploadProfilePhotoFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val encoded = withContext(Dispatchers.IO) {
                ProfileCodec.compressProfileImageToDataUrlOrFallback(context, uri)
            }
            updateProfilePhoto(encoded)
        }
    }

    fun updateProfilePhoto(uriString: String) {
        viewModelScope.launch {
            settingsStore.saveLocalProfileExtras(photoUri = uriString)
            val current = myProfile.value
            if (current != null && hasFirebaseAuth) {
                repository.saveCompleteProfile(
                    profile = current.copy(photoUri = uriString),
                    bio = current.bio,
                    interests = current.interests
                )
            }
            _toastMessage.value = if (uriString.isBlank()) "Profile photo removed" else "Profile photo updated & shared!"
        }
    }

    fun saveUserProfileDetails(
        fullName: String,
        about: String,
        bio: String,
        interests: List<String>,
        photoUri: String? = null,
        phone: String? = null,
        country: String? = null,
        countryFlag: String? = null,
        currency: String? = null
    ) {
        val current = myProfile.value
        val resolvedName = fullName.trim().ifBlank { current?.fullName ?: initialDisplayName.ifBlank { "PGLOB Member" } }
        val resolvedAbout = about.trim().ifBlank { current?.about ?: "Hey there! I am using PGLOB APP." }
        val cleanBio = ProfileCodec.sanitizeBio(bio.trim())
        val cleanInterests = interests
            .mapNotNull { ProfileCodec.normalizeInterestTag(it).takeIf { t -> t.isNotBlank() } }
            .distinctBy { it.lowercase() }
            .take(ProfileCodec.MAX_INTERESTS_COUNT)

        completeProfileSetup(
            fullName = resolvedName,
            username = current?.username ?: localSettings.value.savedUsername.ifBlank { "pglob_${currentUserId.take(6)}" },
            usernameVerified = current?.usernameVerified ?: false,
            phone = phone ?: current?.phone ?: localSettings.value.chattingPhoneNumber,
            email = current?.email ?: localSettings.value.savedEmail,
            country = country ?: current?.country ?: localSettings.value.savedCountryName,
            countryFlag = countryFlag ?: current?.countryFlag ?: localSettings.value.savedCountryFlag,
            currency = currency ?: current?.currency ?: localSettings.value.savedCurrency,
            language = current?.language ?: localSettings.value.appLanguage,
            about = resolvedAbout,
            photoUri = photoUri ?: current?.photoUri ?: localSettings.value.localPhotoUri,
            bio = cleanBio,
            interests = cleanInterests,
            isInitialSignup = false
        )
    }

    fun setWallpaper(wallpaperId: String, customUri: String? = null) {
        viewModelScope.launch {
            settingsStore.setWallpaper(wallpaperId, customUri)
            _toastMessage.value = "Wallpaper updated"
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsStore.setThemeMode(mode)
            _toastMessage.value = "Theme switched"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setBiometricEnabled(enabled)
            if (hasFirebaseAuth) {
                myProfile.value?.let { repository.saveCompleteProfile(it.copy(fingerprintEnabled = enabled)) }
            }
        }
    }

    fun setAppLock(enabled: Boolean, timeoutSec: Int? = null) {
        viewModelScope.launch {
            settingsStore.setAppLock(enabled, timeoutSec)
            _toastMessage.value = if (enabled) "App Lock ON" else "App Lock OFF"
        }
    }

    fun toggleLockedChat(chatId: String) {
        viewModelScope.launch {
            val wasLocked = localSettings.value.lockedChatIds.contains(chatId)
            settingsStore.toggleLockedChat(chatId)
            _toastMessage.value = if (wasLocked) "Chat unlocked" else "Chat moved to Locked Chats 🔒"
        }
    }

    fun toggleFavoriteChat(chatId: String) {
        viewModelScope.launch {
            val wasFav = localSettings.value.favoriteChatIds.contains(chatId)
            settingsStore.toggleFavoriteChat(chatId)
            _toastMessage.value = if (wasFav) "Removed from Favorites" else "Added to Favorites ⭐"
        }
    }

    fun setHideLockedChats(hide: Boolean) {
        viewModelScope.launch { settingsStore.setHideLockedChats(hide) }
    }

    fun setDisappearingHours(hours: Int) {
        viewModelScope.launch {
            settingsStore.setDisappearingHours(hours)
            val label = when (hours) {
                24 -> "24 Hours"
                168 -> "7 Days"
                2160 -> "90 Days"
                else -> "OFF"
            }
            _toastMessage.value = "Disappearing messages set to $label"
        }
    }

    fun updatePrivacy(
        readReceipts: Boolean? = null,
        showOnline: Boolean? = null,
        lastSeen: String? = null,
        profilePhoto: String? = null,
        about: String? = null,
        status: String? = null,
        groups: String? = null,
        silenceUnknownCallers: Boolean? = null,
        liveLocation: String? = null
    ) {
        viewModelScope.launch {
            settingsStore.updatePrivacy(
                readReceipts, showOnline, lastSeen, profilePhoto, about, status, groups, silenceUnknownCallers, liveLocation
            )
            _toastMessage.value = "Privacy settings updated"
        }
    }

    fun setTwoStep(enabled: Boolean, pin: String? = null) {
        viewModelScope.launch {
            settingsStore.setTwoStep(enabled, pin)
            _toastMessage.value = if (enabled) "Two-Step Verification enabled 🔐" else "Two-Step Verification disabled"
        }
    }

    fun setHideBalance(hide: Boolean) {
        viewModelScope.launch { settingsStore.setHideBalance(hide) }
    }

    fun setBoldFontMode(bold: Boolean) {
        viewModelScope.launch {
            settingsStore.setBoldFontMode(bold)
            _toastMessage.value = if (bold) "Bold typography ON" else "Standard typography"
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            settingsStore.setAppLanguage(lang)
            if (hasFirebaseAuth) {
                myProfile.value?.let { repository.saveCompleteProfile(it.copy(language = lang)) }
            }
            _toastMessage.value = "App Language set to $lang"
        }
    }

    fun updateNotificationPrefs(
        master: Boolean? = null,
        messages: Boolean? = null,
        groups: Boolean? = null,
        pay: Boolean? = null,
        security: Boolean? = null,
        preview: Boolean? = null,
        vibrate: Boolean? = null,
        dnd: Boolean? = null
    ) {
        viewModelScope.launch {
            settingsStore.updateNotificationPrefs(master, messages, groups, pay, security, preview, vibrate, dnd)
        }
    }

    fun setTransactionPin(pin: String) {
        viewModelScope.launch {
            settingsStore.saveLocalProfileExtras(transactionPin = pin)
            if (hasFirebaseAuth) {
                val current = myProfile.value
                if (current != null) {
                    repository.saveCompleteProfile(current.copy(transactionPin = pin))
                }
            }
            settingsStore.setPayLockUntil(0L)
            failedPinAttempts = 0
            addNotification("Security PIN Updated 🔐", "Your 4-digit PGLOB Transaction PIN was saved.", "SECURITY")
            _toastMessage.value = "Transaction PIN saved"
        }
    }

    fun addNotification(
        title: String,
        body: String,
        category: String,
        requestAmount: Double = 0.0,
        requestFrom: String = "",
        targetChatId: String = "",
        targetStatusId: String = ""
    ) {
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val item = InAppNotificationItem(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = title,
            body = body,
            category = category,
            timeLabel = now,
            isUnread = true,
            requestAmount = requestAmount,
            requestFrom = requestFrom,
            targetChatId = targetChatId,
            targetStatusId = targetStatusId
        )
        _notifications.value = listOf(item) + _notifications.value
        val s = localSettings.value
        val categoryAllowed = when (category) {
            "MESSAGE", "STATUS_REPLY" -> s.notifMessages
            "PAY", "REQUEST" -> s.notifPay
            "SECURITY" -> s.notifSecurity
            else -> true
        }
        if (categoryAllowed) {
            PglobNotificationHelper.postLocalAlert(
                context = appContext,
                title = title,
                body = body,
                notifEnabled = s.notifMaster,
                dndEnabled = s.dndEnabled,
                showPreview = s.notifPreview,
                vibrate = s.notifVibrate
            )
        }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
    }

    fun removeNotification(id: String) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
    }

    fun logoutDevice(id: String) {
        _linkedDevices.value = _linkedDevices.value.filterNot { it.id == id && !it.isCurrent }
        _toastMessage.value = "Device session terminated"
    }

    fun logoutAllOtherDevices() {
        _linkedDevices.value = _linkedDevices.value.filter { it.isCurrent }
        addNotification("All Other Devices Signed Out", "Only this device remains logged into PGLOB APP.", "SECURITY")
        _toastMessage.value = "Logged out from all other devices"
    }

    fun clearLastReceipt() {
        _lastCompletedReceipt.value = null
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            settingsStore.clearLocalCacheOnly()
            _toastMessage.value = "Cache cleared! Chats & Wallet remain safely synced."
        }
    }

    fun onUserLoggedOutCleanup(forgetDeviceAccount: Boolean = false) {
        viewModelScope.launch {
            settingsStore.logoutExplicitSession(forgetDeviceAccount)
        }
    }

    fun logoutExplicitly(forgetDeviceAccount: Boolean = false) {
        viewModelScope.launch {
            settingsStore.logoutExplicitSession(forgetDeviceAccount)
            _toastMessage.value = "Logged out safely. Sign in anytime with your Phone & Password."
        }
    }

    fun setStayLoggedIn(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setStayLoggedIn(enabled)
            _toastMessage.value = if (enabled) "Stay Logged In enabled" else "Stay Logged In disabled"
        }
    }

    fun setFingerprintSignInEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setFingerprintSignInEnabled(enabled)
            _toastMessage.value = if (enabled) "Fingerprint / Face ID Sign In enabled" else "Fingerprint / Face ID Sign In disabled"
        }
    }

    fun updateNumberPrivacy(
        chatNumberPrivacy: String? = null,
        accountNumberVisibility: String? = null
    ) {
        viewModelScope.launch {
            settingsStore.updateNumberPrivacy(
                chatNumberPrivacy = chatNumberPrivacy,
                accountNumberVisibility = accountNumberVisibility
            )
            _toastMessage.value = "Number privacy updated"
        }
    }

    fun addApprovedChatNumber(rawPhone: String) {
        val cleaned = rawPhone.trim()
        if (cleaned.length < 5) {
            _toastMessage.value = "Enter a valid phone number"
            return
        }
        viewModelScope.launch {
            val updated = localSettings.value.approvedChatNumbers + cleaned
            settingsStore.updateNumberPrivacy(approvedChatNumbers = updated)
            _toastMessage.value = "Added $cleaned to approved list"
        }
    }

    fun removeApprovedChatNumber(rawPhone: String) {
        viewModelScope.launch {
            val updated = localSettings.value.approvedChatNumbers - rawPhone
            settingsStore.updateNumberPrivacy(approvedChatNumbers = updated)
            _toastMessage.value = "Removed from approved list"
        }
    }

    fun updateEmailOptional(newEmail: String) {
        val trimmed = newEmail.trim()
        viewModelScope.launch {
            settingsStore.saveLocalProfileExtras(email = trimmed)
            val current = myProfile.value
            if (current != null && hasFirebaseAuth) {
                repository.saveCompleteProfile(current.copy(email = trimmed))
            }
            _toastMessage.value = if (trimmed.isBlank()) "Optional email cleared" else "Email saved: $trimmed"
        }
    }

    fun changePhonePassword(currentPassword: String, newPassword: String): Boolean {
        val settings = localSettings.value
        val phoneKey = SecurityAndNumberCodec.digitsOnly(
            settings.chattingPhoneNumber.ifBlank { settings.savedPhoneNumber }
        )
        if (settings.passwordHash.isNotBlank()) {
            val enteredHash = SecurityAndNumberCodec.sha256Hash(phoneKey, currentPassword)
            if (enteredHash != settings.passwordHash) {
                _toastMessage.value = "Current password is incorrect"
                return false
            }
        }
        if (!SecurityAndNumberCodec.isPasswordAcceptable(newPassword)) {
            _toastMessage.value = "New password needs min 8 chars with letters and numbers"
            return false
        }
        val nextHash = SecurityAndNumberCodec.sha256Hash(phoneKey, newPassword)
        viewModelScope.launch {
            val updatedAccountsJson = updateStoredAccountPasswordHash(
                settings.registeredAccountsJson,
                phoneKey,
                nextHash
            )
            settingsStore.updatePasswordHash(nextHash, updatedAccountsJson)
            addNotification("Password Changed 🔐", "Your PGLOB login password was updated successfully.", "SECURITY")
            _toastMessage.value = "Password Changed ✅"
        }
        return true
    }

    private fun updateStoredAccountPasswordHash(accountsJson: String, phoneDigits: String, newHash: String): String {
        return try {
            val arr = JSONArray(accountsJson)
            val out = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val objDigits = SecurityAndNumberCodec.digitsOnly(obj.optString("phone"))
                if (objDigits == phoneDigits || objDigits.takeLast(10) == phoneDigits.takeLast(10)) {
                    obj.put("passwordHash", newHash)
                }
                out.put(obj)
            }
            out.toString()
        } catch (_: Exception) {
            accountsJson
        }
    }

    fun isRecipientChattingPhone(input: String): Boolean {
        val settings = localSettings.value
        val myChatPhone = settings.chattingPhoneNumber.ifBlank { myProfile.value?.phone.orEmpty() }
        val otherPhones = listOf("+234 803 111 2233", "+234 809 445 6677", "+1 415 555 0199")
        return SecurityAndNumberCodec.looksLikeChattingPhoneInsteadOfAccount(input, myChatPhone, otherPhones)
    }

    fun setSecretVaultPin(pin: String) {
        viewModelScope.launch {
            settingsStore.setSecretVaultPin(pin)
            _toastMessage.value = "Secret Vault PIN configured"
        }
    }

    fun saveSecretVaultNotes(notes: String) {
        viewModelScope.launch {
            settingsStore.setSecretVaultNotes(notes)
            _toastMessage.value = "Encrypted inside Secret Vault"
        }
    }

    fun toggleBlockUser(targetUid: String) {
        viewModelScope.launch {
            settingsStore.toggleBlockUser(targetUid)
            _toastMessage.value = "Updated blocked list"
        }
    }

    fun sendChatMessage(
        chatId: String,
        memberUids: List<String>,
        text: String,
        messageType: String = "TEXT",
        mediaUri: String = "",
        amount: Double = 0.0,
        giftName: String = "",
        replyToText: String = ""
    ) {
        if (text.isBlank() && mediaUri.isBlank()) return
        val senderName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Me"
        val messageId = "msg_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        viewModelScope.launch {
            if (!isOnline.value && hasFirebaseAuth) {
                pendingDao.insert(
                    PendingMessageEntity(
                        id = messageId,
                        chatId = chatId,
                        senderId = currentUserId,
                        senderName = senderName,
                        memberUidsJoined = (memberUids + currentUserId).distinct().joinToString(","),
                        text = text,
                        messageType = messageType,
                        mediaUri = mediaUri,
                        amount = amount,
                        giftName = giftName,
                        replyToText = replyToText
                    )
                )
                _toastMessage.value = "Offline: queued to auto-send when back online"
                return@launch
            }

            val nowMs = System.currentTimeMillis()
            val newMsg = ChatMessage(
                id = messageId,
                chatId = chatId,
                senderId = currentUserId,
                senderName = senderName,
                memberUids = (memberUids + currentUserId).distinct(),
                text = text,
                messageType = messageType,
                mediaUri = mediaUri,
                amount = amount,
                giftName = giftName,
                replyToText = replyToText,
                isRead = true
            )
            appendLocalMessage(chatId, newMsg, text)
            chatMessageDao?.upsert(
                ChatMessageEntity(
                    id = messageId,
                    chatId = chatId,
                    senderId = currentUserId,
                    senderName = senderName,
                    memberUidsJoined = (memberUids + currentUserId).distinct().joinToString(","),
                    messageContent = text,
                    messageType = messageType,
                    mediaUri = mediaUri,
                    amount = amount,
                    giftName = giftName,
                    replyToText = replyToText,
                    timestampMillis = nowMs,
                    isRead = true,
                    isPendingOffline = false
                )
            )

            if (hasFirebaseAuth) {
                val res = repository.sendMessage(
                    chatId = chatId,
                    memberUids = memberUids,
                    senderName = senderName,
                    text = text,
                    messageType = messageType,
                    mediaUri = mediaUri,
                    amount = amount,
                    giftName = giftName,
                    replyToText = replyToText,
                    messageId = messageId
                )
                if (res.isSuccess) {
                    repository.updateCoinsAndWallet(coinsDelta = 5L)
                } else {
                    pendingDao.insert(
                        PendingMessageEntity(
                            id = messageId,
                            chatId = chatId,
                            senderId = currentUserId,
                            senderName = senderName,
                            memberUidsJoined = (memberUids + currentUserId).distinct().joinToString(","),
                            text = text,
                            messageType = messageType,
                            mediaUri = mediaUri,
                            amount = amount,
                            giftName = giftName,
                            replyToText = replyToText
                        )
                    )
                }
            }
            settingsStore.updateLocalWalletAndCoins(coinsDelta = 5L)
        }
    }

    private fun appendLocalMessage(chatId: String, msg: ChatMessage, previewText: String) {
        val currentMap = _localMessages.value.toMutableMap()
        val existingList = currentMap[chatId].orEmpty()
        currentMap[chatId] = existingList + msg
        _localMessages.value = currentMap

        _localChats.value = _localChats.value.map { thread ->
            if (thread.id == chatId) {
                thread.copy(
                    lastMessage = previewText.ifBlank { "Media" },
                    lastSenderId = msg.senderId,
                    unreadCount = 0
                )
            } else thread
        }
    }

    private suspend fun flushPendingMessages() {
        val pending = pendingDao.getAllPending()
        for (item in pending) {
            val members = item.memberUidsJoined.split(",").filter { it.isNotBlank() }
            val res = repository.sendMessage(
                chatId = item.chatId,
                memberUids = members,
                senderName = item.senderName,
                text = item.text,
                messageType = item.messageType,
                mediaUri = item.mediaUri,
                amount = item.amount,
                giftName = item.giftName,
                replyToText = item.replyToText,
                messageId = item.id
            )
            if (res.isSuccess) {
                pendingDao.deleteById(item.id)
            }
        }
    }

    fun reactToMessage(chatId: String, messageId: String, reaction: String) {
        viewModelScope.launch {
            val currentMap = _localMessages.value.toMutableMap()
            currentMap[chatId] = currentMap[chatId].orEmpty().map { m ->
                if (m.id == messageId) {
                    m.copy(reaction = reaction)
                } else m
            }
            _localMessages.value = currentMap
            if (hasFirebaseAuth) {
                repository.reactToMessage(chatId, messageId, reaction)
            }
        }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            val currentMap = _localMessages.value.toMutableMap()
            currentMap[chatId] = currentMap[chatId].orEmpty().filterNot { it.id == messageId }
            _localMessages.value = currentMap
            chatMessageDao?.deleteById(messageId)
            if (hasFirebaseAuth) {
                repository.deleteMessage(chatId, messageId)
            }
        }
    }

    fun openOrCreateChatWith(title: String, targetUid: String, isGroup: Boolean = false, flag: String = "🌍", onReady: (String) -> Unit) {
        val chatId = if (isGroup) {
            "grp_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        } else {
            "dm_${listOf(currentUserId, targetUid).sorted().joinToString("_")}".take(100)
        }
        if (_localChats.value.none { it.id == chatId }) {
            _localChats.value = listOf(
                ChatThread(
                    id = chatId,
                    title = title,
                    isGroup = isGroup,
                    lastMessage = "Say hello on PGLOB",
                    memberUids = listOf(currentUserId, targetUid).distinct(),
                    peerFlag = flag,
                    peerOnline = true
                )
            ) + _localChats.value
        }
        onReady(chatId)
        viewModelScope.launch {
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(chatId, title, listOf(currentUserId, targetUid), isGroup, flag)
            }
        }
    }

    fun updateChatFlags(chatId: String, archived: Boolean? = null, pinned: Boolean? = null, muted: Boolean? = null) {
        viewModelScope.launch {
            _localChats.value = _localChats.value.map { t ->
                if (t.id == chatId) {
                    t.copy(
                        isArchived = archived ?: t.isArchived,
                        isPinned = pinned ?: t.isPinned,
                        isMuted = muted ?: t.isMuted
                    )
                } else t
            }
            if (hasFirebaseAuth) {
                repository.updateChatFlags(chatId, archived, pinned, muted)
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            _localChats.value = _localChats.value.filterNot { it.id == chatId }
            if (hasFirebaseAuth) {
                repository.deleteChat(chatId)
            }
            _toastMessage.value = "Chat deleted"
        }
    }

    fun sendGiftInChat(chatId: String, memberUids: List<String>, giftName: String, emoji: String, costCoins: Long) {
        val currentCoins = myProfile.value?.coins ?: 0L
        if (currentCoins < costCoins) {
            _toastMessage.value = "Not enough coins! Need $costCoins coins."
            return
        }
        viewModelScope.launch {
            settingsStore.updateLocalWalletAndCoins(coinsDelta = -costCoins)
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = -costCoins)
                repository.recordTransaction(
                    txType = "GIFT",
                    amount = costCoins.toDouble(),
                    fee = 0.0,
                    currency = "COINS",
                    method = "In-Chat Gift",
                    destination = chatId,
                    status = "PAID",
                    note = "Sent $giftName"
                )
            }
            _localTransactions.value = listOf(
                PayTransaction(
                    id = "gift_${UUID.randomUUID().toString().take(8)}",
                    userId = currentUserId,
                    txType = "GIFT",
                    amount = costCoins.toDouble(),
                    fee = 0.0,
                    currency = "COINS",
                    method = "In-Chat Gift",
                    destination = chatId,
                    status = "PAID",
                    note = "Sent $giftName"
                )
            ) + _localTransactions.value
            sendChatMessage(
                chatId = chatId,
                memberUids = memberUids,
                text = "Sent $emoji $giftName ($costCoins coins)!",
                messageType = "GIFT",
                amount = costCoins.toDouble(),
                giftName = "$emoji $giftName"
            )
            _toastMessage.value = "$emoji $giftName sent!"
        }
    }

    fun verifyPinOrAllow(enteredPin: String, bypassWithBiometric: Boolean = false): Boolean {
        if (bypassWithBiometric) {
            failedPinAttempts = 0
            viewModelScope.launch {
                settingsStore.setPayLockUntil(0L)
                settingsStore.setLastPayAuthMillis(System.currentTimeMillis())
            }
            return true
        }
        val lockUntil = localSettings.value.payLockUntilMillis
        val now = System.currentTimeMillis()
        if (now < lockUntil) {
            val remSec = ((lockUntil - now) / 1000L).coerceAtLeast(1L)
            _toastMessage.value = "PIN temporarily locked (${remSec}s). Use Fingerprint / Face Unlock or wait."
            return false
        }
        val savedPin = myProfile.value?.transactionPin.orEmpty()
        if (savedPin.isBlank()) {
            _toastMessage.value = "Please create your 4-digit Transaction PIN first."
            return false
        }
        return if (enteredPin == savedPin) {
            failedPinAttempts = 0
            viewModelScope.launch { settingsStore.setLastPayAuthMillis(now) }
            true
        } else {
            failedPinAttempts++
            if (failedPinAttempts >= 3) {
                viewModelScope.launch {
                    settingsStore.setPayLockUntil(System.currentTimeMillis() + 60_000L)
                }
                addNotification("Failed PIN Alert ⚠️", "3 incorrect PIN attempts detected. PIN locked for 60s.", "SECURITY")
                _toastMessage.value = "Wrong PIN 3 times. Locked for 1 minute (Biometric still works)."
            } else {
                _toastMessage.value = "Incorrect PIN (${3 - failedPinAttempts} tries left)"
            }
            false
        }
    }

    fun executePayAction(
        txType: String,
        amount: Double,
        fee: Double,
        method: String,
        destination: String,
        note: String,
        chatIdForPacket: String? = null,
        chatMembers: List<String> = emptyList()
    ) {
        val profile = myProfile.value ?: return
        val currency = profile.currency.ifBlank { "₦" }
        val limit = localSettings.value.dailyPayLimit
        if (txType == "SEND" && isRecipientChattingPhone(destination)) {
            val acctNum = profile.payAccountNumber.ifBlank { localSettings.value.pglobAccountNumber }
            _toastMessage.value = "Wrong number - This is Chatting Number, NOT Account Number. Use PGLOB Account Number ($acctNum) for money"
            return
        }
        if (txType in listOf("SEND", "AIRTIME", "DATA", "BILL", "WITHDRAW") && amount > limit) {
            _toastMessage.value = "Exceeds daily limit of $currency${limit.toInt()}"
            return
        }
        if (txType in listOf("SEND", "AIRTIME", "DATA", "BILL") && profile.walletBalance < amount) {
            _toastMessage.value = "Insufficient wallet balance. Tap Add Money or Add Test Money first!"
            return
        }

        viewModelScope.launch {
            val walletDelta = when (txType) {
                "ADD" -> amount
                "SEND", "AIRTIME", "DATA", "BILL" -> -amount
                else -> 0.0
            }
            if (walletDelta != 0.0) {
                settingsStore.updateLocalWalletAndCoins(walletDelta = walletDelta)
                if (hasFirebaseAuth) {
                    repository.updateCoinsAndWallet(walletDelta = walletDelta)
                }
            }
            val status = if (txType == "WITHDRAW") "PENDING" else "PAID"
            val remoteTxId = if (hasFirebaseAuth) {
                repository.recordTransaction(
                    txType = txType,
                    amount = amount,
                    fee = fee,
                    currency = currency,
                    method = method,
                    destination = destination,
                    status = status,
                    note = note
                ).getOrNull()
            } else null
            val txId = remoteTxId ?: "PG_${System.currentTimeMillis().toString().takeLast(8)}"
            val txItem = PayTransaction(
                id = txId,
                userId = currentUserId,
                txType = txType,
                amount = amount,
                fee = fee,
                currency = currency,
                method = method,
                destination = destination,
                status = status,
                note = note
            )
            _localTransactions.value = listOf(txItem) + _localTransactions.value
            _lastCompletedReceipt.value = txItem
            if (txType == "ADD") {
                addNotification("Payment Received 💰", "Wallet credited with $currency${amount.toInt()} via $method.", "PAY")
            } else {
                addNotification("Payment Sent ✅", "$currency${amount.toInt()} to $destination ($method).", "PAY")
            }
            if (chatIdForPacket != null) {
                val msgType = if (txType == "SEND") "MONEY" else "MONEY_REQUEST"
                val text = if (txType == "SEND") {
                    "You received $currency${amount.toInt()} from @${profile.username}"
                } else {
                    "@${profile.username} requested $currency${amount.toInt()} for $note"
                }
                sendChatMessage(
                    chatId = chatIdForPacket,
                    memberUids = chatMembers,
                    text = text,
                    messageType = msgType,
                    amount = amount
                )
            }
            _toastMessage.value = when (txType) {
                "WITHDRAW" -> "Withdrawal Request Sent! You will receive money in 24 hours."
                "ADD" -> "Wallet funded with $currency${amount.toInt()}!"
                else -> "$method successful! Receipt ID: ${txId.take(10)}"
            }
        }
    }

    fun requestWithdrawCoins(
        coinsToWithdraw: Long,
        amountCurrency: Double,
        feeCurrency: Double,
        method: String,
        destination: String
    ) {
        val currentCoins = myProfile.value?.coins ?: 0L
        if (currentCoins < 5000L || currentCoins < coinsToWithdraw) {
            _toastMessage.value = "You need 5000 coins to withdraw, keep earning!"
            return
        }
        viewModelScope.launch {
            settingsStore.updateLocalWalletAndCoins(coinsDelta = -coinsToWithdraw)
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = -coinsToWithdraw)
                repository.recordTransaction(
                    txType = "WITHDRAW",
                    amount = amountCurrency,
                    fee = feeCurrency,
                    currency = myProfile.value?.currency ?: "₦",
                    method = method,
                    destination = destination,
                    status = "PENDING",
                    note = "Coin withdrawal ($coinsToWithdraw coins)"
                )
            }
            _localTransactions.value = listOf(
                PayTransaction(
                    id = "wdr_${UUID.randomUUID().toString().take(8)}",
                    userId = currentUserId,
                    txType = "WITHDRAW",
                    amount = amountCurrency,
                    fee = feeCurrency,
                    currency = myProfile.value?.currency ?: "₦",
                    method = method,
                    destination = destination,
                    status = "PENDING",
                    note = "Coin withdrawal ($coinsToWithdraw coins)"
                )
            ) + _localTransactions.value
            _toastMessage.value = "Withdrawal Request Sent! You will receive money in 24 hours."
        }
    }

    fun claimEarnTask(taskName: String, rewardCoins: Long, isDailyLogin: Boolean = false, isSpin: Boolean = false) {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000L)
        if (isDailyLogin && localSettings.value.lastDailyLoginDay == today) {
            _toastMessage.value = "Daily Login already claimed today! Come back tomorrow."
            return
        }
        if (isSpin && localSettings.value.lastSpinEpochDay == today) {
            _toastMessage.value = "Daily Spin used today! Try Lucky Dice or Quiz."
            return
        }
        viewModelScope.launch {
            if (isDailyLogin) settingsStore.setLastDailyLoginDay(today)
            if (isSpin) settingsStore.setLastSpinDay(today)
            val diamonds = rewardCoins / 100L
            settingsStore.updateLocalWalletAndCoins(coinsDelta = rewardCoins, diamondsDelta = diamonds)
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = rewardCoins, diamondsDelta = diamonds)
            }
            _toastMessage.value = "$taskName: +$rewardCoins coins added!"
        }
    }

    fun upgradeVip(tier: String, costCoins: Long) {
        val currentCoins = myProfile.value?.coins ?: 0L
        if (currentCoins < costCoins) {
            _toastMessage.value = "Need $costCoins coins for $tier VIP!"
            return
        }
        viewModelScope.launch {
            settingsStore.updateLocalWalletAndCoins(coinsDelta = -costCoins, vipTier = tier)
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = -costCoins, vipTier = tier)
            }
            _toastMessage.value = "Upgraded to $tier VIP!"
        }
    }

    fun enableDatingProfile(age: Int, gender: String, lookingFor: String, bio: String, city: String, goal: String) {
        if (age < 18) {
            _toastMessage.value = "You must be 18+ to use Dating"
            return
        }
        viewModelScope.launch {
            val current = myProfile.value
            if (current != null && hasFirebaseAuth) {
                repository.saveCompleteProfile(
                    profile = current.copy(datingEnabled = true),
                    age = age,
                    gender = gender,
                    lookingFor = lookingFor,
                    bio = bio,
                    city = city,
                    goal = goal
                )
            }
            _toastMessage.value = "Dating Profile is now LIVE!"
        }
    }

    fun disableDatingProfile() {
        viewModelScope.launch {
            val current = myProfile.value
            if (current != null && hasFirebaseAuth) {
                repository.saveCompleteProfile(current.copy(datingEnabled = false))
            }
            _toastMessage.value = "Dating Profile turned OFF"
        }
    }

    fun swipeDating(target: PublicProfile, action: String, onMatch: (PublicProfile) -> Unit) {
        viewModelScope.launch {
            if (action == "SUPER_LIKE") {
                val coins = myProfile.value?.coins ?: 0L
                if (coins < 50L) {
                    _toastMessage.value = "Super Like costs 50 coins!"
                    return@launch
                }
                settingsStore.updateLocalWalletAndCoins(coinsDelta = -50L)
                if (hasFirebaseAuth) {
                    repository.updateCoinsAndWallet(coinsDelta = -50L)
                }
            }
            if (hasFirebaseAuth) {
                val res = repository.swipeDatingProfile(target, action)
                if (res.getOrNull() == true) {
                    onMatch(target)
                }
            } else if (action != "PASS") {
                onMatch(target)
            }
        }
    }

    fun boostDatingProfile() {
        val coins = myProfile.value?.coins ?: 0L
        if (coins < 200L) {
            _toastMessage.value = "Boost costs 200 coins!"
            return
        }
        viewModelScope.launch {
            settingsStore.updateLocalWalletAndCoins(coinsDelta = -200L)
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = -200L)
            }
            _toastMessage.value = "Profile boosted for 1 hour!"
        }
    }

    fun postStatus(
        caption: String,
        mediaType: String = "TEXT",
        mediaUri: String = "",
        privacyMode: String = "PUBLIC"
    ) {
        viewModelScope.launch {
            val profile = myProfile.value
            val displayName = profile?.fullName?.ifBlank { initialDisplayName } ?: "Me"
            val username = profile?.username?.ifBlank { "pglob_user" } ?: "pglob_user"
            val flag = profile?.countryFlag ?: "🌍"
            val isPublic = privacyMode != "PRIVATE"
            val res = if (hasFirebaseAuth) {
                repository.postStatus(
                    displayName = displayName,
                    username = username,
                    countryFlag = flag,
                    mediaType = mediaType,
                    caption = caption,
                    mediaUri = mediaUri,
                    isPublic = isPublic,
                    privacyMode = privacyMode
                )
            } else Result.success("st_local_${UUID.randomUUID().toString().take(10)}")
            val newId = res.getOrNull() ?: "st_local_${UUID.randomUUID().toString().take(10)}"
            val localItem = StatusPost(
                id = newId,
                userId = currentUserId,
                displayName = displayName,
                username = username,
                countryFlag = flag,
                mediaType = mediaType,
                caption = caption,
                mediaUri = mediaUri,
                avatarUri = profile?.photoUri.orEmpty(),
                isPublic = isPublic,
                privacyMode = privacyMode,
                likesCount = 0,
                reshareCount = 0,
                viewsCount = 1,
                timeLabel = "Just now"
            )
            prependLocalStatus(localItem)
            _toastMessage.value = if (privacyMode == "PRIVATE") {
                "Private Status posted (Only Share With) 🔒"
            } else {
                "Status shared for 24h! 🌍"
            }
        }
    }

    fun recordStatusView(status: StatusPost) {
        if (status.id.isBlank() || localSettings.value.viewedStatusIds.contains(status.id)) return
        viewModelScope.launch {
            settingsStore.markStatusViewed(status.id)
        }
    }

    fun toggleLikeStatus(status: StatusPost, emoji: String = "❤️", forceReaction: Boolean = false) {
        viewModelScope.launch {
            val map = parseReactionsMap(localSettings.value.statusReactionsJson).toMutableMap()
            val existing = map[status.id]
            val myName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Paul"
            if (!forceReaction && existing != null) {
                map.remove(status.id)
                settingsStore.saveStatusReactionsJson(encodeReactionsMap(map))
            } else {
                map[status.id] = emoji
                settingsStore.saveStatusReactionsJson(encodeReactionsMap(map))
                recordStatusInteraction(status.id, "LIKED", emoji)
                addNotification(
                    title = "PGLOB APP: $myName liked ${status.displayName}'s Status $emoji",
                    body = if (emoji == "❤️") "$myName liked your Status ❤️ — Tap to see viewers"
                    else "$myName reacted $emoji to your Status — Tap to see viewers",
                    category = "STATUS_LIKE",
                    targetStatusId = status.id
                )
            }
        }
    }

    fun getStatusViewersAndLikers(status: StatusPost): Pair<List<StatusViewerRecord>, List<StatusViewerRecord>> {
        val myReaction = statusReactions.value[status.id]
        val myName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Paul Odogun"
        val myPhoto = myProfile.value?.photoUri.orEmpty()

        val baseLikers = mutableListOf<StatusViewerRecord>()
        if (myReaction != null) {
            baseLikers += StatusViewerRecord(
                uid = currentUserId,
                name = "$myName (You)",
                photoUri = myPhoto,
                reaction = myReaction,
                isLiked = true,
                viewedAt = "Just now"
            )
        }
        val seedLikers = listOf(
            StatusViewerRecord("u_blessing", "Blessing Gold 🇳🇬", reaction = "❤️", isLiked = true, viewedAt = "1m ago"),
            StatusViewerRecord("u_amina", "Amina Yusuf 🇳🇬", reaction = "❤️", isLiked = true, viewedAt = "2m ago"),
            StatusViewerRecord("u_marcus", "Marcus Vance 🇺🇸", reaction = "🔥", isLiked = true, viewedAt = "4m ago"),
            StatusViewerRecord("u_kwame", "Kwame Mensah 🇬🇭", reaction = "❤️", isLiked = true, viewedAt = "7m ago"),
            StatusViewerRecord("u_priya", "Priya Sharma 🇮🇳", reaction = "😍", isLiked = true, viewedAt = "11m ago"),
            StatusViewerRecord("u_chidi", "Chidi Okafor 🇳🇬", reaction = "👍", isLiked = true, viewedAt = "15m ago"),
            StatusViewerRecord("u_sofia", "Sofia Garcia 🇪🇸", reaction = "❤️", isLiked = true, viewedAt = "20m ago"),
            StatusViewerRecord("u_liam", "Liam Carter 🇬🇧", reaction = "❤️", isLiked = true, viewedAt = "24m ago"),
            StatusViewerRecord("u_aisha", "Aisha Khan 🇦🇪", reaction = "❤️", isLiked = true, viewedAt = "29m ago"),
            StatusViewerRecord("u_kofi", "DJ Kofi 🇬🇭", reaction = "😂", isLiked = true, viewedAt = "35m ago"),
            StatusViewerRecord("u_elena", "Prof. Elena 🇬🇧", reaction = "❤️", isLiked = true, viewedAt = "42m ago")
        )
        val neededSeedLikers = (status.likesCount - baseLikers.size).coerceAtLeast(0)
        baseLikers += seedLikers.take(neededSeedLikers)

        val baseViewers = mutableListOf(
            StatusViewerRecord(currentUserId, "$myName (You)", myPhoto, myReaction ?: "", myReaction != null, "Just now"),
            StatusViewerRecord("v_1", "Blessing Gold 🇳🇬", reaction = "❤️", isLiked = true, viewedAt = "1m ago"),
            StatusViewerRecord("v_2", "Amina Yusuf 🇳🇬", reaction = "❤️", isLiked = true, viewedAt = "2m ago"),
            StatusViewerRecord("v_3", "Marcus Vance 🇺🇸", reaction = "🔥", isLiked = true, viewedAt = "4m ago"),
            StatusViewerRecord("v_4", "Kwame Mensah 🇬🇭", viewedAt = "6m ago"),
            StatusViewerRecord("v_5", "Priya Sharma 🇮🇳", viewedAt = "10m ago"),
            StatusViewerRecord("v_6", "Tunde Bakare 🇳🇬", viewedAt = "14m ago"),
            StatusViewerRecord("v_7", "Fatima Bello 🇳🇬", viewedAt = "18m ago"),
            StatusViewerRecord("v_8", "Daniel Kim 🇰🇷", viewedAt = "22m ago"),
            StatusViewerRecord("v_9", "Zara Fan Club 🌍", viewedAt = "30m ago")
        )
        return baseLikers to baseViewers
    }

    fun replyToStatus(
        status: StatusPost,
        replyText: String,
        messageType: String = "TEXT",
        mediaUri: String = "",
        onChatReady: ((String) -> Unit)? = null
    ) {
        val cleanReply = replyText.trim()
        if (cleanReply.isBlank() && mediaUri.isBlank()) return
        viewModelScope.launch {
            val myName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Blessing Gold"
            val isSelf = status.userId == currentUserId
            val ownerUid = if (isSelf) currentUserId else status.userId.ifBlank { "owner_${status.id}" }
            val chatId = if (isSelf) {
                "self_notes_${currentUserId}".take(100)
            } else {
                "dm_${listOf(currentUserId, ownerUid).sorted().joinToString("_")}".take(100)
            }
            val chatTitle = if (isSelf) {
                "My Status Notes 📝 (Self)"
            } else {
                "${status.displayName} ${status.countryFlag}"
            }
            val members = if (isSelf) listOf(currentUserId) else listOf(currentUserId, ownerUid)
            if (_localChats.value.none { it.id == chatId }) {
                _localChats.value = listOf(
                    ChatThread(
                        id = chatId,
                        title = chatTitle,
                        isGroup = false,
                        lastMessage = cleanReply.ifBlank { "Status reply" },
                        memberUids = members,
                        peerFlag = status.countryFlag,
                        peerOnline = true
                    )
                ) + _localChats.value
            }
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(
                    chatId = chatId,
                    title = chatTitle,
                    memberUids = members,
                    isGroup = false,
                    peerFlag = status.countryFlag
                )
            }
            val quoted = StatusQuoteCodec.encodeReply(
                statusId = status.id,
                ownerName = if (isSelf) "My Status (Self Note)" else status.displayName,
                ownerHandle = status.username.ifBlank { status.displayName.replace(" ", "") },
                mediaType = status.mediaType,
                mediaUri = status.mediaUri,
                caption = status.caption
            )
            val bodyText = when {
                messageType == "VOICE" -> cleanReply.ifBlank { "🎙️ Voice Reply" }
                else -> cleanReply
            }
            sendChatMessage(
                chatId = chatId,
                memberUids = members,
                text = bodyText,
                messageType = messageType,
                mediaUri = mediaUri,
                replyToText = quoted
            )
            val notifPreview = if (messageType == "VOICE") "Voice Note 🎙️ $bodyText" else bodyText
            recordStatusInteraction(status.id, "REPLIED", notifPreview.take(60))
            addNotification(
                title = if (isSelf) "PGLOB APP: Saved Self Note on Status" else "PGLOB APP: $myName replied to your Status",
                body = "'$notifPreview' — Tap to open Chat",
                category = "STATUS_REPLY",
                targetChatId = chatId,
                targetStatusId = status.id
            )
            _toastMessage.value = if (isSelf) {
                "Saved as Self Note in Chat 📝"
            } else {
                "Reply sent to ${status.displayName}'s Chat 💬"
            }
            onChatReady?.invoke(chatId)
        }
    }

    fun reshareStatusToMyStatus(original: StatusPost, customCaption: String) {
        if (original.privacyMode == "PRIVATE") {
            _toastMessage.value = "This Status cannot be reshared - Private"
            return
        }
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "Paul"
            val myHandle = profile?.username?.ifBlank { "paul" } ?: "paul"
            val origOwnerName = original.resharedFromName.ifBlank { original.displayName }
            val origOwnerHandle = original.resharedFromHandle.ifBlank {
                original.username.ifBlank { original.displayName.replace(" ", "") }
            }
            val finalCaption = if (customCaption.isNotBlank()) {
                "${customCaption.trim()}\n\n${original.caption}"
            } else {
                original.caption
            }
            val res = if (hasFirebaseAuth) {
                repository.postStatus(
                    displayName = myName,
                    username = myHandle,
                    countryFlag = profile?.countryFlag ?: "🌍",
                    mediaType = original.mediaType,
                    caption = finalCaption.take(300),
                    mediaUri = original.mediaUri,
                    isPublic = true,
                    privacyMode = "PUBLIC",
                    resharedFromName = origOwnerName,
                    resharedFromHandle = origOwnerHandle,
                    originalStatusId = original.originalStatusId.ifBlank { original.id }
                )
            } else Result.success("st_reshare_${UUID.randomUUID().toString().take(10)}")
            val newId = res.getOrNull() ?: "st_reshare_${UUID.randomUUID().toString().take(10)}"
            val localReshare = StatusPost(
                id = newId,
                userId = currentUserId,
                displayName = myName,
                username = myHandle,
                countryFlag = profile?.countryFlag ?: "🌍",
                mediaType = original.mediaType,
                caption = finalCaption.take(300),
                mediaUri = original.mediaUri,
                avatarUri = profile?.photoUri.orEmpty(),
                isPublic = true,
                privacyMode = "PUBLIC",
                resharedFromName = origOwnerName,
                resharedFromHandle = origOwnerHandle,
                originalStatusId = original.originalStatusId.ifBlank { original.id },
                likesCount = 0,
                reshareCount = 0,
                viewsCount = 1,
                timeLabel = "Just now"
            )
            prependLocalStatus(localReshare)
            incrementStatusReshareCount(original.id)
            recordStatusInteraction(original.id, "RESHARED", "Reshared to Status")
            addNotification(
                title = "PGLOB APP: $myName reshared your Status 🔁",
                body = "Reshared from @$origOwnerHandle • '${original.caption.take(50)}'",
                category = "STATUS_RESHARE",
                targetStatusId = original.id
            )
            _toastMessage.value = "Reshared to My Status! 🔁"
        }
    }

    fun sendStatusToChat(
        status: StatusPost,
        targetChatId: String,
        targetTitle: String,
        targetMemberUids: List<String>,
        onSentOpenChat: ((String) -> Unit)? = null
    ) {
        if (status.privacyMode == "PRIVATE") {
            _toastMessage.value = "This Status cannot be reshared - Private"
            return
        }
        viewModelScope.launch {
            val origOwnerName = status.resharedFromName.ifBlank { status.displayName }
            val origOwnerHandle = status.resharedFromHandle.ifBlank {
                status.username.ifBlank { status.displayName.replace(" ", "") }
            }
            val members = (targetMemberUids + currentUserId).distinct()
            if (_localChats.value.none { it.id == targetChatId }) {
                _localChats.value = listOf(
                    ChatThread(
                        id = targetChatId,
                        title = targetTitle,
                        isGroup = targetChatId.startsWith("grp_") || targetChatId.startsWith("lounge_"),
                        lastMessage = "Forwarded Status",
                        memberUids = members,
                        peerFlag = status.countryFlag,
                        peerOnline = true
                    )
                ) + _localChats.value
            }
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(
                    chatId = targetChatId,
                    title = targetTitle,
                    memberUids = members,
                    isGroup = targetChatId.startsWith("grp_") || targetChatId.startsWith("lounge_"),
                    peerFlag = status.countryFlag
                )
            }
            val quoted = StatusQuoteCodec.encodeForward(
                statusId = status.id,
                ownerName = origOwnerName,
                ownerHandle = origOwnerHandle,
                mediaType = status.mediaType,
                mediaUri = status.mediaUri,
                caption = status.caption
            )
            val msgType = if (status.mediaType == "IMAGE" && status.mediaUri.isNotBlank()) "IMAGE" else "TEXT"
            val text = "🔁 Forwarded from $origOwnerName Status\n${status.caption}"
            sendChatMessage(
                chatId = targetChatId,
                memberUids = members,
                text = text,
                messageType = msgType,
                mediaUri = if (msgType == "IMAGE") status.mediaUri else "",
                replyToText = quoted
            )
            incrementStatusReshareCount(status.id)
            recordStatusInteraction(status.id, "RESHARED", "Shared to $targetTitle")
            _toastMessage.value = "Status forwarded to $targetTitle! 💬"
            onSentOpenChat?.invoke(targetChatId)
        }
    }

    private suspend fun incrementStatusReshareCount(statusId: String) {
        val map = parseIntMap(localSettings.value.statusReshareCountsJson).toMutableMap()
        map[statusId] = (map[statusId] ?: 0) + 1
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        settingsStore.saveStatusReshareCountsJson(obj.toString())
    }

    private suspend fun prependLocalStatus(item: StatusPost) {
        val current = parseLocalStatuses(localSettings.value.localStatusesJson)
        val updated = (listOf(item) + current).take(30)
        settingsStore.saveLocalStatusesJson(encodeLocalStatuses(updated))
    }

    private fun defaultGlobalStatuses(): List<StatusPost> {
        return listOf(
            StatusPost(
                id = "st_zara123",
                userId = "user_zara123",
                displayName = "Zara Bella",
                username = "ZaraBella",
                countryFlag = "🇳🇬",
                mediaType = "TEXT",
                caption = "Golden hour glam in Lagos ✨👑 Ready for PGLOB Live tonight!",
                mediaUri = "",
                isPublic = true,
                privacyMode = "PUBLIC",
                likesCount = 11,
                reshareCount = 4,
                viewsCount = 45,
                timeLabel = "28 minutes ago"
            ),
            StatusPost(
                id = "st_amina",
                userId = "amina_ng",
                displayName = "Amina Yusuf",
                username = "amina_lagos",
                countryFlag = "🇳🇬",
                mediaType = "TEXT",
                caption = "Just paid my Benin Ladies Ajo round on PGLOB Escrow ✅💚",
                mediaUri = "",
                isPublic = true,
                privacyMode = "PUBLIC",
                likesCount = 18,
                reshareCount = 3,
                viewsCount = 62,
                timeLabel = "8m ago"
            ),
            StatusPost(
                id = "st_marcus",
                userId = "marcus_us",
                displayName = "Marcus Vance",
                username = "marcus_nyc",
                countryFlag = "🇺🇸",
                mediaType = "VIDEO",
                caption = "Live Music Room tonight at 9 PM GMT 🎶🔥",
                mediaUri = "",
                isPublic = true,
                privacyMode = "PUBLIC",
                likesCount = 24,
                reshareCount = 7,
                viewsCount = 89,
                timeLabel = "15m ago"
            ),
            StatusPost(
                id = "st_priya",
                userId = "priya_in",
                displayName = "Priya Sharma",
                username = "priya_tech",
                countryFlag = "🇮🇳",
                mediaType = "TEXT",
                caption = "Earned 500 coins on PGLOB Quiz today 💎 Who is beating my score?",
                mediaUri = "",
                isPublic = true,
                privacyMode = "PUBLIC",
                likesCount = 9,
                reshareCount = 2,
                viewsCount = 31,
                timeLabel = "32m ago"
            ),
            StatusPost(
                id = "st_blessing_priv",
                userId = "blessing_ng",
                displayName = "Blessing Gold",
                username = "blessing_gold",
                countryFlag = "🇳🇬",
                mediaType = "TEXT",
                caption = "Private VIP circle update 🔒 — Secret birthday location tonight!",
                mediaUri = "",
                isPublic = true,
                privacyMode = "PRIVATE",
                likesCount = 5,
                reshareCount = 0,
                viewsCount = 6,
                timeLabel = "1h ago"
            )
        )
    }

    private fun parseReactionsMap(json: String): Map<String, String> = try {
        val obj = JSONObject(json)
        buildMap {
            obj.keys().forEach { key ->
                val value = obj.optString(key)
                if (value.isNotBlank()) put(key, value)
            }
        }
    } catch (_: Exception) {
        emptyMap()
    }

    private fun encodeReactionsMap(map: Map<String, String>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun parseIntMap(json: String): Map<String, Int> = try {
        val obj = JSONObject(json)
        buildMap {
            obj.keys().forEach { key ->
                put(key, obj.optInt(key, 0))
            }
        }
    } catch (_: Exception) {
        emptyMap()
    }

    private fun parseLocalStatuses(json: String): List<StatusPost> = try {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    StatusPost(
                        id = o.optString("id"),
                        userId = o.optString("userId"),
                        displayName = o.optString("displayName"),
                        username = o.optString("username"),
                        countryFlag = o.optString("countryFlag", "🌍"),
                        mediaType = o.optString("mediaType", "TEXT"),
                        caption = o.optString("caption"),
                        mediaUri = o.optString("mediaUri"),
                        avatarUri = o.optString("avatarUri"),
                        isPublic = o.optBoolean("isPublic", true),
                        privacyMode = o.optString("privacyMode", "PUBLIC"),
                        resharedFromName = o.optString("resharedFromName"),
                        resharedFromHandle = o.optString("resharedFromHandle"),
                        originalStatusId = o.optString("originalStatusId"),
                        likesCount = o.optInt("likesCount", 0),
                        reshareCount = o.optInt("reshareCount", 0),
                        viewsCount = o.optInt("viewsCount", 1),
                        timeLabel = o.optString("timeLabel", "Just now")
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun encodeLocalStatuses(items: List<StatusPost>): String {
        val arr = JSONArray()
        items.forEach { st ->
            arr.put(
                JSONObject().apply {
                    put("id", st.id)
                    put("userId", st.userId)
                    put("displayName", st.displayName)
                    put("username", st.username)
                    put("countryFlag", st.countryFlag)
                    put("mediaType", st.mediaType)
                    put("caption", st.caption)
                    put("mediaUri", st.mediaUri)
                    put("avatarUri", st.avatarUri)
                    put("isPublic", st.isPublic)
                    put("privacyMode", st.privacyMode)
                    put("resharedFromName", st.resharedFromName)
                    put("resharedFromHandle", st.resharedFromHandle)
                    put("originalStatusId", st.originalStatusId)
                    put("likesCount", st.likesCount)
                    put("reshareCount", st.reshareCount)
                    put("viewsCount", st.viewsCount)
                    put("timeLabel", st.timeLabel)
                }
            )
        }
        return arr.toString()
    }

    // ==================== STATUS NOTES (WRITE / VIEW / LIKE / RESHARE / REPLACE / EDIT / DELETE) ====================

    fun postStatusNote(
        text: String,
        bgColor: String = "#00A884",
        music: String = "",
        mood: String = "",
        privacy: String = "EVERYONE",
        customAllowedUids: List<String> = emptyList(),
        durationHours: Int = 24
    ) {
        val cleanText = StatusNoteCodec.clampNoteText(text.trim())
        if (cleanText.isBlank()) {
            _toastMessage.value = "Write something on your mind first"
            return
        }
        viewModelScope.launch {
            val profile = myProfile.value
            val isPro = (profile?.vipTier ?: localSettings.value.localVipTier) != "FREE" || isSuperAdmin()
            val effectiveHours = if (durationHours == 48 && isPro) 48 else 24
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myAvatar = profile?.photoUri.orEmpty()
            val newNote = StatusNoteCodec.createNewNote(
                userId = currentUserId,
                authorName = myName,
                authorAvatarUri = myAvatar,
                text = cleanText,
                bgColor = bgColor,
                music = music,
                mood = mood,
                privacy = privacy,
                customAllowedUids = customAllowedUids,
                durationHours = effectiveHours
            )
            val currentLocal = StatusNoteCodec.parseNotesJson(localSettings.value.statusNotesJson)
            val removedIds = currentLocal.filter { it.userId == currentUserId }.map { it.id }.toSet()
            _deletedStatusNoteIds.value = _deletedStatusNoteIds.value + removedIds
            val updated = listOf(newNote) + currentLocal.filterNot { it.userId == currentUserId }
            settingsStore.saveStatusNotesJson(StatusNoteCodec.encodeNotesJson(updated))
            settingsStore.setDismissedExpiredNoteId("")
            if (hasFirebaseAuth) {
                repository.postOrReplaceStatusNote(newNote)
            }
            _toastMessage.value = if (durationHours == 48 && !isPro) {
                "Note posted for 24h (Upgrade to PGLOB PRO for 48h Notes)"
            } else {
                "Status Note posted for ${effectiveHours}h!"
            }
        }
    }

    fun editStatusNote(
        existing: StatusNote,
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int
    ) {
        val cleanText = StatusNoteCodec.clampNoteText(text.trim())
        if (cleanText.isBlank()) {
            _toastMessage.value = "Note text cannot be empty"
            return
        }
        viewModelScope.launch {
            val profile = myProfile.value
            val isPro = (profile?.vipTier ?: localSettings.value.localVipTier) != "FREE" || isSuperAdmin()
            val effectiveHours = if (durationHours == 48 && isPro) 48 else 24
            val updatedNote = StatusNoteCodec.editNoteKeepingSameId(
                existing = existing,
                text = cleanText,
                bgColor = bgColor,
                music = music,
                mood = mood,
                privacy = privacy,
                customAllowedUids = customAllowedUids,
                durationHours = effectiveHours
            )
            upsertLocalStatusNote(updatedNote)
            if (hasFirebaseAuth) {
                repository.editStatusNote(
                    noteId = updatedNote.id,
                    text = updatedNote.text,
                    bgColor = updatedNote.bgColor,
                    music = updatedNote.music,
                    mood = updatedNote.mood,
                    privacy = updatedNote.privacy,
                    customAllowedUids = updatedNote.customAllowedUids,
                    durationHours = updatedNote.durationHours,
                    expiresAt = updatedNote.expiresAt ?: Timestamp.now()
                )
            }
            _toastMessage.value = "Status Note updated ✔️"
        }
    }

    fun deleteStatusNote(noteId: String) {
        if (noteId.isBlank()) return
        viewModelScope.launch {
            _deletedStatusNoteIds.value = _deletedStatusNoteIds.value + noteId
            val currentLocal = StatusNoteCodec.parseNotesJson(localSettings.value.statusNotesJson)
            settingsStore.saveStatusNotesJson(
                StatusNoteCodec.encodeNotesJson(currentLocal.filterNot { it.id == noteId })
            )
            settingsStore.setDismissedExpiredNoteId(noteId)
            if (hasFirebaseAuth) {
                repository.deleteStatusNote(noteId)
            }
            _toastMessage.value = "Status Note deleted"
        }
    }

    fun toggleLikeStatusNote(note: StatusNote) {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myPhoto = profile?.photoUri.orEmpty()
            val nowMs = System.currentTimeMillis()
            val alreadyLiked = note.likes.any { it.uid == currentUserId }
            val nextLikes = if (alreadyLiked) {
                note.likes.filterNot { it.uid == currentUserId }
            } else {
                listOf(StatusNoteActor(currentUserId, myName, myPhoto, nowMs)) + note.likes.filterNot { it.uid == currentUserId }
            }
            val updated = note.copy(likes = nextLikes)
            upsertLocalStatusNote(updated)
            if (hasFirebaseAuth) {
                repository.updateStatusNoteInteractions(noteId = note.id, likes = nextLikes)
            }
            if (!alreadyLiked && note.userId != currentUserId) {
                addNotification(
                    title = "$myName liked your Note ❤️",
                    body = "\"${note.text.take(60)}\" — Tap to view Note",
                    category = "STATUS_NOTE_LIKE",
                    targetStatusId = note.id
                )
            }
        }
    }

    fun recordStatusNoteView(note: StatusNote) {
        if (note.id.isBlank() || note.userId == currentUserId) return
        if (note.viewers.any { it.uid == currentUserId }) return
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myPhoto = profile?.photoUri.orEmpty()
            val nextViewers = (listOf(StatusNoteActor(currentUserId, myName, myPhoto, System.currentTimeMillis())) + note.viewers)
                .distinctBy { it.uid }
                .take(500)
            upsertLocalStatusNote(note.copy(viewers = nextViewers))
            if (hasFirebaseAuth) {
                repository.updateStatusNoteInteractions(noteId = note.id, viewers = nextViewers)
            }
        }
    }

    fun reshareStatusNoteToMyNote(original: StatusNote) {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myPhoto = profile?.photoUri.orEmpty()
            val nowMs = System.currentTimeMillis()
            val nextReshares = listOf(StatusNoteActor(currentUserId, myName, myPhoto, nowMs)) + original.reshares
            upsertLocalStatusNote(original.copy(reshares = nextReshares))
            if (hasFirebaseAuth) {
                repository.updateStatusNoteInteractions(noteId = original.id, reshares = nextReshares)
            }
            val isPro = (profile?.vipTier ?: localSettings.value.localVipTier) != "FREE" || isSuperAdmin()
            val newNote = StatusNoteCodec.buildResharedNote(
                original = original,
                resharerUid = currentUserId,
                resharerName = myName,
                resharerAvatarUri = myPhoto,
                durationHours = if (isPro) 24 else 24,
                nowMillis = nowMs
            )
            val currentLocal = StatusNoteCodec.parseNotesJson(localSettings.value.statusNotesJson)
            val removedIds = currentLocal.filter { it.userId == currentUserId }.map { it.id }.toSet()
            _deletedStatusNoteIds.value = _deletedStatusNoteIds.value + removedIds
            val merged = listOf(newNote) + currentLocal.filterNot { it.userId == currentUserId }
            settingsStore.saveStatusNotesJson(StatusNoteCodec.encodeNotesJson(merged))
            settingsStore.setDismissedExpiredNoteId("")
            if (hasFirebaseAuth) {
                repository.postOrReplaceStatusNote(newNote)
            }
            if (original.userId != currentUserId) {
                addNotification(
                    title = "$myName reshared your Note ↗️",
                    body = "\"${original.text.take(60)}\"",
                    category = "STATUS_NOTE_RESHARE",
                    targetStatusId = original.id
                )
            }
            _toastMessage.value = "Reshared to My Status Note! ↗️"
        }
    }

    fun reshareStatusNoteToMyStatus(original: StatusNote) {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myPhoto = profile?.photoUri.orEmpty()
            val nowMs = System.currentTimeMillis()
            val nextReshares = listOf(StatusNoteActor(currentUserId, myName, myPhoto, nowMs)) + original.reshares
            upsertLocalStatusNote(original.copy(reshares = nextReshares))
            if (hasFirebaseAuth) {
                repository.updateStatusNoteInteractions(noteId = original.id, reshares = nextReshares)
            }
            val origName = original.resharedFromName.ifBlank { original.authorName }
            postStatus(
                caption = "Reshared from $origName: ${original.text}".take(300),
                mediaType = "TEXT",
                privacyMode = "PUBLIC"
            )
            if (original.userId != currentUserId) {
                addNotification(
                    title = "$myName reshared your Note to Status ↗️",
                    body = "\"${original.text.take(60)}\"",
                    category = "STATUS_NOTE_RESHARE",
                    targetStatusId = original.id
                )
            }
            _toastMessage.value = "Reshared Note to My Status! 📤"
        }
    }

    fun shareStatusNoteToChat(
        note: StatusNote,
        targetChatId: String,
        targetTitle: String,
        targetMemberUids: List<String>,
        onSentOpenChat: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "PGLOB Member"
            val myPhoto = profile?.photoUri.orEmpty()
            val nowMs = System.currentTimeMillis()
            val nextReshares = listOf(StatusNoteActor(currentUserId, myName, myPhoto, nowMs)) + note.reshares
            upsertLocalStatusNote(note.copy(reshares = nextReshares))
            if (hasFirebaseAuth) {
                repository.updateStatusNoteInteractions(noteId = note.id, reshares = nextReshares)
            }
            val members = (targetMemberUids + currentUserId).distinct()
            if (_localChats.value.none { it.id == targetChatId }) {
                _localChats.value = listOf(
                    ChatThread(
                        id = targetChatId,
                        title = targetTitle,
                        isGroup = targetChatId.startsWith("grp_") || targetChatId.startsWith("lounge_"),
                        lastMessage = "Shared Status Note",
                        memberUids = members,
                        peerFlag = "📝",
                        peerOnline = true
                    )
                ) + _localChats.value
            }
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(
                    chatId = targetChatId,
                    title = targetTitle,
                    memberUids = members,
                    isGroup = targetChatId.startsWith("grp_") || targetChatId.startsWith("lounge_"),
                    peerFlag = "📝"
                )
            }
            val quoted = StatusQuoteCodec.encodeForward(
                statusId = note.id,
                ownerName = note.resharedFromName.ifBlank { note.authorName },
                ownerHandle = note.authorName.replace(" ", ""),
                mediaType = "NOTE",
                mediaUri = "",
                caption = note.text
            )
            sendChatMessage(
                chatId = targetChatId,
                memberUids = members,
                text = "↗️ Status Note from ${note.authorName}:\n\"${note.text}\"",
                messageType = "TEXT",
                replyToText = quoted
            )
            if (note.userId != currentUserId) {
                addNotification(
                    title = "$myName shared your Note ↗️",
                    body = "Shared to $targetTitle",
                    category = "STATUS_NOTE_RESHARE",
                    targetStatusId = note.id
                )
            }
            _toastMessage.value = "Note shared to $targetTitle 💬"
            onSentOpenChat?.invoke(targetChatId)
        }
    }

    fun replyToStatusNote(
        note: StatusNote,
        replyText: String,
        onChatReady: ((String) -> Unit)? = null
    ) {
        val cleanReply = replyText.trim()
        if (cleanReply.isBlank()) return
        viewModelScope.launch {
            val isSelf = note.userId == currentUserId
            val ownerUid = if (isSelf) currentUserId else note.userId.ifBlank { "note_${note.id}" }
            val chatId = if (isSelf) {
                "self_notes_${currentUserId}".take(100)
            } else {
                "dm_${listOf(currentUserId, ownerUid).sorted().joinToString("_")}".take(100)
            }
            val chatTitle = if (isSelf) "My Status Notes 📝 (Self)" else note.authorName
            val members = if (isSelf) listOf(currentUserId) else listOf(currentUserId, ownerUid)
            if (_localChats.value.none { it.id == chatId }) {
                _localChats.value = listOf(
                    ChatThread(
                        id = chatId,
                        title = chatTitle,
                        isGroup = false,
                        lastMessage = cleanReply,
                        memberUids = members,
                        peerFlag = "📝",
                        peerOnline = true
                    )
                ) + _localChats.value
            }
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(chatId, chatTitle, members, isGroup = false, peerFlag = "📝")
            }
            val quoted = StatusQuoteCodec.encodeReply(
                statusId = note.id,
                ownerName = if (isSelf) "My Status Note" else note.authorName,
                ownerHandle = note.authorName.replace(" ", ""),
                mediaType = "NOTE",
                mediaUri = "",
                caption = note.text
            )
            sendChatMessage(
                chatId = chatId,
                memberUids = members,
                text = cleanReply,
                messageType = "TEXT",
                replyToText = quoted
            )
            _toastMessage.value = if (isSelf) "Saved as Self Note 💬" else "Reply sent to ${note.authorName} 💬"
            onChatReady?.invoke(chatId)
        }
    }

    fun reportStatusNote(note: StatusNote, reason: String) {
        val cleanReason = reason.trim().ifBlank { "Inappropriate Status Note" }
        submitReport(
            targetType = "STATUS_NOTE",
            targetId = note.id,
            reason = "Note by ${note.authorName} (${note.userId}): \"${note.text.take(80)}\" — $cleanReason"
        )
    }

    fun adminDeleteStatusNote(noteId: String) {
        if (!canAccessAdminDashboard()) return
        viewModelScope.launch {
            _deletedStatusNoteIds.value = _deletedStatusNoteIds.value + noteId
            val currentLocal = StatusNoteCodec.parseNotesJson(localSettings.value.statusNotesJson)
            settingsStore.saveStatusNotesJson(
                StatusNoteCodec.encodeNotesJson(currentLocal.filterNot { it.id == noteId })
            )
            if (hasFirebaseAuth) {
                repository.deleteStatusNote(noteId)
            }
            appendAdminLog("Deleted Status Note", "Admin removed Status Note ($noteId) from moderation queue")
            _toastMessage.value = "Status Note deleted by Admin Moderation"
        }
    }

    private suspend fun upsertLocalStatusNote(note: StatusNote) {
        val currentLocal = StatusNoteCodec.parseNotesJson(localSettings.value.statusNotesJson)
        val filtered = currentLocal.filterNot { it.id == note.id }
        settingsStore.saveStatusNotesJson(StatusNoteCodec.encodeNotesJson(listOf(note) + filtered))
    }

    fun postProduct(title: String, priceCoins: Int, priceCash: Double, category: String, description: String, location: String, imageUri: String) {
        viewModelScope.launch {
            val profile = myProfile.value
            val localItem = MarketProduct(
                id = "prod_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                sellerId = currentUserId,
                sellerName = profile?.fullName?.ifBlank { initialDisplayName } ?: initialDisplayName,
                sellerFlag = profile?.countryFlag ?: "🌍",
                sellerVerified = profile?.usernameVerified ?: true,
                title = title,
                priceCoins = priceCoins,
                priceCash = priceCash,
                category = category,
                description = description,
                location = location,
                imageUri = imageUri
            )
            _localMarketProducts.value = listOf(localItem) + _localMarketProducts.value
            if (hasFirebaseAuth) {
                repository.postMarketProduct(
                    sellerName = localItem.sellerName,
                    sellerFlag = localItem.sellerFlag,
                    sellerVerified = localItem.sellerVerified,
                    title = title,
                    priceCoins = priceCoins,
                    priceCash = priceCash,
                    category = category,
                    description = description,
                    location = location,
                    imageUri = imageUri
                )
            }
            _toastMessage.value = "Product listed on PGLOB Market!"
        }
    }

    fun buyMarketProduct(product: MarketProduct) {
        val profile = myProfile.value ?: return
        if (profile.coins < product.priceCoins) {
            _toastMessage.value = "Need ${product.priceCoins} coins to buy ${product.title}!"
            return
        }
        viewModelScope.launch {
            settingsStore.updateLocalWalletAndCoins(coinsDelta = -product.priceCoins.toLong())
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = -product.priceCoins.toLong())
                repository.recordTransaction(
                    txType = "MARKET",
                    amount = product.priceCoins.toDouble(),
                    fee = product.priceCoins * 0.05,
                    currency = "COINS",
                    method = "PGLOB Market",
                    destination = product.sellerName,
                    status = "PAID",
                    note = "Bought ${product.title} (5% owner fee)"
                )
            }
            _localTransactions.value = listOf(
                PayTransaction(
                    id = "mkt_${UUID.randomUUID().toString().take(8)}",
                    userId = currentUserId,
                    txType = "MARKET",
                    amount = product.priceCoins.toDouble(),
                    fee = product.priceCoins * 0.05,
                    currency = "COINS",
                    method = "PGLOB Market",
                    destination = product.sellerName,
                    status = "PAID",
                    note = "Bought ${product.title} (5% owner fee)"
                )
            ) + _localTransactions.value
            _toastMessage.value = "Purchased ${product.title}!"
        }
    }

    suspend fun purchaseMarketProductWithEscrow(
        product: MarketProduct,
        quantity: Int,
        deliveryOption: String,
        deliveryAddress: String,
        deliveryFeeCash: Double,
        paymentMethod: String // "WALLET", "COINS", "CASH"
    ): MarketCheckoutOutcome {
        val safeQty = quantity.coerceIn(1, 20)
        if (deliveryOption != "Pickup / Meet Seller" && deliveryAddress.isBlank()) {
            return MarketCheckoutOutcome.Failure(
                reason = "Please enter your delivery address or select Pickup / Meet Seller.",
                paymentMethod = paymentMethod
            )
        }
        val profile = myProfile.value
        val currency = profile?.currency?.ifBlank { "₦" } ?: "₦"
        val availableWallet = profile?.walletBalance ?: localSettings.value.localWalletBalance
        val availableCoins = profile?.coins ?: localSettings.value.localCoins

        val totalCash = (product.priceCash * safeQty) + deliveryFeeCash
        val deliveryFeeCoins = if (deliveryFeeCash > 0.0) (deliveryFeeCash / 500.0).toInt().coerceAtLeast(5) else 0
        val totalCoins = (product.priceCoins * safeQty) + deliveryFeeCoins
        val orderDigits = (100000 + kotlin.random.Random.nextInt(900000)).toString()
        val orderNumber = "PGLOB-MKT-$orderDigits"

        return when (paymentMethod) {
            "WALLET" -> {
                if (availableWallet < totalCash) {
                    MarketCheckoutOutcome.Failure(
                        reason = "Insufficient PGLOB Wallet balance: Need $currency${String.format(Locale.US, "%,.0f", totalCash)}, available $currency${String.format(Locale.US, "%,.0f", availableWallet)}. Fund Wallet in Pay tab or switch to Coins / Cash on Delivery.",
                        paymentMethod = "PGLOB Wallet"
                    )
                } else {
                    settingsStore.updateLocalWalletAndCoins(walletDelta = -totalCash)
                    if (hasFirebaseAuth) {
                        repository.updateCoinsAndWallet(walletDelta = -totalCash)
                        repository.recordTransaction(
                            txType = "MARKET",
                            amount = totalCash,
                            fee = 0.0,
                            currency = currency,
                            method = "PGLOB Wallet Escrow",
                            destination = product.sellerName,
                            status = "PAID",
                            note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption) • Escrow Safe"
                        )
                    }
                    _localTransactions.value = listOf(
                        PayTransaction(
                            id = "mkt_${UUID.randomUUID().toString().take(8)}",
                            userId = currentUserId,
                            txType = "MARKET",
                            amount = totalCash,
                            fee = 0.0,
                            currency = currency,
                            method = "PGLOB Wallet Escrow",
                            destination = product.sellerName,
                            status = "PAID",
                            note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption)"
                        )
                    ) + _localTransactions.value
                    addNotification(
                        title = "Order Placed ($orderNumber) 🔒",
                        body = "$currency${String.format(Locale.US, "%,.0f", totalCash)} held in PGLOB Escrow for ${product.title} (${product.sellerName}).",
                        category = "PAY"
                    )
                    _toastMessage.value = "Order $orderNumber placed! Protected by PGLOB Escrow."
                    MarketCheckoutOutcome.Success(
                        orderNumber = orderNumber,
                        productTitle = product.title,
                        sellerName = product.sellerName,
                        quantity = safeQty,
                        deliveryOption = deliveryOption,
                        paymentMethod = "PGLOB Wallet ($currency)",
                        paidSummary = "$currency${String.format(Locale.US, "%,.0f", totalCash)} deducted from Wallet",
                        escrowHoldSummary = "Funds locked safely in PGLOB Escrow until you confirm delivery."
                    )
                }
            }

            "COINS" -> {
                if (availableCoins < totalCoins) {
                    MarketCheckoutOutcome.Failure(
                        reason = "Insufficient PGLOB Coins: Need $totalCoins Coins, available $availableCoins Coins. Earn more Coins in Earn/Games or switch to Wallet / Cash on Delivery.",
                        paymentMethod = "PGLOB Coins"
                    )
                } else {
                    settingsStore.updateLocalWalletAndCoins(coinsDelta = -totalCoins.toLong())
                    if (hasFirebaseAuth) {
                        repository.updateCoinsAndWallet(coinsDelta = -totalCoins.toLong())
                        repository.recordTransaction(
                            txType = "MARKET",
                            amount = totalCoins.toDouble(),
                            fee = 0.0,
                            currency = "COINS",
                            method = "PGLOB Coins Escrow",
                            destination = product.sellerName,
                            status = "PAID",
                            note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption)"
                        )
                    }
                    _localTransactions.value = listOf(
                        PayTransaction(
                            id = "mkt_${UUID.randomUUID().toString().take(8)}",
                            userId = currentUserId,
                            txType = "MARKET",
                            amount = totalCoins.toDouble(),
                            fee = 0.0,
                            currency = "COINS",
                            method = "PGLOB Coins Escrow",
                            destination = product.sellerName,
                            status = "PAID",
                            note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption)"
                        )
                    ) + _localTransactions.value
                    addNotification(
                        title = "Order Placed ($orderNumber) 🪙",
                        body = "$totalCoins Coins locked in PGLOB Escrow for ${product.title}.",
                        category = "PAY"
                    )
                    _toastMessage.value = "Order $orderNumber placed using $totalCoins Coins!"
                    MarketCheckoutOutcome.Success(
                        orderNumber = orderNumber,
                        productTitle = product.title,
                        sellerName = product.sellerName,
                        quantity = safeQty,
                        deliveryOption = deliveryOption,
                        paymentMethod = "PGLOB Coins",
                        paidSummary = "$totalCoins Coins deducted",
                        escrowHoldSummary = "Coins held safely in PGLOB Escrow until seller delivers."
                    )
                }
            }

            else -> {
                if (hasFirebaseAuth) {
                    repository.recordTransaction(
                        txType = "MARKET",
                        amount = totalCash,
                        fee = 0.0,
                        currency = currency,
                        method = "Cash on Delivery (Escrow Verified)",
                        destination = product.sellerName,
                        status = "PENDING",
                        note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption)"
                    )
                }
                _localTransactions.value = listOf(
                    PayTransaction(
                        id = "mkt_${UUID.randomUUID().toString().take(8)}",
                        userId = currentUserId,
                        txType = "MARKET",
                        amount = totalCash,
                        fee = 0.0,
                        currency = currency,
                        method = "Cash on Delivery (Escrow Verified)",
                        destination = product.sellerName,
                        status = "PENDING",
                        note = "$orderNumber • ${safeQty}x ${product.title} ($deliveryOption)"
                    )
                ) + _localTransactions.value
                addNotification(
                    title = "Cash Order Confirmed ($orderNumber) 📦",
                    body = "Pay $currency${String.format(Locale.US, "%,.0f", totalCash)} on delivery for ${product.title}.",
                    category = "PAY"
                )
                _toastMessage.value = "Cash on Delivery order $orderNumber confirmed!"
                MarketCheckoutOutcome.Success(
                    orderNumber = orderNumber,
                    productTitle = product.title,
                    sellerName = product.sellerName,
                    quantity = safeQty,
                    deliveryOption = deliveryOption,
                    paymentMethod = "Cash on Delivery",
                    paidSummary = "$currency${String.format(Locale.US, "%,.0f", totalCash)} payable on delivery",
                    escrowHoldSummary = "Order verified by PGLOB Escrow Protection — inspect item before paying rider."
                )
            }
        }
    }

    fun refreshOrdersFromFirestore() {
        viewModelScope.launch {
            _isRefreshingOrders.value = true
            _ordersSyncError.value = null
            if (!hasFirebaseAuth) {
                _isRefreshingOrders.value = false
                return@launch
            }
            val result = repository.fetchTransactionsFromFirestore(currentUserId)
            result.onSuccess { fetched ->
                _fetchedFirestoreTransactions.value = fetched
            }.onFailure { err ->
                _ordersSyncError.value = err.localizedMessage ?: "Unable to sync orders from Firestore"
            }
            _isRefreshingOrders.value = false
        }
    }

    fun markOrderDelivered(orderId: String, orderRef: String) {
        viewModelScope.launch {
            settingsStore.markOrderDelivered(orderId)
            addNotification(
                title = "Order Delivered ($orderRef) ✅",
                body = "Delivery confirmed for $orderRef. PGLOB Escrow released safely.",
                category = "PAY"
            )
            _toastMessage.value = "Order $orderRef marked as Delivered!"
        }
    }

    fun postReel(caption: String, hashtag: String, music: String) {
        viewModelScope.launch {
            val name = myProfile.value?.fullName ?: initialDisplayName
            settingsStore.updateLocalWalletAndCoins(coinsDelta = 10L)
            if (hasFirebaseAuth) {
                repository.postReel(name, caption, hashtag, music)
                repository.updateCoinsAndWallet(coinsDelta = 10L)
            }
            _toastMessage.value = "Reel posted! +10 coins earned!"
        }
    }

    fun likeReel(reel: ReelPost) {
        viewModelScope.launch {
            if (hasFirebaseAuth) {
                repository.likeReel(reel)
            }
        }
    }

    fun postJob(title: String, company: String, salary: String, jobType: String, location: String, description: String) {
        viewModelScope.launch {
            if (hasFirebaseAuth) {
                repository.postJob(title, company, salary, jobType, location, description)
            }
            _toastMessage.value = "Job posted!"
        }
    }

    fun createLiveRoom(title: String, category: String, mode: String, countryFlag: String) {
        viewModelScope.launch {
            val name = myProfile.value?.fullName ?: initialDisplayName
            if (hasFirebaseAuth) {
                repository.createLiveRoom(name, title, category, mode, countryFlag)
            }
            _toastMessage.value = "Live $mode Room started!"
        }
    }

    fun completeLearnCourse(courseId: String, rewardCoins: Int) {
        if (localSettings.value.completedCourses.contains(courseId)) {
            _toastMessage.value = "Course already completed — certificate unlocked!"
            return
        }
        viewModelScope.launch {
            settingsStore.markCourseCompleted(courseId)
            settingsStore.updateLocalWalletAndCoins(coinsDelta = rewardCoins.toLong())
            if (hasFirebaseAuth) {
                repository.updateCoinsAndWallet(coinsDelta = rewardCoins.toLong())
            }
            _toastMessage.value = "Certificate earned! +$rewardCoins coins awarded!"
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            if (hasFirebaseAuth) {
                repository.submitReport(targetType, targetId, reason)
            }
            appendAdminLog("Safety Report Submitted", "$targetType ($targetId): $reason")
            _toastMessage.value = "Report submitted to Admin Moderation."
        }
    }

    fun setDailyPayLimit(limit: Double) {
        viewModelScope.launch {
            settingsStore.setDailyPayLimit(limit)
            _toastMessage.value = "Daily Pay limit updated to ${limit.toInt()}"
        }
    }

    private val _adminUsers = MutableStateFlow<List<UserPrivateProfile>>(emptyList())
    val adminUsers: StateFlow<List<UserPrivateProfile>> = _adminUsers.asStateFlow()

    private val _adminTransactions = MutableStateFlow<List<PayTransaction>>(emptyList())
    val adminTransactions: StateFlow<List<PayTransaction>> = _adminTransactions.asStateFlow()

    private val _adminReports = MutableStateFlow<List<SafetyReport>>(emptyList())
    val adminReports: StateFlow<List<SafetyReport>> = _adminReports.asStateFlow()

    fun refreshAdminData() {
        viewModelScope.launch {
            val users = repository.fetchAdminUsers()
            _adminUsers.value = users.ifEmpty { listOfNotNull(myProfile.value) }
            val txs = repository.fetchAdminTransactions()
            _adminTransactions.value = txs.ifEmpty { transactions.value }
            val reps = repository.fetchAdminReports()
            _adminReports.value = reps.ifEmpty { myReports.value }
        }
    }

    fun adminSetUserBanned(targetUid: String, banned: Boolean) {
        viewModelScope.launch {
            val res = repository.adminSetUserBanned(targetUid, banned)
            _toastMessage.value = if (res.isSuccess) {
                refreshAdminData()
                if (banned) "User banned" else "User unbanned"
            } else {
                "Sign in with Owner Google account for global user ban"
            }
        }
    }

    fun adminGrantCoins(targetUid: String, deltaCoins: Long) {
        viewModelScope.launch {
            val res = if (targetUid == currentUserId) {
                repository.updateCoinsAndWallet(coinsDelta = deltaCoins)
            } else {
                repository.adminGrantCoins(targetUid, deltaCoins)
            }
            _toastMessage.value = if (res.isSuccess) {
                refreshAdminData()
                "Updated coins ($deltaCoins)"
            } else {
                "Owner Google account required for cross-user grant"
            }
        }
    }

    fun adminUpdateTxStatus(txId: String, status: String) {
        viewModelScope.launch {
            val res = repository.adminUpdateTransactionStatus(txId, status)
            if (res.isSuccess) {
                refreshAdminData()
                _toastMessage.value = "Transaction marked $status"
            } else {
                _toastMessage.value = "Owner Google account required to modify status"
            }
        }
    }

    fun adminUpdateReportStatus(reportId: String, status: String) {
        viewModelScope.launch {
            val res = repository.adminUpdateReportStatus(reportId, status)
            if (res.isSuccess) {
                refreshAdminData()
                _toastMessage.value = "Report marked $status"
            } else {
                _toastMessage.value = "Owner Google account required to resolve report"
            }
        }
    }

    // ==================== v15 STATUS FAVORITES, STARRED MESSAGES & 4-TAB INSIGHTS ====================

    fun toggleStatusFavorite(status: StatusPost) {
        viewModelScope.launch {
            val saved = settingsStore.toggleStatusFavorite(status.id)
            _toastMessage.value = if (saved) "Saved ${status.displayName}'s Status to Favorites" else "Removed from Status Favorites"
        }
    }

    fun toggleStarMessage(messageId: String) {
        viewModelScope.launch {
            val starred = settingsStore.toggleStarredMessage(messageId)
            _toastMessage.value = if (starred) "Message starred" else "Message unstarred"
        }
    }

    fun recordStatusInteraction(statusId: String, type: String, detailText: String = "") {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "Paul Odo"
            val myHandle = profile?.username?.ifBlank { "paulodogun" } ?: "paulodogun"
            val tracker = StatusInteractionTracker(
                statusId = statusId,
                userName = "$myName (You)",
                userHandle = myHandle,
                photoUri = profile?.photoUri.orEmpty(),
                interactionType = type,
                detailText = detailText,
                timeLabel = "Just now"
            )
            val current = statusTrackers.value.filterNot {
                it.statusId == statusId && it.interactionType == type && it.userHandle == myHandle && type in listOf("LIKED", "VIEWED")
            }
            settingsStore.saveStatusTrackersJson(ContributionAdminCodec.encodeStatusTrackers(listOf(tracker) + current))
        }
    }

    data class StatusInsightsTabs(
        val liked: List<StatusViewerRecord>,
        val replied: List<StatusViewerRecord>,
        val reshared: List<StatusViewerRecord>,
        val viewed: List<StatusViewerRecord>
    )

    fun getStatusInsightsTabs(status: StatusPost): StatusInsightsTabs {
        val (baseLikers, baseViewers) = getStatusViewersAndLikers(status)
        val live = statusTrackers.value.filter { it.statusId == status.id }
        val liveLiked = live.filter { it.interactionType == "LIKED" }.map {
            StatusViewerRecord("live_l_${it.userHandle}", it.userName, it.photoUri, it.detailText.ifBlank { "❤️" }, true, it.timeLabel)
        }
        val liveReplied = live.filter { it.interactionType == "REPLIED" }.map {
            StatusViewerRecord("live_r_${it.timeLabel}", it.userName, it.photoUri, it.detailText, false, it.timeLabel)
        } + listOf(
            StatusViewerRecord("rep_1", "Blessing Gold 🇳🇬", reaction = "Looking great! 🔥", isLiked = false, viewedAt = "4m ago"),
            StatusViewerRecord("rep_2", "Amina Yusuf 🇳🇬", reaction = "Count me in!", isLiked = false, viewedAt = "9m ago")
        )
        val liveReshared = live.filter { it.interactionType == "RESHARED" }.map {
            StatusViewerRecord("live_s_${it.timeLabel}", it.userName, it.photoUri, "🔁 Reshared", false, it.timeLabel)
        } + listOf(
            StatusViewerRecord("rs_1", "Marcus Vance 🇺🇸", reaction = "🔁 Reshared to Status", isLiked = false, viewedAt = "6m ago"),
            StatusViewerRecord("rs_2", "Kwame Mensah 🇬🇭", reaction = "🔁 Shared to Chat", isLiked = false, viewedAt = "12m ago")
        )
        return StatusInsightsTabs(
            liked = (liveLiked + baseLikers).distinctBy { it.name },
            replied = liveReplied,
            reshared = liveReshared,
            viewed = baseViewers
        )
    }

    // ==================== v15 GROUP CONTRIBUTION (AJO / ESUSU) ====================

    fun createContributionGroup(
        name: String,
        purpose: String,
        type: ContributionType,
        amountPerRound: Double,
        currency: String,
        maxMembers: Int,
        startDateLabel: String,
        endDateLabel: String,
        payoutOrderType: PayoutOrderType,
        requireLateFee: Boolean,
        lateFeeAmount: Double,
        mustPayBeforeTurn: Boolean,
        adminCanRemoveUnpaid: Boolean,
        screenshotBlockEnabled: Boolean,
        invitedProfiles: List<PublicProfile>,
        linkedChatIdOverride: String = "",
        groupPhotoUri: String = "",
        groupIconEmoji: String = "",
        rulesText: String = "",
        allowSubAdminApproveRegistration: Boolean = false,
        onCreated: (ContributionGroup) -> Unit = {}
    ) {
        viewModelScope.launch {
            val profile = myProfile.value
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: "Paul Odo"
            val myHandle = profile?.username?.ifBlank { "paulodogun" } ?: "paulodogun"
            val groupId = "ajo_${UUID.randomUUID().toString().replace("-", "").take(10)}"
            val chatId = linkedChatIdOverride.ifBlank { "grp_ajo_${groupId.takeLast(8)}" }

            val meMember = ContributionMember(
                uid = currentUserId,
                name = myName,
                handle = myHandle,
                photoUri = profile?.photoUri.orEmpty(),
                turnOrder = 1,
                role = "ADMIN",
                trustScore = 95,
                paidCurrentRound = false,
                hasCollected = false,
                joinedDateLabel = "Today"
            )
            val otherMembers = invitedProfiles
                .filter { it.userId != currentUserId }
                .take((maxMembers - 1).coerceAtLeast(1))
                .mapIndexed { idx, p ->
                    ContributionMember(
                        uid = p.userId,
                        name = p.displayName.ifBlank { "Member ${idx + 2}" },
                        handle = p.username.ifBlank { "member_${idx + 2}" },
                        photoUri = p.photoUri,
                        turnOrder = idx + 2,
                        role = "MEMBER",
                        trustScore = 85,
                        paidCurrentRound = false,
                        hasCollected = false,
                        joinedDateLabel = "Today"
                    )
                }
            val finalMembers = if (payoutOrderType == PayoutOrderType.RANDOM) {
                (listOf(meMember) + otherMembers).shuffled().mapIndexed { index, m -> m.copy(turnOrder = index + 1) }
            } else {
                listOf(meMember) + otherMembers
            }
            val effectiveRules = rulesText.trim().ifBlank {
                "1. Pay $currency${amountPerRound.toInt()} per ${type.label} cycle before due date.\n2. All funds remain locked inside PGLOB Escrow until release.\n3. Group Admin approves proof within 48h or PGLOB Auto-Approves verified Escrow payments."
            }

            val group = ContributionGroup(
                id = groupId,
                name = name.trim(),
                purpose = purpose.trim().ifBlank { "Group savings & support circle" },
                type = type,
                amountPerRound = amountPerRound,
                currency = currency,
                members = finalMembers,
                maxMembers = maxMembers,
                currentRound = 1,
                totalRounds = finalMembers.size.coerceAtLeast(1),
                totalEscrowBalance = 0.0,
                startDateLabel = startDateLabel,
                endDateLabel = endDateLabel,
                nextDueDateLabel = startDateLabel,
                daysRemaining = type.cycleDays,
                payoutOrderType = payoutOrderType,
                status = ContributionStatus.ACTIVE,
                requireLateFee = requireLateFee,
                lateFeeAmount = lateFeeAmount,
                mustPayBeforeTurn = mustPayBeforeTurn,
                adminCanRemoveUnpaid = adminCanRemoveUnpaid,
                screenshotBlockEnabled = screenshotBlockEnabled,
                linkedChatId = chatId,
                creatorUid = currentUserId,
                groupPhotoUri = groupPhotoUri,
                groupIconEmoji = groupIconEmoji,
                rulesText = effectiveRules,
                agreedMemberUids = listOf(currentUserId),
                allowSubAdminApproveRegistration = allowSubAdminApproveRegistration,
                paymentAccountMode = "PGLOB_ESCROW"
            )
            val updated = listOf(group) + contributions.value
            settingsStore.saveContributionsJson(ContributionAdminCodec.encodeContributions(updated))

            val memberUids = (finalMembers.map { it.uid } + currentUserId).distinct()
            if (_localChats.value.none { it.id == chatId }) {
                _localChats.value = listOf(
                    ChatThread(
                        id = chatId,
                        title = "${group.name} (Ajo)",
                        isGroup = true,
                        lastMessage = "🔒 PGLOB Escrow Contribution Group Created",
                        memberUids = memberUids,
                        peerFlag = groupIconEmoji.ifBlank { "💰" },
                        peerOnline = true
                    )
                ) + _localChats.value
            }
            if (hasFirebaseAuth) {
                repository.createOrOpenChat(
                    chatId = chatId,
                    title = "${group.name} (Ajo)",
                    memberUids = memberUids,
                    isGroup = true,
                    peerFlag = "💰"
                )
            }
            val cardPayload = ContributionCodec.encode(
                groupId = group.id,
                title = "💰 Contribution started! $currency${amountPerRound.toInt()} / ${type.label}",
                subtitle = "${finalMembers.size} members • Pot $currency${group.totalPotPerRound.toInt()} • Tap to view",
                badge = "AJO ESCROW"
            )
            sendChatMessage(
                chatId = chatId,
                memberUids = memberUids,
                text = "💰 Contribution started! ${group.name} ($currency${amountPerRound.toInt()} / ${type.label})",
                messageType = "TEXT",
                replyToText = cardPayload
            )
            appendAdminLog("Created Contribution Group", "${group.name} • $currency${amountPerRound.toInt()} / ${type.label}")
            _toastMessage.value = "Contribution group created & posted to chat!"
            onCreated(group)
        }
    }

    fun payContributionRound(
        groupId: String,
        paymentMethod: String,
        isLate: Boolean = false,
        proofRef: String = "",
        proofUri: String = "",
        onCompletedReceipt: (PayTransaction) -> Unit = {}
    ) {
        submitContributionPaymentWithProof(
            groupId = groupId,
            paymentMethod = paymentMethod,
            isLate = isLate,
            proofRef = proofRef,
            proofUri = proofUri,
            onCompletedReceipt = onCompletedReceipt
        )
    }

    fun agreeToContributionRules(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (group.agreedMemberUids.contains(currentUserId)) return
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(agreedMemberUids = group.agreedMemberUids + currentUserId))
            _toastMessage.value = "Group Rules accepted ✅"
        }
    }

    fun switchGroupToPglobEscrow(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isGroupCreatorOrSuperAdmin(group)) {
            _toastMessage.value = "Only Group Admin or Owner can switch payment account mode."
            return
        }
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(paymentAccountMode = "PGLOB_ESCROW"))
            appendAdminLog("Switched to PGLOB Escrow", "${group.name} moved to PGLOB Escrow protection")
            _toastMessage.value = "Switched to PGLOB Escrow 🔒"
        }
    }

    fun toggleAllowSubAdminApproveReg(groupId: String, allowed: Boolean) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isGroupCreatorOrSuperAdmin(group)) {
            _toastMessage.value = "Only Group Admin can change Sub-Admin registration permissions."
            return
        }
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(allowSubAdminApproveRegistration = allowed))
            val updatedSubs = groupSubAdmins.value.map { sub ->
                if (sub.groupId == groupId) sub.copy(canApproveNewMember = allowed) else sub
            }
            settingsStore.saveGroupSubAdminsJson(ContributionAdminCodec.encodeGroupSubAdmins(updatedSubs))
            _toastMessage.value = if (allowed) {
                "Sub-Admins can now approve new registrations"
            } else {
                "Sub-Admin registration approval turned OFF"
            }
        }
    }

    fun submitContributionPaymentWithProof(
        groupId: String,
        paymentMethod: String,
        isLate: Boolean = false,
        proofRef: String = "",
        proofUri: String = "",
        onCompletedReceipt: (PayTransaction) -> Unit = {}
    ) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (group.escrowFrozen) {
            _toastMessage.value = "Escrow is currently frozen by Owner during dispute review."
            return
        }
        if (group.status != ContributionStatus.ACTIVE) {
            _toastMessage.value = "This contribution group is currently ${group.status.label}."
            return
        }
        val me = group.members.firstOrNull { it.uid == currentUserId }
        if (me == null) {
            _toastMessage.value = "Join this contribution group first to pay."
            return
        }
        if (!group.agreedMemberUids.contains(currentUserId)) {
            _toastMessage.value = "Please read and tap 'I Agree to Rules' before paying."
            return
        }
        if (me.paidCurrentRound || me.paymentApprovalStatus == "WAITING_APPROVAL") {
            _toastMessage.value = if (me.paymentApprovalStatus == "WAITING_APPROVAL") {
                "Your payment is waiting for Admin Approval (48h countdown active)."
            } else {
                "You have already paid for Round ${group.currentRound}!"
            }
            return
        }
        val lateFee = if (group.requireLateFee && isLate) group.lateFeeAmount else 0.0
        val totalCharge = group.amountPerRound + lateFee
        val profile = myProfile.value
        val isWalletPay = paymentMethod.contains("PGLOB", ignoreCase = true) || paymentMethod.contains("Wallet", ignoreCase = true)
        if (isWalletPay && (profile?.walletBalance ?: 0.0) < totalCharge) {
            _toastMessage.value = "Insufficient PGLOB Balance. Top up Wallet or use Bank Card / Transfer."
            return
        }

        viewModelScope.launch {
            if (isWalletPay) {
                settingsStore.updateLocalWalletAndCoins(walletDelta = -totalCharge)
                if (hasFirebaseAuth) {
                    repository.updateCoinsAndWallet(walletDelta = -totalCharge)
                }
            }
            val nowMillis = System.currentTimeMillis()
            val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
            val myName = profile?.fullName?.ifBlank { initialDisplayName } ?: me.name
            val refText = proofRef.trim().ifBlank {
                if (isWalletPay) "PGLOB Wallet Escrow #${nowMillis.toString().takeLast(6)}"
                else "Transfer Proof #${nowMillis.toString().takeLast(6)}"
            }
            val maskedAcct = SecurityAndNumberCodec.maskAccountNumber(
                profile?.payAccountNumber?.ifBlank { localSettings.value.pglobAccountNumber } ?: "9000123890"
            )

            // Hold payment in WAITING_APPROVAL (Admin approves within 48h, or PGLOB Auto-Approves after 48h!)
            val updatedMembers = group.members.map { m ->
                if (m.uid == currentUserId) {
                    m.copy(
                        paymentApprovalStatus = "WAITING_APPROVAL",
                        paymentSubmittedAtMillis = nowMillis,
                        paymentProofRef = refText,
                        paymentProofUri = proofUri,
                        rejectionReason = ""
                    )
                } else m
            }
            val receiptId = "PG-PEND-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
            val pendingHist = ContributionRoundHistory(
                id = "ch_${UUID.randomUUID().toString().take(8)}",
                roundNumber = group.currentRound,
                payerUid = currentUserId,
                payerName = myName,
                amount = group.amountPerRound,
                lateFee = lateFee,
                currency = group.currency,
                paymentMethod = paymentMethod,
                paidAtLabel = nowLabel,
                wasOnTime = !isLate,
                receiptNumber = receiptId,
                escrowStatus = if (isWalletPay) "IN_ESCROW" else "PENDING_PROOF",
                approvedByName = "Waiting Admin (48h)",
                maskedAccountNumber = maskedAcct
            )

            if (hasFirebaseAuth) {
                repository.recordTransaction(
                    txType = "AJO_CONTRIBUTION",
                    amount = totalCharge,
                    fee = lateFee,
                    currency = group.currency,
                    method = paymentMethod,
                    destination = group.name,
                    status = "PENDING",
                    note = "Round ${group.currentRound} • Waiting Admin Approval (48h)"
                )
            }
            val txReceipt = PayTransaction(
                id = receiptId,
                userId = currentUserId,
                txType = "AJO_CONTRIBUTION",
                amount = totalCharge,
                fee = lateFee,
                currency = group.currency,
                method = paymentMethod,
                destination = group.name,
                status = "PENDING",
                note = "Round ${group.currentRound} • $refText"
            )
            _localTransactions.value = listOf(txReceipt) + _localTransactions.value
            _lastCompletedReceipt.value = txReceipt

            saveUpdatedGroup(
                group.copy(
                    members = updatedMembers,
                    roundHistory = listOf(pendingHist) + group.roundHistory
                )
            )
            if (group.linkedChatId.isNotBlank()) {
                val card = ContributionCodec.encode(
                    groupId = group.id,
                    title = "⏳ $myName submitted ${group.currency}${totalCharge.toInt()}",
                    subtitle = "$paymentMethod • Waiting Group Admin Approval (48h countdown)",
                    badge = "ESCROW PENDING"
                )
                sendChatMessage(
                    chatId = group.linkedChatId,
                    memberUids = group.members.map { it.uid },
                    text = "⏳ $myName submitted ${group.currency}${totalCharge.toInt()} proof ($refText) for Round ${group.currentRound}",
                    replyToText = card
                )
            }
            addNotification(
                title = "⏳ Payment Submitted (${group.name})",
                body = "Waiting for Group Admin approval within 48h. PGLOB Wallet payments auto-approve if delayed.",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            _toastMessage.value = "Submitted! Waiting for Admin Approval (48h countdown started)."
            onCompletedReceipt(txReceipt)
        }
    }

    fun adminApproveContributionPayment(groupId: String, memberUid: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isGroupCreatorOrSuperAdmin(group)) {
            _toastMessage.value = "Sub-Admins cannot approve payments. Only Group Admin or Owner can approve."
            return
        }
        val member = group.members.firstOrNull { it.uid == memberUid } ?: return
        if (member.paidCurrentRound && member.paymentApprovalStatus == "APPROVED") {
            _toastMessage.value = "${member.name} is already approved for Round ${group.currentRound}."
            return
        }
        viewModelScope.launch {
            val adminName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Group Admin"
            val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
            val receiptNo = "PG-AJO-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
            val maskedAcct = SecurityAndNumberCodec.maskAccountNumber("9045${member.turnOrder}12890")
            val updatedMembers = group.members.map { m ->
                if (m.uid == memberUid) {
                    m.copy(
                        paidCurrentRound = true,
                        paidRoundsCount = m.paidRoundsCount + 1,
                        trustScore = ContributionAdminCodec.nextTrustScore(m.trustScore, onTime = true),
                        paymentApprovalStatus = "APPROVED",
                        approvedByAdminName = adminName,
                        approvedAtLabel = nowLabel,
                        rejectionReason = ""
                    )
                } else m
            }
            val nextEscrow = group.totalEscrowBalance + group.amountPerRound
            val newHist = ContributionRoundHistory(
                id = "ap_${UUID.randomUUID().toString().take(8)}",
                roundNumber = group.currentRound,
                payerUid = member.uid,
                payerName = member.name,
                amount = group.amountPerRound,
                currency = group.currency,
                paymentMethod = member.paymentProofRef.ifBlank { "PGLOB Wallet Escrow" },
                paidAtLabel = nowLabel,
                wasOnTime = true,
                receiptNumber = receiptNo,
                escrowStatus = "IN_ESCROW",
                approvedByName = adminName,
                maskedAccountNumber = maskedAcct
            )
            val newAudit = ContributionAuditEntry(
                id = "au_${UUID.randomUUID().toString().take(8)}",
                paidByName = member.name,
                amount = group.amountPerRound,
                currency = group.currency,
                paidAtLabel = nowLabel,
                escrowInLabel = "PGLOB Escrow Locked ($maskedAcct)",
                approvedByAdmin = adminName,
                receiptNumber = receiptNo
            )
            saveUpdatedGroup(
                group.copy(
                    members = updatedMembers,
                    totalEscrowBalance = nextEscrow,
                    roundHistory = listOf(newHist) + group.roundHistory,
                    auditLog = listOf(newAudit) + group.auditLog
                )
            )
            addNotification(
                title = "✅ ${group.name}: Payment Approved",
                body = "$adminName approved ${member.name}'s ${group.currency}${group.amountPerRound.toInt()} into PGLOB Escrow (Receipt $receiptNo).",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            appendAdminLog("Approved Ajo Payment", "${member.name} • ${group.currency}${group.amountPerRound.toInt()} in ${group.name}")
            _toastMessage.value = "Approved! ${group.currency}${group.amountPerRound.toInt()} locked in PGLOB Escrow ✅"
        }
    }

    fun adminRejectContributionPayment(groupId: String, memberUid: String, reason: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isGroupCreatorOrSuperAdmin(group)) {
            _toastMessage.value = "Sub-Admins cannot reject payments. Only Group Admin or Owner."
            return
        }
        val member = group.members.firstOrNull { it.uid == memberUid } ?: return
        viewModelScope.launch {
            val cleanReason = reason.trim().ifBlank { "Proof mismatch - please re-upload clear payment proof" }
            val updatedMembers = group.members.map { m ->
                if (m.uid == memberUid) {
                    m.copy(
                        paidCurrentRound = false,
                        paymentApprovalStatus = "REJECTED",
                        rejectionReason = cleanReason
                    )
                } else m
            }
            saveUpdatedGroup(group.copy(members = updatedMembers))
            addNotification(
                title = "❌ Payment Rejected (${group.name})",
                body = "${member.name}: $cleanReason",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            appendAdminLog("Rejected Ajo Payment", "${member.name} in ${group.name}: $cleanReason")
            _toastMessage.value = "Payment marked Rejected — member notified."
        }
    }

    fun requestContributionCollection(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (group.escrowFrozen) {
            _toastMessage.value = "Escrow is frozen by Owner while resolving dispute."
            return
        }
        val currentTurn = group.currentTurnMember
        if (currentTurn == null || currentTurn.uid != currentUserId) {
            _toastMessage.value = "It is ${currentTurn?.name ?: "another member"}'s turn to collect Round ${group.currentRound}."
            return
        }
        if (group.paidMembersCount < group.members.size) {
            _toastMessage.value = "Waiting for all members (${group.paidMembersCount}/${group.members.size}) to complete Round ${group.currentRound}."
            return
        }
        if (currentTurn.collectionStatus == "REQUESTED" || currentTurn.hasCollected) {
            _toastMessage.value = "Collection already requested / completed for this round."
            return
        }
        viewModelScope.launch {
            val updatedMembers = group.members.map { m ->
                if (m.uid == currentUserId) {
                    m.copy(
                        collectionStatus = "REQUESTED",
                        collectionRequestedAtMillis = System.currentTimeMillis()
                    )
                } else m
            }
            saveUpdatedGroup(group.copy(members = updatedMembers))
            addNotification(
                title = "💰 Collection Requested (${group.name})",
                body = "${currentTurn.name} requested Escrow pot ${group.currency}${group.totalPotPerRound.toInt()} (48h window).",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            _toastMessage.value = "Collection requested! Admin has 48h to confirm release from Escrow."
        }
    }

    fun adminConfirmAndReleaseCollection(groupId: String, memberUid: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isGroupCreatorOrSuperAdmin(group)) {
            _toastMessage.value = "Sub-Admins CANNOT approve collection release. Only Group Admin or Owner."
            return
        }
        if (group.escrowFrozen) {
            _toastMessage.value = "Escrow is frozen by Super Admin."
            return
        }
        val recipient = group.members.firstOrNull { it.uid == memberUid } ?: group.currentTurnMember ?: return
        val pot = group.totalPotPerRound
        if (group.totalEscrowBalance < pot && !isSuperAdmin()) {
            _toastMessage.value = "Not enough Escrow balance yet (${group.currency}${group.totalEscrowBalance.toInt()} / ${group.currency}${pot.toInt()})."
            return
        }
        viewModelScope.launch {
            val adminName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Group Admin"
            val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
            val releasePot = minOf(pot, group.totalEscrowBalance).takeIf { it > 0.0 } ?: pot
            val nextEscrow = (group.totalEscrowBalance - releasePot).coerceAtLeast(0.0)
            val receiptNo = "PG-REL-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
            val maskedAcct = SecurityAndNumberCodec.maskAccountNumber(
                localSettings.value.pglobAccountNumber.ifBlank { "9055${recipient.turnOrder}00890" }
            )
            if (recipient.uid == currentUserId) {
                settingsStore.updateLocalWalletAndCoins(walletDelta = releasePot)
                if (hasFirebaseAuth) {
                    repository.updateCoinsAndWallet(walletDelta = releasePot)
                }
            }
            if (hasFirebaseAuth) {
                repository.recordTransaction(
                    txType = "AJO_PAYOUT",
                    amount = releasePot,
                    fee = 0.0,
                    currency = group.currency,
                    method = "PGLOB Escrow Direct Wallet",
                    destination = recipient.name,
                    status = "PAID",
                    note = "${group.name} Round ${group.currentRound} • Receipt $receiptNo"
                )
            }
            _localTransactions.value = listOf(
                PayTransaction(
                    id = receiptNo,
                    userId = currentUserId,
                    txType = "AJO_PAYOUT",
                    amount = releasePot,
                    fee = 0.0,
                    currency = group.currency,
                    method = "PGLOB Escrow Direct Wallet",
                    destination = recipient.name,
                    status = "PAID",
                    note = "${group.name} Round ${group.currentRound} ($maskedAcct)"
                )
            ) + _localTransactions.value

            val isLastRound = group.currentRound >= group.members.size
            val nextRound = if (isLastRound) group.currentRound else group.currentRound + 1
            val nextStatus = if (isLastRound) ContributionStatus.COMPLETED else ContributionStatus.ACTIVE
            val updatedMembers = group.members.map { m ->
                val collectedNow = m.uid == recipient.uid
                m.copy(
                    hasCollected = m.hasCollected || collectedNow,
                    collectionStatus = if (collectedNow) "COLLECTED" else if (isLastRound) m.collectionStatus else "WAITING",
                    collectedAtLabel = if (collectedNow) nowLabel else m.collectedAtLabel,
                    collectedApprovedBy = if (collectedNow) adminName else m.collectedApprovedBy,
                    paidCurrentRound = if (isLastRound) true else false,
                    paymentApprovalStatus = if (isLastRound) m.paymentApprovalStatus else "UNPAID",
                    paymentSubmittedAtMillis = if (isLastRound) m.paymentSubmittedAtMillis else 0L,
                    paymentProofRef = if (isLastRound) m.paymentProofRef else ""
                )
            }
            val payoutHist = ContributionRoundHistory(
                id = "rel_${UUID.randomUUID().toString().take(8)}",
                roundNumber = group.currentRound,
                payerUid = recipient.uid,
                payerName = recipient.name,
                amount = releasePot,
                currency = group.currency,
                paymentMethod = "PGLOB Escrow -> Wallet",
                paidAtLabel = nowLabel,
                wasOnTime = true,
                isPayoutEvent = true,
                payoutRecipientName = recipient.name,
                receiptNumber = receiptNo,
                escrowStatus = "RELEASED_TO_MEMBER",
                approvedByName = adminName,
                maskedAccountNumber = maskedAcct
            )
            val auditEntry = ContributionAuditEntry(
                id = "aur_${UUID.randomUUID().toString().take(8)}",
                paidByName = "${group.name} Escrow Pot",
                amount = releasePot,
                currency = group.currency,
                paidAtLabel = nowLabel,
                escrowInLabel = "Released ($maskedAcct)",
                approvedByAdmin = adminName,
                releasedToName = recipient.name,
                releasedAtLabel = nowLabel,
                receiptNumber = receiptNo
            )
            saveUpdatedGroup(
                group.copy(
                    members = updatedMembers,
                    totalEscrowBalance = nextEscrow,
                    currentRound = nextRound,
                    status = nextStatus,
                    roundHistory = listOf(payoutHist) + group.roundHistory,
                    auditLog = listOf(auditEntry) + group.auditLog
                )
            )
            if (group.linkedChatId.isNotBlank()) {
                val card = ContributionCodec.encode(
                    groupId = group.id,
                    title = "🎉 Escrow Released: ${group.currency}${releasePot.toInt()}",
                    subtitle = "Sent directly to ${recipient.name} Wallet ($maskedAcct) • Approved by $adminName",
                    badge = "ESCROW RELEASED"
                )
                sendChatMessage(
                    chatId = group.linkedChatId,
                    memberUids = updatedMembers.map { it.uid },
                    text = "🎉 Round ${group.currentRound} Escrow ${group.currency}${releasePot.toInt()} released directly to ${recipient.name}'s PGLOB Wallet!",
                    replyToText = card
                )
            }
            addNotification(
                title = "🎉 Escrow Released: ${group.currency}${releasePot.toInt()}",
                body = "${recipient.name} received Round ${group.currentRound} directly from PGLOB Escrow (Receipt $receiptNo).",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            appendAdminLog("Released Escrow Pot", "${group.currency}${releasePot.toInt()} to ${recipient.name} (${group.name})")
            _toastMessage.value = "Released ${group.currency}${releasePot.toInt()} directly from PGLOB Escrow to ${recipient.name}!"
        }
    }

    fun run48HourAutoApproveCheck(simulateElapsed48h: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val prepared = if (simulateElapsed48h) {
                val expiredStamp = now - ContributionAdminCodec.AUTO_APPROVE_WINDOW_MS - 60_000L
                contributions.value.map { g ->
                    val nextMembers = g.members.map { m ->
                        val modPay = if (m.paymentApprovalStatus == "WAITING_APPROVAL") {
                            m.copy(paymentSubmittedAtMillis = expiredStamp)
                        } else m
                        if (modPay.collectionStatus == "REQUESTED") {
                            modPay.copy(collectionRequestedAtMillis = expiredStamp)
                        } else modPay
                    }
                    g.copy(members = nextMembers)
                }
            } else {
                contributions.value
            }

            val (nextList, events) = ContributionAdminCodec.evaluateAutoApprove48h(prepared, now)
            if (events.isEmpty()) {
                _toastMessage.value = "48h Auto-Approve Check: All pending items are still within 48h window."
                return@launch
            }
            // Credit local/remote wallet if current user received an AUTO_COLLECTED payout
            nextList.forEachIndexed { idx, updatedGroup ->
                val oldGroup = prepared.getOrNull(idx)
                updatedGroup.members.forEach { newM ->
                    val oldM = oldGroup?.members?.firstOrNull { it.uid == newM.uid }
                    if (newM.uid == currentUserId &&
                        newM.collectionStatus == "AUTO_COLLECTED" &&
                        oldM?.collectionStatus != "AUTO_COLLECTED"
                    ) {
                        val pot = updatedGroup.totalPotPerRound
                        settingsStore.updateLocalWalletAndCoins(walletDelta = pot)
                        if (hasFirebaseAuth) {
                            repository.updateCoinsAndWallet(walletDelta = pot)
                        }
                    }
                }
            }
            settingsStore.saveContributionsJson(ContributionAdminCodec.encodeContributions(nextList))
            events.forEach { ev ->
                addNotification("⏰ PGLOB 48h Auto-Approve", ev, "PAY")
                appendAdminLog("48h Auto-Approve", ev)
            }
            _toastMessage.value = events.first()
        }
    }

    fun toggleGroupEscrowFrozen(groupId: String, frozen: Boolean) {
        if (!isSuperAdmin()) {
            _toastMessage.value = "Only Super Admin Owner can freeze or unfreeze Escrow."
            return
        }
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(escrowFrozen = frozen))
            appendAdminLog(if (frozen) "Frozen Escrow" else "Unfroze Escrow", "${group.name} • Escrow ${if (frozen) "FROZEN" else "ACTIVE"}")
            addNotification(
                title = if (frozen) "🧊 Escrow Frozen (${group.name})" else "✅ Escrow Unfrozen (${group.name})",
                body = if (frozen) "Owner placed temporary Escrow freeze during dispute check." else "Escrow operations resumed.",
                category = "SECURITY"
            )
            _toastMessage.value = if (frozen) "Escrow frozen for ${group.name}" else "Escrow unfrozen for ${group.name}"
        }
    }

    fun ownerRefundContributionGroup(groupId: String) {
        if (!isSuperAdmin()) {
            _toastMessage.value = "Only Super Admin Owner can issue a full Escrow refund."
            return
        }
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (group.totalEscrowBalance <= 0.0) {
            _toastMessage.value = "Escrow balance is already 0 for ${group.name}."
            return
        }
        viewModelScope.launch {
            val escrowTotal = group.totalEscrowBalance
            val myMember = group.members.firstOrNull { it.uid == currentUserId }
            val myShare = if (myMember?.paidCurrentRound == true) group.amountPerRound else 0.0
            if (myShare > 0.0) {
                settingsStore.updateLocalWalletAndCoins(walletDelta = myShare)
                if (hasFirebaseAuth) {
                    repository.updateCoinsAndWallet(walletDelta = myShare)
                }
            }
            val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
            val receiptNo = "PG-REF-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
            val refundHist = ContributionRoundHistory(
                id = "ref_${UUID.randomUUID().toString().take(8)}",
                roundNumber = group.currentRound,
                payerUid = currentUserId,
                payerName = "All Paid Members",
                amount = escrowTotal,
                currency = group.currency,
                paymentMethod = "Owner Escrow Refund -> Wallets",
                paidAtLabel = nowLabel,
                isPayoutEvent = true,
                payoutRecipientName = "Group Members",
                receiptNumber = receiptNo,
                escrowStatus = "REFUNDED",
                approvedByName = "Owner (paulodogun197)"
            )
            val refundAudit = ContributionAuditEntry(
                id = "aurf_${UUID.randomUUID().toString().take(8)}",
                paidByName = "${group.name} Escrow",
                amount = escrowTotal,
                currency = group.currency,
                paidAtLabel = nowLabel,
                escrowInLabel = "Refunded to Member Wallets",
                approvedByAdmin = "Super Admin Owner",
                releasedToName = "Paid Members",
                releasedAtLabel = nowLabel,
                receiptNumber = receiptNo
            )
            val resetMembers = group.members.map { m ->
                m.copy(
                    paidCurrentRound = false,
                    paymentApprovalStatus = "UNPAID",
                    paymentSubmittedAtMillis = 0L
                )
            }
            saveUpdatedGroup(
                group.copy(
                    members = resetMembers,
                    totalEscrowBalance = 0.0,
                    escrowFrozen = false,
                    status = ContributionStatus.PAUSED,
                    roundHistory = listOf(refundHist) + group.roundHistory,
                    auditLog = listOf(refundAudit) + group.auditLog
                )
            )
            appendAdminLog("Owner Escrow Refund", "Refunded ${group.currency}${escrowTotal.toInt()} in ${group.name}")
            _toastMessage.value = "Refunded ${group.currency}${escrowTotal.toInt()} back to member wallets!"
        }
    }

    fun reportContributionDispute(groupId: String, reason: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        submitReport("CONTRIBUTION_DISPUTE", "${group.name} (${group.id})", reason)
    }

    fun isGroupCreatorOrSuperAdmin(group: ContributionGroup): Boolean {
        return isSuperAdmin() || group.creatorUid == currentUserId
    }

    fun generateContributionPdfReceipt(group: ContributionGroup, amount: Double, payerName: String, roundNumber: Int) {
        val lines = listOf(
            "Group Name: ${group.name}",
            "Contribution Type: ${group.type.label}",
            "Round Number: Round $roundNumber of ${group.totalRounds}",
            "Member / Recipient: $payerName",
            "Amount Secured: ${group.currency}${String.format(Locale.US, "%,.0f", amount)}",
            "Escrow Pool Balance: ${group.currency}${String.format(Locale.US, "%,.0f", group.totalEscrowBalance)}",
            "Next Turn Date: ${group.nextDueDateLabel}",
            "Receipt Status: VERIFIED & PAID",
            "Generated At: ${SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date())}"
        )
        val res = PdfReceiptGenerator.generateAndShareReceipt(
            context = appContext,
            title = "${group.name} • Round $roundNumber Receipt",
            lines = lines,
            filePrefix = "ajo_${group.id}_r$roundNumber"
        )
        _toastMessage.value = if (res.isSuccess) "PDF Receipt generated!" else "Could not open PDF share sheet"
    }

    fun sendContributionReminder(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!canPerformGroupAction(group) { it.canRemindMembers }) {
            _toastMessage.value = "You don't have permission for this. Contact Group Admin."
            return
        }
        viewModelScope.launch {
            val unpaidNames = group.members.filter { !it.paidCurrentRound }.joinToString(", ") { it.name.substringBefore(" ") }
            addNotification(
                title = "⏰ Reminder: Pay ${group.currency}${group.amountPerRound.toInt()} today",
                body = "${group.name} (Round ${group.currentRound}) • Pending: ${unpaidNames.ifBlank { "All Paid" }}",
                category = "PAY",
                targetChatId = group.linkedChatId
            )
            if (group.linkedChatId.isNotBlank()) {
                val card = ContributionCodec.encode(
                    groupId = group.id,
                    title = "⏰ Reminder: Pay ${group.currency}${group.amountPerRound.toInt()} today",
                    subtitle = "Round ${group.currentRound} due ${group.nextDueDateLabel}. Pending: ${unpaidNames.ifBlank { "None" }}",
                    badge = "REMINDER"
                )
                sendChatMessage(
                    chatId = group.linkedChatId,
                    memberUids = group.members.map { it.uid },
                    text = "⏰ Reminder: Please pay ${group.currency}${group.amountPerRound.toInt()} for ${group.name}!",
                    replyToText = card
                )
            }
            appendAdminLog("Sent Ajo Reminder", "${group.name} Round ${group.currentRound}")
            _toastMessage.value = "Payment reminder sent to unpaid members!"
        }
    }

    fun adminReorderTurn(groupId: String, memberUid: String, moveUp: Boolean) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!canPerformGroupAction(group) { it.canChangeTurnOrder }) {
            _toastMessage.value = "You don't have permission for this. Contact Group Admin."
            return
        }
        val sorted = group.members.sortedBy { it.turnOrder }.toMutableList()
        val idx = sorted.indexOfFirst { it.uid == memberUid }
        if (idx == -1) return
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (targetIdx !in sorted.indices) return
        val swapped = sorted[idx]
        sorted[idx] = sorted[targetIdx]
        sorted[targetIdx] = swapped
        val reindexed = sorted.mapIndexed { i, m -> m.copy(turnOrder = i + 1) }
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(members = reindexed))
            appendAdminLog("Reordered Turn List", "${group.name} • Updated turn order")
            _toastMessage.value = "Turn order updated!"
        }
    }

    fun adminPauseResumeContribution(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        val isCreatorOrSuper = isSuperAdmin() || group.creatorUid == currentUserId || hasAppPermission(AppAdminPermission.MANAGE_CONTRIBUTIONS)
        val groupSub = getGroupSubAdminPermissions(groupId)
        if (!isCreatorOrSuper && (groupSub == null || groupSub.cannotEndGroup)) {
            _toastMessage.value = "You don't have permission for this. Contact Super Admin."
            return
        }
        val nextStatus = if (group.status == ContributionStatus.PAUSED) ContributionStatus.ACTIVE else ContributionStatus.PAUSED
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(status = nextStatus))
            appendAdminLog("${nextStatus.label} Contribution", "${group.name} marked ${nextStatus.label}")
            _toastMessage.value = "${group.name} is now ${nextStatus.label}"
        }
    }

    fun adminEndContribution(groupId: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        val isCreatorOrSuper = isSuperAdmin() || group.creatorUid == currentUserId || hasAppPermission(AppAdminPermission.MANAGE_CONTRIBUTIONS)
        if (!isCreatorOrSuper) {
            _toastMessage.value = "Only Group Creator or Super Admin can end a Contribution group."
            return
        }
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(status = ContributionStatus.COMPLETED))
            appendAdminLog("Ended Contribution Group", "${group.name} completed & archived")
            _toastMessage.value = "${group.name} marked Completed"
        }
    }

    fun adminEditContributionAmount(groupId: String, newAmount: Double, newLateFee: Double) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        val groupSub = getGroupSubAdminPermissions(groupId)
        if (groupSub != null && groupSub.cannotChangeAmount && !isSuperAdmin() && group.creatorUid != currentUserId) {
            _toastMessage.value = "Sub-Admins cannot change contribution amount."
            return
        }
        if (group.paidMembersCount > 0 || group.currentRound > 1) {
            _toastMessage.value = "Amount is locked once members have started paying!"
            return
        }
        viewModelScope.launch {
            saveUpdatedGroup(group.copy(amountPerRound = newAmount, lateFeeAmount = newLateFee))
            appendAdminLog("Edited Contribution Amount", "${group.name} set to ${group.currency}${newAmount.toInt()}")
            _toastMessage.value = "Amount updated before start!"
        }
    }

    fun adminAddContributionMember(groupId: String, name: String, handle: String, uid: String = "mem_${UUID.randomUUID().toString().take(8)}") {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!canPerformGroupAction(group) { it.canAddRemoveMember }) {
            _toastMessage.value = "You don't have permission for this. Contact Group Admin."
            return
        }
        if (group.members.any { it.uid == uid }) {
            _toastMessage.value = "$name is already in ${group.name}"
            return
        }
        val nextTurn = (group.members.maxOfOrNull { it.turnOrder } ?: 0) + 1
        val newMember = ContributionMember(
            uid = uid,
            name = name.trim(),
            handle = handle.trim().ifBlank { name.lowercase().replace(" ", "_") },
            turnOrder = nextTurn,
            role = "MEMBER",
            trustScore = 85,
            joinedDateLabel = "Today"
        )
        viewModelScope.launch {
            saveUpdatedGroup(
                group.copy(
                    members = group.members + newMember,
                    totalRounds = (group.members.size + 1),
                    maxMembers = maxOf(group.maxMembers, group.members.size + 1)
                )
            )
            appendAdminLog("Added Ajo Member", "Added $name to ${group.name}")
            _toastMessage.value = "$name added to ${group.name}!"
        }
    }

    fun adminRemoveContributionMember(groupId: String, memberUid: String) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!canPerformGroupAction(group) { it.canAddRemoveMember }) {
            _toastMessage.value = "You don't have permission for this. Contact Group Admin."
            return
        }
        val target = group.members.firstOrNull { it.uid == memberUid } ?: return
        if (target.uid == group.creatorUid) {
            _toastMessage.value = "Cannot remove the Group Creator."
            return
        }
        if (target.hasCollected && target.paidCurrentRound.not() && !isSuperAdmin()) {
            _toastMessage.value = "Cannot remove member with unsettled payout cycle—report dispute to Super Admin."
            return
        }
        viewModelScope.launch {
            val remaining = group.members.filterNot { it.uid == memberUid }
                .mapIndexed { idx, m -> m.copy(turnOrder = idx + 1) }
            saveUpdatedGroup(group.copy(members = remaining, totalRounds = remaining.size.coerceAtLeast(1)))
            appendAdminLog("Removed Ajo Member", "Removed ${target.name} (Trust ${target.trustScore}) from ${group.name}")
            _toastMessage.value = "${target.name} removed from ${group.name}"
        }
    }

    fun approveJoinRequest(groupId: String, requesterLabel: String, approve: Boolean) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        val isCreatorOrSuper = isGroupCreatorOrSuperAdmin(group) || hasAppPermission(AppAdminPermission.MANAGE_CONTRIBUTIONS)
        val groupSub = getGroupSubAdminPermissions(groupId)
        val subAllowed = group.allowSubAdminApproveRegistration && groupSub != null && groupSub.canApproveNewMember
        if (!isCreatorOrSuper && !subAllowed) {
            _toastMessage.value = "Only Group Admin can approve registrations (unless enabled in Group Settings)."
            return
        }
        viewModelScope.launch {
            val nextPending = group.pendingJoinRequests.filterNot { it == requesterLabel }
            if (approve) {
                val cleanName = requesterLabel.substringBefore(" (").trim()
                val cleanHandle = requesterLabel.substringAfter("@", "member").substringBefore(")")
                val newMember = ContributionMember(
                    uid = "req_${UUID.randomUUID().toString().take(8)}",
                    name = cleanName,
                    handle = cleanHandle,
                    turnOrder = group.members.size + 1,
                    role = "MEMBER",
                    trustScore = 82,
                    joinedDateLabel = "Today"
                )
                saveUpdatedGroup(
                    group.copy(
                        members = group.members + newMember,
                        pendingJoinRequests = nextPending,
                        totalRounds = group.members.size + 1
                    )
                )
                appendAdminLog("Approved Join Request", "$cleanName approved into ${group.name}")
                _toastMessage.value = "$cleanName joined ${group.name}!"
            } else {
                saveUpdatedGroup(group.copy(pendingJoinRequests = nextPending))
                _toastMessage.value = "Join request declined"
            }
        }
    }

    fun leaveContributionGroup(groupId: String, confirmForfeitPaid: Boolean = false): String? {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return null
        val me = group.members.firstOrNull { it.uid == currentUserId } ?: return null
        if (me.hasCollected) {
            val msg = "You cannot leave after collecting your turn! Please complete the remaining rounds."
            _toastMessage.value = msg
            return msg
        }
        if (me.paidCurrentRound && !confirmForfeitPaid) {
            return "CONFIRM_FORFEIT_PAID"
        }
        viewModelScope.launch {
            val remaining = group.members.filterNot { it.uid == currentUserId }
                .mapIndexed { idx, m -> m.copy(turnOrder = idx + 1) }
            saveUpdatedGroup(group.copy(members = remaining, totalRounds = remaining.size.coerceAtLeast(1)))
            _toastMessage.value = "You left ${group.name}"
        }
        return "LEFT"
    }

    fun assignGroupSubAdmin(
        groupId: String,
        memberUid: String,
        memberName: String,
        permissions: GroupSubAdminPermissions
    ) {
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        if (!isSuperAdmin() && group.creatorUid != currentUserId) {
            _toastMessage.value = "Only Group Creator or Super Admin can assign Group Sub-Admins."
            return
        }
        val existingSubCount = group.members.count { it.role == "SUB_ADMIN" && it.uid != memberUid }
        if (existingSubCount >= 2) {
            _toastMessage.value = "Maximum of 2 Sub-Admins allowed per Contribution group."
            return
        }
        val sanitizedPermissions = permissions.copy(
            cannotChangeAmount = true,
            cannotEndGroup = true,
            canApproveNewMember = group.allowSubAdminApproveRegistration && permissions.canApproveNewMember
        )
        viewModelScope.launch {
            val updatedMembers = group.members.map { m ->
                if (m.uid == memberUid && m.role != "ADMIN") m.copy(role = "SUB_ADMIN") else m
            }
            saveUpdatedGroup(group.copy(members = updatedMembers))
            val filtered = groupSubAdmins.value.filterNot { it.groupId == groupId && it.userUid == memberUid }
            settingsStore.saveGroupSubAdminsJson(
                ContributionAdminCodec.encodeGroupSubAdmins(filtered + sanitizedPermissions)
            )
            addNotification(
                title = "👑 Assigned as Group Sub-Admin",
                body = "$memberName is now a Sub-Admin for ${group.name} (Assist only — cannot approve money/payouts).",
                category = "SECURITY"
            )
            appendAdminLog("Assigned Group Sub-Admin", "$memberName in ${group.name}")
            _toastMessage.value = "$memberName is now Group Sub-Admin!"
        }
    }

    fun getGroupSubAdminPermissions(groupId: String, uid: String = currentUserId): GroupSubAdminPermissions? {
        return groupSubAdmins.value.firstOrNull { it.groupId == groupId && it.userUid == uid }
    }

    private fun canPerformGroupAction(group: ContributionGroup, subCheck: (GroupSubAdminPermissions) -> Boolean): Boolean {
        if (isSuperAdmin() || group.creatorUid == currentUserId) return true
        if (hasAppPermission(AppAdminPermission.MANAGE_CONTRIBUTIONS)) return true
        val sub = getGroupSubAdminPermissions(group.id) ?: return false
        return subCheck(sub)
    }

    private suspend fun saveUpdatedGroup(updatedGroup: ContributionGroup) {
        val list = contributions.value.map { if (it.id == updatedGroup.id) updatedGroup else it }
        settingsStore.saveContributionsJson(ContributionAdminCodec.encodeContributions(list))
    }

    // ==================== v15 SUPER ADMIN & SUB-ADMIN CONTROLS ====================

    fun myEffectiveEmail(): String =
        (myProfile.value?.email?.takeIf { it.isNotBlank() } ?: initialEmail).trim()

    fun isSuperAdmin(): Boolean =
        myEffectiveEmail().equals(ContributionAdminCodec.SUPER_ADMIN_EMAIL, ignoreCase = true)

    fun currentAppSubAdmin(): AppSubAdmin? {
        val email = myEffectiveEmail()
        return appSubAdmins.value.firstOrNull {
            it.uid == currentUserId || (email.isNotBlank() && it.email.equals(email, ignoreCase = true))
        }
    }

    fun canAccessAdminDashboard(): Boolean = isSuperAdmin() || currentAppSubAdmin() != null

    fun hasAppPermission(permission: AppAdminPermission): Boolean {
        if (isSuperAdmin()) return true
        return currentAppSubAdmin()?.permissions?.contains(permission) == true
    }

    fun addOrUpdateAppSubAdmin(
        name: String,
        email: String,
        username: String,
        roleLabel: String,
        permissions: Set<AppAdminPermission>,
        uid: String = "sub_${UUID.randomUUID().toString().take(8)}"
    ) {
        if (!isSuperAdmin()) {
            _toastMessage.value = "You don't have permission for this. Contact Super Admin."
            return
        }
        if (permissions.isEmpty()) {
            _toastMessage.value = "Select at least 1 Sub-Admin permission."
            return
        }
        viewModelScope.launch {
            val cleanEmail = email.trim().ifBlank { "${username.ifBlank { "subadmin" }}@pglob.app" }
            val entry = AppSubAdmin(
                uid = uid,
                name = name.trim().ifBlank { "Sub-Admin" },
                email = cleanEmail,
                username = username.trim().ifBlank { cleanEmail.substringBefore("@") },
                roleLabel = roleLabel.trim().ifBlank { "Sub-Admin" },
                permissions = permissions,
                addedByEmail = myEffectiveEmail().ifBlank { ContributionAdminCodec.SUPER_ADMIN_EMAIL },
                addedAtLabel = "Today"
            )
            val nextList = listOf(entry) + appSubAdmins.value.filterNot {
                it.uid == uid || it.email.equals(cleanEmail, ignoreCase = true)
            }
            settingsStore.saveAppSubAdminsJson(ContributionAdminCodec.encodeAppSubAdmins(nextList))
            val ownerName = myProfile.value?.fullName?.ifBlank { "Paul" } ?: "Paul"
            addNotification(
                title = "👑 Sub-Admin Assigned",
                body = "You have been assigned as PGLOB APP Sub-Admin by $ownerName (${entry.name})",
                category = "SECURITY"
            )
            appendAdminLog("Added / Updated Sub-Admin", "${entry.name} (${permissions.size} permissions)")
            _toastMessage.value = "${entry.name} saved as Sub-Admin!"
        }
    }

    fun removeAppSubAdmin(uid: String) {
        if (!isSuperAdmin()) {
            _toastMessage.value = "Only Super Admin can remove Sub-Admins."
            return
        }
        viewModelScope.launch {
            val removed = appSubAdmins.value.firstOrNull { it.uid == uid }
            val nextList = appSubAdmins.value.filterNot { it.uid == uid }
            settingsStore.saveAppSubAdminsJson(ContributionAdminCodec.encodeAppSubAdmins(nextList))
            appendAdminLog("Removed Sub-Admin", "${removed?.name ?: uid} removed from Sub-Admins")
            _toastMessage.value = "Sub-Admin removed"
        }
    }

    fun adminForceContributionPayout(groupId: String) {
        if (!hasAppPermission(AppAdminPermission.APPROVE_PAYOUTS)) {
            _toastMessage.value = "You don't have permission for this. Contact Super Admin."
            return
        }
        val group = contributions.value.firstOrNull { it.id == groupId } ?: return
        val recipient = group.currentTurnMember ?: return
        viewModelScope.launch {
            val pot = group.totalPotPerRound
            val nextEscrow = (group.totalEscrowBalance - pot).coerceAtLeast(0.0)
            val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
            val isLast = group.currentRound >= group.members.size
            val updatedMembers = group.members.map { m ->
                m.copy(
                    hasCollected = m.hasCollected || m.uid == recipient.uid,
                    paidCurrentRound = if (isLast) true else false
                )
            }
            val payoutHist = ContributionRoundHistory(
                id = "fpo_${UUID.randomUUID().toString().take(8)}",
                roundNumber = group.currentRound,
                payerUid = recipient.uid,
                payerName = recipient.name,
                amount = pot,
                currency = group.currency,
                paymentMethod = "Admin Force Payout",
                paidAtLabel = nowLabel,
                isPayoutEvent = true,
                payoutRecipientName = recipient.name
            )
            saveUpdatedGroup(
                group.copy(
                    members = updatedMembers,
                    totalEscrowBalance = nextEscrow,
                    currentRound = if (isLast) group.currentRound else group.currentRound + 1,
                    status = if (isLast) ContributionStatus.COMPLETED else ContributionStatus.ACTIVE,
                    roundHistory = listOf(payoutHist) + group.roundHistory
                )
            )
            appendAdminLog("Forced Escrow Payout", "${group.currency}${pot.toInt()} to ${recipient.name} (${group.name})")
            _toastMessage.value = "Force payout released to ${recipient.name}!"
        }
    }

    fun adminBroadcastAnnouncement(
        title: String,
        message: String,
        targetScope: String,
        targetGroupName: String = "",
        targetGroupChatId: String = ""
    ) {
        if (!hasAppPermission(AppAdminPermission.SEND_BROADCAST)) {
            _toastMessage.value = "You don't have permission for this. Contact Super Admin."
            return
        }
        if (title.isBlank() || message.isBlank()) {
            _toastMessage.value = "Enter broadcast title and message"
            return
        }
        viewModelScope.launch {
            val sender = myProfile.value?.fullName?.ifBlank { "Super Admin" } ?: "Super Admin"
            val item = BroadcastAnnouncement(
                id = "bc_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                message = message.trim(),
                targetScope = targetScope,
                targetGroupName = targetGroupName,
                sentByName = sender,
                sentAtLabel = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            )
            settingsStore.saveBroadcastsJson(ContributionAdminCodec.encodeBroadcasts(listOf(item) + broadcasts.value))
            addNotification("📢 ${item.title}", item.message, "SECURITY")
            if (targetGroupChatId.isNotBlank()) {
                sendChatMessage(
                    chatId = targetGroupChatId,
                    memberUids = listOf(currentUserId),
                    text = "📢 ${item.title}\n${item.message}"
                )
            }
            appendAdminLog("Sent Broadcast", "${item.title} -> $targetScope")
            _toastMessage.value = "Announcement broadcast sent!"
        }
    }

    fun updateAppAdminConfig(config: AppAdminConfig) {
        if (!hasAppPermission(AppAdminPermission.EDIT_APP_SETTINGS)) {
            _toastMessage.value = "You don't have permission for this. Contact Super Admin."
            return
        }
        viewModelScope.launch {
            settingsStore.saveAppAdminConfigJson(ContributionAdminCodec.encodeAppAdminConfig(config))
            appendAdminLog("Updated App Settings", "App: ${config.appName}, Fee: ${config.transferFeePercent}%, Late Fee: ${config.defaultCurrency}${config.defaultLateFee.toInt()}")
            _toastMessage.value = "App Admin settings saved!"
        }
    }

    private suspend fun appendAdminLog(actionTitle: String, details: String) {
        val actorEmail = myEffectiveEmail().ifBlank { "system@pglob.app" }
        val actorName = myProfile.value?.fullName?.ifBlank { initialDisplayName } ?: "Paul Odo"
        val actorRole = when {
            isSuperAdmin() -> "Super Admin"
            currentAppSubAdmin() != null -> "Sub-Admin"
            else -> "Group Admin"
        }
        val entry = AdminActivityLog(
            id = "log_${UUID.randomUUID().toString().take(8)}",
            actorEmail = actorEmail,
            actorName = actorName,
            actorRole = actorRole,
            actionTitle = actionTitle,
            details = details,
            timestampLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date())
        )
        settingsStore.saveAdminLogsJson(ContributionAdminCodec.encodeAdminLogs(listOf(entry) + adminActivityLogs.value))
    }

    fun deleteMarketProduct(productId: String) {
        viewModelScope.launch {
            val res = repository.deleteMarketProduct(productId)
            _toastMessage.value = if (res.isSuccess) "Product removed" else "Permission required to delete"
        }
    }

    fun deleteJobListing(jobId: String) {
        viewModelScope.launch {
            val res = repository.deleteJobListing(jobId)
            _toastMessage.value = if (res.isSuccess) "Job removed" else "Permission required to delete"
        }
    }

    private var aiJob: Job? = null

    // PGLOB AI via Firebase AI Logic (gemini-3.8-flash with fast fallback)
    fun askPglobAi(prompt: String) {
        if (prompt.isBlank()) return
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                var lastText = ""
                aiRepository.streamPrompt(prompt).collect { accumulated ->
                    lastText = accumulated
                    _aiState.value = AiUiState.Streaming(accumulated)
                }
                _aiState.value = AiUiState.Success(lastText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(e.localizedMessage ?: "PGLOB AI failed")
            }
        }
    }

    fun clearAiState() {
        aiJob?.cancel()
        aiJob = null
        _aiState.value = AiUiState.Idle
    }

    private fun handleStreamError(tag: String, error: Throwable) {
        val fe = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (fe == null && error is CancellationException) return
        Log.w("PglobVM", "Stream error [$tag]: ${(fe ?: error).message}")
    }
}

fun createPglobViewModelFactory(
    context: Context,
    currentUserId: String,
    email: String,
    displayName: String
) = androidx.lifecycle.viewmodel.viewModelFactory {
    initializer {
        val app = context.applicationContext
        val db = PglobDatabase.getInstance(app)
        PglobViewModel(
            appContext = app,
            repository = PglobRepository(app),
            aiRepository = AiRepository(),
            settingsStore = LocalSettingsStore(app),
            pendingDao = db.pendingMessageDao(),
            chatMessageDao = db.chatMessageDao(),
            networkFlow = NetworkMonitor.observeOnline(app),
            currentUserId = currentUserId,
            initialEmail = email,
            initialDisplayName = displayName
        )
    }
}

object OrderHistoryMapper {
    private val orderRefRegex = Regex("PGLOB-[A-Z]+-\\d+")

    fun mapTransactionToOrderRecord(
        tx: PayTransaction,
        deliveredOrderIds: Set<String> = emptySet()
    ): OrderHistoryRecord {
        val statusUpper = tx.status.trim().uppercase(Locale.US)
        val methodLower = tx.method.lowercase(Locale.US)
        val noteLower = tx.note.lowercase(Locale.US)
        val deliveryStatus = when {
            deliveredOrderIds.contains(tx.id) -> OrderDeliveryStatus.DELIVERED
            statusUpper == "APPROVED" || statusUpper == "DELIVERED" -> OrderDeliveryStatus.DELIVERED
            statusUpper == "PENDING" -> OrderDeliveryStatus.PENDING
            methodLower.contains("escrow") || noteLower.contains("escrow") || (tx.txType == "MARKET" && statusUpper == "PAID") ->
                OrderDeliveryStatus.ESCROWED
            else -> OrderDeliveryStatus.DELIVERED
        }

        val extractedRef = orderRefRegex.find(tx.note)?.value
            ?: "#${tx.id.ifBlank { UUID.randomUUID().toString() }.replace("tx_", "").replace("mkt_", "").take(8).uppercase(Locale.US)}"

        val cleanedTitle = tx.note
            .replace(orderRefRegex, "")
            .replace("• Escrow Safe", "", ignoreCase = true)
            .replace("•", " ")
            .trim()
            .replace(Regex("\\s+"), " ")
            .ifBlank { "${tx.txType} • ${tx.method}" }

        val timestampMillis = tx.createdAt?.toDate()?.time ?: System.currentTimeMillis()
        val dateLabel = if (tx.createdAt != null) {
            SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US).format(tx.createdAt.toDate())
        } else {
            "Just now"
        }

        return OrderHistoryRecord(
            id = tx.id,
            orderReference = extractedRef,
            title = cleanedTitle,
            counterpartyName = tx.destination.ifBlank { "PGLOB Global" },
            counterpartyId = tx.counterpartyId,
            txType = tx.txType,
            amount = tx.amount,
            fee = tx.fee,
            currency = tx.currency,
            paymentMethod = tx.method,
            deliveryStatus = deliveryStatus,
            rawStatus = tx.status,
            note = tx.note,
            timestampMillis = timestampMillis,
            dateLabel = dateLabel
        )
    }
}
