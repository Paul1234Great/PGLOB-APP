package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.LocalAppSettings
import com.example.data.local.LocalSettingsStore
import com.example.data.model.ChatThread
import com.example.data.model.MarketProduct
import com.example.data.model.StatusNote
import com.example.ui.PglobViewModel
import com.example.ui.components.AppLockScreen
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.PglobAiAssistantSheet
import com.example.ui.components.PglobListBackground
import com.example.ui.components.PinOrBiometricConfirmSheet
import com.example.ui.components.createTempCameraUri
import com.example.ui.createPglobViewModelFactory
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListTabContent
import com.example.ui.screens.ChatsTopHeaderAndTabs
import com.example.ui.screens.CommunitiesScreen
import com.example.ui.screens.ConnectGlobalScreen
import com.example.ui.screens.ContributionDetailScreen
import com.example.ui.screens.ContributionsListScreen
import com.example.ui.screens.CreateContributionWizardSheet
import com.example.ui.screens.DatingScreen
import com.example.ui.screens.EarnWalletScreen
import com.example.ui.screens.FullScreenStatusNoteViewer
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LiveWorldRoomsScreen
import com.example.ui.screens.MarketProductCheckoutOverlay
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.NewChatOrGroupSheet
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.PayReceiptSuccessDialog
import com.example.ui.screens.PglobPayScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SmartOnboardingSetupScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatusViewerDialog
import com.example.ui.screens.UpdatesScreen
import com.example.ui.screens.WallpaperPickerBottomSheet
import com.example.ui.screens.WelcomeAuthScreen
import com.example.ui.screens.WriteStatusNoteOverlay
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobChatTheme
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobThemeMode
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.PglobStrings
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must configure App Check BEFORE any Firebase AI Logic / Firestore calls
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            PglobAppEntry()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

private fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
    val auth = Firebase.auth
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}

@Composable
fun PglobAppEntry() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val settingsStore = remember { LocalSettingsStore(context) }
    val entrySettings by settingsStore.settingsFlow.collectAsStateWithLifecycle(initialValue = LocalAppSettings())

    val currentUser by observeAuthState().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
    var splashFinished by remember { mutableStateOf(false) }

    val hasActiveSession = currentUser != null || (entrySettings.isLoggedInSession && entrySettings.stayLoggedIn)

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null && !entrySettings.isLoggedInSession) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val entryThemeMode = remember(entrySettings.themeMode) {
        PglobThemeMode.entries.firstOrNull { it.name == entrySettings.themeMode } ?: PglobThemeMode.PGLOB_BLACK
    }

    PglobChatTheme(themeMode = entryThemeMode, boldFontMode = entrySettings.boldFontMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasActiveSession) {
                val activeUid = currentUser?.uid ?: entrySettings.localSessionUid.ifBlank { "local_pglob_user" }
                val activeEmail = currentUser?.email?.ifBlank { entrySettings.savedEmail } ?: entrySettings.savedEmail
                val activeDisplayName = currentUser?.displayName?.ifBlank { entrySettings.savedDisplayName } ?: entrySettings.savedDisplayName
                key(activeUid) {
                    val viewModel: PglobViewModel = viewModel(
                        factory = createPglobViewModelFactory(
                            context = context,
                            currentUserId = activeUid,
                            email = activeEmail,
                            displayName = activeDisplayName
                        )
                    )
                    PglobAuthenticatedApp(
                        viewModel = viewModel,
                        userEmail = activeEmail,
                        userDisplayName = activeDisplayName,
                        isSplashBlocking = !splashFinished
                    )
                }
            } else if (splashFinished) {
                WelcomeAuthScreen(
                    localSettings = entrySettings,
                    onForgetDeviceAccount = {
                        scope.launch { settingsStore.clearKnownAccountOnDevice() }
                    },
                    onSelectLocale = { _, language, _ ->
                        scope.launch { settingsStore.setAppLanguage(language) }
                    },
                    onPhoneSignUpComplete = { uid, fullName, phone, countryName, countryFlag, currency, email, passwordHash, pglobAccountNumber, updatedJson ->
                        scope.launch {
                            settingsStore.savePhoneAuthSession(
                                uid = uid,
                                displayName = fullName,
                                phone = phone,
                                countryName = countryName,
                                countryFlag = countryFlag,
                                currency = currency,
                                email = email,
                                passwordHash = passwordHash,
                                pglobAccountNumber = pglobAccountNumber,
                                registeredAccountsJson = updatedJson
                            )
                            // Award +50 Daily Welcome Coins immediately on Sign Up
                            settingsStore.updateLocalWalletAndCoins(coinsDelta = 50L)
                            settingsStore.setStayLoggedIn(true)
                        }
                    },
                    onPhoneSignInSuccess = { uid, fullName, phone, countryName, countryFlag, currency, email, passwordHash, pglobAccountNumber ->
                        scope.launch {
                            settingsStore.savePhoneAuthSession(
                                uid = uid,
                                displayName = fullName,
                                phone = phone,
                                countryName = countryName,
                                countryFlag = countryFlag,
                                currency = currency,
                                email = email,
                                passwordHash = passwordHash,
                                pglobAccountNumber = pglobAccountNumber
                            )
                        }
                    },
                    onFailedSignInAttempt = { attempts, lockUntilMillis ->
                        scope.launch {
                            settingsStore.recordFailedSignInAttempt(attempts, lockUntilMillis)
                        }
                    },
                    onPasswordResetComplete = { newHash, updatedJson ->
                        scope.launch {
                            settingsStore.updatePasswordHash(newHash, updatedJson)
                        }
                    },
                    onAuthSuccess = {}
                )
            }

            if (!splashFinished) {
                SplashScreen(onFinished = { splashFinished = true })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PglobAuthenticatedApp(
    viewModel: PglobViewModel,
    userEmail: String,
    userDisplayName: String,
    isSplashBlocking: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val localSettings by viewModel.localSettings.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val profileResolved by viewModel.profileResolved.collectAsStateWithLifecycle()
    val myProfile by viewModel.myProfile.collectAsStateWithLifecycle()
    val publicProfiles by viewModel.publicProfiles.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val orderHistoryRecords by viewModel.orderHistoryRecords.collectAsStateWithLifecycle()
    val isRefreshingOrders by viewModel.isRefreshingOrders.collectAsStateWithLifecycle()
    val ordersSyncError by viewModel.ordersSyncError.collectAsStateWithLifecycle()
    val marketProducts by viewModel.marketProducts.collectAsStateWithLifecycle()
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()
    val statusReactions by viewModel.statusReactions.collectAsStateWithLifecycle()
    val statusNotes by viewModel.statusNotes.collectAsStateWithLifecycle()
    val myActiveStatusNote by viewModel.myActiveStatusNote.collectAsStateWithLifecycle()
    val myExpiredStatusNote by viewModel.myExpiredStatusNote.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val liveRooms by viewModel.liveRooms.collectAsStateWithLifecycle()
    val datingMatches by viewModel.datingMatches.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val lastCompletedReceipt by viewModel.lastCompletedReceipt.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val showWelcomeConfetti by viewModel.showWelcomeConfetti.collectAsStateWithLifecycle()

    val themeMode = remember(localSettings.themeMode) {
        PglobThemeMode.entries.firstOrNull { it.name == localSettings.themeMode } ?: PglobThemeMode.PGLOB_BLACK
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(isSplashBlocking) {
        if (!isSplashBlocking && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var isAppLocked by remember { mutableStateOf(false) }
    var initializedLock by remember { mutableStateOf(false) }
    var lastBackgroundMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(profileResolved, localSettings.appLockEnabled) {
        if (profileResolved && !initializedLock) {
            initializedLock = true
            if (localSettings.appLockEnabled) {
                isAppLocked = true
            }
        }
    }

    DisposableEffect(lifecycleOwner, localSettings.appLockEnabled, localSettings.appLockTimeoutSec) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    lastBackgroundMillis = System.currentTimeMillis()
                }
                Lifecycle.Event.ON_START -> {
                    if (localSettings.appLockEnabled && lastBackgroundMillis > 0L) {
                        val elapsedSec = (System.currentTimeMillis() - lastBackgroundMillis) / 1000L
                        if (elapsedSec >= localSettings.appLockTimeoutSec) {
                            isAppLocked = true
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(toastMessage!!)
            viewModel.clearToast()
        }
    }

    PglobChatTheme(themeMode = themeMode, boldFontMode = localSettings.boldFontMode) {
        if (!profileResolved) {
            Box(
                modifier = Modifier.fillMaxSize().background(PglobBlack),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = PglobGold)
                    Text("Syncing your PGLOB World...", color = PglobGreyText)
                }
            }
            return@PglobChatTheme
        }

        if (myProfile == null || myProfile!!.username.isBlank()) {
            SmartOnboardingSetupScreen(
                defaultEmail = userEmail,
                defaultName = userDisplayName,
                onCheckUsername = { viewModel.checkUsernameAvailable(it) },
                onConfigureSecurity = { pin, biometric ->
                    if (pin.length == 4) {
                        viewModel.setTransactionPin(pin)
                    }
                    viewModel.setBiometricEnabled(biometric)
                },
                onFinishSetup = { fullName, username, usernameVerified, phone, email, country, flag, currency, lang, about, photoUri ->
                    viewModel.completeProfileSetup(
                        fullName = fullName,
                        username = username,
                        usernameVerified = usernameVerified,
                        phone = phone,
                        email = email,
                        country = country,
                        countryFlag = flag,
                        currency = currency,
                        language = lang,
                        about = about,
                        photoUri = photoUri,
                        isInitialSignup = true
                    )
                }
            )
            return@PglobChatTheme
        }

        if (myProfile?.isBanned == true) {
            Box(modifier = Modifier.fillMaxSize().background(PglobBlack).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Account Suspended", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Your PGLOB APP account has been restricted by Admin Moderation.", color = PglobWhite)
                    Button(
                        onClick = {
                            viewModel.logoutExplicitly(false)
                            Firebase.auth.signOut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) { Text("Sign Out") }
                }
            }
            return@PglobChatTheme
        }

        if (isAppLocked) {
            AppLockScreen(
                displayName = myProfile?.fullName.orEmpty(),
                photoUri = myProfile?.photoUri.orEmpty(),
                savedPin = myProfile?.transactionPin.orEmpty(),
                biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
                onUnlockSuccess = { isAppLocked = false },
                onResetPinWithOtp = { newPin -> viewModel.setTransactionPin(newPin) },
                onSwitchAccount = {
                    scope.launch {
                        viewModel.onUserLoggedOutCleanup(forgetDeviceAccount = false)
                        try { credentialManager.clearCredentialState(ClearCredentialStateRequest()) } catch (_: Exception) {}
                        Firebase.auth.signOut()
                    }
                }
            )
            return@PglobChatTheme
        }

        // Main App Navigation State
        var bottomTab by remember { mutableStateOf("Chats") } // Chats, Updates, Communities, Calls
        var activeTopTab by remember { mutableStateOf("Chats") }
        var activeChatFilter by remember { mutableStateOf("All") } // All, Unread, Favorites, Groups, Locked
        var searchQuery by remember { mutableStateOf("") }
        var showArchivedOnly by remember { mutableStateOf(false) }
        var activeChatId by remember { mutableStateOf<String?>(null) }
        var activeCheckoutProduct by remember { mutableStateOf<MarketProduct?>(null) }
        var showOrderHistory by remember { mutableStateOf(false) }
        var showProfileSettings by remember { mutableStateOf(false) }
        var showAdminDashboard by remember { mutableStateOf(false) }
        var showContributionsList by remember { mutableStateOf(false) }
        var activeContributionId by remember { mutableStateOf<String?>(null) }
        var showCreateContributionSheet by remember { mutableStateOf(false) }
        var createContribPrefillTitle by remember { mutableStateOf("") }
        var createContribLinkedChatId by remember { mutableStateOf("") }
        var showFabActionMenuSheet by remember { mutableStateOf(false) }
        var showWallpaperSheet by remember { mutableStateOf(false) }
        var showNewChatSheet by remember { mutableStateOf(false) }
        var showGlobalAiSheet by remember { mutableStateOf(false) }
        var showNotificationSheet by remember { mutableStateOf(false) }
        var showLockedFolderUnlockPrompt by remember { mutableStateOf(false) }
        var viewingStatusId by remember { mutableStateOf<String?>(null) }
        var viewingStatusOpenViewers by remember { mutableStateOf(false) }
        var viewingStatusNoteId by remember { mutableStateOf<String?>(null) }
        var showWriteStatusNote by remember { mutableStateOf(false) }
        var noteToEditOrReplace by remember { mutableStateOf<StatusNote?>(null) }
        var editNoteKeepSameId by remember { mutableStateOf(false) }
        var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
        val appUiStr = remember(localSettings.appLanguage) { PglobStrings.forLanguage(localSettings.appLanguage) }
        val unreadNotifCount = remember(notifications) { notifications.count { it.isUnread } }

        val quickCameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { ok ->
            if (ok && tempCameraUri != null) {
                viewModel.postStatus("Captured with PGLOB Camera", mediaType = "IMAGE", mediaUri = tempCameraUri.toString())
                bottomTab = "Updates"
            }
        }

        val quickCamPermLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val u = createTempCameraUri(context)
                tempCameraUri = u
                quickCameraLauncher.launch(u)
            } else {
                viewModel.showToast("Allow Camera in Settings to capture a Status photo")
            }
        }

        val activeChat: ChatThread? = remember(chats, activeChatId) {
            chats.firstOrNull { it.id == activeChatId }
        }

        val totalUnreadChats = remember(chats) {
            chats.sumOf { it.unreadCount }.coerceAtLeast(chats.count { !it.isArchived })
        }

        PglobListBackground {
            when {
                showAdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        localSettings = localSettings,
                        marketProducts = marketProducts,
                        jobs = jobs,
                        onBack = { showAdminDashboard = false },
                        onOpenContribution = { gid ->
                            showAdminDashboard = false
                            activeContributionId = gid
                        }
                    )
                }

                activeContributionId != null -> {
                    ContributionDetailScreen(
                        groupId = activeContributionId!!,
                        viewModel = viewModel,
                        onBack = { activeContributionId = null },
                        onOpenLinkedChat = { cid ->
                            activeContributionId = null
                            showContributionsList = false
                            activeChatId = cid
                            bottomTab = "Chats"
                            activeTopTab = "Chats"
                        }
                    )
                }

                showContributionsList -> {
                    ContributionsListScreen(
                        viewModel = viewModel,
                        onBack = { showContributionsList = false },
                        onOpenGroup = { gid -> activeContributionId = gid },
                        onCreateNew = {
                            createContribPrefillTitle = ""
                            createContribLinkedChatId = ""
                            showCreateContributionSheet = true
                        }
                    )
                }

                showProfileSettings -> {
                    ProfileAndSettingsScreen(
                        myProfile = myProfile,
                        localSettings = localSettings,
                        viewModel = viewModel,
                        canAccessAdmin = viewModel.canAccessAdminDashboard(),
                        onBack = { showProfileSettings = false },
                        onOpenWallpaperSheet = { showWallpaperSheet = true },
                        onOpenAdminDashboard = {
                            showProfileSettings = false
                            showAdminDashboard = true
                        }
                    )
                }

                activeChat != null -> {
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                        ChatDetailScreen(
                            chat = activeChat,
                            viewModel = viewModel,
                            myProfile = myProfile,
                            localSettings = localSettings,
                            wallpaperId = localSettings.wallpaperId,
                            customWallpaperUri = localSettings.customWallpaperUri,
                            isOnline = isOnline,
                            aiState = aiState,
                            onBack = { activeChatId = null },
                            onOpenWallpaperSheet = { showWallpaperSheet = true },
                            onOpenStatusById = { stId ->
                                if (statusNotes.any { it.id == stId }) {
                                    viewingStatusNoteId = stId
                                } else {
                                    viewingStatusOpenViewers = false
                                    viewingStatusId = stId
                                }
                            },
                            onOpenContribution = { gid -> activeContributionId = gid },
                            onCreateContributionForChat = { chatThread ->
                                createContribPrefillTitle = chatThread.title
                                createContribLinkedChatId = chatThread.id
                                showCreateContributionSheet = true
                            }
                        )
                    }
                }

                activeCheckoutProduct != null -> {
                    val checkoutProd = activeCheckoutProduct!!
                    val checkoutCurrency = myProfile?.currency?.ifBlank { "₦" } ?: "₦"
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                        MarketProductCheckoutOverlay(
                            product = checkoutProd,
                            currencySymbol = checkoutCurrency,
                            walletBalance = myProfile?.walletBalance ?: localSettings.localWalletBalance,
                            coinsBalance = myProfile?.coins ?: localSettings.localCoins,
                            defaultLocation = myProfile?.country ?: checkoutProd.location,
                            defaultCountry = myProfile?.country?.ifBlank { "Nigeria" } ?: "Nigeria",
                            defaultPhone = myProfile?.phone?.ifBlank { localSettings.chattingPhoneNumber } ?: localSettings.chattingPhoneNumber,
                            onDismiss = { activeCheckoutProduct = null },
                            onChatSeller = {
                                viewModel.openOrCreateChatWith(
                                    title = "${checkoutProd.sellerName} ${checkoutProd.sellerFlag}",
                                    targetUid = checkoutProd.sellerId,
                                    flag = checkoutProd.sellerFlag
                                ) { chatId ->
                                    viewModel.sendChatMessage(
                                        chatId,
                                        listOf(viewModel.currentUserId, checkoutProd.sellerId),
                                        "Hi! Is \"${checkoutProd.title}\" still available on PGLOB Market?"
                                    )
                                    activeCheckoutProduct = null
                                    activeChatId = chatId
                                    bottomTab = "Chats"
                                    activeTopTab = "Chats"
                                }
                            },
                            onAddMoney = {
                                activeCheckoutProduct = null
                                bottomTab = "Chats"
                                activeTopTab = "Pay"
                                viewModel.showToast("Tap Fund Wallet in Pay to add money")
                            },
                            onViewOrders = {
                                activeCheckoutProduct = null
                                showOrderHistory = true
                            },
                            onConfirmPurchase = { qty, deliveryOption, deliveryAddress, deliveryFeeCash, paymentMethod ->
                                viewModel.purchaseMarketProductWithEscrow(
                                    product = checkoutProd,
                                    quantity = qty,
                                    deliveryOption = deliveryOption,
                                    deliveryAddress = deliveryAddress,
                                    deliveryFeeCash = deliveryFeeCash,
                                    paymentMethod = paymentMethod
                                )
                            }
                        )
                    }
                }

                showOrderHistory -> {
                    OrderHistoryScreen(
                        orders = orderHistoryRecords,
                        isRefreshing = isRefreshingOrders,
                        syncError = ordersSyncError,
                        onBack = { showOrderHistory = false },
                        onRefresh = { viewModel.refreshOrdersFromFirestore() },
                        onConfirmDelivered = { order ->
                            viewModel.markOrderDelivered(order.id, order.orderReference)
                        },
                        onChatSeller = { order ->
                            val targetUid = order.counterpartyId.ifBlank {
                                "seller_${order.counterpartyName.lowercase().filter { it.isLetterOrDigit() }.take(12)}"
                            }
                            viewModel.openOrCreateChatWith(
                                title = order.counterpartyName,
                                targetUid = targetUid,
                                flag = "🛍️"
                            ) { chatId ->
                                showOrderHistory = false
                                activeChatId = chatId
                                bottomTab = "Chats"
                                activeTopTab = "Chats"
                            }
                        },
                        onBrowseMarket = {
                            showOrderHistory = false
                            bottomTab = "Chats"
                            activeTopTab = "Market"
                        }
                    )
                }

                else -> {
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            NavigationBar(
                                containerColor = WaBackground,
                                contentColor = WaTextPrimary,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                val navItems = listOf(
                                    Pair("Chats" to appUiStr.chats, Icons.AutoMirrored.Filled.Chat to totalUnreadChats),
                                    Pair("Updates" to appUiStr.updates, Icons.Default.Update to statuses.size),
                                    Pair("Communities" to appUiStr.communities, Icons.Default.Groups to 0),
                                    Pair("Calls" to appUiStr.calls, Icons.Default.Call to 1)
                                )
                                navItems.forEach { (keyLabelPair, iconBadgePair) ->
                                    val (tabKey, displayLabel) = keyLabelPair
                                    val (icon, badgeCount) = iconBadgePair
                                    val selected = bottomTab == tabKey
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { bottomTab = tabKey },
                                        icon = {
                                            Box {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = displayLabel,
                                                    tint = if (selected) WaPureBlack else WaTextPrimary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(if (selected) WaGreen else Color.Transparent)
                                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                                )
                                                if (badgeCount > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .clip(CircleShape)
                                                            .background(WaGreen)
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                                            color = WaTextPrimary,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                displayLabel,
                                                color = if (selected) WaGreen else WaTextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                                        modifier = Modifier.testTag("bottom_nav_${tabKey.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(innerPadding)
                        ) {
                            when (bottomTab) {
                                "Chats" -> {
                                    ChatsTopHeaderAndTabs(
                                        activeTopTab = activeTopTab,
                                        searchQuery = searchQuery,
                                        appLanguage = localSettings.appLanguage,
                                        unreadNotifCount = unreadNotifCount,
                                        activeChatFilter = activeChatFilter,
                                        lockedCount = localSettings.lockedChatIds.size,
                                        hideLockedChats = localSettings.hideLockedChats,
                                        onSelectChatFilter = { activeChatFilter = it },
                                        onOpenNotifications = { showNotificationSheet = true },
                                        onOpenLockedFolder = { showLockedFolderUnlockPrompt = true },
                                        onSearchChange = { searchQuery = it },
                                        onAskAiClick = { showGlobalAiSheet = true },
                                        onSelectTopTab = {
                                            activeTopTab = it
                                            showArchivedOnly = false
                                        },
                                        onCameraClick = {
                                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                            if (granted) {
                                                val u = createTempCameraUri(context)
                                                tempCameraUri = u
                                                quickCameraLauncher.launch(u)
                                            } else {
                                                quickCamPermLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        onOpenProfileSettings = { showProfileSettings = true },
                                        onOpenWallpaperPicker = { showWallpaperSheet = true },
                                        onNewGroupClick = { showNewChatSheet = true },
                                        onNewContributionClick = { showContributionsList = true },
                                        onArchivedClick = { showArchivedOnly = !showArchivedOnly },
                                        showArchivedRow = !showArchivedOnly,
                                        archivedCount = chats.count { it.isArchived }
                                    )

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        when (activeTopTab) {
                                            "Chats" -> ChatListTabContent(
                                                chats = chats,
                                                currentUserId = viewModel.currentUserId,
                                                searchQuery = searchQuery,
                                                showArchivedOnly = showArchivedOnly,
                                                chatFilter = activeChatFilter,
                                                lockedChatIds = localSettings.lockedChatIds,
                                                favoriteChatIds = localSettings.favoriteChatIds,
                                                statusNotes = statusNotes,
                                                myName = myProfile?.fullName?.ifBlank { userDisplayName } ?: userDisplayName,
                                                myPhotoUri = myProfile?.photoUri.orEmpty(),
                                                myActiveStatusNote = myActiveStatusNote,
                                                myExpiredStatusNote = myExpiredStatusNote,
                                                onAddOrReplaceNote = {
                                                    noteToEditOrReplace = myActiveStatusNote
                                                    editNoteKeepSameId = false
                                                    showWriteStatusNote = true
                                                },
                                                onOpenNote = { note -> viewingStatusNoteId = note.id },
                                                onBackFromArchived = { showArchivedOnly = false },
                                                onOpenChat = { activeChatId = it.id },
                                                onTogglePin = { viewModel.updateChatFlags(it.id, pinned = !it.isPinned) },
                                                onToggleArchive = { viewModel.updateChatFlags(it.id, archived = !it.isArchived) },
                                                onToggleMute = { viewModel.updateChatFlags(it.id, muted = !it.isMuted) },
                                                onToggleLockChat = { viewModel.toggleLockedChat(it.id) },
                                                onToggleFavoriteChat = { viewModel.toggleFavoriteChat(it.id) },
                                                onDeleteChat = { viewModel.deleteChat(it.id) },
                                                onNewChatClick = { showFabActionMenuSheet = true }
                                            )

                                            "Earn" -> EarnWalletScreen(
                                                myProfile = myProfile,
                                                publicProfiles = publicProfiles,
                                                transactions = transactions,
                                                localSettings = localSettings,
                                                viewModel = viewModel
                                            )

                                            "Live" -> LiveWorldRoomsScreen(
                                                liveRooms = liveRooms,
                                                aiState = aiState,
                                                viewModel = viewModel
                                            )

                                            "Connect" -> ConnectGlobalScreen(
                                                publicProfiles = publicProfiles,
                                                currentUserId = viewModel.currentUserId,
                                                viewModel = viewModel,
                                                onOpenChatId = { chatId ->
                                                    activeChatId = chatId
                                                    activeTopTab = "Chats"
                                                }
                                            )

                                            "Market" -> MarketScreen(
                                                products = marketProducts,
                                                myProfile = myProfile,
                                                localSettings = localSettings,
                                                viewModel = viewModel,
                                                onOpenChatId = { chatId ->
                                                    activeChatId = chatId
                                                    bottomTab = "Chats"
                                                    activeTopTab = "Chats"
                                                },
                                                onOpenCheckout = { prod ->
                                                    activeCheckoutProduct = prod
                                                },
                                                onOpenOrderHistory = { showOrderHistory = true }
                                            )

                                            "Reels" -> ReelsScreen(
                                                reels = reels,
                                                myProfile = myProfile,
                                                viewModel = viewModel
                                            )

                                            "Games" -> GamesScreen(
                                                myProfile = myProfile,
                                                localSettings = localSettings,
                                                viewModel = viewModel
                                            )

                                            "Learn" -> LearnScreen(
                                                localSettings = localSettings,
                                                viewModel = viewModel
                                            )

                                            "Jobs" -> JobsScreen(
                                                jobs = jobs,
                                                viewModel = viewModel,
                                                onOpenChatId = { chatId ->
                                                    activeChatId = chatId
                                                    activeTopTab = "Chats"
                                                }
                                            )

                                            "Pay" -> PglobPayScreen(
                                                myProfile = myProfile,
                                                transactions = transactions,
                                                localSettings = localSettings,
                                                viewModel = viewModel,
                                                canAccessAdmin = viewModel.canAccessAdminDashboard(),
                                                onOpenContributions = { showContributionsList = true },
                                                onOpenAdminDashboard = { showAdminDashboard = true },
                                                onOpenOrderHistory = { showOrderHistory = true }
                                            )

                                            "Dating" -> DatingScreen(
                                                myProfile = myProfile,
                                                publicProfiles = publicProfiles,
                                                matches = datingMatches,
                                                blockedIds = localSettings.blockedUserIds,
                                                viewModel = viewModel,
                                                onOpenChatById = { chatId ->
                                                    activeChatId = chatId
                                                    activeTopTab = "Chats"
                                                }
                                            )
                                        }
                                    }
                                }

                                "Updates" -> UpdatesScreen(
                                    statuses = statuses,
                                    statusReactions = statusReactions,
                                    viewedStatusIds = localSettings.viewedStatusIds,
                                    viewModel = viewModel,
                                    statusNotes = statusNotes,
                                    myName = myProfile?.fullName?.ifBlank { userDisplayName } ?: userDisplayName,
                                    myPhotoUri = myProfile?.photoUri.orEmpty(),
                                    myActiveStatusNote = myActiveStatusNote,
                                    myExpiredStatusNote = myExpiredStatusNote,
                                    onAddOrReplaceNote = {
                                        noteToEditOrReplace = myActiveStatusNote
                                        editNoteKeepSameId = false
                                        showWriteStatusNote = true
                                    },
                                    onOpenNote = { note -> viewingStatusNoteId = note.id },
                                    onOpenStatus = { stId, openViewers ->
                                        viewingStatusOpenViewers = openViewers
                                        viewingStatusId = stId
                                    }
                                )
                                "Communities" -> CommunitiesScreen(viewModel = viewModel)
                                "Calls" -> CallsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            // Global Modals & Confetti Overlays
            if (showWallpaperSheet) {
                WallpaperPickerBottomSheet(
                    currentWallpaperId = localSettings.wallpaperId,
                    customUri = localSettings.customWallpaperUri,
                    viewModel = viewModel,
                    onDismiss = { showWallpaperSheet = false }
                )
            }

            if (showNewChatSheet) {
                NewChatOrGroupSheet(
                    publicProfiles = publicProfiles,
                    currentUserId = viewModel.currentUserId,
                    onDismiss = { showNewChatSheet = false },
                    onOpenOrCreate = { title, targetUid, isGroup, flag ->
                        viewModel.openOrCreateChatWith(title, targetUid, isGroup, flag) { id ->
                            activeChatId = id
                        }
                    }
                )
            }

            if (showGlobalAiSheet) {
                PglobAiAssistantSheet(
                    aiState = aiState,
                    initialPrompt = searchQuery,
                    onAsk = { viewModel.askPglobAi(it) },
                    onDismiss = { showGlobalAiSheet = false; viewModel.clearAiState() }
                )
            }

            if (showNotificationSheet) {
                NotificationCenterSheet(
                    notifications = notifications,
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onDelete = { viewModel.removeNotification(it) },
                    onClickItem = { item ->
                        viewModel.markAllNotificationsRead()
                        showNotificationSheet = false
                        if (item.targetChatId.isNotBlank()) {
                            activeChatId = item.targetChatId
                            bottomTab = "Chats"
                            activeTopTab = "Chats"
                        } else if (item.targetStatusId.isNotBlank()) {
                            if (item.category == "STATUS_NOTE_LIKE" || item.category == "STATUS_NOTE_RESHARE") {
                                viewingStatusNoteId = item.targetStatusId
                            } else {
                                viewingStatusOpenViewers = (item.category == "STATUS_LIKE")
                                viewingStatusId = item.targetStatusId
                            }
                        }
                    },
                    onPayRequest = { item ->
                        viewModel.executePayAction(
                            txType = "SEND",
                            amount = item.requestAmount,
                            fee = 0.0,
                            method = "PGLOB Request Settlement",
                            destination = item.requestFrom.ifBlank { "Member" },
                            note = item.title
                        )
                        viewModel.removeNotification(item.id)
                        showNotificationSheet = false
                    },
                    onDismiss = { showNotificationSheet = false }
                )
            }

            if (showFabActionMenuSheet) {
                ModalBottomSheet(onDismissRequest = { showFabActionMenuSheet = false }, containerColor = WaCard) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Start New", color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Surface(
                            color = WaInput,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                showFabActionMenuSheet = false
                                showNewChatSheet = true
                            }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = WaGreen, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("New Chat or Group", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Text("Message a contact or create a PGLOB group", color = WaTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                        Surface(
                            color = WaInput,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                showFabActionMenuSheet = false
                                showContributionsList = true
                            }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = WaGreen, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("💰 My Contributions (Ajo / Esusu)", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Text("View active savings circles, turns & escrow pot", color = WaTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                        Surface(
                            color = WaInput,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                showFabActionMenuSheet = false
                                createContribPrefillTitle = ""
                                createContribLinkedChatId = ""
                                showCreateContributionSheet = true
                            }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = PglobGold, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("New Contribution Group", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Text("Start a Daily, Weekly or Monthly Ajo circle with PGLOB Escrow", color = WaTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            if (showCreateContributionSheet) {
                CreateContributionWizardSheet(
                    viewModel = viewModel,
                    prefillTitle = createContribPrefillTitle,
                    linkedChatId = createContribLinkedChatId,
                    onDismiss = { showCreateContributionSheet = false },
                    onCreated = { created ->
                        showCreateContributionSheet = false
                        activeContributionId = created.id
                    }
                )
            }

            if (viewingStatusId != null) {
                StatusViewerDialog(
                    statuses = statuses,
                    initialStatusId = viewingStatusId!!,
                    statusReactions = statusReactions,
                    statusFavoritesIds = localSettings.statusFavoritesIds,
                    chats = chats,
                    publicProfiles = publicProfiles,
                    viewModel = viewModel,
                    initialOpenViewers = viewingStatusOpenViewers,
                    onDismiss = {
                        viewingStatusId = null
                        viewingStatusOpenViewers = false
                    },
                    onOpenChatById = { chatId ->
                        viewingStatusId = null
                        viewingStatusOpenViewers = false
                        activeChatId = chatId
                        bottomTab = "Chats"
                        activeTopTab = "Chats"
                    }
                )
            }

            if (viewingStatusNoteId != null) {
                FullScreenStatusNoteViewer(
                    notes = statusNotes,
                    initialNoteId = viewingStatusNoteId!!,
                    currentUserId = viewModel.currentUserId,
                    canAdminModerate = viewModel.canAccessAdminDashboard(),
                    chats = chats,
                    contributions = contributions,
                    onDismiss = { viewingStatusNoteId = null },
                    onRecordView = { viewModel.recordStatusNoteView(it) },
                    onToggleLike = { viewModel.toggleLikeStatusNote(it) },
                    onReshareToMyNote = {
                        viewModel.reshareStatusNoteToMyNote(it)
                        viewingStatusNoteId = null
                    },
                    onReshareToMyStatus = {
                        viewModel.reshareStatusNoteToMyStatus(it)
                        viewingStatusNoteId = null
                        bottomTab = "Updates"
                    },
                    onShareToChat = { note, chatId, title, members ->
                        viewModel.shareStatusNoteToChat(note, chatId, title, members) { targetChatId ->
                            viewingStatusNoteId = null
                            activeChatId = targetChatId
                            bottomTab = "Chats"
                            activeTopTab = "Chats"
                        }
                    },
                    onReplyToNote = { note, replyText ->
                        viewModel.replyToStatusNote(note, replyText) { targetChatId ->
                            viewingStatusNoteId = null
                            activeChatId = targetChatId
                            bottomTab = "Chats"
                            activeTopTab = "Chats"
                        }
                    },
                    onReplaceNote = { existing ->
                        viewingStatusNoteId = null
                        noteToEditOrReplace = existing
                        editNoteKeepSameId = false
                        showWriteStatusNote = true
                    },
                    onEditNote = { existing ->
                        viewingStatusNoteId = null
                        noteToEditOrReplace = existing
                        editNoteKeepSameId = true
                        showWriteStatusNote = true
                    },
                    onDeleteNote = { noteId ->
                        viewModel.deleteStatusNote(noteId)
                        viewingStatusNoteId = null
                    },
                    onReportNote = { note, reason ->
                        viewModel.reportStatusNote(note, reason)
                    },
                    onBlockAuthor = { authorUid ->
                        viewModel.toggleBlockUser(authorUid)
                    },
                    onAdminDeleteNote = { noteId ->
                        viewModel.adminDeleteStatusNote(noteId)
                        viewingStatusNoteId = null
                    },
                    onShowMessage = { msg -> viewModel.showToast(msg) },
                    publicProfiles = publicProfiles,
                    blockedUserIds = localSettings.blockedUserIds,
                    onOpenUserChat = { target ->
                        viewModel.openOrCreateChatWith(
                            "${target.displayName} ${target.countryFlag}",
                            target.userId,
                            false,
                            target.countryFlag
                        ) { targetChatId ->
                            viewingStatusNoteId = null
                            activeChatId = targetChatId
                            bottomTab = "Chats"
                            activeTopTab = "Chats"
                        }
                    },
                    onReportUser = { target ->
                        viewModel.submitReport("USER_PROFILE", target.userId, "Reported from Status Note")
                    }
                )
            }

            if (showWriteStatusNote) {
                val isProUser = (myProfile?.vipTier ?: localSettings.localVipTier) != "FREE" || viewModel.isSuperAdmin()
                WriteStatusNoteOverlay(
                    existingNote = noteToEditOrReplace,
                    isEditingSameId = editNoteKeepSameId,
                    isProUser = isProUser,
                    publicProfiles = publicProfiles,
                    currentUserId = viewModel.currentUserId,
                    onDismiss = {
                        showWriteStatusNote = false
                        noteToEditOrReplace = null
                        editNoteKeepSameId = false
                    },
                    onSubmitNewOrReplace = { text, bgColor, music, mood, privacy, customUids, durationHours ->
                        viewModel.postStatusNote(
                            text = text,
                            bgColor = bgColor,
                            music = music,
                            mood = mood,
                            privacy = privacy,
                            customAllowedUids = customUids,
                            durationHours = durationHours
                        )
                        showWriteStatusNote = false
                        noteToEditOrReplace = null
                        editNoteKeepSameId = false
                    },
                    onSubmitEdit = { existing, text, bgColor, music, mood, privacy, customUids, durationHours ->
                        viewModel.editStatusNote(
                            existing = existing,
                            text = text,
                            bgColor = bgColor,
                            music = music,
                            mood = mood,
                            privacy = privacy,
                            customAllowedUids = customUids,
                            durationHours = durationHours
                        )
                        showWriteStatusNote = false
                        noteToEditOrReplace = null
                        editNoteKeepSameId = false
                    }
                )
            }

            if (showLockedFolderUnlockPrompt) {
                PinOrBiometricConfirmSheet(
                    title = "Unlock Locked Chats 🔒",
                    summary = "Verify PIN or Fingerprint/Face ID to view Locked Chats",
                    hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
                    biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
                    lockUntilMillis = localSettings.payLockUntilMillis,
                    onDismiss = { showLockedFolderUnlockPrompt = false },
                    onSetPin = { viewModel.setTransactionPin(it) },
                    onConfirmWithPin = { pin, bio ->
                        if (viewModel.verifyPinOrAllow(pin, bio)) {
                            activeChatFilter = "Locked"
                            showLockedFolderUnlockPrompt = false
                        }
                    }
                )
            }

            lastCompletedReceipt?.let { receiptTx ->
                PayReceiptSuccessDialog(
                    tx = receiptTx,
                    onDismiss = { viewModel.clearLastReceipt() }
                )
            }

            if (showWelcomeConfetti) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissWelcomeConfetti() },
                    containerColor = WaCard,
                    title = { Text("Welcome to PGLOB APP!", color = WaGreen, fontWeight = FontWeight.Medium) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Your account @${myProfile?.username.orEmpty()} is live!", color = WaTextPrimary)
                            Text("🪙 +100 Bonus Coins & 💎 +1 Diamond added to your Earn Wallet!", color = WaGreen, fontWeight = FontWeight.Medium)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissWelcomeConfetti() },
                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                        ) { Text("Let's Explore", fontWeight = FontWeight.Medium) }
                    }
                )
            }
        }
    }
}
