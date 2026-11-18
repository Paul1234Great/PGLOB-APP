package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.SolidColor
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.LocalAppSettings
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.GlobalCatalogData
import com.example.data.model.PublicProfile
import com.example.data.model.StatusNote
import com.example.data.model.UserPrivateProfile
import com.example.ui.AiUiState
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.PglobAiAssistantSheet
import com.example.ui.components.PinOrBiometricConfirmSheet
import com.example.ui.components.PglobChatBackground
import com.example.ui.components.createTempCameraUri
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobBlueTick
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaBlueTick
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaMyBubble
import com.example.ui.theme.WaOtherBubble
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.ContributionCodec
import com.example.util.CountryPayCatalog
import com.example.util.PglobStrings
import com.example.util.StatusQuoteCodec
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val HOME_TOP_TABS = listOf(
    "Chats", "Earn", "Live", "Connect", "Market",
    "Reels", "Games", "Learn", "Jobs", "Pay", "Dating"
)

@Composable
fun ChatsTopHeaderAndTabs(
    activeTopTab: String,
    searchQuery: String,
    appLanguage: String = "English",
    unreadNotifCount: Int = 0,
    activeChatFilter: String = "All",
    lockedCount: Int = 0,
    hideLockedChats: Boolean = false,
    onSelectChatFilter: (String) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenLockedFolder: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onAskAiClick: () -> Unit,
    onSelectTopTab: (String) -> Unit,
    onCameraClick: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onNewGroupClick: () -> Unit,
    onNewContributionClick: () -> Unit = {},
    onArchivedClick: () -> Unit,
    showArchivedRow: Boolean = true,
    archivedCount: Int = 0
) {
    var showMenu by remember { mutableStateOf(false) }
    var revealHiddenLocked by remember { mutableStateOf(false) }
    val str = remember(appLanguage) { PglobStrings.forLanguage(appLanguage) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WaBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.example.ui.components.PglobBrandLogo(size = 32.dp)
                Text(
                    text = str.appTitle,
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 29.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("notifications_bell")) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = WaTextPrimary, modifier = Modifier.size(24.dp))
                    }
                    if (unreadNotifCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .clip(CircleShape)
                                .background(WaGreen)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (unreadNotifCount > 9) "9+" else unreadNotifCount.toString(),
                                color = WaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                IconButton(onClick = onCameraClick) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = WaTextPrimary, modifier = Modifier.size(24.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = WaTextPrimary, modifier = Modifier.size(24.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(WaCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Group", color = WaTextPrimary) },
                            onClick = { showMenu = false; onNewGroupClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("New Contribution (Ajo)", color = WaGreen) },
                            onClick = { showMenu = false; onNewContributionClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("${str.lockedChats} ($lockedCount)", color = WaTextPrimary) },
                            onClick = { showMenu = false; onOpenLockedFolder() }
                        )
                        DropdownMenuItem(
                            text = { Text("Chat Wallpaper", color = WaTextPrimary) },
                            onClick = { showMenu = false; onOpenWallpaperPicker() }
                        )
                        DropdownMenuItem(
                            text = { Text(str.settings, color = WaTextPrimary) },
                            onClick = { showMenu = false; onOpenProfileSettings() }
                        )
                    }
                }
            }
        }

        // Rounded Search Pill (48.dp, #202C33)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(WaInput)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = WaTextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                singleLine = true,
                cursorBrush = SolidColor(WaGreen),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = WaTextPrimary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text(str.searchPlaceholder, color = WaTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    inner()
                }
            )
            Surface(
                color = Color(0xFF0E2B25),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onAskAiClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI", tint = WaGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        if (activeTopTab == "Chats") {
            // Filter Pills: All | Unread | Favorites | Groups | Locked
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Unread", "Favorites", "Groups", "Locked").forEach { filter ->
                    val selected = activeChatFilter == filter
                    Surface(
                        color = if (selected) WaGreen else WaInput,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .clickable {
                                if (filter == "Locked") onOpenLockedFolder() else onSelectChatFilter(filter)
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (filter == "Locked") "🔒 $filter" else filter,
                                color = if (selected) WaBackground else WaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (lockedCount > 0 && (!hideLockedChats || revealHiddenLocked)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLockedFolder() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked Chats", tint = WaGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(str.lockedChats, color = WaTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("$lockedCount", color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            } else if (lockedCount > 0 && hideLockedChats) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { revealHiddenLocked = true }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tap to reveal hidden Locked Chats", color = WaTextSecondary, fontSize = 12.sp)
                }
            }
        }

        if (showArchivedRow && activeTopTab == "Chats") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArchivedClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Archive, contentDescription = "Archived", tint = WaTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(str.archived, color = WaTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (archivedCount > 0) {
                    Text(archivedCount.toString(), color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Scrollable Top Super-App Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HOME_TOP_TABS.forEach { tab ->
                val isSelected = tab == activeTopTab
                Surface(
                    color = if (isSelected) WaGreen else WaInput,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .clickable { onSelectTopTab(tab) }
                        .testTag("top_tab_${tab.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) WaBackground else WaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = WaDivider, thickness = 1.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListTabContent(
    chats: List<ChatThread>,
    currentUserId: String,
    searchQuery: String,
    showArchivedOnly: Boolean,
    chatFilter: String = "All",
    lockedChatIds: Set<String> = emptySet(),
    favoriteChatIds: Set<String> = emptySet(),
    statusNotes: List<StatusNote> = emptyList(),
    myName: String = "Me",
    myPhotoUri: String = "",
    myActiveStatusNote: StatusNote? = null,
    myExpiredStatusNote: StatusNote? = null,
    onAddOrReplaceNote: () -> Unit = {},
    onOpenNote: (StatusNote) -> Unit = {},
    onBackFromArchived: () -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    onTogglePin: (ChatThread) -> Unit,
    onToggleArchive: (ChatThread) -> Unit,
    onToggleMute: (ChatThread) -> Unit,
    onToggleLockChat: (ChatThread) -> Unit = {},
    onToggleFavoriteChat: (ChatThread) -> Unit = {},
    onDeleteChat: (ChatThread) -> Unit,
    onNewChatClick: () -> Unit
) {
    val visibleChats = remember(chats, searchQuery, showArchivedOnly, chatFilter, lockedChatIds, favoriteChatIds) {
        chats.filter { it.isArchived == showArchivedOnly }
            .filter { c ->
                when (chatFilter) {
                    "Locked" -> lockedChatIds.contains(c.id)
                    "Unread" -> !lockedChatIds.contains(c.id) && c.unreadCount > 0
                    "Favorites" -> !lockedChatIds.contains(c.id) && favoriteChatIds.contains(c.id)
                    "Groups" -> !lockedChatIds.contains(c.id) && c.isGroup
                    else -> !lockedChatIds.contains(c.id)
                }
            }
            .filter { searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.lastMessage.contains(searchQuery, true) }
    }
    var actionTargetChat by remember { mutableStateOf<ChatThread?>(null) }

    if (showArchivedOnly) {
        BackHandler { onBackFromArchived() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showArchivedOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackFromArchived) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                    }
                    Text("Archived Chats", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                }
            }

            if (visibleChats.isEmpty()) {
                if (!showArchivedOnly) {
                    StatusNotesHorizontalStrip(
                        notes = statusNotes,
                        currentUserId = currentUserId,
                        myName = myName,
                        myPhotoUri = myPhotoUri,
                        myActiveNote = myActiveStatusNote,
                        myExpiredNote = myExpiredStatusNote,
                        onAddOrReplaceNote = onAddOrReplaceNote,
                        onOpenNote = onOpenNote
                    )
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            when {
                                showArchivedOnly -> "No archived chats"
                                chatFilter == "Locked" -> "No locked chats yet"
                                chatFilter == "Favorites" -> "Long-press a chat to add to Favorites ⭐"
                                else -> "Start a Global Conversation"
                            },
                            color = WaTextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Tap the + button to start a New Chat, Group or Contribution", color = WaTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    if (!showArchivedOnly) {
                        item(key = "status_notes_strip_header") {
                            StatusNotesHorizontalStrip(
                                notes = statusNotes,
                                currentUserId = currentUserId,
                                myName = myName,
                                myPhotoUri = myPhotoUri,
                                myActiveNote = myActiveStatusNote,
                                myExpiredNote = myExpiredStatusNote,
                                onAddOrReplaceNote = onAddOrReplaceNote,
                                onOpenNote = onOpenNote
                            )
                        }
                    }
                    items(visibleChats, key = { it.id }) { chat ->
                        val isLocked = lockedChatIds.contains(chat.id)
                        val isFav = favoriteChatIds.contains(chat.id)
                        val timeText = remember(chat.updatedAt, chat.createdAt) {
                            val date = (chat.updatedAt ?: chat.createdAt)?.toDate()
                            if (date != null) SimpleDateFormat("h:mm a", Locale.getDefault()).format(date) else "Now"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .combinedClickable(
                                    onClick = { onOpenChat(chat) },
                                    onLongClick = { actionTargetChat = chat }
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(
                                name = chat.title,
                                size = 48.dp,
                                showOnlineDot = chat.peerOnline && !chat.isGroup,
                                hasStatusRing = !chat.isGroup
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = chat.title,
                                        color = WaTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (chat.isPinned) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = WaTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                    if (isFav) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.Star, contentDescription = "Favorite", tint = WaGold, modifier = Modifier.size(14.dp))
                                    }
                                    if (isLocked) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = WaGreen, modifier = Modifier.size(14.dp))
                                    }
                                    if (chat.isMuted) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Muted", tint = WaTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (chat.lastSenderId == currentUserId) {
                                        Icon(
                                            Icons.Default.DoneAll,
                                            contentDescription = "Delivered",
                                            tint = WaBlueTick,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = chat.lastMessage,
                                        color = WaTextSecondary,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = timeText,
                                    color = if (chat.unreadCount > 0) WaGreen else WaTextSecondary,
                                    fontSize = 12.sp
                                )
                                if (chat.unreadCount > 0) {
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(width = 24.dp, height = 20.dp)
                                            .clip(CircleShape)
                                            .background(if (chat.isMuted) WaTextSecondary else WaGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                            color = WaBackground,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = WaDivider, thickness = 1.dp, modifier = Modifier.padding(start = 76.dp))
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WaGreen, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Your personal messages are end-to-end encrypted",
                                color = WaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewChatClick,
            containerColor = WaGreen,
            contentColor = WaTextPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .testTag("new_chat_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Chat", tint = WaTextPrimary)
        }
    }

    actionTargetChat?.let { target ->
        val isLocked = lockedChatIds.contains(target.id)
        val isFav = favoriteChatIds.contains(target.id)
        AlertDialog(
            onDismissRequest = { actionTargetChat = null },
            containerColor = WaCard,
            title = { Text(target.title, color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = {
                Column {
                    TextButton(onClick = { onTogglePin(target); actionTargetChat = null }) {
                        Text(if (target.isPinned) "Unpin Chat" else "Pin Chat", color = WaTextPrimary)
                    }
                    TextButton(onClick = { onToggleFavoriteChat(target); actionTargetChat = null }) {
                        Text(if (isFav) "Remove from Favorites" else "Add to Favorites ⭐", color = WaGreen)
                    }
                    TextButton(onClick = { onToggleLockChat(target); actionTargetChat = null }) {
                        Text(if (isLocked) "Unlock Chat" else "Lock Chat 🔒", color = WaGreen)
                    }
                    TextButton(onClick = { onToggleMute(target); actionTargetChat = null }) {
                        Text(if (target.isMuted) "Unmute Notifications" else "Mute Notifications", color = WaTextPrimary)
                    }
                    TextButton(onClick = { onToggleArchive(target); actionTargetChat = null }) {
                        Text(if (target.isArchived) "Unarchive Chat" else "Archive Chat", color = WaTextPrimary)
                    }
                    TextButton(onClick = { onDeleteChat(target); actionTargetChat = null }) {
                        Text("Delete Chat", color = PglobRed)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { actionTargetChat = null }) { Text("Close", color = WaGreen) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ChatDetailScreen(
    chat: ChatThread,
    viewModel: PglobViewModel,
    myProfile: UserPrivateProfile?,
    localSettings: LocalAppSettings = LocalAppSettings(),
    wallpaperId: String,
    customWallpaperUri: String,
    isOnline: Boolean,
    aiState: AiUiState,
    onBack: () -> Unit,
    onOpenWallpaperSheet: () -> Unit,
    onOpenStatusById: (String) -> Unit = {},
    onOpenContribution: (String) -> Unit = {},
    onCreateContributionForChat: (ChatThread) -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val messages by viewModel.observeMessagesWithPending(chat.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val allChats by viewModel.chats.collectAsStateWithLifecycle()
    val publicProfiles by viewModel.publicProfiles.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var replyToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showGroupInfoSheet by remember { mutableStateOf(false) }
    var inspectedPeerProfile by remember { mutableStateOf<PublicProfile?>(null) }
    var forwardTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showSendMoneySheet by remember { mutableStateOf(false) }
    var showPinConfirmForMoney by remember { mutableStateOf<Double?>(null) }
    var showAiSheet by remember { mutableStateOf(false) }
    var aiInitialPrompt by remember { mutableStateOf("") }
    var showTranslateSheet by remember { mutableStateOf(false) }
    var targetTranslateLang by remember { mutableStateOf(localSettings.appLanguage.ifBlank { "English" }) }
    var selectedMessageForActions by remember { mutableStateOf<ChatMessage?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }
    var showEmojiStrip by remember { mutableStateOf(false) }
    var inCallMode by remember { mutableStateOf<String?>(null) } // "AUDIO" or "VIDEO"
    var pendingAttachSheet by remember { mutableStateOf(false) }

    // Media / Voice recording state
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var previewMediaUri by remember { mutableStateOf<String?>(null) }
    var previewCaption by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var activeRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var activeAudioFile by remember { mutableStateOf<File?>(null) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var playingMsgId by remember { mutableStateOf<String?>(null) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try { activeRecorder?.release() } catch (_: Exception) {}
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordSeconds = 0
            while (isRecordingVoice) {
                delay(1000L)
                recordSeconds++
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            previewMediaUri = uri.toString()
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.sendChatMessage(
                chatId = chat.id,
                memberUids = chat.memberUids,
                text = "📄 Document attached",
                messageType = "FILE",
                mediaUri = uri.toString()
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok && tempPhotoUri != null) {
            previewMediaUri = tempPhotoUri.toString()
        }
    }

    var permissionDeniedNotice by remember { mutableStateOf<String?>(null) }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempCameraUri(context)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionDeniedNotice = "PGLOB APP needs Camera permission. Allow in Settings to take photos."
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val outFile = File.createTempFile("pglob_voice_", ".m4a", context.cacheDir)
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(outFile.absolutePath)
                recorder.prepare()
                recorder.start()
                activeRecorder = recorder
                activeAudioFile = outFile
                isRecordingVoice = true
            } catch (e: Exception) {
                viewModel.showToast("Mic start failed: ${e.localizedMessage}")
            }
        } else {
            permissionDeniedNotice = "PGLOB APP needs Microphone permission for Voice Notes. Allow in Settings."
        }
    }

    PglobChatBackground(
        wallpaperId = wallpaperId,
        customUri = customWallpaperUri
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // PGLOB Chat Header (Name clickable for Group Info + Ajo Contribution + Group Admins)
            Surface(color = WaCard, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showGroupInfoSheet = true }
                            .testTag("chat_header_info_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chat.isGroup) {
                            com.example.ui.components.PglobBrandLogo(size = 32.dp)
                        } else {
                            AvatarCircle(name = chat.title, size = 40.dp, showOnlineDot = chat.peerOnline, hasStatusRing = true)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.title,
                                color = WaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                lineHeight = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val disLabel = when (localSettings.disappearingHours) {
                                24 -> " • 24h"
                                168 -> " • 7d"
                                2160 -> " • 90d"
                                else -> ""
                            }
                            Text(
                                text = if (isOnline) "Online$disLabel • tap for info" else "Waiting for network...",
                                color = if (isOnline) WaTextSecondary else WaGold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = { inCallMode = "VIDEO" }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = WaTextPrimary)
                    }
                    IconButton(onClick = { inCallMode = "AUDIO" }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = WaTextPrimary)
                    }
                    Box {
                        IconButton(onClick = { showChatMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Chat Options", tint = WaTextPrimary)
                        }
                        DropdownMenu(
                            expanded = showChatMenu,
                            onDismissRequest = { showChatMenu = false },
                            modifier = Modifier.background(WaCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Group / Contact Info", color = WaTextPrimary) },
                                onClick = { showChatMenu = false; showGroupInfoSheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Group Contribution (Ajo)", color = WaGreen) },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = WaGreen) },
                                onClick = {
                                    showChatMenu = false
                                    val linked = contributions.firstOrNull { it.linkedChatId == chat.id }
                                    if (linked != null) onOpenContribution(linked.id)
                                    else onCreateContributionForChat(chat)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Chat Wallpaper", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = null, tint = WaTextPrimary) },
                                onClick = { showChatMenu = false; onOpenWallpaperSheet() }
                            )
                            DropdownMenuItem(
                                text = { Text("Translate Chat 🌐", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, tint = WaTextPrimary) },
                                onClick = { showChatMenu = false; showTranslateSheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text("PGLOB AI Assistant ✨", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WaGreen) },
                                onClick = {
                                    showChatMenu = false
                                    val lastHistory = messages.takeLast(6).joinToString("\n") { "${it.senderName}: ${it.text}" }
                                    aiInitialPrompt = if (lastHistory.isNotBlank()) "Summarize or translate this chat:\n$lastHistory" else ""
                                    showAiSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Send Money 💳", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = WaGreen) },
                                onClick = { showChatMenu = false; showSendMoneySheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Send Gift 🎁", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = WaGold) },
                                onClick = { showChatMenu = false; showGiftSheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text(if (localSettings.lockedChatIds.contains(chat.id)) "Unlock Chat" else "Lock Chat 🔒", color = WaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WaTextPrimary) },
                                onClick = { showChatMenu = false; viewModel.toggleLockedChat(chat.id) }
                            )
                            DropdownMenuItem(
                                text = { Text("Report Chat", color = PglobRed) },
                                onClick = {
                                    showChatMenu = false
                                    viewModel.submitReport("CHAT", chat.id, "Reported from chat menu")
                                }
                            )
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            color = PglobBlack.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Today • 🔒 End-to-End Encrypted",
                                color = PglobGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == viewModel.currentUserId
                    val timeStr = remember(msg.createdAt) {
                        val d = msg.createdAt?.toDate()
                        if (d != null) SimpleDateFormat("h:mm a", Locale.getDefault()).format(d) else "Now"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 290.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .background(
                                    when {
                                        msg.messageType == "MONEY" || msg.messageType == "GIFT" -> Color(0xFF2D2300)
                                        isMe -> Color(0xFF005C4B)
                                        else -> Color(0xFF202C33)
                                    }
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (msg.messageType == "IMAGE" && msg.mediaUri.isNotBlank()) {
                                            fullScreenImageUri = msg.mediaUri
                                        }
                                    },
                                    onLongClick = { selectedMessageForActions = msg }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (msg.replyToText.isNotBlank()) {
                                val parsedContribution = remember(msg.replyToText) { ContributionCodec.decodeOrNull(msg.replyToText) }
                                val parsedStatus = remember(msg.replyToText) { StatusQuoteCodec.decodeOrNull(msg.replyToText) }
                                if (parsedContribution != null) {
                                    Surface(
                                        color = WaCard,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                            .border(1.dp, WaGreen.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                            .clickable { onOpenContribution(parsedContribution.groupId) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                color = WaGreen.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = parsedContribution.badge,
                                                    color = WaGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = parsedContribution.title,
                                                color = WaTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = parsedContribution.subtitle,
                                                color = WaTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                } else if (parsedStatus != null) {
                                    Surface(
                                        color = WaBackground,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                            .border(1.dp, WaGreen.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                            .clickable { onOpenStatusById(parsedStatus.statusId) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(WaCard),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (parsedStatus.mediaUri.isNotBlank() && parsedStatus.mediaType == "IMAGE") {
                                                    AsyncImage(
                                                        model = Uri.parse(parsedStatus.mediaUri),
                                                        contentDescription = "Status thumbnail",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else if (parsedStatus.mediaType == "VIDEO") {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Video status", tint = WaGreen, modifier = Modifier.size(26.dp))
                                                } else {
                                                    Text("Aa", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (parsedStatus.isForward) "Forwarded from ${parsedStatus.ownerName} Status"
                                                    else "Replied to Status • ${parsedStatus.ownerName}",
                                                    color = WaGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = parsedStatus.caption.ifBlank { parsedStatus.mediaType },
                                                    color = WaTextPrimary,
                                                    fontSize = 13.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = "↩ ${msg.replyToText}",
                                            color = WaTextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }

                            if (msg.mediaUri.isNotBlank() && msg.messageType == "IMAGE") {
                                AsyncImage(
                                    model = Uri.parse(msg.mediaUri),
                                    contentDescription = "Chat photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            if (msg.messageType == "VOICE" && msg.mediaUri.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (playingMsgId == msg.id) {
                                                mediaPlayer.stop()
                                                playingMsgId = null
                                            } else {
                                                try {
                                                    mediaPlayer.reset()
                                                    mediaPlayer.setDataSource(msg.mediaUri)
                                                    mediaPlayer.prepare()
                                                    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(playbackSpeed)
                                                    mediaPlayer.setOnCompletionListener { playingMsgId = null }
                                                    mediaPlayer.start()
                                                    playingMsgId = msg.id
                                                } catch (_: Exception) {
                                                    viewModel.showToast("Voice note ready")
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (playingMsgId == msg.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play voice",
                                            tint = if (isMe) PglobBlack else PglobGold
                                        )
                                    }
                                    Text(
                                        text = "Voice Note ${msg.text}",
                                        color = if (isMe) PglobBlack else PglobWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    TextButton(onClick = {
                                        playbackSpeed = if (playbackSpeed == 1f) 2f else 1f
                                    }) {
                                        Text("${playbackSpeed.toInt()}x", color = if (isMe) PglobBlack else PglobGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                val textColor = when {
                                    msg.messageType == "MONEY" || msg.messageType == "GIFT" -> PglobGold
                                    else -> PglobWhite
                                }
                                Text(
                                    text = msg.text,
                                    color = textColor,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = if (msg.messageType in listOf("MONEY", "GIFT")) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            Row(
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (localSettings.starredMessageIds.contains(msg.id)) {
                                    Icon(Icons.Default.Star, contentDescription = "Starred", tint = WaGold, modifier = Modifier.size(12.dp))
                                }
                                if (msg.reaction.isNotBlank()) {
                                    Text(msg.reaction, fontSize = 13.sp)
                                }
                                Text(
                                    text = if (msg.isPendingOffline) "Waiting for network..." else timeStr,
                                    color = WaTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                if (isMe) {
                                    Icon(
                                        imageVector = if (msg.isPendingOffline) Icons.Default.Schedule else Icons.Default.DoneAll,
                                        contentDescription = "Status",
                                        tint = if (msg.isPendingOffline) WaTextSecondary else WaBlueTick,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(6.dp)) }
            }

            // Quick Action Chips inside chat: Send Money, Gift, PGLOB AI, Translate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PglobBlack.copy(alpha = 0.65f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { showSendMoneySheet = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Send Money", color = PglobGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { showGiftSheet = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Gift", color = PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { showAiSheet = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("PGLOB AI", color = PglobGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { showTranslateSheet = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = PglobGold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Translate", color = PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (replyToMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PglobDarkGrey)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Replying to: ${replyToMessage!!.text}",
                        color = PglobGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )
                    IconButton(onClick = { replyToMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = PglobWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (showEmojiStrip) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PglobDarkGrey)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("❤️", "😂", "🔥", "🌍", "👑", "💎", "💰", "🙏", "🎉", "😍").forEach { em ->
                        Text(
                            em,
                            fontSize = 24.sp,
                            modifier = Modifier.clickable { inputText += em }
                        )
                    }
                }
            }

            // PGLOB Bottom Input Bar (52dp #2A3942 pill + #00A884 circle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackground)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(WaInput)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PglobRed))
                        Spacer(Modifier.width(8.dp))
                        Text("Recording ${recordSeconds}s...", color = WaTextPrimary, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            try { activeRecorder?.stop(); activeRecorder?.release() } catch (_: Exception) {}
                            activeRecorder = null
                            isRecordingVoice = false
                        }) {
                            Text("Cancel", color = PglobRed)
                        }
                    }
                    Surface(
                        color = WaGreen,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp).clickable {
                            try { activeRecorder?.stop(); activeRecorder?.release() } catch (_: Exception) {}
                            activeRecorder = null
                            isRecordingVoice = false
                            activeAudioFile?.let { file ->
                                viewModel.sendChatMessage(
                                    chatId = chat.id,
                                    memberUids = chat.memberUids,
                                    text = "(${recordSeconds}s)",
                                    messageType = "VOICE",
                                    mediaUri = file.absolutePath
                                )
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Stop, contentDescription = "Send Voice Note", tint = WaBackground)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(WaInput)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showEmojiStrip = !showEmojiStrip }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji", tint = WaTextSecondary)
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            cursorBrush = SolidColor(WaGreen),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = WaTextPrimary),
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                            decorationBox = { inner ->
                                if (inputText.isEmpty()) Text("Message", color = WaTextSecondary, fontSize = 15.sp)
                                inner()
                            }
                        )
                        IconButton(onClick = { pendingAttachSheet = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = WaTextSecondary)
                        }
                        IconButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    val uri = createTempCameraUri(context)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = WaTextSecondary)
                        }
                    }

                    if (inputText.isBlank()) {
                        Surface(
                            color = WaGreen,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp).clickable {
                                micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = WaBackground)
                            }
                        }
                    } else {
                        Surface(
                            color = WaGreen,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp).clickable {
                                viewModel.sendChatMessage(
                                    chatId = chat.id,
                                    memberUids = chat.memberUids,
                                    text = inputText.trim(),
                                    replyToText = replyToMessage?.text.orEmpty()
                                )
                                inputText = ""
                                replyToMessage = null
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = WaBackground)
                            }
                        }
                    }
                }
            }
        }

        // In-App Audio / Video Call Overlay
        if (inCallMode != null) {
            CallInProgressOverlay(
                peerName = chat.title,
                isVideo = inCallMode == "VIDEO",
                onEndCall = { inCallMode = null }
            )
        }
    }

    // Preview Photo before sending
    if (previewMediaUri != null) {
        AlertDialog(
            onDismissRequest = { previewMediaUri = null },
            containerColor = PglobDarkGrey,
            title = { Text("Send Media", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(
                        model = Uri.parse(previewMediaUri),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    OutlinedTextField(
                        value = previewCaption,
                        onValueChange = { previewCaption = it },
                        label = { Text("Add caption...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendChatMessage(
                            chatId = chat.id,
                            memberUids = chat.memberUids,
                            text = previewCaption.ifBlank { "📷 Photo" },
                            messageType = "IMAGE",
                            mediaUri = previewMediaUri!!
                        )
                        previewMediaUri = null
                        previewCaption = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { previewMediaUri = null }) { Text("Cancel", color = PglobWhite) }
            }
        )
    }

    if (fullScreenImageUri != null) {
        AlertDialog(
            onDismissRequest = { fullScreenImageUri = null },
            containerColor = PglobBlack,
            text = {
                AsyncImage(
                    model = Uri.parse(fullScreenImageUri),
                    contentDescription = "Full photo",
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { fullScreenImageUri = null }) { Text("Close", color = PglobGold) }
            }
        )
    }

    // Message Long-Press Actions
    selectedMessageForActions?.let { msg ->
        val isStarred = localSettings.starredMessageIds.contains(msg.id)
        AlertDialog(
            onDismissRequest = { selectedMessageForActions = null },
            containerColor = WaCard,
            title = { Text("Message Actions", color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        listOf("❤️", "😂", "🔥", "👍", "👑").forEach { r ->
                            Text(r, fontSize = 26.sp, modifier = Modifier.clickable {
                                viewModel.reactToMessage(chat.id, msg.id, r)
                                selectedMessageForActions = null
                            })
                        }
                    }
                    TextButton(onClick = { replyToMessage = msg; selectedMessageForActions = null }) {
                        Text("Reply", color = WaTextPrimary)
                    }
                    TextButton(onClick = {
                        viewModel.toggleStarMessage(msg.id)
                        selectedMessageForActions = null
                    }) {
                        Text(if (isStarred) "Unstar Message" else "Star Message ⭐", color = WaGold)
                    }
                    TextButton(onClick = {
                        forwardTargetMessage = msg
                        selectedMessageForActions = null
                    }) {
                        Text("Forward Message ↪", color = WaGreen)
                    }
                    TextButton(onClick = {
                        aiInitialPrompt = "Translate this message to English, Spanish, and French: \"${msg.text}\""
                        selectedMessageForActions = null
                        showAiSheet = true
                    }) {
                        Text("Translate with PGLOB AI ✨", color = WaGreen)
                    }
                    if (msg.senderId == viewModel.currentUserId) {
                        TextButton(onClick = { viewModel.deleteMessage(chat.id, msg.id); selectedMessageForActions = null }) {
                            Text("Delete Message", color = PglobRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForActions = null }) { Text("Close", color = WaGreen) }
            }
        )
    }

    // Forward Message Picker
    forwardTargetMessage?.let { fwdMsg ->
        ModalBottomSheet(onDismissRequest = { forwardTargetMessage = null }, containerColor = WaCard) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Forward message to...", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                Text("\"${fwdMsg.text}\"", color = WaTextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(allChats, key = { it.id }) { targetChat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.sendChatMessage(
                                        chatId = targetChat.id,
                                        memberUids = targetChat.memberUids,
                                        text = "↪ Forwarded: ${fwdMsg.text}",
                                        messageType = fwdMsg.messageType,
                                        mediaUri = fwdMsg.mediaUri
                                    )
                                    viewModel.showToast("Forwarded to ${targetChat.title}")
                                    forwardTargetMessage = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(name = targetChat.title, size = 42.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(targetChat.title, color = WaTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text("Send", color = WaGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Group / Contact Info Modal (Entry Point for Group Contribution Ajo & Group Sub-Admins & User Profile)
    if (showGroupInfoSheet) {
        val linkedGroup = contributions.firstOrNull { it.linkedChatId == chat.id }
        val peerUid = if (!chat.isGroup) chat.memberUids.firstOrNull { it != viewModel.currentUserId } else null
        val peerProfile = peerUid?.let { uid ->
            publicProfiles.firstOrNull { it.userId == uid } ?: PublicProfile(
                userId = uid,
                displayName = chat.title,
                username = chat.title.lowercase().replace(Regex("[^a-z0-9]"), "_").take(20),
                countryFlag = chat.peerFlag,
                bio = "Active PGLOB member",
                isOnline = chat.peerOnline
            )
        }
        ModalBottomSheet(onDismissRequest = { showGroupInfoSheet = false }, containerColor = WaCard) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarCircle(
                        name = chat.title,
                        photoUri = peerProfile?.photoUri.orEmpty(),
                        size = 56.dp,
                        showOnlineDot = chat.peerOnline
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(chat.title, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                        Text(
                            text = if (chat.isGroup) "Group • ${chat.memberUids.size.coerceAtLeast(2)} members" else "@${peerProfile?.username ?: "member"} • Encrypted PGLOB Chat",
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                if (peerProfile != null) {
                    Surface(
                        color = WaInput,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Bio & Interests", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = peerProfile.bio.ifBlank { peerProfile.about.ifBlank { "Using PGLOB globally." } },
                                color = WaTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (peerProfile.interests.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    peerProfile.interests.take(6).forEach { tag ->
                                        Surface(
                                            color = WaGreen.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.border(1.dp, WaGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                        ) {
                                            Text(
                                                text = tag,
                                                color = WaTextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    showGroupInfoSheet = false
                                    inspectedPeerProfile = peerProfile
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("chat_view_peer_profile_btn")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = WaGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("View Full Profile", color = WaGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                HorizontalDivider(color = WaDivider, thickness = 1.dp)

                // Group Contribution (Ajo / Esusu) Entry Point
                Surface(
                    color = WaInput,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Group Contribution (Ajo / Esusu)", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        if (linkedGroup != null) {
                            Text(
                                "${linkedGroup.name} • ${linkedGroup.currency}${linkedGroup.amountPerRound.toInt()} / ${linkedGroup.type.label} (${linkedGroup.paidMembersCount}/${linkedGroup.members.size} paid)",
                                color = WaTextSecondary,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = {
                                    showGroupInfoSheet = false
                                    onOpenContribution(linkedGroup.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Open Contribution Group", fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Text(
                                "Pool savings automatically with PGLOB Escrow, Turn Order, and PDF Receipts.",
                                color = WaTextSecondary,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = {
                                    showGroupInfoSheet = false
                                    onCreateContributionForChat(chat)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Create Contribution for this Group", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Group Admins & Sub-Admins summary
                if (linkedGroup != null) {
                    val admins = linkedGroup.members.filter { it.role == "ADMIN" || it.role == "SUB_ADMIN" }
                    Surface(
                        color = WaInput,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            showGroupInfoSheet = false
                            onOpenContribution(linkedGroup.id)
                        }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("👑 Group Admins (${admins.size})", color = WaGold, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            admins.forEach { admin ->
                                Text("• ${admin.name} (${admin.role.replace("_", "-")})", color = WaTextPrimary, fontSize = 13.sp)
                            }
                            Text("Tap to manage Group Sub-Admins & permissions", color = WaGreen, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    inspectedPeerProfile?.let { target ->
        PublicUserProfileSheet(
            profile = target,
            isOwnProfile = target.userId == viewModel.currentUserId,
            isBlocked = localSettings.blockedUserIds.contains(target.userId),
            onDismiss = { inspectedPeerProfile = null },
            onMessageUser = { inspectedPeerProfile = null },
            onReportUser = {
                viewModel.submitReport("USER_PROFILE", target.userId, "Reported from Chat Info")
                inspectedPeerProfile = null
            },
            onToggleBlock = {
                viewModel.toggleBlockUser(target.userId)
                inspectedPeerProfile = null
            }
        )
    }

    if (showGiftSheet) {
        ModalBottomSheet(onDismissRequest = { showGiftSheet = false }, containerColor = PglobDarkGrey) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Send Luxury Gift 🎁", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Balance: ${myProfile?.coins ?: 0} coins", color = PglobGreyText)
                GlobalCatalogData.gifts.forEach { gift ->
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.sendGiftInChat(chat.id, chat.memberUids, gift.name, gift.emoji, gift.costCoins)
                            showGiftSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${gift.emoji}  ${gift.name}", color = PglobWhite, fontWeight = FontWeight.Bold)
                            Text("${gift.costCoins} coins", color = PglobGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSendMoneySheet) {
        var amountStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSendMoneySheet = false },
            containerColor = PglobDarkGrey,
            title = { Text("Send Money in Chat 💳", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Wallet Balance: ${myProfile?.currency ?: "₦"}${myProfile?.walletBalance?.toInt() ?: 0}", color = PglobWhite)
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            showSendMoneySheet = false
                            showPinConfirmForMoney = amt
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showSendMoneySheet = false }) { Text("Cancel", color = PglobWhite) }
            }
        )
    }

    if (pendingAttachSheet) {
        ModalBottomSheet(onDismissRequest = { pendingAttachSheet = false }, containerColor = PglobDarkGrey) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Attach & Share in Chat", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttachTile("Gallery", Icons.Default.PhotoLibrary, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    }
                    AttachTile("Camera", Icons.Default.CameraAlt, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            val u = createTempCameraUri(context)
                            tempPhotoUri = u
                            cameraLauncher.launch(u)
                        } else {
                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    AttachTile("Document", Icons.Default.AttachFile, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        docPickerLauncher.launch("*/*")
                    }
                    AttachTile("Location", Icons.Default.LocationOn, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        val locText = "📍 Shared Location: ${myProfile?.cityOrCountryLabel() ?: "Global Earth 🌍"}"
                        viewModel.sendChatMessage(chat.id, chat.memberUids, locText)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttachTile("Contact", Icons.Default.Contacts, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        viewModel.sendChatMessage(chat.id, chat.memberUids, "👤 Contact Card: ${myProfile?.fullName ?: "Member"} (@${myProfile?.username ?: "pglob"})")
                    }
                    AttachTile("Poll", Icons.Default.Poll, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        viewModel.sendChatMessage(chat.id, chat.memberUids, "📊 Poll: Meet online today? [Yes 👍] [Later ⏰] [Busy]")
                    }
                    AttachTile("Event", Icons.Default.Event, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        viewModel.sendChatMessage(chat.id, chat.memberUids, "📅 Event Invite: Global PGLOB Live Hangout • Today 8:00 PM GMT")
                    }
                    AttachTile("PGLOB AI", Icons.Default.AutoAwesome, Modifier.weight(1f)) {
                        pendingAttachSheet = false
                        showAiSheet = true
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showTranslateSheet) {
        val lastText = messages.lastOrNull()?.text ?: inputText.ifBlank { "Hello friend, how are you doing today?" }
        ModalBottomSheet(onDismissRequest = { showTranslateSheet = false }, containerColor = PglobDarkGrey) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Instant Chat Translator 🌐", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Message: \"$lastText\"", color = PglobWhite, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text("Choose Target Language:", color = PglobGreyText, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlobalCatalogData.languages.forEach { lang ->
                        Surface(
                            color = if (targetTranslateLang == lang) PglobGold else PglobBlack,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable { targetTranslateLang = lang }
                        ) {
                            Text(lang, color = if (targetTranslateLang == lang) PglobBlack else PglobWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                        }
                    }
                }
                Button(
                    onClick = {
                        showTranslateSheet = false
                        aiInitialPrompt = "Translate accurately to $targetTranslateLang and return only the natural translation:\n\"$lastText\""
                        viewModel.askPglobAi(aiInitialPrompt)
                        showAiSheet = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) {
                    Text("Translate to $targetTranslateLang ✨", fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showPinConfirmForMoney != null) {
        val amt = showPinConfirmForMoney!!
        val currency = myProfile?.currency ?: "₦"
        PinOrBiometricConfirmSheet(
            title = "Authorize Chat Transfer",
            summary = "Send $currency${amt.toInt()} to ${chat.title} (Fee: ${currency}10)",
            hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
            biometricEnabled = myProfile?.fingerprintEnabled == true,
            lockUntilMillis = localSettings.payLockUntilMillis,
            isLargeTransfer = amt >= CountryPayCatalog.largeTransferThreshold(currency),
            onDismiss = { showPinConfirmForMoney = null },
            onSetPin = { viewModel.setTransactionPin(it) },
            onConfirmWithPin = { pin, bio ->
                if (bio || !myProfile?.transactionPin.isNullOrBlank()) {
                    if (viewModel.verifyPinOrAllow(pin, bio)) {
                        viewModel.executePayAction(
                            txType = "SEND",
                            amount = amt,
                            fee = 10.0,
                            method = "In-Chat PGLOB Pay",
                            destination = chat.title,
                            note = "Chat money transfer",
                            chatIdForPacket = chat.id,
                            chatMembers = chat.memberUids
                        )
                        showPinConfirmForMoney = null
                    }
                }
            }
        )
    }

    if (showAiSheet) {
        PglobAiAssistantSheet(
            aiState = aiState,
            initialPrompt = aiInitialPrompt,
            onAsk = { viewModel.askPglobAi(it) },
            onInsertIntoChat = { aiReply ->
                viewModel.sendChatMessage(chat.id, chat.memberUids, aiReply)
            },
            onDismiss = { showAiSheet = false; viewModel.clearAiState() }
        )
    }

    if (permissionDeniedNotice != null) {
        AlertDialog(
            onDismissRequest = { permissionDeniedNotice = null },
            containerColor = PglobDarkGrey,
            title = { Text("Permission Required", color = PglobGold) },
            text = { Text(permissionDeniedNotice!!, color = PglobWhite) },
            confirmButton = {
                TextButton(onClick = { permissionDeniedNotice = null }) { Text("OK", color = PglobGold) }
            }
        )
    }
}

@Composable
private fun CallInProgressOverlay(
    peerName: String,
    isVideo: Boolean,
    onEndCall: () -> Unit
) {
    var seconds by remember { mutableIntStateOf(0) }
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            seconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0D))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarCircle(name = peerName, size = 110.dp, showOnlineDot = true)
            Spacer(Modifier.height(18.dp))
            Text(peerName, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                if (isVideo) "End-to-End Encrypted Video • ${seconds}s"
                else "End-to-End Encrypted Voice • ${seconds}s",
                color = PglobGold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { muted = !muted },
                modifier = Modifier.size(56.dp).clip(CircleShape).background(PglobDarkGrey)
            ) {
                Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = PglobWhite)
            }
            IconButton(
                onClick = onEndCall,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(PglobRed)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = PglobWhite)
            }
            IconButton(
                onClick = { speaker = !speaker },
                modifier = Modifier.size(56.dp).clip(CircleShape).background(PglobDarkGrey)
            ) {
                Icon(if (speaker) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Speaker", tint = PglobWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatOrGroupSheet(
    publicProfiles: List<PublicProfile>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onOpenOrCreate: (title: String, targetUid: String, isGroup: Boolean, flag: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var showGroupInput by remember { mutableStateOf(false) }
    var inspectedMemberProfile by remember { mutableStateOf<PublicProfile?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PglobDarkGrey) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("New Chat / Group / Community", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedButton(
                onClick = { showGroupInput = !showGroupInput },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showGroupInput) "Hide New Group" else "+ Create New Group", color = PglobGold)
            }
            if (showGroupInput) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                onOpenOrCreate(groupName.trim(), "group_room", true, "🌍")
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) { Text("Create") }
                }
            }
            Text("Global Members on PGLOB APP", color = PglobGreyText, fontSize = 13.sp)
            val others = publicProfiles.filter { it.userId != currentUserId }
            val fallbackPeers = if (others.isEmpty()) {
                listOf(
                    PublicProfile(userId = "amina_ng", displayName = "Amina Yusuf", username = "amina_lagos", countryFlag = "🇳🇬", country = "Nigeria", city = "Lagos", bio = "Gadget vendor & Ajo circle organizer in Lagos.", interests = listOf("Business", "Ajo Savings", "Tech & AI")),
                    PublicProfile(userId = "marcus_us", displayName = "Marcus Vance", username = "marcus_nyc", countryFlag = "🇺🇸", country = "USA", city = "New York", bio = "Audio creator & global live room host.", interests = listOf("Music", "Photography", "Travel")),
                    PublicProfile(userId = "kwame_gh", displayName = "Kwame Mensah", username = "kwame_accra", countryFlag = "🇬🇭", country = "Ghana", city = "Accra", bio = "Kente tailor & West Africa trader.", interests = listOf("Fashion", "Design", "Business"))
                )
            } else others

            LazyColumn(modifier = Modifier.height(260.dp)) {
                items(fallbackPeers, key = { it.userId }) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenOrCreate("${p.displayName} ${p.countryFlag}", p.userId, false, p.countryFlag)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCircle(name = p.displayName, photoUri = p.photoUri, size = 44.dp, showOnlineDot = p.isOnline)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${p.displayName} ${p.countryFlag}", color = PglobWhite, fontWeight = FontWeight.Bold)
                            Text("@${p.username} • ${p.country}", color = PglobGreyText, fontSize = 12.sp)
                            val bioPreview = p.bio.ifBlank { p.interests.take(3).joinToString(" • ") }
                            if (bioPreview.isNotBlank()) {
                                Text(bioPreview, color = WaTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(
                            onClick = { inspectedMemberProfile = p },
                            modifier = Modifier.size(48.dp).testTag("new_chat_view_profile_${p.userId}")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "View ${p.displayName} profile", tint = WaGreen)
                        }
                        Text("Chat", color = PglobGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    inspectedMemberProfile?.let { target ->
        PublicUserProfileSheet(
            profile = target,
            isOwnProfile = target.userId == currentUserId,
            onDismiss = { inspectedMemberProfile = null },
            onMessageUser = {
                onOpenOrCreate("${target.displayName} ${target.countryFlag}", target.userId, false, target.countryFlag)
                inspectedMemberProfile = null
                onDismiss()
            }
        )
    }
}

@Composable
private fun AttachTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = PglobBlack,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = PglobGold, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

private fun UserPrivateProfile.cityOrCountryLabel(): String =
    "$countryFlag $country".trim().ifBlank { "Global Earth 🌍" }
