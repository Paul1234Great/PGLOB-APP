package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalAppSettings
import com.example.data.model.GlobalCatalogData
import com.example.data.model.PayTransaction
import com.example.data.model.PublicProfile
import com.example.data.model.UserPrivateProfile
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.PinOrBiometricConfirmSheet
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.CountryPayCatalog
import com.example.util.FlagSecureEffect
import kotlinx.coroutines.delay

@Composable
fun EarnWalletScreen(
    myProfile: UserPrivateProfile?,
    publicProfiles: List<PublicProfile>,
    transactions: List<PayTransaction>,
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel
) {
    val coins = myProfile?.coins ?: 100L
    val diamonds = myProfile?.diamonds ?: 1L
    val currency = myProfile?.currency ?: "₦"
    val approxValue = if (currency == "₦") (coins / 10.0) else (coins / 1000.0)

    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showSecretVault by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            // Luxury Gold Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF2B2100), Color(0xFF141000), PglobDarkGrey)))
                    .border(1.dp, PglobGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("EARN & REWARDS", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(color = PglobGold, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                "${myProfile?.vipTier ?: "FREE"} VIP",
                                color = PglobBlack,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("🪙 $coins Coins", color = PglobWhite, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Text("≈ $currency${String.format("%.2f", approxValue)}", color = PglobGreen, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("💎 $diamonds Diamonds", color = Color(0xFF70E4FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("100 coins = 1 Diamond", color = PglobGreyText, fontSize = 12.sp)
                        }
                    }
                    Text("1000 coins = ₦100 = $1 = £0.80 • Min withdraw: 5000 coins", color = PglobGreyText, fontSize = 12.sp)
                    Button(
                        onClick = { showWithdrawSheet = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("withdraw_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Withdraw Coins", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Daily Tasks
        item {
            Text("Earn More Tasks", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            val tasks = listOf(
                Triple("Daily Login Bonus", 50L, true),
                Triple("Watch Sponsored Ad", 20L, false),
                Triple("Refer a Global Friend", 500L, false),
                Triple("Complete Profile Milestone", 100L, false),
                Triple("Invite 5 Friends Challenge", 1000L, false)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { (title, reward, isDaily) ->
                    Surface(
                        color = PglobDarkGrey,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, color = PglobWhite, fontWeight = FontWeight.Bold)
                                Text("+$reward coins", color = PglobGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { viewModel.claimEarnTask(title, reward, isDailyLogin = isDaily) },
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                            ) { Text("Claim", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        // VIP Tiers + Secret Vault
        item {
            Text("VIP & Secret Vault", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).border(1.dp, PglobGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("GOLD VIP 👑", color = PglobGold, fontWeight = FontWeight.Bold)
                        Text("Gold border + tick + See Dating Likes", color = PglobGreyText, fontSize = 12.sp)
                        Button(
                            onClick = { viewModel.upgradeVip("GOLD", 1000L) },
                            colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("1000 🪙", fontWeight = FontWeight.Bold) }
                    }
                }
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFF70E4FF), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("DIAMOND VIP 💎", color = Color(0xFF70E4FF), fontWeight = FontWeight.Bold)
                        Text("Diamond crown + Secret Vault + Live VIP", color = PglobGreyText, fontSize = 12.sp)
                        Button(
                            onClick = { viewModel.upgradeVip("DIAMOND", 5000L) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70E4FF), contentColor = PglobBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("5000 🪙", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showSecretVault = true },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = PglobGold)
                Spacer(Modifier.width(8.dp))
                Text("Open Secret Vault (10s Disappearing Notes)", color = PglobGold, fontWeight = FontWeight.Bold)
            }
        }

        // Leaderboard Top 10 + Daily King/Queen
        item {
            Text("Global Leaderboard • Daily KING/QUEEN 👑", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            val leaders = remember(publicProfiles, myProfile) {
                val base = if (publicProfiles.isEmpty()) {
                    listOf(
                        PublicProfile(userId = "l1", displayName = "Amina Yusuf", countryFlag = "🇳🇬", coins = 12400L, isKingQueen = true),
                        PublicProfile(userId = "l2", displayName = "Marcus Vance", countryFlag = "🇺🇸", coins = 9800L),
                        PublicProfile(userId = "l3", displayName = "Priya Sharma", countryFlag = "🇮🇳", coins = 7500L),
                        PublicProfile(userId = "l4", displayName = "Kwame Mensah", countryFlag = "🇬🇭", coins = 5200L)
                    )
                } else publicProfiles
                base.sortedByDescending { it.coins }.take(10)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                leaders.forEachIndexed { idx, p ->
                    Surface(color = PglobDarkGrey, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (idx == 0) "👑 #1" else "#${idx + 1}",
                                color = if (idx == 0) PglobGold else PglobGreyText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )
                            AvatarCircle(name = p.displayName, size = 38.dp, badgeEmoji = p.countryFlag)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.displayName, color = PglobWhite, fontWeight = FontWeight.Bold)
                                if (idx == 0) Text("DAILY KING / QUEEN (+1000 bonus)", color = PglobGold, fontSize = 12.sp)
                            }
                            Text("🪙 ${p.coins}", color = PglobGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showWithdrawSheet) {
        WithdrawBottomSheet(
            myProfile = myProfile,
            transactions = transactions.filter { it.txType == "WITHDRAW" },
            localSettings = localSettings,
            viewModel = viewModel,
            onDismiss = { showWithdrawSheet = false }
        )
    }

    if (showSecretVault) {
        SecretVaultDialog(
            localSettings = localSettings,
            viewModel = viewModel,
            onDismiss = { showSecretVault = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    myProfile: UserPrivateProfile?,
    transactions: List<PayTransaction>,
    localSettings: LocalAppSettings = LocalAppSettings(),
    viewModel: PglobViewModel,
    onDismiss: () -> Unit
) {
    val coins = myProfile?.coins ?: 0L
    val currency = myProfile?.currency ?: "₦"
    val country = myProfile?.country ?: "Nigeria"
    val countryBanks = remember(country) { CountryPayCatalog.banksForCountry(country) }
    val methods = remember(country) {
        (CountryPayCatalog.methodsForCountry(country).filterNot { it == "PGLOB User" } +
            listOf("PayPal - USA/UK", "Cash App", "USDT Crypto", "Airtime")).distinct()
    }
    var selectedMethod by remember { mutableStateOf(methods.first()) }
    var selectedBank by remember { mutableStateOf(countryBanks.first()) }
    var showBankMenu by remember { mutableStateOf(false) }
    var destinationInput by remember { mutableStateOf("") }
    var verifiedAccountName by remember { mutableStateOf<String?>(null) }
    var verifyingAccount by remember { mutableStateOf(false) }
    var coinsAmountStr by remember { mutableStateOf("5000") }
    var showPinConfirm by remember { mutableStateOf(false) }

    val coinsToWithdraw = coinsAmountStr.toLongOrNull() ?: 0L
    val grossCash = if (currency == "₦") coinsToWithdraw / 10.0 else coinsToWithdraw / 1000.0
    val feeCash = grossCash * 0.10
    val netCash = grossCash - feeCash

    LaunchedEffect(destinationInput, selectedBank, selectedMethod) {
        if (destinationInput.length >= 6) {
            verifyingAccount = true
            delay(250L)
            verifiedAccountName = CountryPayCatalog.lookupAccountName(
                destinationInput,
                if (selectedMethod.contains("Bank", true)) selectedBank else selectedMethod,
                myProfile?.fullName ?: "PGLOB User"
            )
            verifyingAccount = false
        } else {
            verifiedAccountName = null
            verifyingAccount = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PglobDarkGrey) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Withdraw Coins", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Balance: $coins coins (${myProfile?.diamonds ?: 0} Diamonds)", color = PglobWhite)

            if (coins < 5000L) {
                Surface(color = PglobRed.copy(alpha = 0.16f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("You need 5000 coins to withdraw, keep earning!", color = PglobRed, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                        ) { Text("Earn More") }
                    }
                }
            }

            Text("Withdraw Method", color = PglobGreyText, fontSize = 13.sp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                methods.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { m ->
                            Surface(
                                color = if (selectedMethod == m) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { selectedMethod = m }
                            ) {
                                Text(
                                    m,
                                    color = if (selectedMethod == m) PglobBlack else PglobWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedMethod.contains("Bank", true)) {
                Box {
                    OutlinedButton(onClick = { showBankMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Bank: $selectedBank", color = PglobWhite)
                    }
                    DropdownMenu(expanded = showBankMenu, onDismissRequest = { showBankMenu = false }) {
                        countryBanks.forEach { b ->
                            DropdownMenuItem(text = { Text(b) }, onClick = { selectedBank = b; showBankMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = destinationInput,
                    onValueChange = { if (it.length <= 16 && it.all(Char::isDigit)) destinationInput = it },
                    label = { Text("Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val hint = when {
                    selectedMethod.contains("PayPal") -> "PayPal Email"
                    selectedMethod.contains("USDT") -> "USDT TRC20/ERC20 Wallet Address"
                    else -> "Account / Phone / Tag"
                }
                OutlinedTextField(
                    value = destinationInput,
                    onValueChange = { destinationInput = it },
                    label = { Text(hint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (verifyingAccount) {
                Text("Verifying account name...", color = PglobGold, fontSize = 12.sp)
            } else if (verifiedAccountName != null) {
                Text(
                    "✔️ Account Verified: $verifiedAccountName",
                    color = PglobGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = coinsAmountStr,
                onValueChange = { coinsAmountStr = it.filter(Char::isDigit) },
                label = { Text("Coins to Withdraw (Min 5000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(color = PglobBlack, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Withdraw: $currency${String.format("%.2f", grossCash)}", color = PglobWhite)
                    Text("Fee (10%): $currency${String.format("%.2f", feeCash)}", color = PglobGreyText)
                    Text("You Receive: $currency${String.format("%.2f", netCash)}", color = PglobGreen, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { showPinConfirm = true },
                enabled = coins >= 5000L && coinsToWithdraw in 5000L..coins && destinationInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) {
                Text("Confirm Withdraw", fontWeight = FontWeight.ExtraBold)
            }

            if (transactions.isNotEmpty()) {
                Text("Withdraw History", color = PglobGold, fontWeight = FontWeight.Bold)
                transactions.take(5).forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${tx.method} (${tx.destination})", color = PglobWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("${tx.currency}${tx.amount.toInt()} • ${tx.status}", color = PglobGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPinConfirm) {
        PinOrBiometricConfirmSheet(
            title = "Authorize Withdrawal",
            summary = "Withdraw $coinsToWithdraw coins ($currency${String.format("%.2f", netCash)}) via $selectedMethod",
            hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
            biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
            lockUntilMillis = localSettings.payLockUntilMillis,
            isLargeTransfer = netCash >= CountryPayCatalog.largeTransferThreshold(currency),
            onDismiss = { showPinConfirm = false },
            onSetPin = { viewModel.setTransactionPin(it) },
            onConfirmWithPin = { pin, bio ->
                if (viewModel.verifyPinOrAllow(pin, bio)) {
                    viewModel.requestWithdrawCoins(
                        coinsToWithdraw = coinsToWithdraw,
                        amountCurrency = netCash,
                        feeCurrency = feeCash,
                        method = if (selectedMethod.contains("Bank", true)) selectedBank else selectedMethod,
                        destination = destinationInput
                    )
                    showPinConfirm = false
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun SecretVaultDialog(
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel,
    onDismiss: () -> Unit
) {
    FlagSecureEffect(true)
    var unlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var vaultText by remember(localSettings.secretVaultNotes) { mutableStateOf(localSettings.secretVaultNotes) }
    var timerSec by remember { mutableStateOf(10) }

    LaunchedEffect(unlocked) {
        if (unlocked) {
            timerSec = 10
            while (timerSec > 0 && unlocked) {
                delay(1000L)
                timerSec--
            }
            if (timerSec == 0) {
                unlocked = false
                onDismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PglobBlack,
        title = {
            Text(
                if (unlocked) "Secret Vault • Auto-closes in ${timerSec}s" else "Secret Vault 🔒",
                color = PglobGold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (!unlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (localSettings.secretVaultPin.isBlank()) "Create a 4-digit Secret Vault PIN"
                        else "Enter Secret Vault PIN (Disappears in 10s • No Screenshots)",
                        color = PglobGreyText
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinInput = it },
                        label = { Text("4-Digit PIN") },
                        singleLine = true
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚠️ Screenshot warning: Hidden vault active", color = PglobRed, fontSize = 12.sp)
                    OutlinedTextField(
                        value = vaultText,
                        onValueChange = { vaultText = it },
                        label = { Text("Private Secret Notes") },
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!unlocked) {
                        if (localSettings.secretVaultPin.isBlank() && pinInput.length == 4) {
                            viewModel.setSecretVaultPin(pinInput)
                            unlocked = true
                        } else if (pinInput == localSettings.secretVaultPin) {
                            unlocked = true
                        } else {
                            viewModel.showToast("Wrong Vault PIN")
                        }
                    } else {
                        viewModel.saveSecretVaultNotes(vaultText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) {
                Text(if (!unlocked) "Unlock" else "Save & Lock")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = PglobWhite) } }
    )
}

@Composable
fun PglobPayScreen(
    myProfile: UserPrivateProfile?,
    transactions: List<PayTransaction>,
    localSettings: LocalAppSettings = LocalAppSettings(),
    viewModel: PglobViewModel,
    canAccessAdmin: Boolean = false,
    onOpenContributions: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    onOpenOrderHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val currency = myProfile?.currency ?: localSettings.savedCurrency.ifBlank { "₦" }
    val balance = myProfile?.walletBalance ?: localSettings.localWalletBalance
    val acctNum = (myProfile?.payAccountNumber?.ifBlank { localSettings.pglobAccountNumber }
        ?: localSettings.pglobAccountNumber).ifBlank { "9000123456" }
    val acctOwnerName = myProfile?.fullName?.ifBlank { localSettings.savedDisplayName }
        ?: localSettings.savedDisplayName.ifBlank { "PGLOB Member" }
    val hideBalance = localSettings.hideBalance

    var activeAction by remember { mutableStateOf<String?>(null) } // SEND, REQUEST, ADD, AIRTIME, DATA, BILL
    var showWithdraw by remember { mutableStateOf(false) }
    var showAccountQr by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(WaCard)
                    .border(1.dp, WaGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PGLOB Pay", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 17.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🪙 ${myProfile?.coins ?: 0} Coins", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            if (canAccessAdmin) {
                                IconButton(
                                    onClick = onOpenAdminDashboard,
                                    modifier = Modifier.size(32.dp).testTag("pay_admin_crown_button")
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.setHideBalance(!hideBalance) },
                                modifier = Modifier.size(32.dp).testTag("toggle_hide_balance")
                            ) {
                                Icon(
                                    imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (hideBalance) "Show balance" else "Hide balance",
                                    tint = WaGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Text(
                        if (hideBalance) "$currency ••••••" else "$currency${String.format("%,.2f", balance)}",
                        color = PglobWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    )
                    Surface(
                        color = Color(0xFF141100),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PglobGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("My Account Number (For Money ONLY)", color = PglobGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(acctNum, color = PglobWhite, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 1.sp)
                            Text("Bank: PGLOB Wallet • Account Name: $acctOwnerName", color = WaTextSecondary, fontSize = 12.sp)
                            Text("⚠️ DO NOT share your Chatting Number for money", color = PglobRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("PGLOB Account", acctNum))
                                        viewModel.showToast("Account number $acctNum copied!")
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("pay_copy_account_btn")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy account", tint = PglobGold, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy", color = PglobGold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Send money to my PGLOB Wallet:\nAccount Number: $acctNum\nAccount Name: $acctOwnerName\nBank: PGLOB Wallet")
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share PGLOB Account Number"))
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("pay_share_account_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share account", tint = PglobGold, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share", color = PglobGold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { showAccountQr = true },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("pay_qr_account_btn")
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = "Show QR", tint = WaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("QR", color = WaGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.executePayAction(
                                txType = "ADD",
                                amount = 5000.0,
                                fee = 0.0,
                                method = "PGLOB Instant Test",
                                destination = acctNum,
                                note = "Instant Test Credit"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("add_test_money_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Add Test Money ($currency 5,000)", color = PglobGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Group Contribution (Ajo / Esusu) Entry Card inside PGLOB Pay
        item {
            Surface(
                color = WaCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WaGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onOpenContributions() }
                    .testTag("pay_open_contributions_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("💰 Group Contribution (Ajo / Esusu)", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Save with friends, track turns & auto-payout with PGLOB Escrow", color = WaTextSecondary, fontSize = 12.sp)
                    }
                    Surface(color = WaGreen, shape = RoundedCornerShape(12.dp)) {
                        Text("Open →", color = WaPureBlack, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }

        // 8 Action Buttons Grid
        item {
            val actions = listOf(
                Triple("Send Money", Icons.Default.ArrowUpward) { activeAction = "SEND" },
                Triple("Request", Icons.Default.RequestQuote) { activeAction = "REQUEST" },
                Triple("Add Money", Icons.Default.AddCard) { activeAction = "ADD" },
                Triple("Withdraw", Icons.Default.ArrowDownward) { showWithdraw = true },
                Triple("Buy Airtime", Icons.Default.PhoneAndroid) { activeAction = "AIRTIME" },
                Triple("Buy Data", Icons.Default.Wifi) { activeAction = "DATA" },
                Triple("Pay Bill", Icons.AutoMirrored.Filled.ReceiptLong) { activeAction = "BILL" },
                Triple("Banks", Icons.Default.AccountBalance) { activeAction = "SEND" }
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (label, icon, onClick) ->
                            Surface(
                                color = PglobDarkGrey,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).clickable { onClick() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(icon, contentDescription = label, tint = PglobGold, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(label, color = PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = PglobGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pay History", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                OutlinedButton(
                    onClick = onOpenOrderHistory,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("pay_open_orders_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Order History", tint = WaGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Order History", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Text("No Pay transactions yet. Tap Add Money or Send Money above!", color = PglobGreyText)
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Surface(color = PglobDarkGrey, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${tx.txType} • ${tx.method}", color = PglobWhite, fontWeight = FontWeight.Bold)
                            Text("${tx.destination} • ${tx.note}", color = PglobGreyText, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val sign = if (tx.txType == "ADD" || tx.txType == "RECEIVE") "+" else "-"
                            Text(
                                "$sign${tx.currency}${tx.amount.toInt()}",
                                color = if (sign == "+") PglobGreen else PglobGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(tx.status, color = PglobGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    if (activeAction != null) {
        PayActionModal(
            mode = activeAction!!,
            myProfile = myProfile,
            localSettings = localSettings,
            viewModel = viewModel,
            onDismiss = { activeAction = null }
        )
    }

    if (showWithdraw) {
        WithdrawBottomSheet(
            myProfile = myProfile,
            transactions = transactions.filter { it.txType == "WITHDRAW" },
            localSettings = localSettings,
            viewModel = viewModel,
            onDismiss = { showWithdraw = false }
        )
    }

    if (showAccountQr) {
        AlertDialog(
            onDismissRequest = { showAccountQr = false },
            containerColor = WaCard,
            title = { Text("PGLOB Pay Account QR", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(acctOwnerName, color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                        Canvas(modifier = Modifier.size(160.dp).padding(14.dp)) {
                            val cells = 11
                            val cellSize = size.width / cells
                            for (r in 0 until cells) {
                                for (c in 0 until cells) {
                                    val isFinder = (r < 3 && c < 3) || (r < 3 && c >= cells - 3) || (r >= cells - 3 && c < 3)
                                    val bit = ((acctNum[(r * cells + c) % acctNum.length].code + r * 5 + c * 11) % 3) != 0
                                    if (isFinder || bit) {
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
                    Text(acctNum, color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Bank: PGLOB Wallet • DO NOT share Chatting Number for money", color = WaTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("PGLOB Account", acctNum))
                        viewModel.showToast("Account number $acctNum copied!")
                        showAccountQr = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Copy Account", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAccountQr = false }) { Text("Close", color = WaTextPrimary) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayActionModal(
    mode: String,
    myProfile: UserPrivateProfile?,
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel,
    onDismiss: () -> Unit
) {
    val currency = myProfile?.currency ?: "₦"
    val country = myProfile?.country ?: "Nigeria"
    val countryMethods = remember(country) { CountryPayCatalog.methodsForCountry(country) }
    val countryBanks = remember(country) { CountryPayCatalog.banksForCountry(country) }

    var destination by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedBank by remember(countryBanks) { mutableStateOf(countryBanks.first()) }
    var showBankDropdown by remember { mutableStateOf(false) }
    var verifiedRecipientName by remember { mutableStateOf<String?>(null) }
    var verifyingRecipient by remember { mutableStateOf(false) }
    var showSummaryConfirm by remember { mutableStateOf(false) }
    var showMyAcctHelper by remember { mutableStateOf(false) }
    val myAccountNum = (myProfile?.payAccountNumber?.ifBlank { localSettings.pglobAccountNumber }
        ?: localSettings.pglobAccountNumber).ifBlank { "9000123456" }
    val enteredChatPhone = mode == "SEND" && viewModel.isRecipientChattingPhone(destination)

    val options = when (mode) {
        "AIRTIME", "DATA" -> listOf("MTN", "GLO", "Airtel", "9mobile")
        "BILL" -> listOf("DSTV", "GOTV", "Startimes", "NEPA/PHCN", "Water Bill")
        "ADD" -> listOf("Bank Transfer", "Debit Card", "Opay/Palmpay")
        "SEND" -> countryMethods
        else -> listOf("PGLOB User")
    }
    var selectedSubOption by remember(mode, options) { mutableStateOf(options.first()) }
    var selectedDataPlan by remember { mutableStateOf("1GB ₦500" to 500.0) }
    var showPinConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(destination, selectedSubOption, selectedBank, mode) {
        if (mode == "SEND" && destination.length >= 6) {
            verifyingRecipient = true
            delay(250L)
            verifiedRecipientName = CountryPayCatalog.lookupAccountName(
                destination,
                if (selectedSubOption.contains("Bank", true)) selectedBank else selectedSubOption,
                myProfile?.fullName ?: "PGLOB Member"
            )
            verifyingRecipient = false
        } else {
            verifiedRecipientName = null
            verifyingRecipient = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PglobDarkGrey) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                when (mode) {
                    "SEND" -> "Send Money ($country)"
                    "REQUEST" -> "Request Money"
                    "ADD" -> "Add Money to Wallet"
                    "AIRTIME" -> "Buy Airtime"
                    "DATA" -> "Buy Data Bundle"
                    else -> "Pay Utility Bill"
                },
                color = PglobGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                options.forEach { opt ->
                    Surface(
                        color = if (selectedSubOption == opt) PglobGold else PglobBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { selectedSubOption = opt }
                    ) {
                        Text(
                            opt,
                            color = if (selectedSubOption == opt) PglobBlack else PglobWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            if (mode == "SEND" && selectedSubOption != "PGLOB User") {
                Box {
                    OutlinedButton(onClick = { showBankDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Institution: $selectedBank", color = PglobWhite)
                    }
                    DropdownMenu(expanded = showBankDropdown, onDismissRequest = { showBankDropdown = false }) {
                        countryBanks.forEach { b ->
                            DropdownMenuItem(text = { Text(b) }, onClick = { selectedBank = b; showBankDropdown = false })
                        }
                    }
                }
            }

            if (mode == "ADD" && selectedSubOption == "Bank Transfer") {
                Text(
                    "Transfer to PGLOB Virtual Account: ${myProfile?.payAccountNumber ?: "1234567890"} (Instant credit)",
                    color = PglobGreen,
                    fontSize = 13.sp
                )
            }

            if (mode == "DATA") {
                val plans = listOf("100MB ₦100" to 100.0, "500MB ₦300" to 300.0, "1GB ₦500" to 500.0, "5GB ₦2000" to 2000.0)
                plans.forEach { plan ->
                    Surface(
                        color = if (selectedDataPlan == plan) PglobGold.copy(alpha = 0.25f) else PglobBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedDataPlan = plan
                            amountStr = plan.second.toInt().toString()
                        }
                    ) {
                        Text(plan.first, color = PglobWhite, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (mode != "ADD") {
                val label = when (mode) {
                    "AIRTIME", "DATA" -> "Phone Number"
                    "BILL" -> "Smartcard / Meter Number"
                    else -> if (selectedSubOption == "PGLOB User") "PGLOB Account Number (10 digits) / @Tag" else "Account Number (10 digits)"
                }
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text(label) },
                    isError = enteredChatPhone,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pay_destination_input")
                )
                if (enteredChatPhone) {
                    Surface(
                        color = PglobRed.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, PglobRed, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = PglobRed, modifier = Modifier.size(18.dp))
                                Text(
                                    "Wrong number - This is Chatting Number, NOT Account Number.",
                                    color = PglobRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                "Use a 10-digit PGLOB Account Number for money (e.g. yours is $myAccountNum). Phone numbers starting with + or matching Chatting Numbers cannot receive Pay transfers.",
                                color = PglobWhite,
                                fontSize = 12.sp
                            )
                            OutlinedButton(
                                onClick = { showMyAcctHelper = !showMyAcctHelper },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Show My Account Number", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            if (showMyAcctHelper) {
                                Text("My PGLOB Account Number: $myAccountNum (Bank: PGLOB Wallet)", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else if (verifyingRecipient) {
                    Text("Checking account details...", color = PglobGold, fontSize = 12.sp)
                } else if (verifiedRecipientName != null) {
                    Text("✔️ Verified: $verifiedRecipientName", color = PglobGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (mode != "DATA") {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter(Char::isDigit) },
                    label = { Text("Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Reference") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val amt = amountStr.toDoubleOrNull() ?: 0.0
            val fee = if (mode == "SEND") 10.0 else 0.0
            if (amt > 0 && mode == "SEND") {
                Text("Fee: $currency${fee.toInt()} • Receiver gets $currency${(amt - fee).coerceAtLeast(0.0).toInt()}", color = PglobGreyText)
            }

            Button(
                onClick = {
                    if (mode == "ADD" || mode == "REQUEST") {
                        viewModel.executePayAction(
                            txType = mode,
                            amount = amt,
                            fee = 0.0,
                            method = selectedSubOption,
                            destination = destination.ifBlank { myAccountNum },
                            note = note.ifBlank { mode }
                        )
                        onDismiss()
                    } else {
                        showSummaryConfirm = true
                    }
                },
                enabled = !enteredChatPhone && amt > 0.0 && (mode == "ADD" || destination.isNotBlank()),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("pay_action_continue_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) {
                Text("Continue", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showSummaryConfirm) {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        val fee = if (mode == "SEND") 10.0 else 0.0
        val resolvedMethod = if (mode == "SEND" && selectedSubOption != "PGLOB User") "$selectedSubOption ($selectedBank)" else selectedSubOption
        AlertDialog(
            onDismissRequest = { showSummaryConfirm = false },
            containerColor = PglobDarkGrey,
            title = { Text("Confirm Payment Summary", color = PglobGold, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Action: $mode", color = PglobWhite, fontWeight = FontWeight.Bold)
                    Text("Method: $resolvedMethod", color = PglobGreyText)
                    Text("To: ${verifiedRecipientName ?: destination}", color = PglobGreen, fontWeight = FontWeight.Bold)
                    Text("Amount: $currency${amt.toInt()} (Fee: $currency${fee.toInt()})", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    if (note.isNotBlank()) Text("Note: $note", color = PglobGreyText, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSummaryConfirm = false
                        showPinConfirm = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Proceed to PIN / Biometric", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryConfirm = false }) { Text("Edit", color = PglobWhite) }
            }
        )
    }

    if (showPinConfirm) {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        val fee = if (mode == "SEND") 10.0 else 0.0
        val resolvedMethod = if (mode == "SEND" && selectedSubOption != "PGLOB User") "$selectedSubOption ($selectedBank)" else selectedSubOption
        PinOrBiometricConfirmSheet(
            title = "Authorize $mode",
            summary = "Confirm $currency${amt.toInt()} to ${verifiedRecipientName ?: destination} ($resolvedMethod)",
            hasPinConfigured = !myProfile?.transactionPin.isNullOrBlank(),
            biometricEnabled = localSettings.biometricEnabled || myProfile?.fingerprintEnabled == true,
            lockUntilMillis = localSettings.payLockUntilMillis,
            isLargeTransfer = amt >= CountryPayCatalog.largeTransferThreshold(currency),
            onDismiss = { showPinConfirm = false },
            onSetPin = { viewModel.setTransactionPin(it) },
            onConfirmWithPin = { pin, bio ->
                if (viewModel.verifyPinOrAllow(pin, bio)) {
                    viewModel.executePayAction(
                        txType = mode,
                        amount = amt,
                        fee = fee,
                        method = resolvedMethod,
                        destination = verifiedRecipientName ?: destination,
                        note = note.ifBlank { resolvedMethod }
                    )
                    showPinConfirm = false
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun PayReceiptSuccessDialog(
    tx: PayTransaction,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val summaryText = "PGLOB PAY Receipt\nRef ID: ${tx.id}\nType: ${tx.txType}\nMethod: ${tx.method}\nTo: ${tx.destination}\nAmount: ${tx.currency}${String.format("%,.2f", tx.amount)}\nFee: ${tx.currency}${String.format("%,.2f", tx.fee)}\nStatus: ${tx.status}"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PglobDarkGrey,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                com.example.ui.components.PglobBrandLogo(size = 60.dp)
                Spacer(Modifier.height(10.dp))
                Text("Transaction ${tx.status}!", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Surface(
                color = PglobBlack,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, PglobGold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${tx.currency}${String.format("%,.2f", tx.amount)}", color = PglobGreen, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Text("Reference: ${tx.id}", color = PglobGreyText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("Type: ${tx.txType}", color = PglobWhite, fontSize = 13.sp)
                    Text("Method: ${tx.method}", color = PglobWhite, fontSize = 13.sp)
                    Text("Destination: ${tx.destination}", color = PglobWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Fee: ${tx.currency}${String.format("%,.2f", tx.fee)}", color = PglobGreyText, fontSize = 12.sp)
                    if (tx.note.isNotBlank()) Text("Note: ${tx.note}", color = PglobGold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) { Text("Done", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summaryText)
                }
                context.startActivity(Intent.createChooser(share, "Share PGLOB PAY Receipt"))
            }) {
                Icon(Icons.Default.Share, contentDescription = null, tint = PglobGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Share Receipt", color = PglobGold)
            }
        }
    )
}
