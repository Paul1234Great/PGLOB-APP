package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAppSettings
import com.example.data.model.AppAdminPermission
import com.example.data.model.AppSubAdmin
import com.example.data.model.ContributionStatus
import com.example.data.model.JobListing
import com.example.data.model.MarketProduct
import com.example.ui.PglobViewModel
import com.example.ui.components.PinOrBiometricConfirmSheet
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
import java.util.Locale

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule

private enum class AdminSection(val title: String, val subtitle: String) {
    HOME("PGLOB ADMIN", "Platform control & live monitoring"),
    USERS("Manage Users", "Search users, trust scores, block/unblock"),
    CONTRIBUTIONS("Manage Contributions", "Ajo circles, force payout, pause or end"),
    ESCROW("Escrow / Ajo Money", "Total Escrow, Freeze, Refund & 48h Override (Owner)"),
    PAYMENTS("Manage Payments / Escrow", "Escrow pool, filters & receipts"),
    SUB_ADMINS("Manage Sub-Admins", "11 granular app permissions (Super Admin)"),
    REPORTS("Reports & Disputes", "Investigate, warn, block or resolve"),
    BROADCASTS("Broadcast / Announcement", "Notify all users or contribution circles"),
    ANALYTICS("Reports / Analytics", "Growth, contributions & paid-vs-late charts"),
    SETTINGS("App Settings", "App name, logo, fees & default currency")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PglobViewModel,
    localSettings: LocalAppSettings,
    marketProducts: List<MarketProduct>,
    jobs: List<JobListing>,
    onBack: () -> Unit,
    onOpenContribution: (String) -> Unit = {}
) {
    var activeSection by remember { mutableStateOf(AdminSection.HOME) }
    BackHandler {
        if (activeSection != AdminSection.HOME) activeSection = AdminSection.HOME else onBack()
    }

    val myProfile by viewModel.myProfile.collectAsStateWithLifecycle()
    val users by viewModel.adminUsers.collectAsStateWithLifecycle()
    val txs by viewModel.adminTransactions.collectAsStateWithLifecycle()
    val reports by viewModel.adminReports.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val subAdmins by viewModel.appSubAdmins.collectAsStateWithLifecycle()
    val logs by viewModel.adminActivityLogs.collectAsStateWithLifecycle()
    val broadcasts by viewModel.broadcasts.collectAsStateWithLifecycle()
    val adminConfig by viewModel.appAdminConfig.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshAdminData()
    }

    if (!viewModel.canAccessAdminDashboard()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WaBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WaRed, modifier = Modifier.size(48.dp))
                Text("Access Restricted", color = WaTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Text("Only Owner Super Admin or assigned Sub-Admins can access the Admin Dashboard.", color = WaTextSecondary, fontSize = 14.sp)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val isSuper = viewModel.isSuperAdmin()
    val totalEscrow = remember(contributions) { contributions.sumOf { it.totalEscrowBalance } }
    val pendingPayoutsCount = remember(contributions) {
        contributions.count { it.status == ContributionStatus.ACTIVE && it.paidMembersCount >= it.members.size.coerceAtLeast(1) }
    }

    // Sensitive Action PIN / Biometric Gate
    var pendingSensitiveSummary by remember { mutableStateOf<String?>(null) }
    var pendingSensitiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    fun runSensitiveAction(summary: String, action: () -> Unit) {
        pendingSensitiveSummary = summary
        pendingSensitiveAction = action
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackground)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (activeSection != AdminSection.HOME) activeSection = AdminSection.HOME else onBack() },
                    modifier = Modifier.testTag("admin_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                }
                com.example.ui.components.PglobBrandLogo(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(activeSection.title, color = WaTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                    Text(activeSection.subtitle, color = WaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                Surface(
                    color = if (isSuper) WaGold else WaGreen,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isSuper) "SUPER ADMIN" else "SUB-ADMIN",
                        color = WaPureBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            HorizontalDivider(color = WaDivider, thickness = 1.dp)

            when (activeSection) {
                AdminSection.HOME -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Row 1 of 4 KPI Cards
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                KpiStatCard("Users", users.size.toString(), WaGreen, Modifier.weight(1f))
                                KpiStatCard("Circles", contributions.size.toString(), WaGreen, Modifier.weight(1f))
                                val escrowStr = if (viewModel.hasAppPermission(AppAdminPermission.VIEW_ESCROW_BALANCE)) {
                                    "${adminConfig.defaultCurrency}${formatK(totalEscrow)}"
                                } else "Locked"
                                KpiStatCard("Escrow", escrowStr, WaGold, Modifier.weight(1f))
                                KpiStatCard("Tx Today", txs.size.toString(), WaTextPrimary, Modifier.weight(1f))
                            }
                        }
                        // Row 2 of 4 KPI Cards
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                KpiStatCard("Reports", reports.count { it.status == "OPEN" }.toString(), WaRed, Modifier.weight(1f))
                                KpiStatCard("Payouts", pendingPayoutsCount.toString(), WaGold, Modifier.weight(1f))
                                KpiStatCard("Sub-Admins", subAdmins.size.toString(), WaGreen, Modifier.weight(1f))
                                KpiStatCard("Broadcasts", broadcasts.size.toString(), WaTextPrimary, Modifier.weight(1f))
                            }
                        }

                        // 8 Admin Menu Rows (72dp clean PGLOB Black style)
                        item {
                            Text("Admin Controls", color = WaTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        val menuItems = listOf(
                            Triple(AdminSection.ESCROW, Icons.Default.Lock, isSuper || viewModel.hasAppPermission(AppAdminPermission.VIEW_ESCROW_BALANCE)),
                            Triple(AdminSection.USERS, Icons.Default.People, viewModel.hasAppPermission(AppAdminPermission.MANAGE_USERS)),
                            Triple(AdminSection.CONTRIBUTIONS, Icons.Default.Groups, viewModel.hasAppPermission(AppAdminPermission.MANAGE_CONTRIBUTIONS)),
                            Triple(AdminSection.PAYMENTS, Icons.Default.AccountBalance, viewModel.hasAppPermission(AppAdminPermission.VIEW_TRANSACTIONS)),
                            Triple(AdminSection.SUB_ADMINS, Icons.Default.SupervisorAccount, isSuper),
                            Triple(AdminSection.REPORTS, Icons.Default.ReportProblem, viewModel.hasAppPermission(AppAdminPermission.HANDLE_REPORTS)),
                            Triple(AdminSection.BROADCASTS, Icons.Default.Campaign, viewModel.hasAppPermission(AppAdminPermission.SEND_BROADCAST)),
                            Triple(AdminSection.ANALYTICS, Icons.Default.Analytics, viewModel.hasAppPermission(AppAdminPermission.VIEW_DASHBOARD)),
                            Triple(AdminSection.SETTINGS, Icons.Default.Settings, viewModel.hasAppPermission(AppAdminPermission.EDIT_APP_SETTINGS))
                        )
                        items(menuItems) { (sec, icon, allowed) ->
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clickable {
                                        if (allowed) activeSection = sec
                                        else viewModel.showToast("You don't have permission for ${sec.title}. Contact Super Admin.")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = sec.title, tint = if (allowed) WaGreen else WaTextSecondary, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sec.title, color = if (allowed) WaTextPrimary else WaTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                        Text(if (allowed) sec.subtitle else "Permission required", color = WaTextSecondary, fontSize = 13.sp)
                                    }
                                    if (!allowed) Text("🔒", fontSize = 14.sp)
                                }
                            }
                        }

                        // Immutable Activity Log
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                                Icon(Icons.Default.History, contentDescription = null, tint = WaGold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Activity Log (Immutable)", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        items(logs.take(15), key = { it.id }) { log ->
                            Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(log.actionTitle, color = WaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text(log.timestampLabel, color = WaTextSecondary, fontSize = 12.sp)
                                    }
                                    Text(log.details, color = WaTextSecondary, fontSize = 12.sp)
                                    Text("${log.actorName} (${log.actorRole})", color = WaGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                AdminSection.USERS -> {
                    var q by remember { mutableStateOf("") }
                    val filteredUsers = remember(users, q) {
                        users.filter { q.isBlank() || it.fullName.contains(q, true) || it.username.contains(q, true) || it.email.contains(q, true) }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = q,
                                onValueChange = { q = it },
                                placeholder = { Text("Search users by name, @username, email...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        items(filteredUsers, key = { it.userId }) { u ->
                            val blocked = u.isBanned || localSettings.blockedUserIds.contains(u.userId)
                            Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${u.countryFlag} ${u.fullName} (@${u.username})", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                        Text(if (blocked) "BLOCKED" else "Active", color = if (blocked) WaRed else WaGreen, fontSize = 12.sp)
                                    }
                                    Text("${u.email} • Coins: ${u.coins} • Balance: ${u.currency}${u.walletBalance.toInt()}", color = WaTextSecondary, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { viewModel.adminGrantCoins(u.userId, 100L) }) {
                                            Text("+100 Coins", color = WaGreen, fontSize = 12.sp)
                                        }
                                        OutlinedButton(onClick = {
                                            runSensitiveAction("${if (blocked) "Unblock" else "Block"} ${u.fullName}") {
                                                viewModel.adminSetUserBanned(u.userId, !u.isBanned)
                                                viewModel.toggleBlockUser(u.userId)
                                            }
                                        }) {
                                            Text(if (blocked) "Unblock" else "Block User", color = WaRed, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.ESCROW -> {
                    val frozenCount = contributions.count { it.escrowFrozen }
                    val pendingProofsCount = contributions.sumOf { g ->
                        g.members.count { it.paymentApprovalStatus == "WAITING_APPROVAL" || it.paymentApprovalStatus == "UNDER_REVIEW" }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, WaGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🔒 Total Escrow Locked Across All Ajo Groups", color = WaTextSecondary, fontSize = 12.sp)
                                    Text(
                                        "${adminConfig.defaultCurrency}${String.format(Locale.US, "%,.0f", totalEscrow)}",
                                        color = WaGreen,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Frozen Groups: $frozenCount • Pending Proofs / Reviews: $pendingProofsCount", color = WaGold, fontSize = 12.sp)
                                    OutlinedButton(
                                        onClick = { viewModel.run48HourAutoApproveCheck(simulateElapsed48h = true) },
                                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("admin_run_48h_auto_check")
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = WaGold, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Run 48h Auto-Approve Check (All Circles)", color = WaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(contributions, key = { "escrow_${it.id}" }) { g ->
                            val waitingCount = g.members.count { it.paymentApprovalStatus == "WAITING_APPROVAL" || it.paymentApprovalStatus == "UNDER_REVIEW" }
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (g.escrowFrozen) WaRed else WaDivider, RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(g.name, color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Escrow: ${g.currency}${String.format(Locale.US, "%,.0f", g.totalEscrowBalance)} • Mode: ${g.paymentAccountMode}",
                                                color = WaGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text("Round ${g.currentRound}/${g.totalRounds} • Pending Proofs: $waitingCount", color = WaTextSecondary, fontSize = 12.sp)
                                        }
                                        if (g.escrowFrozen) {
                                            Surface(color = WaRed.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                                Text("FROZEN", color = WaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(onClick = { onOpenContribution(g.id) }, modifier = Modifier.weight(1f)) {
                                            Text("Inspect", color = WaTextPrimary, fontSize = 12.sp)
                                        }
                                        if (isSuper) {
                                            OutlinedButton(
                                                onClick = { viewModel.toggleGroupEscrowFrozen(g.id, !g.escrowFrozen) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (g.escrowFrozen) "Unfreeze" else "Freeze", color = if (g.escrowFrozen) WaGreen else WaGold, fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    runSensitiveAction("Refund ${g.currency}${g.totalEscrowBalance.toInt()} Escrow back to member Wallets (${g.name})") {
                                                        viewModel.ownerRefundContributionGroup(g.id)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Refund", color = WaRed, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    if (isSuper && g.currentTurnMember != null && !g.escrowFrozen) {
                                        val isLarge = g.totalPotPerRound > 500_000.0
                                        Button(
                                            onClick = {
                                                val potLabel = "${g.currency}${String.format(Locale.US, "%,.0f", g.totalPotPerRound)}"
                                                val prefix = if (isLarge) "LARGE PAYOUT (>₦500k): " else ""
                                                runSensitiveAction("${prefix}Override & release $potLabel to ${g.currentTurnMember?.name}'s Wallet") {
                                                    viewModel.adminConfirmAndReleaseCollection(g.id, g.currentTurnMember!!.uid)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text(
                                                "Owner Override Release -> ${g.currentTurnMember?.name?.substringBefore(" ")}'s Wallet",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.CONTRIBUTIONS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(contributions, key = { it.id }) { g ->
                            Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(g.name, color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                            Text("${g.members.size} members • Round ${g.currentRound}/${g.totalRounds} • Escrow ${g.currency}${g.totalEscrowBalance.toInt()}", color = WaTextSecondary, fontSize = 12.sp)
                                        }
                                        Text(g.status.label, color = if (g.status == ContributionStatus.ACTIVE) WaGreen else WaTextSecondary, fontSize = 12.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(onClick = { onOpenContribution(g.id) }, modifier = Modifier.weight(1f)) {
                                            Text("View", color = WaTextPrimary, fontSize = 12.sp)
                                        }
                                        OutlinedButton(onClick = { viewModel.adminPauseResumeContribution(g.id) }, modifier = Modifier.weight(1f)) {
                                            Text(if (g.status == ContributionStatus.PAUSED) "Resume" else "Pause", color = WaGold, fontSize = 12.sp)
                                        }
                                        if (viewModel.hasAppPermission(AppAdminPermission.APPROVE_PAYOUTS)) {
                                            Button(
                                                onClick = {
                                                    runSensitiveAction("Force Payout ${g.currency}${g.totalPotPerRound.toInt()} to ${g.currentTurnMember?.name ?: "Member"}") {
                                                        viewModel.adminForceContributionPayout(g.id)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                                                modifier = Modifier.weight(1.2f)
                                            ) { Text("Force Payout", fontSize = 12.sp) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.PAYMENTS -> {
                    var txFilter by remember { mutableStateOf("All") }
                    val filteredTx = remember(txs, txFilter) {
                        when (txFilter) {
                            "Paid" -> txs.filter { it.status == "PAID" }
                            "Pending" -> txs.filter { it.status == "PENDING" }
                            "Ajo" -> txs.filter { it.txType.startsWith("AJO") }
                            "Withdraw" -> txs.filter { it.txType == "WITHDRAW" }
                            else -> txs
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Total PGLOB Escrow Pool", color = WaTextSecondary, fontSize = 12.sp)
                                    Text(
                                        if (viewModel.hasAppPermission(AppAdminPermission.VIEW_ESCROW_BALANCE)) {
                                            "${adminConfig.defaultCurrency}${String.format(Locale.US, "%,.0f", totalEscrow)}"
                                        } else "Hidden (Permission Required)",
                                        color = WaGreen,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                listOf("All", "Paid", "Pending", "Ajo", "Withdraw").forEach { f ->
                                    Surface(
                                        color = if (txFilter == f) WaGreen else WaInput,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.clickable { txFilter = f }
                                    ) {
                                        Text(f, color = if (txFilter == f) WaPureBlack else WaTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                    }
                                }
                            }
                        }
                        items(filteredTx, key = { it.id }) { tx ->
                            Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${tx.txType} • ${tx.method}", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        Text("${tx.currency}${tx.amount.toInt()} • ${tx.status}", color = if (tx.status == "PAID") WaGreen else WaGold, fontSize = 13.sp)
                                    }
                                    Text("To: ${tx.destination} • ${tx.note}", color = WaTextSecondary, fontSize = 12.sp)
                                    if (tx.status == "PENDING" && viewModel.hasAppPermission(AppAdminPermission.APPROVE_PAYOUTS)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.adminUpdateTxStatus(tx.id, "PAID") },
                                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                                            ) { Text("Approve", fontSize = 12.sp) }
                                            OutlinedButton(onClick = { viewModel.adminUpdateTxStatus(tx.id, "REFUNDED") }) {
                                                Text("Refund", color = WaRed, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.SUB_ADMINS -> {
                    var showAddSubAdminSheet by remember { mutableStateOf(false) }
                    var editingSubAdmin by remember { mutableStateOf<AppSubAdmin?>(null) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Button(
                                onClick = { editingSubAdmin = null; showAddSubAdminSheet = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_sub_admin_btn"),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                            ) {
                                Text("+ Add Sub-Admin", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        items(subAdmins, key = { it.uid }) { sa ->
                            Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(sa.name, color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                            Text("${sa.email} • @${sa.username} • ${sa.roleLabel}", color = WaTextSecondary, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = {
                                            runSensitiveAction("Remove Sub-Admin ${sa.name}") {
                                                viewModel.removeAppSubAdmin(sa.uid)
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove Sub-Admin", tint = WaRed)
                                        }
                                    }
                                    Text("${sa.permissions.size} of 11 permissions active", color = WaGreen, fontSize = 12.sp)
                                    OutlinedButton(onClick = { editingSubAdmin = sa; showAddSubAdminSheet = true }) {
                                        Text("Edit Permissions", color = WaGold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (showAddSubAdminSheet) {
                        AddOrEditSubAdminSheet(
                            existing = editingSubAdmin,
                            onDismiss = { showAddSubAdminSheet = false },
                            onSave = { name, email, username, roleLabel, perms, uid ->
                                viewModel.addOrUpdateAppSubAdmin(name, email, username, roleLabel, perms, uid)
                                showAddSubAdminSheet = false
                            }
                        )
                    }
                }

                AdminSection.REPORTS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (reports.isEmpty()) {
                            item {
                                Text("Zero open reports or disputes right now.", color = WaGreen, fontSize = 15.sp)
                            }
                        }
                        items(reports, key = { it.id }) { rep ->
                            Surface(color = WaCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${rep.targetType} • ${rep.targetId}", color = WaGold, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(rep.reason, color = WaTextPrimary, fontSize = 14.sp)
                                    Text("Status: ${rep.status}", color = WaTextSecondary, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                        if (rep.targetType == "CONTRIBUTION") {
                                            OutlinedButton(onClick = { onOpenContribution(rep.targetId) }) {
                                                Text("View Group", color = WaGreen, fontSize = 12.sp)
                                            }
                                        }
                                        if (rep.targetType == "STATUS_NOTE") {
                                            OutlinedButton(onClick = {
                                                runSensitiveAction("Delete reported Status Note ${rep.targetId}") {
                                                    viewModel.adminDeleteStatusNote(rep.targetId)
                                                    viewModel.adminUpdateReportStatus(rep.id, "RESOLVED")
                                                }
                                            }) {
                                                Text("Delete Note", color = WaRed, fontSize = 12.sp)
                                            }
                                        }
                                        OutlinedButton(onClick = { viewModel.adminUpdateReportStatus(rep.id, "WARNED") }) {
                                            Text("Warn User", color = WaGold, fontSize = 12.sp)
                                        }
                                        OutlinedButton(onClick = {
                                            runSensitiveAction("Block reported target ${rep.targetId}") {
                                                viewModel.adminSetUserBanned(rep.targetId, true)
                                                viewModel.adminUpdateReportStatus(rep.id, "RESOLVED")
                                            }
                                        }) {
                                            Text("Block User", color = WaRed, fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminUpdateReportStatus(rep.id, "RESOLVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                                        ) { Text("Resolve", fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.BROADCASTS -> {
                    var title by remember { mutableStateOf("") }
                    var message by remember { mutableStateOf("") }
                    var scope by remember { mutableStateOf("ALL_USERS") }
                    var selectedGroupId by remember { mutableStateOf(contributions.firstOrNull()?.id.orEmpty()) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Send Announcement", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("ALL_USERS" to "All Users", "CONTRIBUTION_MEMBERS" to "Ajo Members", "SPECIFIC_GROUP" to "One Group").forEach { (k, lbl) ->
                                            Surface(
                                                color = if (scope == k) WaGreen else WaInput,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.clickable { scope = k }
                                            ) {
                                                Text(lbl, color = if (scope == k) WaPureBlack else WaTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                                            }
                                        }
                                    }
                                    if (scope == "SPECIFIC_GROUP" && contributions.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                            contributions.forEach { g ->
                                                Surface(
                                                    color = if (selectedGroupId == g.id) WaGold else WaInput,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.clickable { selectedGroupId = g.id }
                                                ) {
                                                    Text(g.name, color = if (selectedGroupId == g.id) WaPureBlack else WaTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                                }
                                            }
                                        }
                                    }
                                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Announcement Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            val grp = contributions.firstOrNull { it.id == selectedGroupId }
                                            viewModel.adminBroadcastAnnouncement(
                                                title = title,
                                                message = message,
                                                targetScope = scope,
                                                targetGroupName = grp?.name.orEmpty(),
                                                targetGroupChatId = if (scope == "SPECIFIC_GROUP") grp?.linkedChatId.orEmpty() else ""
                                            )
                                            title = ""; message = ""
                                        },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                                    ) { Text("Send Broadcast Now", fontWeight = FontWeight.Medium) }
                                }
                            }
                        }
                        items(broadcasts, key = { it.id }) { bc ->
                            Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("📢 ${bc.title}", color = WaTextPrimary, fontWeight = FontWeight.Medium)
                                    Text(bc.message, color = WaTextSecondary, fontSize = 13.sp)
                                    Text("${bc.sentByName} • ${bc.sentAtLabel}", color = WaGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                AdminSection.ANALYTICS -> {
                    val paidMembers = contributions.sumOf { it.paidMembersCount }.coerceAtLeast(1)
                    val unpaidMembers = contributions.sumOf { (it.members.size - it.paidMembersCount).coerceAtLeast(0) }.coerceAtLeast(1)
                    val lateMembers = contributions.sumOf { it.members.sumOf { m -> m.lateCount } }.coerceAtLeast(1)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Users Growth Line Chart
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Users Growth (Last 6 Weeks)", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    val points = listOf(12f, 24f, 38f, 52f, 76f, (users.size * 20).coerceAtLeast(95).toFloat())
                                    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                                        val maxP = points.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                        val path = Path()
                                        points.forEachIndexed { idx, v ->
                                            val x = idx * stepX
                                            val y = size.height - ((v / maxP) * (size.height - 20f)) - 10f
                                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                            drawCircle(WaGreen, radius = 6f, center = Offset(x, y))
                                        }
                                        drawPath(path, color = WaGreen, style = Stroke(width = 4f, cap = StrokeCap.Round))
                                    }
                                }
                            }
                        }

                        // 2. Contributions Weekly Bar Chart
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Weekly Contributions Volume", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    val bars = listOf(45f, 68f, 82f, 60f, 95f, 110f)
                                    val maxBar = bars.maxOrNull() ?: 100f
                                    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                                        val slotW = size.width / bars.size
                                        bars.forEachIndexed { idx, v ->
                                            val h = (v / maxBar) * (size.height - 16f)
                                            drawRect(
                                                color = if (idx == bars.lastIndex) WaGreen else WaGold,
                                                topLeft = Offset(idx * slotW + slotW * 0.2f, size.height - h),
                                                size = Size(slotW * 0.6f, h)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Paid vs Pending vs Late Pie Chart
                        item {
                            Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Contributions Paid vs Pending vs Late", color = WaTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    val total = (paidMembers + unpaidMembers + lateMembers).toFloat()
                                    val paidAngle = 360f * (paidMembers / total)
                                    val pendingAngle = 360f * (unpaidMembers / total)
                                    val lateAngle = 360f - paidAngle - pendingAngle
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Canvas(modifier = Modifier.size(120.dp)) {
                                            drawArc(WaGreen, startAngle = -90f, sweepAngle = paidAngle, useCenter = true)
                                            drawArc(WaTextSecondary, startAngle = -90f + paidAngle, sweepAngle = pendingAngle, useCenter = true)
                                            drawArc(WaRed, startAngle = -90f + paidAngle + pendingAngle, sweepAngle = lateAngle, useCenter = true)
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("🟢 Paid: $paidMembers", color = WaGreen, fontSize = 13.sp)
                                            Text("⚪ Pending: $unpaidMembers", color = WaTextSecondary, fontSize = 13.sp)
                                            Text("🔴 Late: $lateMembers", color = WaRed, fontSize = 13.sp)
                                            Text("🛍️ Market Items: ${marketProducts.size} • Jobs: ${jobs.size}", color = WaTextSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.SETTINGS -> {
                    var appName by remember(adminConfig.appName) { mutableStateOf(adminConfig.appName) }
                    var feeStr by remember(adminConfig.transferFeePercent) { mutableStateOf(adminConfig.transferFeePercent.toString()) }
                    var lateStr by remember(adminConfig.defaultLateFee) { mutableStateOf(adminConfig.defaultLateFee.toInt().toString()) }
                    var defCurrency by remember(adminConfig.defaultCurrency) { mutableStateOf(adminConfig.defaultCurrency) }
                    var dailyPayStr by remember(localSettings.dailyPayLimit) { mutableStateOf(localSettings.dailyPayLimit.toInt().toString()) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(value = appName, onValueChange = { appName = it }, label = { Text("App Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = feeStr,
                            onValueChange = { feeStr = it },
                            label = { Text("Transfer / Escrow Fee %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = lateStr,
                            onValueChange = { lateStr = it.filter(Char::isDigit) },
                            label = { Text("Default Ajo Late Fee") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(value = defCurrency, onValueChange = { defCurrency = it }, label = { Text("Default Currency (₦ / $ / £)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = dailyPayStr,
                            onValueChange = { dailyPayStr = it.filter(Char::isDigit) },
                            label = { Text("Daily PGLOB Pay Transfer Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                runSensitiveAction("Save App Admin Settings") {
                                    viewModel.updateAppAdminConfig(
                                        adminConfig.copy(
                                            appName = appName.ifBlank { "PGLOB APP" },
                                            transferFeePercent = feeStr.toDoubleOrNull() ?: 0.5,
                                            defaultLateFee = lateStr.toDoubleOrNull() ?: 500.0,
                                            defaultCurrency = defCurrency.ifBlank { "₦" }
                                        )
                                    )
                                    dailyPayStr.toDoubleOrNull()?.let { viewModel.setDailyPayLimit(it) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
                        ) { Text("Save App Settings", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }

    if (pendingSensitiveSummary != null && pendingSensitiveAction != null) {
        PinOrBiometricConfirmSheet(
            title = "Admin Verification",
            summary = pendingSensitiveSummary!!,
            hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
            biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
            lockUntilMillis = localSettings.payLockUntilMillis,
            onDismiss = {
                pendingSensitiveSummary = null
                pendingSensitiveAction = null
            },
            onSetPin = { viewModel.setTransactionPin(it) },
            onConfirmWithPin = { pin, bio ->
                if (viewModel.verifyPinOrAllow(pin, bio)) {
                    pendingSensitiveAction?.invoke()
                    pendingSensitiveSummary = null
                    pendingSensitiveAction = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditSubAdminSheet(
    existing: AppSubAdmin?,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, username: String, roleLabel: String, perms: Set<AppAdminPermission>, uid: String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var email by remember { mutableStateOf(existing?.email.orEmpty()) }
    var username by remember { mutableStateOf(existing?.username.orEmpty()) }
    var roleLabel by remember { mutableStateOf(existing?.roleLabel ?: "Sub-Admin") }
    val selectedPerms = remember {
        mutableStateMapOf<AppAdminPermission, Boolean>().apply {
            val defaults = existing?.permissions ?: setOf(AppAdminPermission.VIEW_DASHBOARD, AppAdminPermission.MANAGE_CONTRIBUTIONS)
            AppAdminPermission.entries.forEach { p -> put(p, defaults.contains(p)) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WaCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(if (existing == null) "Add App Sub-Admin" else "Edit Sub-Admin Permissions", color = WaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("@username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = roleLabel, onValueChange = { roleLabel = it }, label = { Text("Role Label (e.g. Finance Sub-Admin)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            HorizontalDivider(color = WaDivider)
            Text("Sub-Admin Permissions (11 Controls)", color = WaGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            AppAdminPermission.entries.forEach { perm ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(perm.label, color = WaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(perm.description, color = WaTextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = selectedPerms[perm] == true,
                        onCheckedChange = { selectedPerms[perm] = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = WaPureBlack, checkedTrackColor = WaGreen)
                    )
                }
            }

            Button(
                onClick = {
                    val activeSet = selectedPerms.filterValues { it }.keys.toSet()
                    onSave(
                        name,
                        email,
                        username,
                        roleLabel,
                        activeSet,
                        existing?.uid ?: "sub_${System.currentTimeMillis().toString().takeLast(6)}"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack)
            ) {
                Text("Save Sub-Admin", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun KpiStatCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(color = WaCard, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(label, color = WaTextSecondary, fontSize = 12.sp, maxLines = 1)
        }
    }
}

private fun formatK(value: Double): String = if (value >= 1000.0) {
    "${(value / 1000.0).toInt()}k"
} else {
    value.toInt().toString()
}
