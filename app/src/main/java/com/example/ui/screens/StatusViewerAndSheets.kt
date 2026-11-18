package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.ChatThread
import com.example.data.model.PublicProfile
import com.example.data.model.StatusPost
import com.example.data.model.StatusViewerRecord
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobSurfaceElevated
import com.example.ui.theme.PglobWhite
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
import com.example.util.StatusQuoteCodec
import kotlinx.coroutines.delay
import java.io.File

private val STATUS_REACTIONS = listOf("❤️", "😂", "😮", "😢", "😡", "👍")
private val QUICK_REPLY_EMOJIS = listOf("❤️", "😂", "😍", "🔥", "👑", "🙏", "🎉", "👍", "😭", "💎")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatusViewerDialog(
    statuses: List<StatusPost>,
    initialStatusId: String,
    statusReactions: Map<String, String>,
    statusFavoritesIds: Set<String> = emptySet(),
    chats: List<ChatThread>,
    publicProfiles: List<PublicProfile>,
    viewModel: PglobViewModel,
    initialOpenViewers: Boolean = false,
    onDismiss: () -> Unit,
    onOpenChatById: (String) -> Unit
) {
    val startIndex = remember(statuses, initialStatusId) {
        statuses.indexOfFirst { it.id == initialStatusId }.coerceAtLeast(0)
    }
    var currentIndex by remember(initialStatusId) { mutableIntStateOf(startIndex) }
    val safeStatuses = statuses.ifEmpty { listOf(StatusPost(id = "empty", displayName = "PGLOB APP", caption = "No active Status")) }
    val currentStatus = safeStatuses[currentIndex.coerceIn(0, safeStatuses.lastIndex)]
    val activeReaction = statusReactions[currentStatus.id]
    val isLiked = activeReaction != null
    val isFavorited = statusFavoritesIds.contains(currentStatus.id)

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val replyFocusRequester = remember { FocusRequester() }

    var showReactionBar by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var showEmojiStrip by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(initialOpenViewers) }
    var showReshareSheet by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var isVideoPlaying by remember { mutableStateOf(true) }
    var progress by remember(currentStatus.id) { mutableFloatStateOf(0f) }

    // Center big heart / reaction pop animation
    var centerPopEmoji by remember { mutableStateOf<String?>(null) }
    val popScale = remember { Animatable(0f) }
    val popAlpha = remember { Animatable(0f) }
    var popTrigger by remember { mutableIntStateOf(0) }

    // Voice note reply recording
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceSeconds by remember { mutableIntStateOf(0) }
    var activeRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var activeAudioFile by remember { mutableStateOf<File?>(null) }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            viewModel.showToast("Allow Microphone in Settings to send Voice Note replies")
        }
    }

    fun startVoiceReplyRecording() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val outFile = File.createTempFile("pglob_st_voice_", ".m4a", context.cacheDir)
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
        } catch (_: Exception) {
            // Fallback simulated voice note if audio hardware unavailable in virtual device
            val fallbackFile = File.createTempFile("pglob_st_voice_", ".m4a", context.cacheDir)
            activeAudioFile = fallbackFile
            activeRecorder = null
            isRecordingVoice = true
        }
    }

    fun finishAndSendVoiceReply() {
        if (!isRecordingVoice) return
        try {
            activeRecorder?.stop()
            activeRecorder?.release()
        } catch (_: Exception) {}
        activeRecorder = null
        isRecordingVoice = false
        val secs = voiceSeconds.coerceAtLeast(1)
        val path = activeAudioFile?.absolutePath.orEmpty()
        viewModel.replyToStatus(
            status = currentStatus,
            replyText = "🎙️ Voice Reply (${secs}s)",
            messageType = "VOICE",
            mediaUri = path
        )
        showReplyInput = false
        showEmojiStrip = false
        keyboardController?.hide()
    }

    DisposableEffect(Unit) {
        onDispose {
            try { activeRecorder?.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceSeconds = 0
            while (isRecordingVoice) {
                delay(1000L)
                voiceSeconds++
            }
        }
    }

    LaunchedEffect(currentStatus.id) {
        viewModel.recordStatusView(currentStatus)
        progress = 0f
    }

    LaunchedEffect(currentStatus.id, showReplyInput, showReactionBar, showViewersSheet, showReshareSheet, showTopMenu) {
        val paused = showReplyInput || showReactionBar || showViewersSheet || showReshareSheet || showTopMenu
        while (!paused && progress < 1f) {
            delay(60L)
            progress = (progress + 0.01f).coerceAtMost(1f)
            if (progress >= 1f) {
                if (currentIndex < safeStatuses.lastIndex) {
                    currentIndex++
                }
            }
        }
    }

    LaunchedEffect(popTrigger) {
        if (popTrigger > 0 && centerPopEmoji != null) {
            popScale.snapTo(0.25f)
            popAlpha.snapTo(1f)
            popScale.animateTo(
                targetValue = 1.35f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            popScale.animateTo(1.0f, animationSpec = tween(140))
            delay(280L)
            popAlpha.animateTo(0f, animationSpec = tween(260))
            centerPopEmoji = null
        }
    }

    LaunchedEffect(showReplyInput) {
        if (showReplyInput) {
            delay(80L)
            try {
                replyFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler {
            when {
                showReplyInput -> {
                    showReplyInput = false
                    showEmojiStrip = false
                    keyboardController?.hide()
                }
                showReactionBar -> showReactionBar = false
                else -> onDismiss()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WaPureBlack)
                .statusBarsPadding()
                .imePadding()
        ) {
            // TOP BAR: Thin 2dp white progress bars + [←] [40dp Pic] [Name 16sp + Time 13sp Grey] [⋮]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaPureBlack)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    safeStatuses.forEachIndexed { idx, _ ->
                        val segFill = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> progress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color.White.copy(alpha = 0.28f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(segFill)
                                    .background(WaTextPrimary)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp).testTag("close_status_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WaTextPrimary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    AvatarCircle(
                        name = currentStatus.displayName,
                        photoUri = currentStatus.avatarUri,
                        size = 40.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStatus.displayName,
                            color = WaTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = currentStatus.timeLabel.ifBlank { "2m ago" },
                                color = WaTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            if (currentStatus.privacyMode == "PRIVATE") {
                                Text(
                                    text = "• Private",
                                    color = WaGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showViewersSheet = true },
                        modifier = Modifier.testTag("status_viewers_button")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Viewed by", tint = WaTextPrimary, modifier = Modifier.size(22.dp))
                    }
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = WaTextPrimary, modifier = Modifier.size(22.dp))
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            modifier = Modifier.background(WaCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Viewed by (${currentStatus.viewsCount})", color = WaTextPrimary) },
                                onClick = { showTopMenu = false; showViewersSheet = true }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isFavorited) "Remove Favorite" else "Save to Favorites", color = WaGreen) },
                                onClick = { showTopMenu = false; viewModel.toggleStatusFavorite(currentStatus) }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy Status Link", color = WaTextPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    val link = StatusQuoteCodec.statusShareUrl(currentStatus.id, currentStatus.username, currentStatus.displayName)
                                    copyToClipboard(context, link)
                                    viewModel.showToast("Link Copied ✔️")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report Status", color = WaRed) },
                                onClick = {
                                    showTopMenu = false
                                    viewModel.submitReport("STATUS", currentStatus.id, "Reported from Status Viewer")
                                }
                            )
                        }
                    }
                }

                if (currentStatus.resharedFromName.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, WaGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    ) {
                        val handle = currentStatus.resharedFromHandle.ifBlank { currentStatus.resharedFromName.replace(" ", "") }
                        Text(
                            text = "🔁 Reshared from @$handle",
                            color = WaGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // MAIN STATUS MEDIA STAGE (Tap left/right to navigate statuses, double-tap to Like ❤️)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(PglobBlack)
                    .pointerInput(currentStatus.id, safeStatuses.size) {
                        detectTapGestures(
                            onDoubleTap = {
                                centerPopEmoji = "❤️"
                                popTrigger++
                                if (!isLiked) viewModel.toggleLikeStatus(currentStatus, "❤️", forceReaction = true)
                            },
                            onTap = { offset ->
                                showReactionBar = false
                                if (showReplyInput) {
                                    showReplyInput = false
                                    keyboardController?.hide()
                                } else if (offset.x < size.width * 0.30f && currentIndex > 0) {
                                    currentIndex--
                                } else if (offset.x > size.width * 0.70f && currentIndex < safeStatuses.lastIndex) {
                                    currentIndex++
                                } else if (currentStatus.mediaType == "VIDEO") {
                                    isVideoPlaying = !isVideoPlaying
                                }
                            }
                        )
                    }
            ) {
                when (currentStatus.mediaType) {
                    "IMAGE" -> {
                        if (currentStatus.mediaUri.isNotBlank()) {
                            AsyncImage(
                                model = Uri.parse(currentStatus.mediaUri),
                                contentDescription = currentStatus.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(PglobDarkGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷 Photo Status", color = PglobGold, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        if (currentStatus.caption.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(PglobBlack)
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = currentStatus.caption,
                                    color = PglobWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    "VIDEO" -> {
                        if (currentStatus.mediaUri.isNotBlank()) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(Uri.parse(currentStatus.mediaUri))
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            if (isVideoPlaying) start()
                                        }
                                    }
                                },
                                update = { vv ->
                                    if (isVideoPlaying && !vv.isPlaying) vv.start()
                                    else if (!isVideoPlaying && vv.isPlaying) vv.pause()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF101318))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Surface(
                                        color = PglobDarkGrey,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(92.dp)
                                            .border(3.dp, PglobGold, CircleShape)
                                            .clickable { isVideoPlaying = !isVideoPlaying }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play or pause video status",
                                                tint = PglobGold,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isVideoPlaying) "🎬 VIDEO STATUS • PLAYING" else "🎬 VIDEO STATUS • PAUSED",
                                        color = PglobGold,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = currentStatus.caption,
                                        color = PglobWhite,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (currentStatus.mediaUri.isNotBlank() && currentStatus.caption.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(PglobBlack)
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = currentStatus.caption,
                                    color = PglobWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    else -> {
                        // TEXT STATUS — Solid #000000 stage with clean centered card
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(WaPureBlack)
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentStatus.caption,
                                    color = WaTextPrimary,
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    }
                }

                // BIG CENTER HEART / REACTION POP BOUNCE ANIMATION
                if (centerPopEmoji != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = centerPopEmoji!!,
                            fontSize = 96.sp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = popScale.value
                                scaleY = popScale.value
                                alpha = popAlpha.value
                            }
                        )
                    }
                }

                // LONG-PRESS 6 REACTIONS PICKER BAR
                androidx.compose.animation.AnimatedVisibility(
                    visible = showReactionBar,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 52.dp)
                ) {
                    Surface(
                        color = PglobDarkGrey,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .border(2.dp, PglobGold, RoundedCornerShape(28.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            STATUS_REACTIONS.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (activeReaction == emoji) PglobGold else PglobBlack)
                                        .clickable {
                                            showReactionBar = false
                                            centerPopEmoji = emoji
                                            popTrigger++
                                            viewModel.toggleLikeStatus(currentStatus, emoji, forceReaction = true)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 26.sp)
                                }
                            }
                        }
                    }
                }
            }

            // REPLY INPUT MODE VS SOLID 80dp BOTTOM BAR
            if (showReplyInput) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PglobBlack)
                        .navigationBarsPadding()
                ) {
                    if (currentStatus.userId == viewModel.currentUserId) {
                        Text(
                            text = "📝 Replying to your own Status (Saved as Self Note)",
                            color = PglobGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
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
                            QUICK_REPLY_EMOJIS.forEach { em ->
                                Text(
                                    text = em,
                                    fontSize = 28.sp,
                                    modifier = Modifier.clickable {
                                        replyText += em
                                    }
                                )
                            }
                        }
                    }

                    if (isRecordingVoice) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PglobDarkGrey)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(PglobRed))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Recording Voice Reply ${voiceSeconds}s...",
                                color = PglobWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = {
                                try { activeRecorder?.stop(); activeRecorder?.release() } catch (_: Exception) {}
                                activeRecorder = null
                                isRecordingVoice = false
                            }) {
                                Text("Cancel", color = PglobRed, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { finishAndSendVoiceReply() },
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                            ) {
                                Text("Send", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PglobBlack)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showEmojiStrip = !showEmojiStrip },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PglobDarkGrey)
                            ) {
                                Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji", tint = PglobGold, modifier = Modifier.size(26.dp))
                            }

                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = {
                                    Text(
                                        text = "Reply to ${currentStatus.displayName}...",
                                        color = PglobGreyText,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                textStyle = TextStyle(
                                    color = PglobWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(replyFocusRequester)
                                    .testTag("status_reply_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PglobDarkGrey,
                                    unfocusedContainerColor = PglobDarkGrey,
                                    focusedBorderColor = PglobGold,
                                    unfocusedBorderColor = PglobGreyText
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )

                            // Hold Mic for Voice Note Reply (also supports tap fallback)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PglobDarkGrey)
                                    .border(1.5.dp, PglobGold, CircleShape)
                                    .pointerInput(currentStatus.id) {
                                        detectTapGestures(
                                            onPress = {
                                                startVoiceReplyRecording()
                                                tryAwaitRelease()
                                                finishAndSendVoiceReply()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Hold for Voice Reply", tint = PglobGold, modifier = Modifier.size(26.dp))
                            }

                            Button(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        viewModel.replyToStatus(
                                            status = currentStatus,
                                            replyText = replyText
                                        )
                                        replyText = ""
                                        showReplyInput = false
                                        showEmojiStrip = false
                                        keyboardController?.hide()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("status_reply_send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Reply", modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            } else {
                // ONE-ROW 64dp BOTTOM BAR: [ Reply Pill ] + 4 Circle Buttons (👍, ❤️, 🔁, ♡)
                val isPrivateStatus = currentStatus.privacyMode == "PRIVATE"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaPureBlack)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reply Pill
                        Surface(
                            color = WaInput,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    showReactionBar = false
                                    showReplyInput = true
                                }
                                .testTag("status_reply_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Reply",
                                    color = WaTextSecondary,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Circle 1: 👍 Quick Like
                        Surface(
                            color = if (activeReaction == "👍") WaGreen else WaInput,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    centerPopEmoji = "👍"
                                    popTrigger++
                                    viewModel.toggleLikeStatus(currentStatus, "👍", forceReaction = activeReaction != "👍")
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Thumbs up",
                                    tint = if (activeReaction == "👍") WaBackground else WaTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Circle 2: ❤️ Heart (tap = Like/Unlike, long-press = 6 reactions)
                        Surface(
                            color = WaInput,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        if (!isLiked) {
                                            centerPopEmoji = "❤️"
                                            popTrigger++
                                        }
                                        viewModel.toggleLikeStatus(currentStatus, "❤️", forceReaction = false)
                                    },
                                    onLongClick = { showReactionBar = true }
                                )
                                .testTag("status_like_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (activeReaction != null && activeReaction != "❤️" && activeReaction != "👍") {
                                    Text(activeReaction, fontSize = 20.sp)
                                } else {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like status",
                                        tint = if (isLiked) WaRed else WaTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Circle 3: 🔁 Reshare
                        Surface(
                            color = WaInput,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    showReactionBar = false
                                    if (isPrivateStatus) {
                                        viewModel.showToast("This Status cannot be reshared - Private")
                                    } else {
                                        showReshareSheet = true
                                    }
                                }
                                .testTag("status_reshare_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPrivateStatus) Icons.Default.Lock else Icons.Default.Repeat,
                                    contentDescription = "Reshare status",
                                    tint = if (isPrivateStatus) WaTextSecondary else WaTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Circle 4: ♡ / ♥ Save Favorite
                        Surface(
                            color = if (isFavorited) WaGreen.copy(alpha = 0.22f) else WaInput,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.toggleStatusFavorite(currentStatus) }
                                .testTag("status_favorite_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isFavorited) "♥" else "♡",
                                    color = if (isFavorited) WaGold else WaTextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showViewersSheet) {
        val insights = remember(currentStatus, statusReactions[currentStatus.id]) {
            viewModel.getStatusInsightsTabs(currentStatus)
        }
        StatusViewersBottomSheet(
            status = currentStatus,
            insights = insights,
            onDismiss = { showViewersSheet = false }
        )
    }

    if (showReshareSheet) {
        ReshareStatusBottomSheet(
            status = currentStatus,
            chats = chats,
            publicProfiles = publicProfiles,
            currentUserId = viewModel.currentUserId,
            onReshareToMyStatus = { caption ->
                viewModel.reshareStatusToMyStatus(currentStatus, caption)
                showReshareSheet = false
            },
            onSendToChat = { targetChatId, targetTitle, targetMembers ->
                viewModel.sendStatusToChat(
                    status = currentStatus,
                    targetChatId = targetChatId,
                    targetTitle = targetTitle,
                    targetMemberUids = targetMembers,
                    onSentOpenChat = { openedId ->
                        showReshareSheet = false
                        onDismiss()
                        onOpenChatById(openedId)
                    }
                )
            },
            onCopyLink = { url ->
                copyToClipboard(context, url)
                viewModel.showToast("Link Copied ✔️")
            },
            onShareExternal = { shareText ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share PGLOB APP Status"))
            },
            onDismiss = { showReshareSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusViewersBottomSheet(
    status: StatusPost,
    insights: PglobViewModel.StatusInsightsTabs,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Liked") }
    val tabs = listOf(
        "Liked" to insights.liked,
        "Replied" to insights.replied,
        "Reshared" to insights.reshared,
        "Viewed" to insights.viewed
    )
    val currentRows = tabs.firstOrNull { it.first == selectedTab }?.second.orEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WaCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Viewed by",
                color = WaTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (label, rows) ->
                    val active = selectedTab == label
                    Surface(
                        color = if (active) WaGreen else WaInput,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { selectedTab = label }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$label (${rows.size})",
                                color = if (active) WaBackground else WaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (currentRows.isEmpty()) {
                Text(
                    text = "No $selectedTab activity yet.",
                    color = WaTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentRows) { rec ->
                        ViewerRowItem(record = rec)
                        HorizontalDivider(color = WaDivider, thickness = 1.dp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ViewerRowItem(record: StatusViewerRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarCircle(name = record.name, photoUri = record.photoUri, size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.name,
                color = WaTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = record.viewedAt,
                color = WaTextSecondary,
                fontSize = 12.sp
            )
        }
        if (record.reaction.isNotBlank()) {
            Text(
                text = record.reaction,
                color = WaTextPrimary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReshareStatusBottomSheet(
    status: StatusPost,
    chats: List<ChatThread>,
    publicProfiles: List<PublicProfile>,
    currentUserId: String,
    onReshareToMyStatus: (String) -> Unit,
    onSendToChat: (targetChatId: String, targetTitle: String, targetMembers: List<String>) -> Unit,
    onCopyLink: (String) -> Unit,
    onShareExternal: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf("MENU") } // MENU, MY_STATUS_PREVIEW, CHAT_PICKER, CHAT_PREVIEW
    var extraCaption by remember { mutableStateOf("") }
    var chatSearch by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }
    var selectedTarget by remember { mutableStateOf<Triple<String, String, List<String>>?>(null) }

    val origOwnerName = status.resharedFromName.ifBlank { status.displayName }
    val origHandle = status.resharedFromHandle.ifBlank {
        status.username.ifBlank { origOwnerName.replace(" ", "") }
    }
    val shareUrl = remember(status.id, status.username, status.displayName) {
        StatusQuoteCodec.statusShareUrl(status.id, status.username, status.displayName)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (mode) {
                "MENU" -> {
                    Text(
                        text = "Reshare Status",
                        color = PglobWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Option 1: 📤 Reshare to My Status (BIG button GOLD)
                    Button(
                        onClick = { mode = "MY_STATUS_PREVIEW" },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("reshare_to_my_status_option")
                    ) {
                        Text(
                            text = "📤 Reshare to My Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Option 2: 💬 Send to Chat
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(1.5.dp, PglobGold.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                            .clickable { mode = "CHAT_PICKER" }
                            .testTag("reshare_send_to_chat_option")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💬 Send to Chat", color = PglobWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Option 3: 🔗 Copy Link
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .clickable {
                                copiedNotice = true
                                onCopyLink(shareUrl)
                            }
                            .testTag("reshare_copy_link_option")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PglobGold)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (copiedNotice) "Link Copied ✔️" else "🔗 Copy Link",
                                    color = if (copiedNotice) PglobGreen else PglobWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = shareUrl,
                                color = PglobGreyText,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }

                    // Option 4: 📲 Share to Other Apps
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .clickable {
                                onShareExternal("Check out @$origHandle's Status on PGLOB APP: \"${status.caption}\" — $shareUrl")
                            }
                            .testTag("reshare_external_option")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = PglobGold)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "📲 Share to Other Apps (Messages, IG, FB, TikTok)",
                                color = PglobWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                "MY_STATUS_PREVIEW" -> {
                    Text(
                        text = "Reshare to My Status",
                        color = PglobWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Tag on top: "Reshared from @ZaraBella" 16sp BOLD GOLD + Original Status below
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, PglobGold, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Reshared from @$origHandle",
                                color = PglobGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            StatusMediaPreviewCard(status = status)
                        }
                    }

                    OutlinedTextField(
                        value = extraCaption,
                        onValueChange = { extraCaption = it },
                        placeholder = {
                            Text("Add Caption...", color = PglobGreyText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        },
                        textStyle = TextStyle(color = PglobWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PglobBlack,
                            unfocusedContainerColor = PglobBlack,
                            focusedBorderColor = PglobGold,
                            unfocusedBorderColor = PglobGreyText
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { mode = "MENU" }) {
                            Text("Back", color = PglobWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onReshareToMyStatus(extraCaption) },
                            colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("post_reshare_my_status_button")
                        ) {
                            Text("Post to My Status", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                "CHAT_PICKER" -> {
                    Text(
                        text = "Send Status to Chat",
                        color = PglobWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = chatSearch,
                        onValueChange = { chatSearch = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PglobGold) },
                        placeholder = { Text("Search chats & groups...", color = PglobGreyText, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(color = PglobWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PglobBlack,
                            unfocusedContainerColor = PglobBlack,
                            focusedBorderColor = PglobGold,
                            unfocusedBorderColor = PglobGreyText
                        )
                    )

                    val targets = remember(chats, publicProfiles, chatSearch) {
                        val existing = chats.map { c -> Triple(c.id, c.title, c.memberUids) }
                        val extras = publicProfiles
                            .filter { it.userId != currentUserId }
                            .map { p ->
                                val dmId = "dm_${listOf(currentUserId, p.userId).sorted().joinToString("_")}".take(100)
                                Triple(dmId, "${p.displayName} ${p.countryFlag}", listOf(currentUserId, p.userId))
                            }
                        val fallbackContacts = listOf(
                            Triple("dm_${listOf(currentUserId, "user_zara123").sorted().joinToString("_")}", "Zara Bella 🇳🇬", listOf(currentUserId, "user_zara123")),
                            Triple("dm_${listOf(currentUserId, "amina_ng").sorted().joinToString("_")}", "Amina Yusuf 🇳🇬", listOf(currentUserId, "amina_ng")),
                            Triple("dm_${listOf(currentUserId, "marcus_us").sorted().joinToString("_")}", "Marcus Vance 🇺🇸", listOf(currentUserId, "marcus_us"))
                        )
                        (existing + extras + fallbackContacts)
                            .distinctBy { it.first }
                            .filter { chatSearch.isBlank() || it.second.contains(chatSearch, ignoreCase = true) }
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(targets) { item ->
                            Surface(
                                color = PglobBlack,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTarget = item
                                        mode = "CHAT_PREVIEW"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(name = item.second, size = 42.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = item.second,
                                        color = PglobWhite,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("Select", color = PglobGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    TextButton(onClick = { mode = "MENU" }) {
                        Text("Back", color = PglobGold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }

                "CHAT_PREVIEW" -> {
                    val target = selectedTarget
                    Text(
                        text = "Send to ${target?.second ?: "Chat"}",
                        color = PglobWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = PglobBlack,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PglobGold, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Forwarded from $origOwnerName Status",
                                color = PglobGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            StatusMediaPreviewCard(status = status)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { mode = "CHAT_PICKER" }) {
                            Text("Back", color = PglobWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (target != null) {
                                    onSendToChat(target.first, target.second, target.third)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                            modifier = Modifier.weight(1f).height(54.dp)
                        ) {
                            Text("Send", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatusMediaPreviewCard(status: StatusPost) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (status.mediaType == "IMAGE" && status.mediaUri.isNotBlank()) {
            AsyncImage(
                model = Uri.parse(status.mediaUri),
                contentDescription = status.caption,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else if (status.mediaType == "VIDEO") {
            Surface(
                color = PglobSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(90.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PglobGold, modifier = Modifier.size(36.dp))
                    Text("🎬 Video Status", color = PglobGold, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        if (status.caption.isNotBlank()) {
            Text(
                text = status.caption,
                color = PglobWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cm?.setPrimaryClip(ClipData.newPlainText("PGLOB Status Link", text))
}
