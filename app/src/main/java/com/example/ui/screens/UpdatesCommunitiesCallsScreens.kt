package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelItem
import com.example.data.model.GlobalCatalogData
import com.example.data.model.StatusNote
import com.example.data.model.StatusPost
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaRed
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary

@Composable
fun UpdatesScreen(
    statuses: List<StatusPost>,
    statusReactions: Map<String, String> = emptyMap(),
    viewedStatusIds: Set<String> = emptySet(),
    viewModel: PglobViewModel,
    statusNotes: List<StatusNote> = emptyList(),
    myName: String = "Me",
    myPhotoUri: String = "",
    myActiveStatusNote: StatusNote? = null,
    myExpiredStatusNote: StatusNote? = null,
    onAddOrReplaceNote: () -> Unit = {},
    onOpenNote: (StatusNote) -> Unit = {},
    onOpenStatus: (statusId: String, openViewers: Boolean) -> Unit = { _, _ -> }
) {
    var showCreateStatus by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showTopMenu by remember { mutableStateOf(false) }
    var statusCaption by remember { mutableStateOf("") }
    var statusMediaUri by remember { mutableStateOf("") }
    var statusMediaType by remember { mutableStateOf("TEXT") }
    var statusPrivacy by remember { mutableStateOf("PUBLIC") }
    var viewingChannel by remember { mutableStateOf<ChannelItem?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            statusMediaUri = uri.toString()
            statusMediaType = "IMAGE"
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            statusMediaUri = uri.toString()
            statusMediaType = "VIDEO"
        }
    }

    val myStatuses = remember(statuses, viewModel.currentUserId) {
        statuses.filter { it.userId == viewModel.currentUserId }
    }
    val otherStatuses = remember(statuses, viewModel.currentUserId, searchText) {
        statuses.filter { it.userId != viewModel.currentUserId }
            .filter { searchText.isBlank() || it.displayName.contains(searchText, ignoreCase = true) }
    }
    val filteredChannels = remember(searchText) {
        GlobalCatalogData.channels.filter {
            searchText.isBlank() || it.name.contains(searchText, ignoreCase = true) || it.lastMessage.contains(searchText, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updates",
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp
                    )
                    Row {
                        IconButton(onClick = {
                            showSearchBar = !showSearchBar
                            if (!showSearchBar) searchText = ""
                        }) {
                            Icon(
                                if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search Updates",
                                tint = WaTextPrimary
                            )
                        }
                        Box {
                            IconButton(onClick = { showTopMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = WaTextPrimary)
                            }
                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false },
                                modifier = Modifier.background(WaCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add Status", color = WaTextPrimary) },
                                    onClick = { showTopMenu = false; showCreateStatus = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Status Privacy", color = WaTextPrimary) },
                                    onClick = { showTopMenu = false; showCreateStatus = true }
                                )
                            }
                        }
                    }
                }

                if (showSearchBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(WaInput)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = WaTextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            singleLine = true,
                            cursorBrush = SolidColor(WaGreen),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = WaTextPrimary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (searchText.isEmpty()) Text("Search status or channels...", color = WaTextSecondary, fontSize = 14.sp)
                                inner()
                            }
                        )
                    }
                }
            }

            item {
                StatusNotesHorizontalStrip(
                    notes = statusNotes,
                    currentUserId = viewModel.currentUserId,
                    myName = myName,
                    myPhotoUri = myPhotoUri,
                    myActiveNote = myActiveStatusNote,
                    myExpiredNote = myExpiredStatusNote,
                    onAddOrReplaceNote = onAddOrReplaceNote,
                    onOpenNote = onOpenNote
                )

                Text(
                    text = "Status",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First item: Add status
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(72.dp)
                            .clickable {
                                val firstMine = myStatuses.firstOrNull()
                                if (firstMine != null) {
                                    onOpenStatus(firstMine.id, false)
                                } else {
                                    showCreateStatus = true
                                }
                            }
                    ) {
                        Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .then(
                                        if (myStatuses.isNotEmpty()) {
                                            Modifier.border(2.dp, WaGreen, CircleShape).padding(3.dp)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarCircle(name = "Me", size = 60.dp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(WaGreen)
                                    .border(2.dp, WaBackground, CircleShape)
                                    .clickable { showCreateStatus = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Status", tint = WaBackground, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (myStatuses.isNotEmpty()) "My status" else "Add status",
                            color = WaTextPrimary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    otherStatuses.forEach { st ->
                        val isSeen = viewedStatusIds.contains(st.id)
                        val ringColor = if (isSeen) WaTextSecondary else WaGreen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { onOpenStatus(st.id, false) }
                                .testTag("status_item_${st.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(2.dp, ringColor, CircleShape)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarCircle(
                                    name = st.displayName,
                                    photoUri = st.avatarUri,
                                    size = 60.dp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = st.displayName,
                                color = WaTextPrimary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = WaDivider, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Channels",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(filteredChannels) { ch ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { viewingChannel = ch }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarCircle(name = ch.name, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ch.name,
                            color = WaTextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (ch.isVoice) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = WaGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                text = ch.lastMessage,
                                color = WaTextSecondary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(ch.time, color = WaGreen, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 20.dp)
                                .clip(CircleShape)
                                .background(WaGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.unreadCount.toString(),
                                color = WaBackground,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                HorizontalDivider(color = WaDivider, thickness = 1.dp, modifier = Modifier.padding(start = 76.dp))
            }
        }

        FloatingActionButton(
            onClick = { showCreateStatus = true },
            containerColor = WaGreen,
            contentColor = WaTextPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Post Status", tint = WaBackground)
        }
    }

    if (showCreateStatus) {
        AlertDialog(
            onDismissRequest = { showCreateStatus = false },
            containerColor = WaCard,
            title = { Text("Post 24h Status", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 20.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = statusCaption,
                        onValueChange = { statusCaption = it },
                        label = { Text("What's happening?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                statusMediaType = "TEXT"
                                statusMediaUri = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (statusMediaType == "TEXT") WaGreen else WaInput,
                                contentColor = if (statusMediaType == "TEXT") WaBackground else WaTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Text") }
                        Button(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (statusMediaType == "IMAGE") WaGreen else WaInput,
                                contentColor = if (statusMediaType == "IMAGE") WaBackground else WaTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Photo") }
                        Button(
                            onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (statusMediaType == "VIDEO") WaGreen else WaInput,
                                contentColor = if (statusMediaType == "VIDEO") WaBackground else WaTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Video") }
                    }
                    if (statusMediaUri.isNotBlank()) {
                        Text("Media attached ($statusMediaType)", color = WaGreen, fontSize = 13.sp)
                    }
                    Text("Privacy:", color = WaTextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("PUBLIC" to "Public", "CONTACTS" to "Contacts", "PRIVATE" to "Private 🔒").forEach { (key, label) ->
                            Surface(
                                color = if (statusPrivacy == key) WaGreen else WaInput,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { statusPrivacy = key }
                            ) {
                                Text(
                                    text = label,
                                    color = if (statusPrivacy == key) WaBackground else WaTextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (statusCaption.isNotBlank() || statusMediaUri.isNotBlank()) {
                            viewModel.postStatus(
                                caption = statusCaption.ifBlank { "Shared a $statusMediaType Status" },
                                mediaType = statusMediaType,
                                mediaUri = statusMediaUri,
                                privacyMode = statusPrivacy
                            )
                            statusCaption = ""
                            statusMediaUri = ""
                            statusMediaType = "TEXT"
                            statusPrivacy = "PUBLIC"
                            showCreateStatus = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                ) { Text("Post Status", fontWeight = FontWeight.Medium) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateStatus = false }) { Text("Cancel", color = WaTextSecondary) }
            }
        )
    }

    viewingChannel?.let { ch ->
        AlertDialog(
            onDismissRequest = { viewingChannel = null },
            containerColor = WaCard,
            title = { Text(ch.name, color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = { Text("Latest Channel Broadcast:\n\n${ch.lastMessage}\n\n${ch.unreadCount} new updates.", color = WaTextSecondary) },
            confirmButton = { TextButton(onClick = { viewingChannel = null }) { Text("Follow & Close", color = WaGreen) } }
        )
    }
}

@Composable
fun CommunitiesScreen(viewModel: PglobViewModel) {
    var showCreateCommunity by remember { mutableStateOf(false) }
    var customCommunityName by remember { mutableStateOf("") }
    var dynamicList by remember { mutableStateOf(GlobalCatalogData.communities) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Communities",
                color = WaTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateCommunity = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(WaInput),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Groups, contentDescription = "New community", tint = WaTextPrimary)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(WaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = WaBackground, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("New community", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            HorizontalDivider(color = WaDivider, thickness = 1.dp)
        }

        items(dynamicList) { comm ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarCircle(name = comm.name, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(comm.name, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showToast("Joined ${comm.name} Announcements") }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(WaGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Announcements", tint = WaGreen)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Announcements", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text(comm.announcement, color = WaTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(comm.announcementTime, color = WaTextSecondary, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showToast("Opening ${comm.groupName}") }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(WaInput),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = "Group", tint = WaGreen)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(comm.groupName, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text(comm.groupMessage, color = WaTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(comm.groupTime, color = WaGreen, fontSize = 12.sp)
                        if (comm.hasMention) {
                            Text("@", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showToast("Viewing all groups in ${comm.name}") }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = WaTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View all", color = WaTextSecondary, fontSize = 14.sp)
                }
                HorizontalDivider(color = WaDivider, thickness = 1.dp)
            }
        }
    }

    if (showCreateCommunity) {
        AlertDialog(
            onDismissRequest = { showCreateCommunity = false },
            containerColor = WaCard,
            title = { Text("Create Community", color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = {
                OutlinedTextField(
                    value = customCommunityName,
                    onValueChange = { customCommunityName = it },
                    label = { Text("Community Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customCommunityName.isNotBlank()) {
                            dynamicList = listOf(
                                com.example.data.model.CommunityItem(
                                    id = "c_${System.currentTimeMillis()}",
                                    name = customCommunityName.trim(),
                                    announcement = "Community created! Welcome members.",
                                    announcementTime = "Now",
                                    groupName = "General Chat",
                                    groupMessage = "Say hello to everyone!",
                                    groupTime = "Now"
                                )
                            ) + dynamicList
                            customCommunityName = ""
                            showCreateCommunity = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateCommunity = false }) { Text("Cancel", color = WaTextSecondary) } }
        )
    }
}

@Composable
fun CallsScreen(viewModel: PglobViewModel) {
    var showKeypad by remember { mutableStateOf(false) }
    var dialNumber by remember { mutableStateOf("") }
    var activeCallPeer by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calls", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 22.sp)
                    Row {
                        IconButton(onClick = { showKeypad = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search calls", tint = WaTextPrimary)
                        }
                        IconButton(onClick = { showKeypad = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Call menu", tint = WaTextPrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Triple("Call", Icons.Default.Call) { activeCallPeer = "Global Support" },
                        Triple("Schedule", Icons.Default.CalendarMonth) { viewModel.showToast("Call scheduled for 8:00 PM!") },
                        Triple("Keypad", Icons.Default.Dialpad) { showKeypad = true },
                        Triple("Favorites", Icons.Default.Favorite) { activeCallPeer = "Amina Yusuf 🇳🇬" }
                    ).forEach { (label, icon, action) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { action() }) {
                            Box(
                                modifier = Modifier.size(52.dp).clip(CircleShape).background(WaInput),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = label, tint = WaGreen)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(label, color = WaTextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = "Ongoing",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Surface(
                    color = WaCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCircle(name = "Fun to chat", size = 48.dp, showOnlineDot = true)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fun to chat", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Text("Ongoing voice chat • 18 live", color = WaTextSecondary, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { activeCallPeer = "Fun to chat (Voice Room)" },
                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Join", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Text(
                    text = "Recent",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(GlobalCatalogData.sampleCalls) { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { activeCallPeer = "${call.name} ${call.flag}" }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarCircle(name = call.name, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(call.name, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text(
                            if (call.isMissed) "Missed • ${call.time}" else call.time,
                            color = if (call.isMissed) WaRed else WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = { activeCallPeer = "${call.name} ${call.flag}" }) {
                        Icon(
                            if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Call back",
                            tint = WaGreen
                        )
                    }
                }
                HorizontalDivider(color = WaDivider, thickness = 1.dp, modifier = Modifier.padding(start = 76.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WaTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Your personal calls are end-to-end encrypted",
                        color = WaTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showKeypad = true },
            containerColor = WaGreen,
            contentColor = WaBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = "New Call")
        }
    }

    if (showKeypad) {
        AlertDialog(
            onDismissRequest = { showKeypad = false },
            containerColor = WaCard,
            title = { Text("PGLOB Dial Keypad", color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(dialNumber.ifBlank { "+234 ..." }, color = WaTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("+", "0", "⌫")
                    ).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { key ->
                                Surface(
                                    color = WaInput,
                                    shape = CircleShape,
                                    modifier = Modifier.size(54.dp).clickable {
                                        if (key == "⌫") {
                                            if (dialNumber.isNotEmpty()) dialNumber = dialNumber.dropLast(1)
                                        } else {
                                            dialNumber += key
                                        }
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(key, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialNumber.isNotBlank()) {
                            activeCallPeer = dialNumber
                            showKeypad = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                ) { Text("Call Now") }
            },
            dismissButton = { TextButton(onClick = { showKeypad = false }) { Text("Close", color = WaTextSecondary) } }
        )
    }

    if (activeCallPeer != null) {
        AlertDialog(
            onDismissRequest = { activeCallPeer = null },
            containerColor = WaCard,
            title = { Text("Calling $activeCallPeer...", color = WaTextPrimary, fontWeight = FontWeight.Medium) },
            text = { Text("End-to-end encrypted PGLOB Audio/Video stream active.", color = WaGreen) },
            confirmButton = {
                Button(
                    onClick = { activeCallPeer = null },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary)
                ) { Text("End Call") }
            }
        )
    }
}
