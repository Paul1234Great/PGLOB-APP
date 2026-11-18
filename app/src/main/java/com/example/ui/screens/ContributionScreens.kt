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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AppAdminPermission
import com.example.data.model.ContributionGroup
import com.example.data.model.ContributionMember
import com.example.data.model.ContributionStatus
import com.example.data.model.ContributionType
import com.example.data.model.GroupSubAdminPermissions
import com.example.data.model.PayoutOrderType
import com.example.data.model.PublicProfile
import com.example.ui.ContributionAdminCodec
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.PinOrBiometricConfirmSheet
import com.example.ui.components.createTempCameraUri
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
import com.example.util.FlagSecureEffect
import java.util.Locale

@Composable
fun ContributionsListScreen(
    viewModel: PglobViewModel,
    onBack: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onCreateNew: () -> Unit
) {
    BackHandler { onBack() }

    val groups by viewModel.contributions.collectAsStateWithLifecycle()
    var filterTab by remember { mutableStateOf("All") }

    val filtered = remember(groups, filterTab) {
        when (filterTab) {
            "Active" -> groups.filter { it.status == ContributionStatus.ACTIVE }
            "My Turn" -> groups.filter { it.currentTurnMember?.uid == viewModel.currentUserId }
            "Completed" -> groups.filter { it.status == ContributionStatus.COMPLETED }
            else -> groups
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Clean PGLOB Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackground)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("contributions_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Group Contributions",
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    )
                    Text(
                        "Ajo / Esusu secured by PGLOB Escrow",
                        color = WaTextSecondary,
                        fontSize = 13.sp
                    )
                }
                TextButton(
                    onClick = onCreateNew,
                    modifier = Modifier.testTag("contributions_header_new_btn")
                ) {
                    Text("+ New", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
            HorizontalDivider(color = WaDivider, thickness = 1.dp)

            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "My Turn", "Completed").forEach { tab ->
                    val selected = filterTab == tab
                    Surface(
                        color = if (selected) WaGreen.copy(alpha = 0.2f) else WaInput,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { filterTab = tab }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                            Text(
                                tab,
                                color = if (selected) WaGreen else WaTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Surface(
                            color = WaCard,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("No contribution circles here yet", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Tap + Create New to start a trusted Daily, Weekly, or Monthly savings circle.", color = WaTextSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { group ->
                        ContributionSummaryCard(
                            group = group,
                            isMyTurn = group.currentTurnMember?.uid == viewModel.currentUserId,
                            onClick = { onOpenGroup(group.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateNew,
            containerColor = WaGreen,
            contentColor = WaPureBlack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_contribution_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Contribution Group")
        }
    }
}

@Composable
private fun ContributionSummaryCard(
    group: ContributionGroup,
    isMyTurn: Boolean,
    onClick: () -> Unit
) {
    val progress = if (group.members.isEmpty()) 0f else group.paidMembersCount.toFloat() / group.members.size.toFloat()
    val statusColor = when {
        isMyTurn && group.status == ContributionStatus.ACTIVE -> WaGold
        group.status == ContributionStatus.ACTIVE -> WaGreen
        else -> WaTextSecondary
    }
    val statusLabel = when {
        isMyTurn && group.status == ContributionStatus.ACTIVE -> "My Turn"
        else -> group.status.label
    }

    Surface(
        color = WaCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("contribution_card_${group.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupPictureBadge(
                    name = group.name,
                    photoUri = group.groupPhotoUri,
                    emoji = group.groupIconEmoji,
                    size = 46.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, color = WaTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text(group.purpose, color = WaTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                "${group.currency}${formatMoney(group.amountPerRound)} / ${group.type.label}  •  Pot ${group.currency}${formatMoney(group.totalPotPerRound)}",
                color = WaGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = WaGreen,
                trackColor = WaInput
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${group.paidMembersCount}/${group.members.size} Paid • Next: ${group.currentTurnMember?.name?.substringBefore(" ") ?: "TBD"} (${group.daysRemaining}d left)",
                    color = WaTextSecondary,
                    fontSize = 12.sp
                )
                Text("🔒 PGLOB Escrow", color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContributionWizardSheet(
    viewModel: PglobViewModel,
    prefillTitle: String = "",
    linkedChatId: String = "",
    onDismiss: () -> Unit,
    onCreated: (ContributionGroup) -> Unit
) {
    val context = LocalContext.current
    val publicProfiles by viewModel.publicProfiles.collectAsStateWithLifecycle()

    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf(prefillTitle) }
    var type by remember { mutableStateOf(ContributionType.WEEKLY) }
    var purpose by remember { mutableStateOf("Weekly savings & member support") }
    var groupPhotoUri by remember { mutableStateOf("") }
    var groupIconEmoji by remember { mutableStateOf("💰") }
    var rulesText by remember {
        mutableStateOf(
            "1. Pay before due time each cycle.\n2. All money stays locked in PGLOB Escrow until release.\n3. Group Admin approves payment proof within 48h or PGLOB auto-approves Wallet payments.\n4. Respect turn order & late fee policy."
        )
    }
    var allowSubAdminApproveReg by remember { mutableStateOf(false) }
    var tempCamUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) groupPhotoUri = uri.toString() }

    val cameraPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok && tempCamUri != null) groupPhotoUri = tempCamUri.toString() }

    val camPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val u = createTempCameraUri(context)
            tempCamUri = u
            cameraPicker.launch(u)
        } else {
            viewModel.showToast("Camera permission needed to take group photo")
        }
    }

    var amountStr by remember { mutableStateOf("10000") }
    var currency by remember { mutableStateOf("₦") }
    var maxMembers by remember { mutableIntStateOf(6) }
    var startDate by remember { mutableStateOf("Fri 21 Nov") }
    var endDate by remember { mutableStateOf("Fri 02 Jan") }
    var payoutOrder by remember { mutableStateOf(PayoutOrderType.ROTATION) }

    var requireLateFee by remember { mutableStateOf(true) }
    var lateFeeStr by remember { mutableStateOf("500") }
    var mustPayBeforeTurn by remember { mutableStateOf(true) }
    var adminCanRemoveUnpaid by remember { mutableStateOf(true) }
    var screenshotBlock by remember { mutableStateOf(true) }
    var memberQuery by remember { mutableStateOf("") }
    val selectedUids = remember { mutableStateMapOf<String, Boolean>() }

    val candidateProfiles = remember(publicProfiles, viewModel.currentUserId) {
        val base = publicProfiles.filter { it.userId != viewModel.currentUserId }
        if (base.isNotEmpty()) base else listOf(
            PublicProfile("m_amina", "Amina Yusuf", "amina_lagos", countryFlag = "🇳🇬", usernameVerified = true),
            PublicProfile("m_blessing", "Blessing Gold", "blessing_gold", countryFlag = "🇳🇬", usernameVerified = true),
            PublicProfile("m_marcus", "Marcus Vance", "marcus_nyc", countryFlag = "🇺🇸", usernameVerified = true),
            PublicProfile("m_kwame", "Kwame Mensah", "kwame_gh", countryFlag = "🇬🇭", usernameVerified = false),
            PublicProfile("m_priya", "Priya Sharma", "priya_tech", countryFlag = "🇮🇳", usernameVerified = true)
        )
    }

    val amountVal = amountStr.toDoubleOrNull() ?: 0.0
    val selectedCount = selectedUids.values.count { it } + 1
    val previewPot = amountVal * maxMembers.coerceAtLeast(selectedCount)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WaCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "New Contribution • Step $step of 3",
                    color = WaTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium
                )
                Text("${(step * 100 / 3)}%", color = WaGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                color = WaGreen,
                trackColor = WaInput
            )

            when (step) {
                1 -> {
                    Text("Group Picture (Photo / Emoji / Initial)", color = WaTextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GroupPictureBadge(
                            name = name.ifBlank { "P" },
                            photoUri = groupPhotoUri,
                            emoji = groupIconEmoji,
                            size = 56.dp
                        )
                        OutlinedButton(
                            onClick = {
                                galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery", color = WaGreen, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    val u = createTempCameraUri(context)
                                    tempCamUri = u
                                    cameraPicker.launch(u)
                                } else {
                                    camPerm.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Camera", color = WaGreen, fontSize = 12.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("💰", "💎", "🚀", "🏠", "🤝", "🌍", "").forEach { em ->
                            val sel = groupPhotoUri.isBlank() && groupIconEmoji == em
                            Surface(
                                color = if (sel) WaGreen else WaInput,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable {
                                    groupPhotoUri = ""
                                    groupIconEmoji = em
                                }
                            ) {
                                Text(
                                    if (em.isBlank()) "First Letter" else em,
                                    color = if (sel) WaPureBlack else WaTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contribution Group Name") },
                        placeholder = { Text("e.g. Benin Ladies Ajo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Cycle Type", color = WaTextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ContributionType.entries.forEach { t ->
                            val active = type == t
                            Surface(
                                color = if (active) WaGreen else WaInput,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).clickable { type = t }
                            ) {
                                Text(
                                    t.label,
                                    color = if (active) WaPureBlack else WaTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("Purpose / Description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rulesText,
                        onValueChange = { rulesText = it },
                        label = { Text("Group Rules (Members must agree before paying)") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RuleToggleRow(
                        label = "Allow Sub-Admin to approve new member registration (Default OFF)",
                        checked = allowSubAdminApproveReg
                    ) { allowSubAdminApproveReg = it }
                }

                2 -> {
                    Text("Currency", color = WaTextSecondary, fontSize = 13.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    ) {
                        listOf("₦", "$", "£", "₵", "KSh", "₹", "R$").forEach { cur ->
                            val sel = currency == cur
                            Surface(
                                color = if (sel) WaGreen else WaInput,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { currency = cur }
                            ) {
                                Text(
                                    cur,
                                    color = if (sel) WaPureBlack else WaTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter(Char::isDigit) },
                        label = { Text("Amount per Round ($currency) - Locked after start") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Members in Circle: $maxMembers", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = maxMembers.toFloat(),
                        onValueChange = { maxMembers = it.toInt().coerceIn(2, 50) },
                        valueRange = 2f..50f,
                        colors = SliderDefaults.colors(thumbColor = WaGreen, activeTrackColor = WaGreen)
                    )

                    Surface(color = WaBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Total Pot Each Round", color = WaTextSecondary, fontSize = 12.sp)
                            Text(
                                "$currency${formatMoney(previewPot)}",
                                color = WaGreen,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text("Auto-calculated: $currency${formatMoney(amountVal)} × $maxMembers members", color = WaTextSecondary, fontSize = 12.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Payout Order", color = WaTextSecondary, fontSize = 13.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PayoutOrderType.entries.forEach { po ->
                            val sel = payoutOrder == po
                            Surface(
                                color = if (sel) WaGreen.copy(alpha = 0.16f) else WaInput,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (sel) WaGreen else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { payoutOrder = po }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(po.label, color = WaTextPrimary, fontSize = 14.sp)
                                    if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = WaGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text("Escrow Rules & Protection", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    RuleToggleRow("Late Payment Fee", requireLateFee) { requireLateFee = it }
                    if (requireLateFee) {
                        OutlinedTextField(
                            value = lateFeeStr,
                            onValueChange = { lateFeeStr = it.filter(Char::isDigit) },
                            label = { Text("Late Fee ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    RuleToggleRow("Must pay round before collecting turn", mustPayBeforeTurn) { mustPayBeforeTurn = it }
                    RuleToggleRow("Admin can remove unpaid member", adminCanRemoveUnpaid) { adminCanRemoveUnpaid = it }
                    RuleToggleRow("Block screenshots inside contribution", screenshotBlock) { screenshotBlock = it }

                    HorizontalDivider(color = WaDivider)
                    Text("Invite Contacts & Members", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = memberQuery,
                        onValueChange = { memberQuery = it },
                        placeholder = { Text("Search contacts to add...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val visibleProfiles = candidateProfiles.filter {
                        memberQuery.isBlank() || it.displayName.contains(memberQuery, true) || it.username.contains(memberQuery, true)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        visibleProfiles.take(6).forEach { prof ->
                            val checked = selectedUids[prof.userId] == true
                            Surface(
                                color = WaInput,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().clickable { selectedUids[prof.userId] = !checked }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { selectedUids[prof.userId] = it },
                                        colors = CheckboxDefaults.colors(checkedColor = WaGreen, uncheckedColor = WaTextSecondary)
                                    )
                                    Text("${prof.countryFlag} ${prof.displayName}", color = WaTextPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                                    Text("@${prof.username}", color = WaTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val inviteLink = "https://pglob.app/ajo/${name.trim().lowercase().replace(" ", "_")}"
                        OutlinedButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Ajo Invite", inviteLink))
                                viewModel.showToast("Invite link copied!")
                            },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy Link", color = WaGreen, fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    setType("text/plain")
                                    putExtra(Intent.EXTRA_TEXT, "Join my PGLOB Contribution Circle \"${name.ifBlank { "Ajo Group" }}\": $currency${amountStr} / ${type.label}. Link: $inviteLink")
                                }
                                context.startActivity(Intent.createChooser(share, "Share Invite Link"))
                            },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Invite", color = WaGreen, fontSize = 13.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Back", color = WaTextPrimary)
                    }
                }
                Button(
                    onClick = {
                        if (step < 3) {
                            if (step == 1 && name.isBlank()) {
                                viewModel.showToast("Enter a Contribution Group Name")
                            } else {
                                step++
                            }
                        } else {
                            val chosen = candidateProfiles.filter { selectedUids[it.userId] == true }
                            viewModel.createContributionGroup(
                                name = name.ifBlank { "PGLOB Ajo Circle" },
                                purpose = purpose,
                                type = type,
                                amountPerRound = amountVal.coerceAtLeast(100.0),
                                currency = currency,
                                maxMembers = maxMembers,
                                startDateLabel = startDate,
                                endDateLabel = endDate,
                                payoutOrderType = payoutOrder,
                                requireLateFee = requireLateFee,
                                lateFeeAmount = lateFeeStr.toDoubleOrNull() ?: 500.0,
                                mustPayBeforeTurn = mustPayBeforeTurn,
                                adminCanRemoveUnpaid = adminCanRemoveUnpaid,
                                screenshotBlockEnabled = screenshotBlock,
                                invitedProfiles = chosen,
                                linkedChatIdOverride = linkedChatId,
                                groupPhotoUri = groupPhotoUri,
                                groupIconEmoji = groupIconEmoji,
                                rulesText = rulesText,
                                allowSubAdminApproveRegistration = allowSubAdminApproveReg,
                                onCreated = onCreated
                            )
                        }
                    },
                    modifier = Modifier.weight(2f).height(52.dp).testTag("wizard_next_or_create_btn"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) {
                    Text(
                        if (step < 3) "Next" else "Create Contribution Group",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = WaTextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = WaPureBlack, checkedTrackColor = WaGreen)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionDetailScreen(
    groupId: String,
    viewModel: PglobViewModel,
    onBack: () -> Unit,
    onOpenLinkedChat: (String) -> Unit
) {
    BackHandler { onBack() }

    val groups by viewModel.contributions.collectAsStateWithLifecycle()
    val myProfile by viewModel.myProfile.collectAsStateWithLifecycle()
    val localSettings by viewModel.localSettings.collectAsStateWithLifecycle()
    val group = remember(groups, groupId) { groups.firstOrNull { it.id == groupId } }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize().background(WaBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Contribution group not found", color = WaTextPrimary, fontSize = 16.sp)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    FlagSecureEffect(group.screenshotBlockEnabled)

    val myMember = group.members.firstOrNull { it.uid == viewModel.currentUserId }
    val iAmCreatorOrSuper = viewModel.isGroupCreatorOrSuperAdmin(group)
    val mySubPerms = viewModel.getGroupSubAdminPermissions(group.id)
    val iAmSubAdmin = !iAmCreatorOrSuper && (myMember?.role == "SUB_ADMIN" || mySubPerms != null)
    val canManageMembers = iAmCreatorOrSuper || (mySubPerms?.canAddRemoveMember == true)
    val canRemind = iAmCreatorOrSuper || (mySubPerms?.canRemindMembers == true)
    val canReorder = iAmCreatorOrSuper || (mySubPerms?.canChangeTurnOrder == true)
    val canApproveReq = iAmCreatorOrSuper || (group.allowSubAdminApproveRegistration && mySubPerms?.canApproveNewMember == true)

    var activeTab by remember { mutableIntStateOf(0) } // 0: Payments, 1: Collection, 2: Members, 3: Group Rules, 4: Audit Log
    var showPaySheet by remember { mutableStateOf(false) }
    var showPinConfirm by remember { mutableStateOf(false) }
    var pendingMethod by remember { mutableStateOf("PGLOB Balance") }
    var applyLateFee by remember { mutableStateOf(false) }
    var proofRefInput by remember { mutableStateOf("") }
    var proofUriInput by remember { mutableStateOf("") }
    var paidSuccessRound by remember { mutableStateOf<Int?>(null) }
    var paidSuccessAmount by remember { mutableStateOf(0.0) }

    var rejectTargetMember by remember { mutableStateOf<ContributionMember?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }
    var showLargeReleaseConfirm by remember { mutableStateOf(false) }

    var selectedMemberModal by remember { mutableStateOf<ContributionMember?>(null) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showRulesSheet by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showForfeitConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var subAdminTarget by remember { mutableStateOf<ContributionMember?>(null) }

    val progress = if (group.members.isEmpty()) 0f else group.paidMembersCount.toFloat() / group.members.size.toFloat()
    val nextTurnMember = group.currentTurnMember
    val nextTurnName = nextTurnMember?.name ?: "TBD"
    val hasAgreedToRules = group.agreedMemberUids.contains(viewModel.currentUserId)
    val nowMillis = remember(groups) { System.currentTimeMillis() }

    val proofImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) proofUriInput = uri.toString() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Group Picture
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackground)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("ajo_detail_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                }
                GroupPictureBadge(
                    name = group.name,
                    photoUri = group.groupPhotoUri,
                    emoji = group.groupIconEmoji,
                    size = 40.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${group.type.label} • Round ${group.currentRound} of ${group.totalRounds} • ${group.status.label}", color = WaTextSecondary, fontSize = 12.sp)
                }
                if (group.linkedChatId.isNotBlank()) {
                    IconButton(onClick = { onOpenLinkedChat(group.linkedChatId) }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Group Chat", tint = WaGreen)
                    }
                }
                IconButton(onClick = { showRulesSheet = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Rules & Admin", tint = WaTextPrimary)
                }
            }

            // Locked Escrow Banner
            Surface(
                color = if (group.escrowFrozen) WaRed.copy(alpha = 0.2f) else Color(0xFF0A2416),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (group.escrowFrozen) {
                            "🧊 ESCROW FROZEN BY OWNER • Dispute Review Active (${group.currency}${formatMoney(group.totalEscrowBalance)})"
                        } else {
                            "🔒 PGLOB Escrow - Money Safe • Total in Escrow Now: ${group.currency}${formatMoney(group.totalEscrowBalance)} - Locked 🔒"
                        },
                        color = if (group.escrowFrozen) WaRed else WaGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (group.paymentAccountMode != "PGLOB_ESCROW") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️ Legacy Personal Account Mode — Switch to PGLOB Escrow", color = WaGold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            if (iAmCreatorOrSuper) {
                                TextButton(onClick = { viewModel.switchGroupToPglobEscrow(group.id) }) {
                                    Text("Switch", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 5 Tabs: Payments | Collection | Members | Group Rules | Audit Log
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackground)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Payments", "Collection", "Members", "Group Rules", "Audit Log").forEachIndexed { idx, label ->
                    val selected = activeTab == idx
                    Surface(
                        color = if (selected) WaGreen else WaInput,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { activeTab = idx }
                            .testTag("ajo_tab_${label.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            label,
                            color = if (selected) WaPureBlack else WaTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = WaDivider, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (activeTab) {
                    // TAB 0: PAYMENTS + 48H APPROVALS + PROOF WORKFLOW
                    0 -> {
                        if (myMember != null && !hasAgreedToRules) {
                            item {
                                Surface(
                                    color = Color(0xFF281D00),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, WaGold, RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("📜 Agree to Group Rules First", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(group.rulesText, color = WaTextPrimary, fontSize = 13.sp)
                                        Button(
                                            onClick = { viewModel.agreeToContributionRules(group.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("ajo_agree_rules_btn")
                                        ) {
                                            Text("I Agree to Rules", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            val paid = myMember?.paidCurrentRound == true
                            val status = myMember?.paymentApprovalStatus ?: "UNPAID"
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        when {
                                            paid -> WaGreen.copy(alpha = 0.5f)
                                            status == "WAITING_APPROVAL" -> WaGold.copy(alpha = 0.5f)
                                            else -> WaRed.copy(alpha = 0.5f)
                                        },
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        when {
                                            myMember == null -> "Viewing Circle (${group.name})"
                                            paid -> "✅ You Paid Round ${group.currentRound} (${status.replace("_", " ")})"
                                            status == "WAITING_APPROVAL" -> "⏳ Waiting Admin Approval (${ContributionAdminCodec.formatRemaining48h(myMember.paymentSubmittedAtMillis, nowMillis)})"
                                            status == "REJECTED" -> "❌ Proof Rejected: ${myMember.rejectionReason}"
                                            status == "UNDER_REVIEW" -> "🔍 External Proof Under Owner Review (48h+)"
                                            else -> "You have NOT PAID Round ${group.currentRound}"
                                        },
                                        color = when {
                                            paid -> WaGreen
                                            status == "WAITING_APPROVAL" -> WaGold
                                            myMember == null -> WaGold
                                            else -> WaRed
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Contribution: ${group.currency}${formatMoney(group.amountPerRound)} / ${group.type.label} • Due ${group.nextDueDateLabel}",
                                        color = WaTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    if (myMember != null && !paid && status != "WAITING_APPROVAL" && group.status == ContributionStatus.ACTIVE) {
                                        Button(
                                            onClick = { showPaySheet = true },
                                            enabled = hasAgreedToRules && !group.escrowFrozen,
                                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("ajo_pay_now_btn"),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                                        ) {
                                            Text("I Have Paid / Pay ${group.currency}${formatMoney(group.amountPerRound)}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (iAmSubAdmin) {
                            item {
                                Surface(color = WaInput, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "🔒 Sub-Admin Notice: Sub-Admins CANNOT approve payments or release Escrow money (Group Admin / Owner only).",
                                        color = WaGold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        val awaitingMembers = group.members.filter {
                            it.paymentApprovalStatus == "WAITING_APPROVAL" || it.paymentApprovalStatus == "UNDER_REVIEW"
                        }
                        if (awaitingMembers.isNotEmpty()) {
                            item {
                                Text("Pending Payment Approvals (${awaitingMembers.size})", color = WaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            items(awaitingMembers, key = { "pending_${it.uid}" }) { pm ->
                                Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().border(1.dp, WaGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(pm.name, color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Surface(color = WaGold.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                                Text(
                                                    ContributionAdminCodec.formatRemaining48h(pm.paymentSubmittedAtMillis, nowMillis),
                                                    color = WaGold,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Text("Proof Ref: ${pm.paymentProofRef.ifBlank { "PGLOB Escrow Submission" }}", color = WaTextSecondary, fontSize = 12.sp)
                                        if (pm.paymentProofUri.isNotBlank()) {
                                            AsyncImage(
                                                model = Uri.parse(pm.paymentProofUri),
                                                contentDescription = "Payment proof photo",
                                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        if (iAmCreatorOrSuper) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                Button(
                                                    onClick = { viewModel.adminApproveContributionPayment(group.id, pm.uid) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                                                    modifier = Modifier.weight(1f).height(46.dp).testTag("ajo_approve_pay_${pm.uid}")
                                                ) { Text("Approve", fontWeight = FontWeight.Bold) }
                                                OutlinedButton(
                                                    onClick = {
                                                        rejectTargetMember = pm
                                                        rejectReasonInput = ""
                                                    },
                                                    modifier = Modifier.weight(1f).height(46.dp).testTag("ajo_reject_pay_${pm.uid}")
                                                ) { Text("Reject", color = WaRed, fontWeight = FontWeight.Bold) }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { viewModel.run48HourAutoApproveCheck(simulateElapsed48h = true) },
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("ajo_run_48h_check_btn")
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = WaGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Run 48h Auto-Approve Check (Demo)", color = WaGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            Text("Round ${group.currentRound} Status (${group.paidMembersCount}/${group.members.size} Paid)", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        items(group.members.sortedBy { it.turnOrder }, key = { "pay_row_${it.uid}" }) { member ->
                            val pillColor = when {
                                member.paidCurrentRound -> WaGreen
                                member.paymentApprovalStatus == "WAITING_APPROVAL" -> WaGold
                                member.paymentApprovalStatus == "REJECTED" -> WaRed
                                else -> WaTextSecondary
                            }
                            val pillText = when (member.paymentApprovalStatus) {
                                "AUTO_APPROVED" -> "AUTO-APPROVED ⏰"
                                "APPROVED" -> "PAID ✅"
                                "WAITING_APPROVAL" -> "WAITING 48H ⏳"
                                "UNDER_REVIEW" -> "OWNER REVIEW 🔍"
                                "REJECTED" -> "REJECTED ❌"
                                else -> if (member.paidCurrentRound) "PAID ✅" else "UNPAID"
                            }
                            Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        AvatarCircle(name = member.name, photoUri = member.photoUri, size = 38.dp)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(member.name, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                            Text(
                                                if (member.approvedByAdminName.isNotBlank()) "By ${member.approvedByAdminName} • ${member.approvedAtLabel}" else "Turn #${member.turnOrder}",
                                                color = WaTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Surface(color = pillColor.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            pillText,
                                            color = pillColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: COLLECTION (ESCROW POT + DIRECT WALLET RELEASE + >500K CONFIRM)
                    1 -> {
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Total Pot This Round", color = WaTextSecondary, fontSize = 14.sp)
                                    Text("${group.currency}${formatMoney(group.totalPotPerRound)}", color = WaTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    LinearProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                                        color = WaGreen,
                                        trackColor = WaInput
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${group.paidMembersCount} / ${group.members.size} Paid", color = WaTextSecondary, fontSize = 13.sp)
                                        Text("In Escrow: ${group.currency}${formatMoney(group.totalEscrowBalance)}", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Next Collector: $nextTurnName (${group.daysRemaining} days left)", color = WaGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        if (nextTurnMember != null) {
                            item {
                                Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, WaGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Round ${group.currentRound} Collection Request", color = WaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("Turn #${nextTurnMember.turnOrder}: ${nextTurnMember.name}", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            when (nextTurnMember.collectionStatus) {
                                                "REQUESTED" -> "Status: REQUESTED (${ContributionAdminCodec.formatRemaining48h(nextTurnMember.collectionRequestedAtMillis, nowMillis)})"
                                                "COLLECTED" -> "Status: COLLECTED ✅ (${nextTurnMember.collectedAtLabel})"
                                                "AUTO_COLLECTED" -> "Status: AUTO-COLLECTED AFTER 48H ⏰"
                                                else -> "Status: Waiting (${group.paidMembersCount}/${group.members.size} paid)"
                                            },
                                            color = WaTextSecondary,
                                            fontSize = 13.sp
                                        )
                                        if (nextTurnMember.uid == viewModel.currentUserId && !nextTurnMember.hasCollected && nextTurnMember.collectionStatus != "REQUESTED") {
                                            Button(
                                                onClick = { viewModel.requestContributionCollection(group.id) },
                                                enabled = group.paidMembersCount >= group.members.size && !group.escrowFrozen,
                                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ajo_request_collection_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = WaGold, contentColor = WaPureBlack)
                                            ) { Text("Request Collection (${group.currency}${formatMoney(group.totalPotPerRound)})", fontWeight = FontWeight.Bold) }
                                        }
                                        if (iAmCreatorOrSuper && !nextTurnMember.hasCollected) {
                                            Button(
                                                onClick = {
                                                    if (group.totalPotPerRound > 500_000.0) {
                                                        showLargeReleaseConfirm = true
                                                    } else {
                                                        viewModel.adminConfirmAndReleaseCollection(group.id, nextTurnMember.uid)
                                                    }
                                                },
                                                enabled = !group.escrowFrozen,
                                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("ajo_release_escrow_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                                            ) {
                                                Text("Confirm & Release Escrow Directly to Wallet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        } else if (iAmSubAdmin) {
                                            Text("🔒 Sub-Admins cannot release Escrow pot. Only Group Admin or Owner.", color = WaRed, fontSize = 12.sp)
                                        }
                                        Text("Note: Group Admin NEVER has a manual Withdraw Escrow button. Funds release ONLY to the current turn collector's Wallet.", color = WaTextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: MEMBERS + PENDING REGISTRATIONS + SUB-ADMIN (MAX 2) + REPORT DISPUTE
                    2 -> {
                        if (canApproveReq && group.pendingJoinRequests.isNotEmpty()) {
                            item {
                                Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Pending Join Requests (${group.pendingJoinRequests.size})", color = WaGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        group.pendingJoinRequests.forEach { req ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(req, color = WaTextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    TextButton(onClick = { viewModel.approveJoinRequest(group.id, req, true) }) {
                                                        Text("Approve", color = WaGreen)
                                                    }
                                                    TextButton(onClick = { viewModel.approveJoinRequest(group.id, req, false) }) {
                                                        Text("Decline", color = WaRed)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val subCount = group.members.count { it.role == "SUB_ADMIN" }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Members & Turn Order (${group.members.size})", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text("Group Sub-Admins: $subCount / 2 Max", color = WaGold, fontSize = 12.sp)
                                }
                                if (canManageMembers) {
                                    TextButton(onClick = { showAddMemberDialog = true }) {
                                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = WaGreen, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add", color = WaGreen, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        items(group.members.sortedBy { it.turnOrder }, key = { it.uid }) { member ->
                            val isCurrentTurn = member.uid == group.currentTurnMember?.uid
                            val circleColor = when {
                                isCurrentTurn -> WaGold
                                member.paidCurrentRound -> WaGreen
                                else -> WaTextSecondary
                            }
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMemberModal = member }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarCircle(name = member.name, photoUri = member.photoUri, size = 48.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(member.name, color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                            if (member.role == "ADMIN" || member.role == "SUB_ADMIN") {
                                                Surface(
                                                    color = if (member.role == "ADMIN") WaGold.copy(alpha = 0.18f) else WaGreen.copy(alpha = 0.18f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        if (member.role == "ADMIN") "Admin" else "Sub-Admin",
                                                        color = if (member.role == "ADMIN") WaGold else WaGreen,
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                when {
                                                    member.hasCollected -> "Collected ✓"
                                                    member.paidCurrentRound -> "Paid Round ${group.currentRound} ✅"
                                                    else -> "Pending Payment"
                                                },
                                                color = if (member.paidCurrentRound) WaGreen else WaRed,
                                                fontSize = 13.sp
                                            )
                                            Text("⭐ ${member.trustScore}", color = WaGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(circleColor.copy(alpha = 0.2f))
                                            .border(1.5.dp, circleColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${member.turnOrder}",
                                            color = circleColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { showReportDialog = true },
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("ajo_report_dispute_btn")
                            ) {
                                Icon(Icons.Default.Report, contentDescription = null, tint = WaRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Report Dispute to Owner", color = WaRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // TAB 3: GROUP RULES + SUB-ADMIN REG PERMISSION + LEAVE
                    3 -> {
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("📜 Official Group Rules", color = WaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(group.rulesText, color = WaTextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                                    HorizontalDivider(color = WaDivider)
                                    Text("• Contribution: ${group.currency}${formatMoney(group.amountPerRound)} / ${group.type.label}", color = WaTextSecondary, fontSize = 13.sp)
                                    Text("• Payout Order: ${group.payoutOrderType.label}", color = WaTextSecondary, fontSize = 13.sp)
                                    Text("• Late Fee: ${if (group.requireLateFee) "${group.currency}${formatMoney(group.lateFeeAmount)}" else "None"}", color = WaTextSecondary, fontSize = 13.sp)
                                    Text("• Must Pay Before Turn: ${if (group.mustPayBeforeTurn) "Yes" else "No"}", color = WaTextSecondary, fontSize = 13.sp)
                                    Text("• Agreed Members: ${group.agreedMemberUids.size} / ${group.members.size}", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (!hasAgreedToRules) {
                                        Button(
                                            onClick = { viewModel.agreeToContributionRules(group.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                                            modifier = Modifier.fillMaxWidth().height(46.dp)
                                        ) { Text("I Agree to Rules", fontWeight = FontWeight.Bold) }
                                    } else {
                                        Text("✅ You agreed to these Group Rules", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (iAmCreatorOrSuper) {
                                        HorizontalDivider(color = WaDivider)
                                        RuleToggleRow(
                                            label = "Allow Sub-Admin to approve registration",
                                            checked = group.allowSubAdminApproveRegistration
                                        ) { allowed ->
                                            viewModel.toggleAllowSubAdminApproveReg(group.id, allowed)
                                        }
                                        Button(
                                            onClick = { showRulesSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaInput, contentColor = WaTextPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Open Admin Controls (Amount / Pause / End)") }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 4: IMMUTABLE AUDIT LOG & MASKED RECEIPTS
                    else -> {
                        item {
                            Text("Immutable Escrow Audit Log (${group.auditLog.size})", color = WaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("All account numbers masked for member privacy (e.g. 1234***890).", color = WaTextSecondary, fontSize = 12.sp)
                        }
                        if (group.auditLog.isEmpty() && group.roundHistory.isEmpty()) {
                            item {
                                Text("No Escrow audit records yet.", color = WaTextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            items(group.auditLog, key = { it.id }) { entry ->
                                Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(entry.paidByName, color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${entry.currency}${formatMoney(entry.amount)}", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text("${entry.escrowInLabel} • Approved: ${entry.approvedByAdmin}", color = WaTextSecondary, fontSize = 12.sp)
                                        if (entry.releasedToName.isNotBlank()) {
                                            Text("Released -> ${entry.releasedToName} (${entry.releasedAtLabel})", color = WaGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Receipt: ${entry.receiptNumber} • ${entry.paidAtLabel}", color = WaTextSecondary, fontSize = 11.sp)
                                            TextButton(
                                                onClick = {
                                                    viewModel.generateContributionPdfReceipt(group, entry.amount, entry.paidByName, group.currentRound)
                                                },
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text("PDF Receipt", color = WaGold, fontSize = 12.sp) }
                                        }
                                    }
                                }
                            }
                            items(group.roundHistory, key = { "ah_${it.id}" }) { h ->
                                Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                if (h.isPayoutEvent) "🎉 Payout -> ${h.payoutRecipientName}" else "✅ ${h.payerName}",
                                                color = if (h.isPayoutEvent) WaGold else WaTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "Round ${h.roundNumber} • ${h.escrowStatus} • ${h.maskedAccountNumber.ifBlank { "9000***890" }} • ${h.paidAtLabel}",
                                                color = WaTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text("${h.currency}${formatMoney(h.amount + h.lateFee)}", color = WaGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar
            HorizontalDivider(color = WaDivider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaCard)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (myMember != null && !myMember.paidCurrentRound && myMember.paymentApprovalStatus != "WAITING_APPROVAL" && group.status == ContributionStatus.ACTIVE) {
                            showPaySheet = true
                        } else if (canRemind) {
                            viewModel.sendContributionReminder(group.id)
                        } else {
                            showHistorySheet = true
                        }
                    },
                    modifier = Modifier.weight(1.2f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when {
                            myMember != null && myMember.paymentApprovalStatus == "WAITING_APPROVAL" -> "Waiting 48h ⏳"
                            myMember != null && !myMember.paidCurrentRound -> "Pay Now"
                            canRemind -> "Remind"
                            else -> "Paid ✓"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                OutlinedButton(
                    onClick = { activeTab = 4 },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = "Audit Log", tint = WaTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Audit", color = WaTextPrimary, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { showRulesSheet = true },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Rules", tint = WaTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rules", color = WaTextPrimary, fontSize = 12.sp)
                }
            }
        }
    }

    // Pay Round Bottom Sheet (with Proof Reference / Photo Upload)
    if (showPaySheet) {
        val lateExtra = if (group.requireLateFee && applyLateFee) group.lateFeeAmount else 0.0
        val total = group.amountPerRound + lateExtra
        ModalBottomSheet(onDismissRequest = { showPaySheet = false }, containerColor = WaCard) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Submit Payment • Round ${group.currentRound}", color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Surface(color = WaBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Locked Contribution Amount", color = WaTextSecondary, fontSize = 12.sp)
                        Text("${group.currency}${formatMoney(total)}", color = WaGreen, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                        Text("🔒 Secured in PGLOB Escrow • 48h Admin Approval Window", color = WaTextSecondary, fontSize = 12.sp)
                    }
                }
                if (group.requireLateFee) {
                    RuleToggleRow("Include Late Fee (+${group.currency}${formatMoney(group.lateFeeAmount)})", applyLateFee) {
                        applyLateFee = it
                    }
                }
                Text("Payment Method", color = WaTextSecondary, fontSize = 13.sp)
                listOf("PGLOB Balance", "Bank Transfer", "Bank Card").forEach { m ->
                    val sel = pendingMethod == m
                    Surface(
                        color = if (sel) WaGreen.copy(alpha = 0.16f) else WaInput,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (sel) WaGreen else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { pendingMethod = m }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(m, color = WaTextPrimary, fontSize = 14.sp)
                            if (sel) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = proofRefInput,
                    onValueChange = { proofRefInput = it },
                    label = { Text("Transfer Reference / Proof Note (Optional)") },
                    placeholder = { Text("e.g. OPay Ref 882190") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { proofImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WaGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (proofUriInput.isBlank()) "Upload Payment Proof Photo (Optional)" else "Proof Photo Attached ✅", color = WaGold, fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        showPaySheet = false
                        showPinConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("ajo_authorize_pay_btn"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) {
                    Text("Authorize with PIN / Biometric", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showPinConfirm) {
        val lateExtra = if (group.requireLateFee && applyLateFee) group.lateFeeAmount else 0.0
        val total = group.amountPerRound + lateExtra
        PinOrBiometricConfirmSheet(
            title = "Authorize Contribution",
            summary = "Submit ${group.currency}${formatMoney(total)} to ${group.name} (Round ${group.currentRound}) via $pendingMethod",
            hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
            biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
            lockUntilMillis = localSettings.payLockUntilMillis,
            isLargeTransfer = total >= 50000.0,
            onDismiss = { showPinConfirm = false },
            onSetPin = { viewModel.setTransactionPin(it) },
            onConfirmWithPin = { pin, bio ->
                if (viewModel.verifyPinOrAllow(pin, bio)) {
                    val paidRound = group.currentRound
                    viewModel.submitContributionPaymentWithProof(
                        groupId = group.id,
                        paymentMethod = pendingMethod,
                        isLate = applyLateFee,
                        proofRef = proofRefInput,
                        proofUri = proofUriInput
                    ) {
                        paidSuccessRound = paidRound
                        paidSuccessAmount = total
                    }
                    showPinConfirm = false
                }
            }
        )
    }

    if (rejectTargetMember != null) {
        AlertDialog(
            onDismissRequest = { rejectTargetMember = null },
            containerColor = WaCard,
            title = { Text("Reject Payment Proof", color = WaRed, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide clear reason for ${rejectTargetMember?.name}:", color = WaTextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. Receipt unclear or wrong amount") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminRejectContributionPayment(group.id, rejectTargetMember!!.uid, rejectReasonInput)
                        rejectTargetMember = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary)
                ) { Text("Reject Proof") }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetMember = null }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    if (showLargeReleaseConfirm && nextTurnMember != null) {
        AlertDialog(
            onDismissRequest = { showLargeReleaseConfirm = false },
            containerColor = WaCard,
            title = { Text("⚠️ Large Escrow Release (>₦500,000)", color = WaGold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Confirm releasing ${group.currency}${formatMoney(group.totalPotPerRound)} from PGLOB Escrow directly to ${nextTurnMember.name}'s Wallet?",
                    color = WaTextPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLargeReleaseConfirm = false
                        viewModel.adminConfirmAndReleaseCollection(group.id, nextTurnMember.uid)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) { Text("Confirm Release", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLargeReleaseConfirm = false }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    if (paidSuccessRound != null) {
        AlertDialog(
            onDismissRequest = { paidSuccessRound = null },
            containerColor = WaCard,
            title = { Text("⏳ Payment Submitted!", color = WaGreen, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${group.currency}${formatMoney(paidSuccessAmount)} for Round $paidSuccessRound submitted into PGLOB Escrow.",
                        color = WaTextPrimary,
                        fontSize = 14.sp
                    )
                    Text("Group Admin has 48h to approve (or PGLOB Auto-Approves verified Wallet payments after 48h).", color = WaGold, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.generateContributionPdfReceipt(
                            group = group,
                            amount = paidSuccessAmount,
                            payerName = myProfile?.fullName ?: "Member",
                            roundNumber = paidSuccessRound!!
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Download / Share PDF Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { paidSuccessRound = null }) { Text("Done", color = WaTextPrimary) }
            }
        )
    }

    // Member History & Trust Score Modal
    selectedMemberModal?.let { member ->
        AlertDialog(
            onDismissRequest = { selectedMemberModal = null },
            containerColor = WaCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AvatarCircle(name = member.name, photoUri = member.photoUri, size = 44.dp)
                    Column {
                        Text(member.name, color = WaTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text("@${member.handle} • Joined ${member.joinedDateLabel}", color = WaTextSecondary, fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = WaBackground, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Trust Score: ⭐ ${member.trustScore} / 100", color = WaGold, fontWeight = FontWeight.Medium)
                            Text("Turn #${member.turnOrder} • Paid Rounds: ${member.paidRoundsCount} • Late: ${member.lateCount} • Missed: ${member.missedCount}", color = WaTextSecondary, fontSize = 12.sp)
                        }
                    }
                    if (canReorder) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.adminReorderTurn(group.id, member.uid, moveUp = true)
                                    selectedMemberModal = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = WaGreen, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Move Up", color = WaGreen, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.adminReorderTurn(group.id, member.uid, moveUp = false)
                                    selectedMemberModal = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = WaGreen, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Move Down", color = WaGreen, fontSize = 12.sp)
                            }
                        }
                    }
                    if (iAmCreatorOrSuper && member.uid != group.creatorUid) {
                        OutlinedButton(
                            onClick = {
                                subAdminTarget = member
                                selectedMemberModal = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👑 Make / Edit Group Sub-Admin (Max 2)", color = WaGold, fontSize = 13.sp)
                        }
                    }
                    if (canManageMembers && member.uid != group.creatorUid) {
                        OutlinedButton(
                            onClick = {
                                viewModel.adminRemoveContributionMember(group.id, member.uid)
                                selectedMemberModal = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = WaRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Remove Member", color = WaRed, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberModal = null }) { Text("Close", color = WaGreen) }
            }
        )
    }

    // Group Sub-Admin Permissions Editor (Strict: Cannot Approve Pay, Release Escrow, Change Amount, or End Group)
    subAdminTarget?.let { target ->
        val existing = viewModel.getGroupSubAdminPermissions(group.id, target.uid)
        var canRemindMem by remember { mutableStateOf(existing?.canRemindMembers ?: true) }
        var canAddRemove by remember { mutableStateOf(existing?.canAddRemoveMember ?: true) }
        var canChangeTurn by remember { mutableStateOf(existing?.canChangeTurnOrder ?: false) }
        var canApproveNew by remember { mutableStateOf(existing?.canApproveNewMember ?: false) }

        AlertDialog(
            onDismissRequest = { subAdminTarget = null },
            containerColor = WaCard,
            title = { Text("Group Sub-Admin • ${target.name}", color = WaGold, fontSize = 17.sp, fontWeight = FontWeight.Medium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Max 2 Sub-Admins per group. Sub-Admins NEVER approve payments, release Escrow, change amounts, or end groups.", color = WaTextSecondary, fontSize = 12.sp)
                    RuleToggleRow("Can remind unpaid members", canRemindMem) { canRemindMem = it }
                    RuleToggleRow("Can add / remove members", canAddRemove) { canAddRemove = it }
                    RuleToggleRow("Can change turn order", canChangeTurn) { canChangeTurn = it }
                    RuleToggleRow("Can approve new join requests (if allowed)", canApproveNew) { canApproveNew = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignGroupSubAdmin(
                            groupId = group.id,
                            memberUid = target.uid,
                            memberName = target.name,
                            permissions = GroupSubAdminPermissions(
                                groupId = group.id,
                                userUid = target.uid,
                                userName = target.name,
                                canRemindMembers = canRemindMem,
                                canAddRemoveMember = canAddRemove,
                                canChangeTurnOrder = canChangeTurn,
                                cannotChangeAmount = true,
                                cannotEndGroup = true,
                                canApproveNewMember = canApproveNew
                            )
                        )
                        subAdminTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) { Text("Save Sub-Admin") }
            },
            dismissButton = {
                TextButton(onClick = { subAdminTarget = null }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    // Round History & PDF Receipts Sheet
    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }, containerColor = WaCard) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Escrow & Round History", color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                if (group.roundHistory.isEmpty()) {
                    Text("No round transactions yet.", color = WaTextSecondary, fontSize = 14.sp)
                } else {
                    LazyColumn(modifier = Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(group.roundHistory, key = { it.id }) { h ->
                            Surface(color = WaBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            if (h.isPayoutEvent) "🎉 Payout -> ${h.payoutRecipientName}" else "✅ ${h.payerName}",
                                            color = if (h.isPayoutEvent) WaGold else WaTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text("Round ${h.roundNumber} • ${h.paymentMethod} • ${h.paidAtLabel}", color = WaTextSecondary, fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${h.currency}${formatMoney(h.amount + h.lateFee)}", color = WaGreen, fontWeight = FontWeight.Medium)
                                        TextButton(
                                            onClick = {
                                                viewModel.generateContributionPdfReceipt(
                                                    group = group,
                                                    amount = h.amount + h.lateFee,
                                                    payerName = h.payerName,
                                                    roundNumber = h.roundNumber
                                                )
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("PDF Receipt", color = WaGold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Rules, Admin Controls, Leave Protection & Report Sheet
    if (showRulesSheet) {
        var editAmountStr by remember { mutableStateOf(group.amountPerRound.toInt().toString()) }
        var editLateStr by remember { mutableStateOf(group.lateFeeAmount.toInt().toString()) }
        ModalBottomSheet(onDismissRequest = { showRulesSheet = false }, containerColor = WaCard) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Rules & Group Controls", color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text("• Payout Order: ${group.payoutOrderType.label}", color = WaTextSecondary, fontSize = 13.sp)
                Text("• Late Fee: ${if (group.requireLateFee) "${group.currency}${formatMoney(group.lateFeeAmount)}" else "None"}", color = WaTextSecondary, fontSize = 13.sp)
                Text("• Must pay before turn: ${if (group.mustPayBeforeTurn) "Yes" else "No"}", color = WaTextSecondary, fontSize = 13.sp)
                Text("• Screenshot Protection: ${if (group.screenshotBlockEnabled) "Enabled 🔒" else "Off"}", color = WaTextSecondary, fontSize = 13.sp)

                if (canRemind) {
                    Button(
                        onClick = { viewModel.sendContributionReminder(group.id); showRulesSheet = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Remind Unpaid Members")
                    }
                }

                if (iAmCreatorOrSuper) {
                    HorizontalDivider(color = WaDivider)
                    Text("Creator / Admin Controls", color = WaGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editAmountStr,
                            onValueChange = { editAmountStr = it.filter(Char::isDigit) },
                            label = { Text("Round Amount") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editLateStr,
                            onValueChange = { editLateStr = it.filter(Char::isDigit) },
                            label = { Text("Late Fee") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.adminEditContributionAmount(
                                group.id,
                                editAmountStr.toDoubleOrNull() ?: group.amountPerRound,
                                editLateStr.toDoubleOrNull() ?: group.lateFeeAmount
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Update Amount (Before First Payment)", color = WaGreen) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { viewModel.adminPauseResumeContribution(group.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(if (group.status == ContributionStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = WaGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (group.status == ContributionStatus.PAUSED) "Resume" else "Pause", color = WaGold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.adminEndContribution(group.id); showRulesSheet = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("End Group", color = WaRed)
                        }
                    }
                }

                HorizontalDivider(color = WaDivider)
                if (myMember != null) {
                    OutlinedButton(
                        onClick = {
                            val res = viewModel.leaveContributionGroup(group.id, confirmForfeitPaid = false)
                            if (res == "CONFIRM_FORFEIT_PAID") {
                                showForfeitConfirm = true
                            } else if (res == "LEFT") {
                                showRulesSheet = false
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave Contribution Group", color = WaRed)
                    }
                }
                OutlinedButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Report, contentDescription = null, tint = WaRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Report Problem to Admin", color = WaRed)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showAddMemberDialog) {
        var memName by remember { mutableStateOf("") }
        var memHandle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            containerColor = WaCard,
            title = { Text("Add Member to ${group.name}", color = WaTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = memName, onValueChange = { memName = it }, label = { Text("Member Name") }, singleLine = true)
                    OutlinedTextField(value = memHandle, onValueChange = { memHandle = it }, label = { Text("@username") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memName.isNotBlank()) {
                            viewModel.adminAddContributionMember(group.id, memName, memHandle)
                            showAddMemberDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                ) { Text("Add Member") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }

    if (showForfeitConfirm) {
        AlertDialog(
            onDismissRequest = { showForfeitConfirm = false },
            containerColor = WaCard,
            title = { Text("Confirm Leave Circle?", color = WaRed) },
            text = { Text("You already paid this round. Leaving forfeits your turn priority for Round ${group.currentRound}.", color = WaTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.leaveContributionGroup(group.id, confirmForfeitPaid = true)
                        showForfeitConfirm = false
                        showRulesSheet = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary)
                ) { Text("Leave Anyway") }
            },
            dismissButton = {
                TextButton(onClick = { showForfeitConfirm = false }) { Text("Stay", color = WaGreen) }
            }
        )
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = WaCard,
            title = { Text("Report Contribution Problem", color = WaTextPrimary) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Describe the issue (late member, dispute...)") },
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isNotBlank()) {
                            viewModel.reportContributionDispute(group.id, reason.trim())
                            showReportDialog = false
                            showRulesSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaRed, contentColor = WaTextPrimary)
                ) { Text("Submit Report") }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel", color = WaTextPrimary) }
            }
        )
    }
}

@Composable
private fun GroupPictureBadge(
    name: String,
    photoUri: String,
    emoji: String,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(WaGreen.copy(alpha = 0.2f))
            .border(1.5.dp, WaGreen, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            photoUri.isNotBlank() -> {
                AsyncImage(
                    model = Uri.parse(photoUri),
                    contentDescription = "$name picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            emoji.isNotBlank() -> {
                Text(emoji, fontSize = (size.value * 0.45f).sp)
            }
            else -> {
                Text(
                    name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                    color = WaGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp
                )
            }
        }
    }
}

private fun formatMoney(amount: Double): String = String.format(Locale.US, "%,.0f", amount)
