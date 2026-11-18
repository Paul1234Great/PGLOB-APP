package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatThread
import com.example.data.model.ContributionGroup
import com.example.data.model.PublicProfile
import com.example.data.model.StatusNote
import com.example.data.model.StatusNoteActor
import com.example.ui.components.AvatarCircle
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaRed
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.StatusNoteCodec
import kotlinx.coroutines.delay

/**
 * Horizontal Status Notes Strip shown at the top of Status (Updates) and Chats tabs.
 */
@Composable
fun StatusNotesHorizontalStrip(
    notes: List<StatusNote>,
    currentUserId: String,
    myName: String,
    myPhotoUri: String,
    myActiveNote: StatusNote?,
    myExpiredNote: StatusNote?,
    onAddOrReplaceNote: () -> Unit,
    onOpenNote: (StatusNote) -> Unit,
    modifier: Modifier = Modifier
) {
    val friendNotes = remember(notes, currentUserId) {
        notes.filter { it.userId != currentUserId }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WaBackground)
            .padding(top = 6.dp, bottom = 6.dp)
            .testTag("status_notes_strip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 1. My Note Slot (Active Note / Expired Note / "+ Add Note")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(106.dp)
                    .clickable {
                        if (myActiveNote != null) onOpenNote(myActiveNote) else onAddOrReplaceNote()
                    }
                    .testTag("status_note_my_slot")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(
                                width = 2.5.dp,
                                color = if (myActiveNote != null) WaGreen else WaInput,
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarCircle(
                            name = myName.ifBlank { "Me" },
                            photoUri = myPhotoUri,
                            size = 58.dp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 18.dp, bottom = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(WaGreen)
                            .border(2.dp, WaBackground, CircleShape)
                            .clickable { onAddOrReplaceNote() }
                            .testTag("status_note_add_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Status Note",
                            tint = WaPureBlack,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    val bubbleText = when {
                        myActiveNote != null -> myActiveNote.text
                        myExpiredNote != null -> "Your Note don expire"
                        else -> "Your Note: + Add Note"
                    }
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = bubbleText,
                            color = WaPureBlack,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                if (myActiveNote == null && myExpiredNote != null) {
                    Surface(
                        color = WaGreen,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable { onAddOrReplaceNote() }
                            .testTag("status_note_write_new_btn")
                    ) {
                        Text(
                            text = "Write New Note",
                            color = WaPureBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (myActiveNote != null) "My Note" else "Your Note",
                        color = WaTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. Friends' Active Notes
            friendNotes.forEach { note ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(106.dp)
                        .clickable { onOpenNote(note) }
                        .testTag("status_note_item_${note.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(106.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(2.5.dp, WaGreen, CircleShape)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarCircle(
                                name = note.authorName,
                                photoUri = note.authorAvatarUri,
                                size = 58.dp
                            )
                        }

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = note.text,
                                color = WaPureBlack,
                                fontSize = 14.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.authorName.substringBefore(" ").ifBlank { note.authorName },
                        color = WaTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        HorizontalDivider(color = WaDivider, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * Full-screen overlay to Write, Replace, or Edit a PGLOB Status Note (max 100 chars).
 */
@Composable
fun WriteStatusNoteOverlay(
    existingNote: StatusNote?,
    isEditingSameId: Boolean,
    isProUser: Boolean,
    publicProfiles: List<PublicProfile>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmitNewOrReplace: (
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int
    ) -> Unit,
    onSubmitEdit: (
        existing: StatusNote,
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int
    ) -> Unit
) {
    BackHandler { onDismiss() }

    var noteText by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(if (isEditingSameId && existingNote != null) existingNote.text else "")
    }
    var selectedBgHex by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(existingNote?.bgColor?.ifBlank { "#00A884" } ?: "#00A884")
    }
    var selectedMood by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(if (isEditingSameId) existingNote?.mood.orEmpty() else "")
    }
    var selectedMusic by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(if (isEditingSameId) existingNote?.music.orEmpty() else "")
    }
    var showMusicPicker by remember { mutableStateOf(false) }
    var showEmojiBar by remember { mutableStateOf(false) }
    var selectedPrivacy by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(if (isEditingSameId && existingNote != null) existingNote.privacy else "EVERYONE")
    }
    var customUids by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(
            if (isEditingSameId && existingNote != null) existingNote.customAllowedUids.toSet()
            else emptySet()
        )
    }
    var selectedDurationHours by remember(existingNote?.id, isEditingSameId) {
        mutableStateOf(if (isEditingSameId && existingNote != null) existingNote.durationHours else 24)
    }

    val preset = StatusNoteCodec.presetForHex(selectedBgHex)
    val charCounter = StatusNoteCodec.formatCharCounter(noteText)
    val selectableProfiles = remember(publicProfiles, currentUserId) {
        publicProfiles.filter { it.userId != currentUserId }
    }

    Surface(
        color = WaBackground,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("write_note_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("write_note_close_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WaTextPrimary
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Write Your Status Note",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (existingNote != null && !isEditingSameId) {
                        Text(
                            text = "Posting replaces your current active Note (resets likes)",
                            color = WaGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (isEditingSameId && existingNote != null) {
                        Text(
                            text = "Editing keeps your Note ID & Likes",
                            color = WaGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = WaDivider, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Preview Card
                Surface(
                    color = preset.bgColor,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, WaDivider, RoundedCornerShape(20.dp))
                        .testTag("write_note_live_preview")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = preset.contentColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${selectedDurationHours}h • Live Preview",
                                    color = preset.contentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (selectedMood.isNotBlank()) {
                                Surface(
                                    color = preset.contentColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = selectedMood,
                                        color = preset.contentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = noteText.ifBlank { "I dey Lagos today, DM for Ajo 🔥" },
                            color = if (noteText.isBlank()) preset.contentColor.copy(alpha = 0.72f) else preset.contentColor,
                            fontSize = 22.sp,
                            lineHeight = 29.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (selectedMusic.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = "Music",
                                    tint = preset.contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = selectedMusic,
                                    color = preset.contentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Big TextField + Emoji toggle + 0/100 counter
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = StatusNoteCodec.clampNoteText(it) },
                        placeholder = {
                            Text(
                                "Write wetin dey your mind... e.g I dey Lagos today, DM for Ajo 🔥",
                                color = WaTextSecondary,
                                fontSize = 16.sp
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = WaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        minLines = 3,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WaInput,
                            unfocusedContainerColor = WaInput,
                            focusedBorderColor = WaGreen,
                            unfocusedBorderColor = WaDivider,
                            cursorColor = WaGreen
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("write_note_textfield")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (showEmojiBar) WaGreen else WaCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { showEmojiBar = !showEmojiBar }
                                .testTag("write_note_emoji_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("😊", fontSize = 18.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (showEmojiBar) "Hide Emoji" else "Add Emoji",
                                    color = if (showEmojiBar) WaPureBlack else WaTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = charCounter,
                            color = when {
                                noteText.length >= 100 -> WaRed
                                noteText.length >= 85 -> WaGold
                                else -> WaGreen
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("write_note_char_counter")
                        )
                    }

                    AnimatedVisibility(visible = showEmojiBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                                .testTag("write_note_emoji_bar"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusNoteCodec.QUICK_EMOJIS.forEach { em ->
                                Surface(
                                    color = WaCard,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable {
                                            noteText = StatusNoteCodec.clampNoteText(noteText + em)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(em, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 5 Background Colors
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Background Color",
                        color = WaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusNoteCodec.BG_PRESETS.forEach { item ->
                            val selected = item.hex.equals(selectedBgHex, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedBgHex = item.hex }
                                    .testTag("write_note_color_${item.label.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(item.bgColor)
                                        .border(
                                            width = if (selected) 3.dp else 1.5.dp,
                                            color = if (selected) Color.White else WaDivider,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "${item.label} selected",
                                            tint = item.contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    color = if (selected) WaGreen else WaTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Optional Music & Mood
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("write_note_mood_section")
                ) {
                    Text(
                        text = "Music & Mood (Optional)",
                        color = WaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (showMusicPicker || selectedMusic.isNotBlank()) WaGreen else WaCard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .clickable { showMusicPicker = !showMusicPicker }
                                .testTag("write_note_add_music_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = "Add Music",
                                    tint = if (showMusicPicker || selectedMusic.isNotBlank()) WaPureBlack else WaTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (selectedMusic.isNotBlank()) selectedMusic else "Add Music",
                                    color = if (showMusicPicker || selectedMusic.isNotBlank()) WaPureBlack else WaTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        StatusNoteCodec.MOOD_OPTIONS.forEach { mood ->
                            val selected = selectedMood == mood
                            Surface(
                                color = if (selected) WaGreen else WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .clickable {
                                        selectedMood = if (selected) "" else mood
                                    }
                                    .testTag("write_note_mood_${mood.filter { it.isLetterOrDigit() }.lowercase()}")
                            ) {
                                Text(
                                    text = mood,
                                    color = if (selected) WaPureBlack else WaTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = showMusicPicker) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(WaCard)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusNoteCodec.MUSIC_PRESETS.forEach { track ->
                                    val chosen = selectedMusic == track
                                    Surface(
                                        color = if (chosen) WaGreen else WaInput,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable {
                                            selectedMusic = if (chosen) "" else track
                                        }
                                    ) {
                                        Text(
                                            text = track,
                                            color = if (chosen) WaPureBlack else WaTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = selectedMusic,
                                onValueChange = { selectedMusic = it.take(80) },
                                placeholder = { Text("Or type song / vibe name...", color = WaTextSecondary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("write_note_music_input")
                            )
                        }
                    }
                }

                // Privacy Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("write_note_privacy_section")
                ) {
                    Text(
                        text = "Who can see this Note?",
                        color = WaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusNoteCodec.PRIVACY_OPTIONS.forEach { (code, label) ->
                            val selected = selectedPrivacy == code
                            Surface(
                                color = if (selected) WaGreen else WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .clickable { selectedPrivacy = code }
                                    .testTag("write_note_privacy_${code.lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) WaPureBlack else WaTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    if (selectedPrivacy == "CUSTOM") {
                        Surface(
                            color = WaCard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Select Friends (${customUids.size} selected)",
                                    color = WaGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectableProfiles.isEmpty()) {
                                    Text("No contacts loaded yet.", color = WaTextSecondary, fontSize = 13.sp)
                                } else {
                                    selectableProfiles.take(15).forEach { person ->
                                        val checked = customUids.contains(person.userId)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    customUids = if (checked) customUids - person.userId else customUids + person.userId
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = {
                                                    customUids = if (checked) customUids - person.userId else customUids + person.userId
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = WaGreen, checkmarkColor = WaPureBlack)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            AvatarCircle(name = person.displayName, photoUri = person.photoUri, size = 34.dp)
                                            Spacer(Modifier.width(10.dp))
                                            Text(person.displayName, color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Duration (24h FREE vs 48h PRO)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("write_note_duration_section")
                ) {
                    Text(
                        text = "Duration",
                        color = WaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            24 to "24 Hours (FREE)",
                            48 to if (isProUser) "48 Hours (PRO ✓)" else "48 Hours (PRO)"
                        ).forEach { (hours, label) ->
                            val selected = selectedDurationHours == hours
                            Surface(
                                color = if (selected) WaGreen else WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDurationHours = hours }
                                    .testTag("write_note_duration_$hours")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) WaPureBlack else WaTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Solid 56.dp Post Note Button
            Surface(
                color = WaBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (isEditingSameId && existingNote != null) {
                            onSubmitEdit(
                                existingNote,
                                noteText,
                                selectedBgHex,
                                selectedMusic,
                                selectedMood,
                                selectedPrivacy,
                                customUids.toList(),
                                selectedDurationHours
                            )
                        } else {
                            onSubmitNewOrReplace(
                                noteText,
                                selectedBgHex,
                                selectedMusic,
                                selectedMood,
                                selectedPrivacy,
                                customUids.toList(),
                                selectedDurationHours
                            )
                        }
                    },
                    enabled = noteText.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaGreen,
                        contentColor = WaPureBlack,
                        disabledContainerColor = WaInput,
                        disabledContentColor = WaTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp)
                        .testTag("write_note_post_btn")
                ) {
                    Text(
                        text = when {
                            isEditingSameId -> "Update Note"
                            existingNote != null -> "Replace & Post Note"
                            else -> "Post Note"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Full-Screen Status Note Viewer with Left/Right swipe between notes, Swipe-Down to close,
 * Like animation, Viewers list, Likers list, 4-option Reshare sheet, Reply, Edit, Replace & Delete.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullScreenStatusNoteViewer(
    notes: List<StatusNote>,
    initialNoteId: String,
    currentUserId: String,
    canAdminModerate: Boolean,
    chats: List<ChatThread>,
    contributions: List<ContributionGroup>,
    onDismiss: () -> Unit,
    onRecordView: (StatusNote) -> Unit,
    onToggleLike: (StatusNote) -> Unit,
    onReshareToMyNote: (StatusNote) -> Unit,
    onReshareToMyStatus: (StatusNote) -> Unit,
    onShareToChat: (note: StatusNote, chatId: String, title: String, members: List<String>) -> Unit,
    onReplyToNote: (note: StatusNote, replyText: String) -> Unit,
    onReplaceNote: (StatusNote) -> Unit,
    onEditNote: (StatusNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    onReportNote: (StatusNote, String) -> Unit,
    onBlockAuthor: (String) -> Unit,
    onAdminDeleteNote: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    publicProfiles: List<PublicProfile> = emptyList(),
    blockedUserIds: Set<String> = emptySet(),
    onOpenUserChat: (PublicProfile) -> Unit = {},
    onReportUser: (PublicProfile) -> Unit = {}
) {
    BackHandler { onDismiss() }

    if (notes.isEmpty()) {
        onDismiss()
        return
    }

    val initialIdx = remember(initialNoteId) {
        notes.indexOfFirst { it.id == initialNoteId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialIdx, pageCount = { notes.size })
    val context = LocalContext.current

    var swipeDownOffset by remember { mutableFloatStateOf(0f) }
    var showReplyBar by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(false) }
    var showLikersSheet by remember { mutableStateOf(false) }
    var showReshareSheet by remember { mutableStateOf(false) }
    var showChatSharePicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Spam or inappropriate content") }
    var inspectedAuthorProfile by remember { mutableStateOf<PublicProfile?>(null) }

    val openAuthorProfile: (StatusNote) -> Unit = { targetNote ->
        inspectedAuthorProfile = publicProfiles.firstOrNull { it.userId == targetNote.userId }
            ?: PublicProfile(
                userId = targetNote.userId,
                displayName = targetNote.authorName,
                username = targetNote.authorName.lowercase().replace(Regex("[^a-z0-9]"), "_").take(20),
                photoUri = targetNote.authorAvatarUri,
                bio = "PGLOB Status Note creator",
                isOnline = true
            )
    }

    var showHeartBurst by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0.4f) }

    val activeNote = notes[pagerState.currentPage.coerceIn(0, notes.lastIndex)]
    val isLikedByMe = activeNote.likes.any { it.uid == currentUserId }

    LaunchedEffect(activeNote.id) {
        onRecordView(activeNote)
    }

    LaunchedEffect(showHeartBurst) {
        if (showHeartBurst) {
            heartScale.snapTo(0.4f)
            heartScale.animateTo(1.35f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            heartScale.animateTo(1.0f, tween(140))
            delay(480)
            showHeartBurst = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaPureBlack)
            .offset { IntOffset(0, swipeDownOffset.toInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (swipeDownOffset > 150f) onDismiss() else swipeDownOffset = 0f
                    },
                    onDragCancel = { swipeDownOffset = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f || swipeDownOffset > 0f) {
                            swipeDownOffset = (swipeDownOffset + dragAmount).coerceAtLeast(0f)
                        }
                    }
                )
            }
            .testTag("full_screen_note_viewer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageNote = notes[page]
                val preset = StatusNoteCodec.presetForHex(pageNote.bgColor)
                val fg = preset.contentColor
                val pageLiked = pageNote.likes.any { it.uid == currentUserId }
                val pageIsMine = pageNote.userId == currentUserId
                val createdLabel = remember(pageNote.id, pageNote.createdAtMillisEffective()) {
                    StatusNoteCodec.formatRelativeTime(pageNote.createdAtMillisEffective())
                }
                val updatedLabel = remember(pageNote.id, pageNote.updatedAtMillisEffective()) {
                    StatusNoteCodec.formatRelativeTime(pageNote.updatedAtMillisEffective())
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(preset.bgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            notes.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (idx <= pagerState.currentPage) fg else fg.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(48.dp).testTag("note_viewer_close_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Note", tint = fg)
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { openAuthorProfile(pageNote) }
                                    .testTag("note_viewer_author_profile_btn"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarCircle(
                                    name = pageNote.authorName,
                                    photoUri = pageNote.authorAvatarUri,
                                    size = 40.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pageNote.authorName,
                                        color = fg,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append(createdLabel)
                                            if (pageNote.edited) {
                                                append(" • Edited ($updatedLabel)")
                                            }
                                            append(" • ${pageNote.durationHours}h • View Profile")
                                        },
                                        color = fg.copy(alpha = 0.86f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(48.dp).testTag("note_viewer_more_btn")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Note options", tint = fg)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(WaCard)
                                ) {
                                    if (pageIsMine) {
                                        DropdownMenuItem(
                                            text = { Text("Replace Note", color = WaTextPrimary, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = WaGreen) },
                                            onClick = { showMenu = false; onReplaceNote(pageNote) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit Note", color = WaTextPrimary, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = WaGold) },
                                            onClick = { showMenu = false; onEditNote(pageNote) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete Note", color = WaRed, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WaRed) },
                                            onClick = { showMenu = false; showDeleteConfirm = true }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Report Note", color = WaGold, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = WaGold) },
                                            onClick = { showMenu = false; showReportDialog = true }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Block ${pageNote.authorName}", color = WaRed, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = WaRed) },
                                            onClick = {
                                                showMenu = false
                                                onBlockAuthor(pageNote.userId)
                                                onDismiss()
                                            }
                                        )
                                    }
                                    if (canAdminModerate && !pageIsMine) {
                                        DropdownMenuItem(
                                            text = { Text("Admin Delete Note", color = WaRed, fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WaRed) },
                                            onClick = {
                                                showMenu = false
                                                onAdminDeleteNote(pageNote.id)
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Center Note Stage
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .border(3.dp, fg.copy(alpha = 0.85f), CircleShape)
                                    .padding(3.dp)
                                    .clickable { openAuthorProfile(pageNote) },
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarCircle(
                                    name = pageNote.authorName,
                                    photoUri = pageNote.authorAvatarUri,
                                    size = 80.dp
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = pageNote.authorName,
                                color = fg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = createdLabel,
                                color = fg.copy(alpha = 0.82f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (pageNote.resharedFromName.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    color = fg.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        AvatarCircle(
                                            name = pageNote.resharedFromName,
                                            photoUri = pageNote.resharedFromAvatarUri,
                                            size = 24.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "↗️ Reshared from @${pageNote.resharedFromName}",
                                            color = fg,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = pageNote.text,
                                color = fg,
                                fontSize = 24.sp,
                                lineHeight = 33.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .testTag("note_viewer_main_text")
                            )

                            if (pageNote.mood.isNotBlank() || pageNote.music.isNotBlank()) {
                                Spacer(Modifier.height(14.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (pageNote.mood.isNotBlank()) {
                                        Surface(
                                            color = fg.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                text = pageNote.mood,
                                                color = fg,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                    if (pageNote.music.isNotBlank()) {
                                        Surface(
                                            color = fg.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = pageNote.music,
                                                    color = fg,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(22.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = fg.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .clickable { showViewersSheet = true }
                                        .testTag("note_viewer_seen_by_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "Viewers", tint = fg, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Seen by ${pageNote.viewers.size} people",
                                            color = fg,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                Surface(
                                    color = fg.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .clickable { showLikersSheet = true }
                                        .testTag("note_viewer_likes_count_btn")
                                ) {
                                    Text(
                                        text = "❤️ ${pageNote.likes.size} Likes",
                                        color = if (pageLiked) WaRed else fg,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            if (pageIsMine) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = WaBackground,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .clickable { onReplaceNote(pageNote) }
                                            .testTag("note_own_replace_btn")
                                    ) {
                                        Text(
                                            text = "Replace",
                                            color = WaGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                    Surface(
                                        color = WaBackground,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .clickable { onEditNote(pageNote) }
                                            .testTag("note_own_edit_btn")
                                    ) {
                                        Text(
                                            text = "Edit Note",
                                            color = WaGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                    Surface(
                                        color = WaBackground,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .clickable { showDeleteConfirm = true }
                                            .testTag("note_own_delete_btn")
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = WaRed,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Swipe left/right for more Notes • Swipe down to close",
                            color = fg.copy(alpha = 0.72f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHeartBurst,
                        enter = fadeIn(tween(80)),
                        exit = fadeOut(tween(180)),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked Note",
                            tint = WaRed,
                            modifier = Modifier
                                .size(96.dp)
                                .graphicsLayer {
                                    scaleX = heartScale.value
                                    scaleY = heartScale.value
                                }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showReplyBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                "Reply to ${activeNote.authorName}'s Note...",
                                color = WaTextSecondary,
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WaInput,
                            unfocusedContainerColor = WaInput,
                            focusedBorderColor = WaGreen,
                            unfocusedBorderColor = WaDivider
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("note_reply_textfield")
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = WaGreen,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                if (replyText.trim().isNotEmpty()) {
                                    onReplyToNote(activeNote, replyText.trim())
                                    replyText = ""
                                    showReplyBar = false
                                }
                            }
                            .testTag("note_reply_send_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Reply",
                                tint = WaPureBlack
                            )
                        }
                    }
                }
            }

            // Solid High-Contrast Bottom Bar
            Surface(
                color = WaBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isLikedByMe) Color(0xFF2B1215) else WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable {
                                if (!isLikedByMe) showHeartBurst = true
                                onToggleLike(activeNote)
                            }
                            .testTag("note_viewer_like_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Note",
                                tint = if (isLikedByMe) WaRed else WaTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${activeNote.likes.size}",
                                color = if (isLikedByMe) WaRed else WaTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(54.dp)
                            .clickable {
                                showChatSharePicker = false
                                showReshareSheet = true
                            }
                            .testTag("note_viewer_reshare_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = "Reshare Note",
                                tint = WaGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (activeNote.reshares.isNotEmpty()) "Reshare ${activeNote.reshares.size}" else "Reshare",
                                color = WaTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        color = if (showReplyBar) WaGreen else WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable { showReplyBar = !showReplyBar }
                            .testTag("note_viewer_reply_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Reply to Note",
                                tint = if (showReplyBar) WaPureBlack else WaTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Reply",
                                color = if (showReplyBar) WaPureBlack else WaTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .clickable { showMenu = true }
                            .testTag("note_viewer_bottom_more_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = WaTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showViewersSheet) {
        StatusNoteActorListSheet(
            title = "Seen by ${activeNote.viewers.size} people",
            emptyText = "No viewers recorded yet.",
            actors = activeNote.viewers,
            onDismiss = { showViewersSheet = false }
        )
    }

    if (showLikersSheet) {
        StatusNoteActorListSheet(
            title = "❤️ ${activeNote.likes.size} Likes",
            emptyText = "No likes yet. Tap ❤️ to be the first!",
            actors = activeNote.likes,
            onDismiss = { showLikersSheet = false }
        )
    }

    if (showReshareSheet) {
        val reshareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showReshareSheet = false },
            sheetState = reshareSheetState,
            containerColor = WaCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("note_reshare_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reshare Status Note",
                        color = WaTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showReshareSheet = false },
                        modifier = Modifier.size(48.dp).testTag("note_reshare_close_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Reshare", tint = WaTextSecondary)
                    }
                }
                Text(
                    text = "\"${activeNote.text}\"",
                    color = WaTextSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                ReshareOptionTile(
                    icon = Icons.Default.Repeat,
                    iconTint = WaGreen,
                    title = "1. Reshare to My Status Note",
                    subtitle = "Copies Note with credit to @${activeNote.authorName}",
                    testTag = "note_reshare_opt_my_note",
                    onClick = {
                        onReshareToMyNote(activeNote)
                        showReshareSheet = false
                    }
                )

                ReshareOptionTile(
                    icon = Icons.Default.Public,
                    iconTint = WaGold,
                    title = "2. Reshare to My Status",
                    subtitle = "Posts Note directly to your 24h PGLOB Status",
                    testTag = "note_reshare_opt_my_status",
                    onClick = {
                        onReshareToMyStatus(activeNote)
                        showReshareSheet = false
                    }
                )

                ReshareOptionTile(
                    icon = Icons.Default.Groups,
                    iconTint = WaGreen,
                    title = "3. Share to Chat / Group / Ajo Group",
                    subtitle = "Send quoted Note card to any PGLOB conversation",
                    testTag = "note_reshare_opt_chat",
                    onClick = { showChatSharePicker = !showChatSharePicker }
                )

                AnimatedVisibility(visible = showChatSharePicker) {
                    Surface(
                        color = WaInput,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(chats, key = { "chat_${it.id}" }) { ch ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WaCard)
                                        .clickable {
                                            onShareToChat(activeNote, ch.id, ch.title, ch.memberUids)
                                            showReshareSheet = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(name = ch.title, size = 34.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = ch.title,
                                        color = WaTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("Send", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            items(contributions, key = { "ajo_${it.id}" }) { ajo ->
                                val ajoChatId = ajo.linkedChatId.ifBlank { "grp_ajo_${ajo.id}" }
                                val memberUids = ajo.members.map { it.uid }.distinct()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WaCard)
                                        .clickable {
                                            onShareToChat(activeNote, ajoChatId, "${ajo.name} (Ajo)", memberUids)
                                            showReshareSheet = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ajo.groupIconEmoji.ifBlank { "💰" }, fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "${ajo.name} (Ajo Group)",
                                        color = WaGold,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("Send", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val link = StatusNoteCodec.noteShareUrl(activeNote.id)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "PGLOB Status Note from ${activeNote.authorName}:\n\"${activeNote.text}\"\n\n$link"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share PGLOB Note"))
                            showReshareSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("note_reshare_opt_external")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("4. Share Outside PGLOB", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val link = StatusNoteCodec.noteShareUrl(activeNote.id)
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("PGLOB Note Link", link))
                            onShowMessage("Note link copied!")
                            showReshareSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("note_reshare_copy_link_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = WaTextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy Link", color = WaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(14.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = WaCard,
            title = {
                Text(
                    text = "Delete this Note?",
                    color = WaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This removes your active Status Note for everyone.",
                    color = WaTextSecondary,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteNote(activeNote.id)
                        if (notes.size <= 1) onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary),
                    modifier = Modifier.testTag("note_delete_confirm_yes")
                ) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("note_delete_confirm_cancel")
                ) {
                    Text("Cancel", color = WaTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = WaCard,
            title = { Text("Report Status Note", color = WaTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Report this Note to PGLOB Admin Moderation:", color = WaTextSecondary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportNote(activeNote, reportReason)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary)
                ) { Text("Submit Report", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel", color = WaTextSecondary) }
            }
        )
    }

    inspectedAuthorProfile?.let { target ->
        PublicUserProfileSheet(
            profile = target,
            isOwnProfile = target.userId == currentUserId,
            isBlocked = blockedUserIds.contains(target.userId),
            onDismiss = { inspectedAuthorProfile = null },
            onMessageUser = {
                inspectedAuthorProfile = null
                onOpenUserChat(target)
            },
            onReportUser = {
                onReportUser(target)
                inspectedAuthorProfile = null
            },
            onToggleBlock = {
                onBlockAuthor(target.userId)
                inspectedAuthorProfile = null
            }
        )
    }
}

@Composable
private fun ReshareOptionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = WaInput,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = WaTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusNoteActorListSheet(
    title: String,
    emptyText: String,
    actors: List<StatusNoteActor>,
    onDismiss: () -> Unit
) {
    val actorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = actorSheetState, containerColor = WaCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("note_actor_list_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = WaTextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("note_actor_sheet_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close List", tint = WaTextSecondary)
                }
            }
            if (actors.isEmpty()) {
                Text(
                    text = emptyText,
                    color = WaTextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(actors, key = { "${it.uid}_${it.timestampMillis}" }) { actor ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(
                                name = actor.name.ifBlank { "Member" },
                                photoUri = actor.photoUri,
                                size = 42.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = actor.name.ifBlank { "PGLOB Member" },
                                    color = WaTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StatusNoteCodec.formatRelativeTime(actor.timestampMillis),
                                    color = WaTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}
