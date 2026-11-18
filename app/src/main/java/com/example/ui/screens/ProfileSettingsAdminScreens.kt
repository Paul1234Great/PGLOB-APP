package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.LocalAppSettings
import com.example.data.local.WallpaperOption
import com.example.data.model.GlobalCatalogData
import com.example.data.model.PublicProfile
import com.example.data.model.UserPrivateProfile
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.CountryPickerSheet
import com.example.ui.components.OtpSixBoxVerification
import com.example.ui.components.PglobChatBackground
import com.example.ui.components.PglobProfileImage
import com.example.ui.components.PoliciesListDialog
import com.example.ui.components.VerifiedTick
import com.example.ui.components.createTempCameraUri
import com.example.util.SecurityAndNumberCodec
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobThemeMode
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.PglobStrings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPickerBottomSheet(
    currentWallpaperId: String,
    customUri: String,
    viewModel: PglobViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setWallpaper(WallpaperOption.CUSTOM_URI.id, uri.toString())
            onDismiss()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok && tempCameraUri != null) {
            viewModel.setWallpaper(WallpaperOption.CUSTOM_URI.id, tempCameraUri.toString())
            onDismiss()
        }
    }

    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val u = createTempCameraUri(context)
            tempCameraUri = u
            cameraLauncher.launch(u)
        } else {
            viewModel.showToast("Allow Camera in Settings to capture a wallpaper")
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WaCard) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Chat Wallpaper", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)

            // Live Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, WaDivider, RoundedCornerShape(16.dp))
            ) {
                PglobChatBackground(wallpaperId = currentWallpaperId, customUri = customUri) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(color = WaGreen, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                "Chat Background Preview",
                                color = WaPureBlack,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val presetOptions = listOf(
                    WallpaperOption.DOODLE_PGLOB,
                    WallpaperOption.SOLID_DARK,
                    WallpaperOption.PLAIN_BLACK
                )
                items(presetOptions) { opt ->
                    val selected = currentWallpaperId == opt.id
                    Surface(
                        color = if (selected) WaGreen.copy(alpha = 0.16f) else WaInput,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (selected) WaGreen else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setWallpaper(opt.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(opt.label, color = WaTextPrimary, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
                            if (selected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = WaGreen)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WaGreen)
                            Spacer(Modifier.width(6.dp))
                            Text("Gallery", color = WaGreen)
                        }
                        OutlinedButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    val u = createTempCameraUri(context)
                                    tempCameraUri = u
                                    cameraLauncher.launch(u)
                                } else {
                                    camPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = WaGreen)
                            Spacer(Modifier.width(6.dp))
                            Text("Camera", color = WaGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAndSettingsScreen(
    myProfile: UserPrivateProfile?,
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel,
    canAccessAdmin: Boolean = viewModel.canAccessAdminDashboard(),
    onBack: () -> Unit,
    onOpenWallpaperSheet: () -> Unit,
    onOpenAdminDashboard: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var settingsSearch by remember { mutableStateOf("") }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showFullPhoto by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showPublicProfilePreview by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showChangeUsernameOtp by remember { mutableStateOf(false) }
    var showRecoveryOtp by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var showLinkedDevicesDialog by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showTwoStepDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showPoliciesDialog by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showSubscriptionsDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showAppUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAccountQrDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val chattingPhone = (myProfile?.phone?.ifBlank {
        localSettings.chattingPhoneNumber.ifBlank { localSettings.savedPhoneNumber }
    } ?: localSettings.chattingPhoneNumber).ifBlank { "Add Chatting Number" }
    val accountNumber = (myProfile?.payAccountNumber?.ifBlank {
        localSettings.pglobAccountNumber
    } ?: localSettings.pglobAccountNumber).ifBlank { "9000123456" }
    val effectiveEmail = myProfile?.email?.ifBlank { localSettings.savedEmail }
        ?: localSettings.savedEmail

    val linkedDevices by viewModel.linkedDevices.collectAsStateWithLifecycle()
    val uiStr = remember(localSettings.appLanguage) { PglobStrings.forLanguage(localSettings.appLanguage) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.uploadProfilePhotoFromUri(context, uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok && tempCameraUri != null) viewModel.uploadProfilePhotoFromUri(context, tempCameraUri!!) }

    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val u = createTempCameraUri(context)
            tempCameraUri = u
            cameraLauncher.launch(u)
        } else {
            viewModel.showToast("Camera permission denied")
        }
    }

    val q = settingsSearch.trim()
    fun matches(vararg keywords: String): Boolean =
        q.isBlank() || keywords.any { it.contains(q, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WaBackground)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
            }
            Text("Profile & ${uiStr.settings}", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 22.sp, modifier = Modifier.weight(1f))
            if (canAccessAdmin) {
                Box {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Profile options", tint = WaTextPrimary)
                    }
                    DropdownMenu(expanded = showTopMenu, onDismissRequest = { showTopMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("👑 Admin Dashboard", color = WaGold, fontWeight = FontWeight.Medium) },
                            onClick = {
                                showTopMenu = false
                                onOpenAdminDashboard()
                            }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = WaDivider, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = settingsSearch,
                    onValueChange = { settingsSearch = it },
                    placeholder = { Text("Search settings (Wallpaper, Privacy, PIN, Language)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Settings") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (canAccessAdmin && matches("admin", "dashboard", "escrow", "sub-admin")) {
                item {
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WaGold, RoundedCornerShape(16.dp))
                            .clickable { onOpenAdminDashboard() }
                            .testTag("admin_dashboard_entry_card")
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Dashboard", tint = WaGold, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("👑 ADMIN DASHBOARD", color = WaGold, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text(
                                    if (viewModel.isSuperAdmin()) "Super Admin • Full Users, Escrow & Sub-Admin Controls"
                                    else "Sub-Admin • Assigned Platform Controls",
                                    color = WaTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text("Open →", color = WaGold, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Profile Card
            if (matches("profile", "name", "username", "photo", "bio", "interest", "account")) {
                item {
                    val displayedBio = myProfile?.bio?.ifBlank { localSettings.localBio }.orEmpty()
                    val displayedInterests = myProfile?.interests?.ifEmpty { localSettings.localInterests } ?: localSettings.localInterests
                    Surface(
                        color = PglobDarkGrey.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PglobGold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .testTag("profile_summary_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clickable { showPhotoOptions = true }
                                        .testTag("profile_upload_photo_btn")
                                ) {
                                    AvatarCircle(
                                        name = myProfile?.fullName ?: "Me",
                                        photoUri = myProfile?.photoUri.orEmpty(),
                                        size = 72.dp,
                                        vipTier = myProfile?.vipTier ?: "FREE",
                                        badgeEmoji = myProfile?.countryFlag
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(myProfile?.fullName ?: "Global Member", color = PglobWhite, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                                        if (myProfile?.usernameVerified == true) {
                                            Spacer(Modifier.width(6.dp))
                                            VerifiedTick(true, isGold = true)
                                        }
                                    }
                                    Text("@${myProfile?.username ?: "username"}", color = PglobGold, fontWeight = FontWeight.SemiBold)
                                    Text(myProfile?.about ?: "Hey there! I am using PGLOB APP.", color = PglobGreyText, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { showEditProfile = true },
                                    modifier = Modifier.size(48.dp).testTag("profile_edit_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PglobGold)
                                }
                            }

                            // Short Bio & Interests (Visible to other users)
                            Surface(
                                color = WaBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WaDivider, RoundedCornerShape(12.dp))
                                    .clickable { showEditProfile = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Short Bio", color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Public Profile", color = WaTextSecondary, fontSize = 11.sp)
                                    }
                                    Text(
                                        text = displayedBio.ifBlank { "Tap Edit Profile to write a short bio so others can know you." },
                                        color = if (displayedBio.isBlank()) WaTextSecondary else WaTextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.testTag("profile_bio_text")
                                    )
                                    Text("Interests", color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (displayedInterests.isEmpty()) {
                                        Text(
                                            text = "No interests added yet • Tap to pick topics",
                                            color = WaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    } else {
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("profile_interests_flow")
                                        ) {
                                            displayedInterests.forEach { tag ->
                                                Surface(
                                                    color = WaGreen.copy(alpha = 0.16f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.border(1.dp, WaGreen.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        color = WaTextPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showEditProfile = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("profile_edit_bio_interests_btn")
                                ) {
                                    Text("Edit Bio & Interests", fontSize = 12.sp, color = WaGreen, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showPublicProfilePreview = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("profile_preview_public_btn")
                                ) {
                                    Text("Public View", fontSize = 12.sp, color = PglobGold, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            // BOX 1: PGLOB Chatting Number (Private Messaging ONLY)
                            Surface(
                                color = WaBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💬 PGLOB Chatting Number", color = WaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Surface(color = WaGreen.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                localSettings.chatNumberPrivacy,
                                                color = WaGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(chattingPhone, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(onClick = { showPrivacySheet = true }) {
                                                Text("Privacy", color = WaGreen, fontSize = 12.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    cm.setPrimaryClip(ClipData.newPlainText("Chatting Number", chattingPhone))
                                                    viewModel.showToast("Chatting number copied!")
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy chatting number", tint = WaTextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // BOX 2: PGLOB Account Number (For Money & Pay ONLY - Safe to share for payment)
                            Surface(
                                color = Color(0xFF141100),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, PglobGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("💰 PGLOB Account Number (Money & Pay ONLY)", color = PglobGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(accountNumber, color = PglobWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = 1.sp)
                                    Text("Bank: PGLOB Wallet • Never share your Chatting Number for money", color = PglobGreyText, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                cm.setPrimaryClip(ClipData.newPlainText("PGLOB Account Number", accountNumber))
                                                viewModel.showToast("Account number $accountNumber copied!")
                                            },
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Copy", color = PglobGold, fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val share = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, "Send money to my PGLOB Wallet:\nAccount Number: $accountNumber\nAccount Name: ${myProfile?.fullName ?: localSettings.savedDisplayName}\nBank: PGLOB Wallet")
                                                }
                                                context.startActivity(Intent.createChooser(share, "Share PGLOB Account Number"))
                                            },
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Share", color = PglobGold, fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { showAccountQrDialog = true },
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        ) {
                                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("QR", color = WaGreen, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showChangeUsernameOtp = true }, modifier = Modifier.weight(1f)) {
                                    Text(if (myProfile?.usernameVerified == true) "Verified ✔️" else "Verify @user", fontSize = 12.sp, color = PglobGold)
                                }
                                OutlinedButton(onClick = { showPhotoOptions = true }, modifier = Modifier.weight(1f)) {
                                    Text("Change Photo", fontSize = 12.sp, color = PglobWhite)
                                }
                            }
                        }
                    }
                }
            }

            if (matches("linked", "device", "web", "session")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Devices,
                        title = "Linked Devices (${linkedDevices.size})",
                        subtitle = "Manage desktop & companion sessions",
                        onClick = { showLinkedDevicesDialog = true }
                    )
                }
            }

            if (matches("wallpaper", "doodle", "chat", "background")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Wallpaper,
                        title = "Chat Wallpaper",
                        subtitle = "Active: ${WallpaperOption.entries.firstOrNull { it.id == localSettings.wallpaperId }?.label ?: "PGLOB Subtle Doodle"}",
                        onClick = onOpenWallpaperSheet
                    )
                }
            }

            if (matches("theme", "color", "gold", "diamond", "bold", "font", "appearance")) {
                item {
                    Surface(color = PglobDarkGrey.copy(alpha = 0.92f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = PglobGold)
                                Spacer(Modifier.width(10.dp))
                                Text("Luxury App Theme (${PglobThemeMode.entries.size} Themes)", color = PglobWhite, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            ) {
                                PglobThemeMode.entries.forEach { mode ->
                                    val active = localSettings.themeMode == mode.name
                                    Surface(
                                        color = if (active) PglobGold else PglobBlack,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.clickable { viewModel.setThemeMode(mode.name) }
                                    ) {
                                        Text(
                                            mode.displayName,
                                            color = if (active) PglobBlack else PglobWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.TextFields, contentDescription = null, tint = PglobGold)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Bold + Big Accessibility Text", color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Extra bold headings & high readability", color = PglobGreyText, fontSize = 12.sp)
                                    }
                                }
                                Switch(
                                    checked = localSettings.boldFontMode,
                                    onCheckedChange = { viewModel.setBoldFontMode(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold)
                                )
                            }
                        }
                    }
                }
            }

            if (matches("privacy", "security", "lock", "disappearing", "receipt", "last seen")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy & Security Controls",
                        subtitle = "App Lock (${if (localSettings.appLockEnabled) "ON" else "OFF"}), Disappearing Msgs, Last Seen, Read Receipts",
                        onClick = { showPrivacySheet = true }
                    )
                }
            }

            if (matches("pin", "wallet", "pay", "recovery", "security")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Lock,
                        title = "Wallet & App 4-Digit PIN",
                        subtitle = if (myProfile?.transactionPin.isNullOrBlank()) "Tap to set 4-digit Security PIN" else "PIN configured • Change or recover via OTP",
                        onClick = { showPinDialog = true }
                    )
                }
            }

            if (matches("two step", "2fa", "verification", "security", "pin")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Shield,
                        title = "Two-Step Verification",
                        subtitle = if (localSettings.twoStepEnabled) "Enabled (6-digit extra PIN active)" else "Add an extra 6-digit PIN for sign-ins",
                        onClick = { showTwoStepDialog = true }
                    )
                }
            }

            if (matches("fingerprint", "face", "biometric", "lock", "security", "stay", "login")) {
                item {
                    Surface(color = PglobDarkGrey.copy(alpha = 0.92f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PglobGold)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Fingerprint / Face Unlock (Pay & App)", color = PglobWhite, fontWeight = FontWeight.Bold)
                                        Text("Authorize Pay Transfers, Locked Chats & App Lock", color = PglobGreyText, fontSize = 12.sp)
                                    }
                                }
                                Switch(
                                    checked = localSettings.biometricEnabled,
                                    onCheckedChange = { viewModel.setBiometricEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold)
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = WaGreen)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Enable Fingerprint / Face ID for Sign In", color = PglobWhite, fontWeight = FontWeight.Bold)
                                        Text("Optional quick sign-in after Logout (Default OFF)", color = PglobGreyText, fontSize = 12.sp)
                                    }
                                }
                                Switch(
                                    checked = localSettings.fingerprintSignInEnabled,
                                    onCheckedChange = { viewModel.setFingerprintSignInEnabled(it) },
                                    modifier = Modifier.testTag("fingerprint_signin_switch"),
                                    colors = SwitchDefaults.colors(checkedThumbColor = WaPureBlack, checkedTrackColor = WaGreen)
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaGreen)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Stay Login - Remember Me", color = PglobWhite, fontWeight = FontWeight.Bold)
                                        Text("Keep account signed in forever until you tap Logout", color = PglobGreyText, fontSize = 12.sp)
                                    }
                                }
                                Switch(
                                    checked = localSettings.stayLoggedIn,
                                    onCheckedChange = { viewModel.setStayLoggedIn(it) },
                                    modifier = Modifier.testTag("stay_logged_in_switch"),
                                    colors = SwitchDefaults.colors(checkedThumbColor = WaPureBlack, checkedTrackColor = WaGreen)
                                )
                            }
                        }
                    }
                }
            }

            if (matches("account", "phone", "email", "password", "country", "security")) {
                item {
                    Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Account & Security Details", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Country: ${localSettings.savedCountryFlag} ${myProfile?.country?.ifBlank { localSettings.savedCountryName } ?: localSettings.savedCountryName}", color = WaTextPrimary, fontSize = 13.sp)
                            Text("Phone (Chatting): $chattingPhone", color = WaTextPrimary, fontSize = 13.sp)
                            Text("PGLOB Account No: $accountNumber", color = WaGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Recovery Email (Optional): ${effectiveEmail.ifBlank { "Not set (Optional)" }}", color = WaTextSecondary, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showEditEmailDialog = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("settings_edit_email_btn")
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (effectiveEmail.isBlank()) "Add Email" else "Change Email", color = WaGreen, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { showChangePasswordDialog = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("settings_change_password_btn")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = WaGold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Change Password", color = WaGold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (matches("notification", "dnd", "sound", "vibrate", "alert")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Notifications,
                        title = "Notifications & Quiet Hours (DND)",
                        subtitle = if (localSettings.notifMaster) "Alerts ON • DND ${if (localSettings.dndEnabled) "10PM–7AM" else "OFF"}" else "Muted",
                        onClick = { showNotificationsDialog = true }
                    )
                }
            }

            if (matches("language", "translate", "pidgin", "yoruba", "hausa", "igbo", "french", "spanish")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Language,
                        title = "App Language (${localSettings.appLanguage})",
                        subtitle = "English, Pidgin, Yoruba, Hausa, Igbo, French, Spanish + 50 more",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            if (matches("storage", "data", "cache", "export", "backup")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Storage,
                        title = "Storage, Cache & Data Export",
                        subtitle = "Manage offline Room cache & download account summary",
                        onClick = { showStorageDialog = true }
                    )
                }
            }

            if (matches("event", "subscription", "vip", "favorite", "list")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Event,
                        title = "Events & Broadcasts (New)",
                        subtitle = "Create a live meetup or group event",
                        onClick = { showCreateEventDialog = true }
                    )
                }
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Star,
                        title = "Subscriptions (${myProfile?.vipTier ?: "FREE"} VIP)",
                        subtitle = "Manage GOLD / DIAMOND VIP perks",
                        onClick = { showSubscriptionsDialog = true }
                    )
                }
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Favorite,
                        title = "Favorites & Locked Lists",
                        subtitle = "${localSettings.favoriteChatIds.size} Favorite • ${localSettings.lockedChatIds.size} Locked chats",
                        onClick = { showFavoritesDialog = true }
                    )
                }
            }

            if (myProfile?.datingEnabled == true && matches("dating", "profile")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Favorite,
                        title = "Turn OFF Dating Profile",
                        subtitle = "Currently visible in Dating Discover",
                        onClick = { viewModel.disableDatingProfile() }
                    )
                }
            }

            if (matches("block", "privacy", "safety")) {
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Block,
                        title = "Blocked Users (${localSettings.blockedUserIds.size})",
                        subtitle = "Tap to inspect and unblock accounts",
                        onClick = { showBlockedUsersDialog = true }
                    )
                }
            }

            if (matches("help", "policy", "terms", "privacy", "update", "invite")) {
                item {
                    SettingsRowCard(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help, Privacy & Policy Center",
                        subtitle = "Privacy Policy, Terms, Anti-Fraud, Community Guidelines",
                        onClick = { showPoliciesDialog = true }
                    )
                }
                item {
                    SettingsRowCard(
                        icon = Icons.Default.Share,
                        title = "Invite Friends & Earn +100 Coins",
                        subtitle = "Share PGLOB APP globally",
                        onClick = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join me on PGLOB APP! Chat, join Ajo contributions, earn coins, use PGLOB Pay & connect worldwide. Add @${myProfile?.username ?: "me"}!")
                            }
                            context.startActivity(Intent.createChooser(share, "Invite Friends to PGLOB APP"))
                        }
                    )
                }
                item {
                    SettingsRowCard(
                        icon = Icons.Default.SystemUpdate,
                        title = "App Updates",
                        subtitle = "PGLOB APP v15 • Up to date",
                        onClick = { showAppUpdateDialog = true }
                    )
                }
            }

            if (matches("delete", "account", "logout", "sign out")) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = PglobRed)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account Permanently", color = PglobRed, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("logout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PglobRed, contentColor = PglobWhite),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PGLOB APP v15 • PGLOB Black Edition", color = WaTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }

    if (showLinkedDevicesDialog) {
        AlertDialog(
            onDismissRequest = { showLinkedDevicesDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Linked Devices", color = PglobGold, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    linkedDevices.forEach { dev ->
                        Surface(color = PglobBlack, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dev.deviceName, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${dev.location} • ${dev.lastActive}", color = if (dev.isCurrent) PglobGreen else PglobGreyText, fontSize = 12.sp)
                                }
                                if (!dev.isCurrent) {
                                    TextButton(onClick = { viewModel.logoutDevice(dev.id) }) {
                                        Text("Remove", color = PglobRed, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.logoutAllOtherDevices() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Out All Other Devices", color = PglobRed, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLinkedDevicesDialog = false }) { Text("Done", color = PglobGold) } }
        )
    }

    if (showPrivacySheet) {
        fun cycleAudience(curr: String): String = when (curr) {
            "Everyone" -> "My Contacts"
            "My Contacts" -> "Nobody"
            else -> "Everyone"
        }
        AlertDialog(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = PglobDarkGrey,
            title = { Text("Privacy & Security Settings", color = PglobGold, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Lock", color = PglobWhite, fontWeight = FontWeight.Bold)
                            Text("Require PIN / Fingerprint / Face to open", color = PglobGreyText, fontSize = 12.sp)
                        }
                        Switch(
                            checked = localSettings.appLockEnabled,
                            onCheckedChange = { viewModel.setAppLock(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(0 to "Now", 60 to "1m", 300 to "5m", 3600 to "1h").forEach { (sec, lbl) ->
                            val sel = localSettings.appLockTimeoutSec == sec
                            Surface(
                                color = if (sel) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable { viewModel.setAppLock(true, sec) }
                            ) {
                                Text(lbl, color = if (sel) PglobBlack else PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Hide Locked Chats Folder", color = PglobWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Switch(
                            checked = localSettings.hideLockedChats,
                            onCheckedChange = { viewModel.setHideLockedChats(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold)
                        )
                    }
                    Text("Disappearing Messages Timer:", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(0 to "OFF", 24 to "24h", 168 to "7d", 2160 to "90d").forEach { (hrs, lbl) ->
                            val sel = localSettings.disappearingHours == hrs
                            Surface(
                                color = if (sel) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable { viewModel.setDisappearingHours(hrs) }
                            ) {
                                Text(lbl, color = if (sel) PglobBlack else PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Read Receipts (Blue Ticks)", color = PglobWhite, fontSize = 13.sp)
                        Switch(checked = localSettings.readReceipts, onCheckedChange = { viewModel.updatePrivacy(readReceipts = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Show Online Status", color = PglobWhite, fontSize = 13.sp)
                        Switch(checked = localSettings.showOnlineStatus, onCheckedChange = { viewModel.updatePrivacy(showOnline = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Silence Unknown Callers", color = PglobWhite, fontSize = 13.sp)
                        Switch(checked = localSettings.silenceUnknownCallers, onCheckedChange = { viewModel.updatePrivacy(silenceUnknownCallers = it) })
                    }
                    listOf<Triple<String, String, () -> Unit>>(
                        Triple("Last Seen", localSettings.lastSeenPrivacy) { viewModel.updatePrivacy(lastSeen = cycleAudience(localSettings.lastSeenPrivacy)) },
                        Triple("Profile Photo", localSettings.profilePhotoPrivacy) { viewModel.updatePrivacy(profilePhoto = cycleAudience(localSettings.profilePhotoPrivacy)) },
                        Triple("About", localSettings.aboutPrivacy) { viewModel.updatePrivacy(about = cycleAudience(localSettings.aboutPrivacy)) },
                        Triple("Status", localSettings.statusPrivacy) { viewModel.updatePrivacy(status = cycleAudience(localSettings.statusPrivacy)) },
                        Triple("Groups", localSettings.groupsPrivacy) { viewModel.updatePrivacy(groups = cycleAudience(localSettings.groupsPrivacy)) },
                        Triple("Live Location", localSettings.liveLocationPrivacy) { viewModel.updatePrivacy(liveLocation = cycleAudience(localSettings.liveLocationPrivacy)) }
                    ).forEach { (lbl, value, toggle) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { toggle() }.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lbl, color = PglobWhite, fontSize = 13.sp)
                            Text(value, color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Text("Who Can See My Chatting Number:", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    listOf("Everyone", "My Contacts Only", "Approved People Only", "Nobody").forEach { mode ->
                        val selected = localSettings.chatNumberPrivacy == mode
                        Surface(
                            color = if (selected) WaGreen.copy(alpha = 0.18f) else PglobBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (selected) WaGreen else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateNumberPrivacy(chatNumberPrivacy = mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mode, color = PglobWhite, fontSize = 13.sp)
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (localSettings.chatNumberPrivacy == "Approved People Only") {
                        var newApproved by remember { mutableStateOf("") }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newApproved,
                                onValueChange = { newApproved = it },
                                label = { Text("Approved Phone / Contact") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    viewModel.addApprovedChatNumber(newApproved)
                                    newApproved = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                            ) { Text("Add") }
                        }
                        localSettings.approvedChatNumbers.forEach { num ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(num, color = PglobWhite, fontSize = 12.sp)
                                TextButton(onClick = { viewModel.removeApprovedChatNumber(num) }) {
                                    Text("Remove", color = PglobRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Text("Account Number Visibility:", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    listOf("Show Only When I Share", "Show to Everyone").forEach { vis ->
                        val selected = localSettings.accountNumberVisibility == vis
                        Surface(
                            color = if (selected) PglobGold.copy(alpha = 0.2f) else PglobBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (selected) PglobGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateNumberPrivacy(accountNumberVisibility = vis) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(vis, color = PglobWhite, fontSize = 13.sp)
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PglobGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacySheet = false }, colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showTwoStepDialog) {
        var pin6 by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTwoStepDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Two-Step Verification 🔐", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (localSettings.twoStepEnabled) "Two-Step Verification is ON." else "Enter a 6-digit PIN to protect new logins.",
                        color = PglobWhite,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = pin6,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin6 = it },
                        label = { Text("6-Digit 2FA PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setTwoStep(true, pin6)
                        showTwoStepDialog = false
                    },
                    enabled = pin6.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Enable 2FA") }
            },
            dismissButton = {
                if (localSettings.twoStepEnabled) {
                    TextButton(onClick = { viewModel.setTwoStep(false, ""); showTwoStepDialog = false }) {
                        Text("Turn OFF", color = PglobRed)
                    }
                } else {
                    TextButton(onClick = { showTwoStepDialog = false }) { Text("Cancel", color = PglobWhite) }
                }
            }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Notifications & DND", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("All Notifications", localSettings.notifMaster) { v: Boolean -> viewModel.updateNotificationPrefs(master = v) },
                        Triple("Direct Messages", localSettings.notifMessages) { v: Boolean -> viewModel.updateNotificationPrefs(messages = v) },
                        Triple("Group Messages", localSettings.notifGroups) { v: Boolean -> viewModel.updateNotificationPrefs(groups = v) },
                        Triple("PGLOB Pay & Requests", localSettings.notifPay) { v: Boolean -> viewModel.updateNotificationPrefs(pay = v) },
                        Triple("Security & PIN Alerts", localSettings.notifSecurity) { v: Boolean -> viewModel.updateNotificationPrefs(security = v) },
                        Triple("Show Message Preview", localSettings.notifPreview) { v: Boolean -> viewModel.updateNotificationPrefs(preview = v) },
                        Triple("Vibrate on Alert", localSettings.notifVibrate) { v: Boolean -> viewModel.updateNotificationPrefs(vibrate = v) },
                        Triple("DND Quiet Hours (10PM–7AM)", localSettings.dndEnabled) { v: Boolean -> viewModel.updateNotificationPrefs(dnd = v) }
                    ).forEach { (lbl, state, setter) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lbl, color = PglobWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Switch(checked = state, onCheckedChange = setter, colors = SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLanguageDialog) {
        var langQuery by remember { mutableStateOf("") }
        val allLangs = remember {
            (GlobalCatalogData.languages + listOf(
                "Pidgin", "Yoruba", "Hausa", "Igbo", "Swahili", "Amharic", "Zulu",
                "Turkish", "Italian", "Indonesian", "Russian", "Korean", "Japanese",
                "German", "Dutch", "Polish", "Thai", "Vietnamese", "Tagalog", "Malay",
                "Bengali", "Urdu", "Persian", "Ukrainian", "Romanian", "Greek"
            )).distinct()
        }
        val filteredLangs = remember(langQuery) {
            if (langQuery.isBlank()) allLangs else allLangs.filter { it.contains(langQuery, true) }
        }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("App Language 🌍", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = langQuery, onValueChange = { langQuery = it }, placeholder = { Text("Search language...") }, singleLine = true)
                    LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredLangs) { lang ->
                            val active = localSettings.appLanguage == lang
                            Surface(
                                color = if (active) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.setAppLanguage(lang)
                                    showLanguageDialog = false
                                }
                            ) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(lang, color = if (active) PglobBlack else PglobWhite, fontWeight = FontWeight.Bold)
                                    if (active) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PglobBlack, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Storage & Data", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Local Cache: Room Offline Message Queue & Encrypted Settings Preferences.", color = PglobWhite, fontSize = 13.sp)
                    Button(
                        onClick = { viewModel.clearLocalCache(); showStorageDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear Temporary Cache", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = {
                            val exportText = "PGLOB APP Account Data Summary\nName: ${myProfile?.fullName}\nUsername: @${myProfile?.username}\nCountry: ${myProfile?.country}\nCoins: ${myProfile?.coins}\nWallet: ${myProfile?.currency}${myProfile?.walletBalance}\nLanguage: ${localSettings.appLanguage}"
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, exportText)
                            }
                            context.startActivity(Intent.createChooser(share, "Export PGLOB APP Data"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Download / Export Data Summary", color = PglobGold) }
                }
            },
            confirmButton = { TextButton(onClick = { showStorageDialog = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Blocked Users", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                if (localSettings.blockedUserIds.isEmpty()) {
                    Text("No blocked accounts.", color = PglobGreyText)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        localSettings.blockedUserIds.forEach { uid ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(uid, color = PglobWhite, modifier = Modifier.weight(1f))
                                TextButton(onClick = { viewModel.toggleBlockUser(uid) }) {
                                    Text("Unblock", color = PglobGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showBlockedUsersDialog = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showPoliciesDialog) {
        PoliciesListDialog(onDismiss = { showPoliciesDialog = false })
    }

    if (showCreateEventDialog) {
        var evtTitle by remember { mutableStateOf("") }
        var evtDate by remember { mutableStateOf("Tonight 8:00 PM") }
        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Create Global Event 📅", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = evtTitle, onValueChange = { evtTitle = it }, label = { Text("Event Title") }, singleLine = true)
                    OutlinedTextField(value = evtDate, onValueChange = { evtDate = it }, label = { Text("Date & Time") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (evtTitle.isNotBlank()) {
                            viewModel.postStatus("📅 EVENT: $evtTitle ($evtDate)")
                            viewModel.addNotification("Event Created 📅", "$evtTitle scheduled for $evtDate", "STATUS")
                            showCreateEventDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Post Event") }
            },
            dismissButton = { TextButton(onClick = { showCreateEventDialog = false }) { Text("Cancel", color = PglobWhite) } }
        )
    }

    if (showSubscriptionsDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionsDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("PGLOB VIP Subscriptions", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Plan: ${myProfile?.vipTier ?: "FREE"} • Balance: ${myProfile?.coins ?: 0} Coins", color = PglobWhite)
                    Button(
                        onClick = { viewModel.upgradeVip("GOLD", 1000L); showSubscriptionsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Upgrade GOLD VIP (1,000 Coins)", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { viewModel.upgradeVip("DIAMOND", 5000L); showSubscriptionsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70E4FF), contentColor = PglobBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Upgrade DIAMOND VIP (5,000 Coins)", fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = { TextButton(onClick = { showSubscriptionsDialog = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showFavoritesDialog) {
        AlertDialog(
            onDismissRequest = { showFavoritesDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Favorites & Locked Lists", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Favorite chats: ${localSettings.favoriteChatIds.size}\nLocked chats: ${localSettings.lockedChatIds.size}\n\nLong-press any chat in the Chats list to toggle Favorite or Locked status.",
                    color = PglobWhite
                )
            },
            confirmButton = { TextButton(onClick = { showFavoritesDialog = false }) { Text("Got it", color = PglobGold) } }
        )
    }

    if (showAppUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showAppUpdateDialog = false },
            containerColor = WaCard,
            title = { Text("PGLOB APP Updates", color = WaGreen, fontWeight = FontWeight.Medium) },
            text = { Text("You are running PGLOB APP v15 (PGLOB Black Edition + Group Contributions Escrow + Admin Controls). All modules are up to date!", color = WaTextPrimary) },
            confirmButton = { TextButton(onClick = { showAppUpdateDialog = false }) { Text("OK", color = WaGreen) } }
        )
    }

    if (showDeleteAccountDialog) {
        var confirmWord by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Delete Account?", color = PglobRed, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This clears your device account session and signs you out. Type DELETE to confirm.", color = PglobWhite, fontSize = 13.sp)
                    OutlinedTextField(value = confirmWord, onValueChange = { confirmWord = it }, label = { Text("Type DELETE") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.onUserLoggedOutCleanup(forgetDeviceAccount = true)
                            try { credentialManager.clearCredentialState(ClearCredentialStateRequest()) } catch (_: Exception) {}
                            Firebase.auth.signOut()
                        }
                        showDeleteAccountDialog = false
                    },
                    enabled = confirmWord.trim().equals("DELETE", ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(containerColor = PglobRed, contentColor = PglobWhite)
                ) { Text("Confirm Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel", color = PglobWhite) } }
        )
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            containerColor = PglobDarkGrey,
            title = { Text("Profile Photo", color = PglobGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showPhotoOptions = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Choose from Gallery", color = PglobWhite) }
                    TextButton(onClick = {
                        showPhotoOptions = false
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            val u = createTempCameraUri(context)
                            tempCameraUri = u
                            cameraLauncher.launch(u)
                        } else {
                            camPermLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) { Text("Take Photo with Camera", color = PglobWhite) }
                    if (!myProfile?.photoUri.isNullOrBlank()) {
                        TextButton(onClick = { showPhotoOptions = false; showFullPhoto = true }) {
                            Text("View Full Screen", color = PglobGold)
                        }
                        TextButton(onClick = { showPhotoOptions = false; viewModel.updateProfilePhoto("") }) {
                            Text("Remove Photo", color = PglobRed)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPhotoOptions = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showFullPhoto && !myProfile?.photoUri.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showFullPhoto = false },
            containerColor = PglobBlack,
            title = { Text(myProfile?.fullName ?: "Profile Photo", color = PglobGold) },
            text = {
                PglobProfileImage(
                    name = myProfile?.fullName ?: "Profile Photo",
                    photoUri = myProfile!!.photoUri,
                    modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(16.dp)),
                    fallbackFontSizeSp = 48f
                )
            },
            confirmButton = { TextButton(onClick = { showFullPhoto = false }) { Text("Close", color = PglobGold) } }
        )
    }

    if (showEditProfile && myProfile != null) {
        var selectedCountry by remember {
            mutableStateOf(GlobalCatalogData.countries.firstOrNull { it.name == myProfile.country } ?: GlobalCatalogData.countries.first())
        }
        if (showCountryPicker) {
            CountryPickerSheet(onDismiss = { showCountryPicker = false }, onSelect = { selectedCountry = it })
        }
        UserProfileEditorSheet(
            initialName = myProfile.fullName,
            initialAbout = myProfile.about,
            initialBio = myProfile.bio.ifBlank { localSettings.localBio },
            initialInterests = myProfile.interests.ifEmpty { localSettings.localInterests },
            initialPhotoUri = myProfile.photoUri,
            initialCountryLabel = "${selectedCountry.flag} ${selectedCountry.name} (${selectedCountry.currency})",
            initialPhone = myProfile.phone,
            onDismiss = { showEditProfile = false },
            onPickPhotoGallery = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onPickPhotoCamera = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    val u = createTempCameraUri(context)
                    tempCameraUri = u
                    cameraLauncher.launch(u)
                } else {
                    camPermLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onRemovePhoto = { viewModel.updateProfilePhoto("") },
            onPickCountry = { showCountryPicker = true },
            onSave = { name, about, bio, interests, phone ->
                viewModel.saveUserProfileDetails(
                    fullName = name,
                    about = about,
                    bio = bio,
                    interests = interests,
                    photoUri = myProfile.photoUri,
                    phone = phone,
                    country = selectedCountry.name,
                    countryFlag = selectedCountry.flag,
                    currency = selectedCountry.currency
                )
                showEditProfile = false
            }
        )
    }

    if (showPublicProfilePreview && myProfile != null) {
        PublicUserProfileSheet(
            profile = PublicProfile(
                id = myProfile.userId,
                userId = myProfile.userId,
                displayName = myProfile.fullName.ifBlank { "PGLOB Member" },
                username = myProfile.username,
                usernameVerified = myProfile.usernameVerified,
                country = myProfile.country,
                countryFlag = myProfile.countryFlag,
                language = myProfile.language,
                about = myProfile.about,
                photoUri = myProfile.photoUri,
                coins = myProfile.coins,
                vipTier = myProfile.vipTier,
                isOnline = true,
                isPublic = true,
                bio = myProfile.bio.ifBlank { localSettings.localBio },
                interests = myProfile.interests.ifEmpty { localSettings.localInterests }
            ),
            isOwnProfile = true,
            onDismiss = { showPublicProfilePreview = false },
            onEditMyProfile = { showEditProfile = true }
        )
    }

    if (showChangeUsernameOtp && myProfile != null) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameOtp = false },
            containerColor = PglobDarkGrey,
            title = { Text("Verify @${myProfile.username}", color = PglobGold) },
            text = {
                OtpSixBoxVerification(
                    targetLabel = "@${myProfile.username}",
                    onVerified = {
                        viewModel.completeProfileSetup(
                            fullName = myProfile.fullName,
                            username = myProfile.username,
                            usernameVerified = true,
                            phone = myProfile.phone,
                            email = myProfile.email,
                            country = myProfile.country,
                            countryFlag = myProfile.countryFlag,
                            currency = myProfile.currency,
                            language = myProfile.language,
                            about = myProfile.about,
                            photoUri = myProfile.photoUri,
                            isInitialSignup = false
                        )
                        showChangeUsernameOtp = false
                    }
                )
            },
            confirmButton = {}
        )
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Transaction PIN & Recovery", color = PglobGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!showRecoveryOtp) {
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) newPin = it },
                            label = { Text("New 4-Digit Transaction PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        TextButton(onClick = { showRecoveryOtp = true }) {
                            Text("Forgot PIN? Verify via OTP", color = PglobGold)
                        }
                    } else {
                        OtpSixBoxVerification(
                            targetLabel = myProfile?.email?.ifBlank { "Your Account" } ?: "Your Account",
                            onVerified = { showRecoveryOtp = false }
                        )
                    }
                }
            },
            confirmButton = {
                if (!showRecoveryOtp) {
                    Button(
                        onClick = {
                            if (newPin.length == 4) {
                                viewModel.setTransactionPin(newPin)
                                showPinDialog = false
                            }
                        },
                        enabled = newPin.length == 4,
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) { Text("Save PIN") }
                }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("Close", color = PglobWhite) } }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = WaCard,
            title = { Text("Logout from PGLOB?", color = WaTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your chats, wallet, and account stay saved safely. You can sign back in anytime with your Phone Number & Password.",
                    color = WaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        scope.launch {
                            viewModel.logoutExplicitly(forgetDeviceAccount = false)
                            try {
                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            } catch (_: Exception) {
                            }
                            Firebase.auth.signOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobRed, contentColor = PglobWhite),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Confirm Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Stay Signed In", color = WaGreen)
                }
            }
        )
    }

    if (showEditEmailDialog) {
        var emailInput by remember { mutableStateOf(effectiveEmail) }
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            containerColor = WaCard,
            title = { Text("Recovery Email (Optional)", color = WaGreen, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add or update an optional email for account recovery and notifications.", color = WaTextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateEmailOptional(emailInput)
                        showEditEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) { Text("Save Email", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditEmailDialog = false }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    if (showChangePasswordDialog) {
        var currPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confPass by remember { mutableStateOf("") }
        var showPass by remember { mutableStateOf(false) }
        val strength = SecurityAndNumberCodec.validatePassword(newPass)
        val canSave = SecurityAndNumberCodec.isPasswordAcceptable(newPass) && newPass == confPass &&
            (localSettings.passwordHash.isBlank() || currPass.isNotBlank())

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = WaCard,
            title = { Text("Change Login Password", color = WaGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (localSettings.passwordHash.isNotBlank()) {
                        OutlinedTextField(
                            value = currPass,
                            onValueChange = { currPass = it },
                            label = { Text("Current Password") },
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (Min 8 chars)") },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle visibility", tint = WaTextSecondary)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Strength: ${strength.label}", color = if (SecurityAndNumberCodec.isPasswordAcceptable(newPass)) WaGreen else WaGold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = confPass,
                        onValueChange = { confPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.changePhonePassword(currPass, newPass)) {
                            showChangePasswordDialog = false
                        }
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) { Text("Update Password", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    if (showAccountQrDialog) {
        AlertDialog(
            onDismissRequest = { showAccountQrDialog = false },
            containerColor = WaCard,
            title = { Text("PGLOB Account QR", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(myProfile?.fullName ?: localSettings.savedDisplayName.ifBlank { "PGLOB Member" }, color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                        Canvas(modifier = Modifier.size(160.dp).padding(14.dp)) {
                            val cells = 11
                            val cellSize = size.width / cells
                            val seedStr = accountNumber
                            for (r in 0 until cells) {
                                for (c in 0 until cells) {
                                    val isFinder = (r < 3 && c < 3) || (r < 3 && c >= cells - 3) || (r >= cells - 3 && c < 3)
                                    val hashBit = ((seedStr[(r * cells + c) % seedStr.length].code + r * 7 + c * 13) % 3) != 0
                                    if (isFinder || hashBit) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize * 0.9f, cellSize * 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(accountNumber, color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Bank: PGLOB Wallet • Use ONLY for receiving money", color = WaTextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("PGLOB Account", accountNumber))
                        viewModel.showToast("Account number $accountNumber copied!")
                        showAccountQrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Copy Account", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAccountQrDialog = false }) { Text("Close", color = WaTextPrimary) }
            }
        )
    }

}

@Composable
private fun SettingsRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = WaCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = WaGreen)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = WaTextPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, color = WaTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

