package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.example.data.model.CountryInfo
import com.example.data.model.GlobalCatalogData
import com.example.ui.AiUiState
import com.example.ui.theme.PglobDiamondCyan
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaBlueTick
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaRed
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.FlagSecureEffect
import com.example.util.InAppNotificationItem
import com.example.util.PolicyCatalog
import kotlinx.coroutines.delay
import java.io.File

fun createTempCameraUri(context: Context, prefix: String = "pglob_cam_", ext: String = ".jpg"): Uri {
    val file = File.createTempFile(prefix, ext, context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun launchBiometricPrompt(
    context: Context,
    title: String = "Unlock PGLOB APP",
    subtitle: String = "Use Fingerprint, Face Unlock, or Device Screen Lock",
    onSuccess: () -> Unit,
    onFallbackOrError: (String) -> Unit
) {
    val activity = context as? FragmentActivity
    if (activity == null) {
        onFallbackOrError("Biometric prompt requires FragmentActivity")
        return
    }
    val manager = BiometricManager.from(activity)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val canAuth = manager.canAuthenticate(authenticators)
    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
        onFallbackOrError("No fingerprint/face enrolled on this device. You can use your 4-digit PIN or enroll biometrics in Settings.")
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.w("Biometric", "Auth error $errorCode: $errString")
                onFallbackOrError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFallbackOrError("Biometric did not match")
            }
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(authenticators)
        .build()
    prompt.authenticate(info)
}

@Composable
fun PglobProfileImage(
    name: String,
    photoUri: String,
    modifier: Modifier = Modifier,
    fallbackFontSizeSp: Float = 20f
) {
    val decodedBitmap: ImageBitmap? = remember(photoUri) {
        if (!photoUri.startsWith("data:image/")) null
        else try {
            val base64Part = photoUri.substringAfter("base64,", "")
            if (base64Part.isBlank()) null
            else {
                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
        } catch (_: Throwable) {
            null
        }
    }
    if (decodedBitmap != null) {
        Image(
            bitmap = decodedBitmap,
            contentDescription = "$name photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else if (photoUri.isNotBlank() && !photoUri.startsWith("data:image/")) {
        AsyncImage(
            model = Uri.parse(photoUri),
            contentDescription = "$name photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = name.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "PG" }
        Box(modifier = modifier.background(WaInput), contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                color = WaTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = fallbackFontSizeSp.sp
            )
        }
    }
}

@Composable
fun AvatarCircle(
    name: String,
    photoUri: String = "",
    size: Dp = 48.dp,
    showOnlineDot: Boolean = false,
    vipTier: String = "FREE",
    badgeEmoji: String? = null,
    hasStatusRing: Boolean = false
) {
    val borderColor = when {
        hasStatusRing -> WaGreen
        vipTier == "DIAMOND" -> PglobDiamondCyan
        vipTier == "GOLD" -> WaGold
        else -> Color.Transparent
    }
    val initials = name.trim().split(" ").filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "PG" }

    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(WaInput)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(2.dp, borderColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri.isNotBlank()) {
                PglobProfileImage(
                    name = name,
                    photoUri = photoUri,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    fallbackFontSizeSp = size.value * 0.34f
                )
            } else {
                Text(
                    text = initials,
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.value * 0.34f).sp
                )
            }
        }
        if (showOnlineDot) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(WaGreen)
                    .border(2.dp, WaBackground, CircleShape)
            )
        }
        if (!badgeEmoji.isNullOrBlank()) {
            Text(
                text = badgeEmoji,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // Kept as a lightweight no-op hook for API compatibility; PGLOB UI stays clean without clutter
}

@Composable
fun OtpSixBoxVerification(
    targetLabel: String,
    onVerified: () -> Unit,
    onChangeTarget: (() -> Unit)? = null
) {
    var otpValue by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("SMS") }
    var countdown by remember { mutableIntStateOf(30) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WaCard, RoundedCornerShape(16.dp))
            .border(1.dp, WaDivider, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "We sent $channel OTP to $targetLabel",
                color = WaTextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (onChangeTarget != null) {
                TextButton(onClick = onChangeTarget) {
                    Text("Change", color = WaGreen)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until 6) {
                val ch = otpValue.getOrNull(i)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(WaInput, RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (ch.isNotEmpty()) WaGreen else WaDivider,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ch,
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
            }
        }

        OutlinedTextField(
            value = otpValue,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }.take(6)
                otpValue = digits
                errorMsg = ""
            },
            label = { Text("Enter 6-digit OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WaGreen,
                unfocusedBorderColor = WaDivider,
                focusedTextColor = WaTextPrimary,
                unfocusedTextColor = WaTextPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { otpValue = "123456"; errorMsg = "" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WaGreen)
            ) {
                Text("Demo 123456", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = {
                    channel = "SMS"
                    countdown = 30
                },
                enabled = countdown == 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (countdown > 0) "00:${countdown.toString().padStart(2, '0')}" else "SMS", maxLines = 1)
            }
            OutlinedButton(
                onClick = {
                    channel = "CALL"
                    countdown = 30
                },
                enabled = countdown == 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("Call Me", maxLines = 1)
            }
        }

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = WaRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                if (otpValue.length == 6) {
                    onVerified()
                } else {
                    errorMsg = "Enter 6 digits or tap Demo 123456"
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
        ) {
            Text("Verify OTP", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onDismiss: () -> Unit,
    onSelect: (CountryInfo) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search) {
        if (search.isBlank()) GlobalCatalogData.countries
        else GlobalCatalogData.countries.filter {
            it.name.contains(search, ignoreCase = true) || it.dialCode.contains(search)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WaCard
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Select Country", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search 115+ countries...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WaInput,
                    unfocusedContainerColor = WaInput,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = WaTextPrimary,
                    unfocusedTextColor = WaTextPrimary
                )
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                items(filtered) { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(c); onDismiss() }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(c.flag, fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(c.name, color = WaTextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text("${c.dialCode} (${c.currency})", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = WaDivider, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun FourBoxPinDisplay(pinValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        for (i in 0 until 4) {
            val filled = i < pinValue.length
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(WaInput, RoundedCornerShape(12.dp))
                    .border(
                        1.5.dp,
                        if (filled) WaGreen else WaDivider,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filled) "●" else "",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinOrBiometricConfirmSheet(
    title: String,
    summary: String,
    hasPinConfigured: Boolean,
    biometricEnabled: Boolean,
    lockUntilMillis: Long = 0L,
    isLargeTransfer: Boolean = false,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onConfirmWithPin: (pin: String, isBiometric: Boolean) -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var showForgotOtp by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var largeTransferAck by remember { mutableStateOf(!isLargeTransfer) }
    var remLockSeconds by remember(lockUntilMillis) {
        mutableIntStateOf(((lockUntilMillis - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0))
    }

    LaunchedEffect(lockUntilMillis) {
        while (true) {
            val rem = ((lockUntilMillis - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
            remLockSeconds = rem
            if (rem <= 0) break
            delay(1000L)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WaCard
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = "Security", tint = WaGreen)
                Spacer(Modifier.width(8.dp))
                Text(title, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            }
            Text(summary, color = WaTextSecondary, fontSize = 14.sp)

            Surface(
                color = WaBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = WaGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PGLOB APP will never ask you for your PIN or OTP.",
                        color = WaTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (isLargeTransfer && !largeTransferAck) {
                Surface(
                    color = WaRed.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, WaRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WaRed)
                            Spacer(Modifier.width(6.dp))
                            Text("Large Transfer Check", color = WaRed, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            "Please double-check the recipient name and account before authorizing this transfer.",
                            color = WaTextPrimary,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = { largeTransferAck = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("I Verified Recipient — Continue", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else if (showForgotOtp) {
                OtpSixBoxVerification(
                    targetLabel = "Your Registered Phone / Email",
                    onVerified = {
                        showForgotOtp = false
                        pin = ""
                        newPin = ""
                        confirmNewPin = ""
                        errorMsg = "OTP Verified. Enter your new 4-digit PIN below."
                    },
                    onChangeTarget = { showForgotOtp = false }
                )
            } else if (!hasPinConfigured) {
                Text(
                    "Create and confirm your 4-digit Transaction PIN:",
                    color = WaTextSecondary,
                    fontSize = 13.sp
                )
                FourBoxPinDisplay(if (newPin.length < 4) newPin else confirmNewPin)
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { newPin = it; errorMsg = null } },
                    label = { Text("Create 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { confirmNewPin = it; errorMsg = null } },
                    label = { Text("Confirm 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) Text(errorMsg!!, color = WaRed, fontSize = 13.sp)
                Button(
                    onClick = {
                        if (newPin.length != 4 || newPin != confirmNewPin) {
                            errorMsg = "Both 4-digit PIN fields must match"
                        } else {
                            onSetPin(newPin)
                            onConfirmWithPin(newPin, false)
                        }
                    },
                    enabled = newPin.length == 4 && confirmNewPin.length == 4,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
                ) {
                    Text("Save PIN & Authorize", fontWeight = FontWeight.Medium)
                }
            } else {
                FourBoxPinDisplay(pin)
                if (remLockSeconds > 0) {
                    val mins = remLockSeconds / 60
                    val secs = remLockSeconds % 60
                    Text(
                        "PIN locked for ${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}. Use Fingerprint / Face Unlock or wait.",
                        color = WaRed,
                        fontSize = 13.sp
                    )
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { pin = it; errorMsg = null } },
                    label = { Text("Enter 4-digit Transaction PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    enabled = remLockSeconds == 0,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) Text(errorMsg!!, color = WaRed, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onConfirmWithPin(pin, false) },
                        enabled = pin.length == 4 && remLockSeconds == 0,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
                    ) {
                        Text("Confirm PIN", fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(
                        onClick = {
                            launchBiometricPrompt(
                                context = context,
                                title = "Authorize with Biometrics",
                                subtitle = summary,
                                onSuccess = { onConfirmWithPin("", true) },
                                onFallbackOrError = { err -> errorMsg = err }
                            )
                        },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint / Face", tint = WaGreen)
                        Spacer(Modifier.width(4.dp))
                        Text(if (biometricEnabled) "Biometric" else "Unlock", color = WaGreen, fontSize = 13.sp)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showForgotOtp = true }) {
                        Text("Forgot PIN? Use OTP", color = WaGreen, fontSize = 13.sp)
                    }
                    TextButton(onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                        } catch (_: Exception) {}
                    }) {
                        Text("Device Biometrics", color = WaTextSecondary, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun AppLockScreen(
    displayName: String,
    photoUri: String,
    savedPin: String,
    biometricEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    onResetPinWithOtp: (String) -> Unit,
    onSwitchAccount: () -> Unit
) {
    FlagSecureEffect(true)
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var lockSeconds by remember { mutableIntStateOf(0) }
    var showForgotOtp by remember { mutableStateOf(false) }
    var settingNewPin by remember { mutableStateOf(false) }

    LaunchedEffect(lockSeconds) {
        if (lockSeconds > 0) {
            delay(1000L)
            lockSeconds--
        }
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled) {
            launchBiometricPrompt(
                context = context,
                title = "Unlock PGLOB APP",
                subtitle = "Use Fingerprint or Face Unlock to enter",
                onSuccess = onUnlockSuccess,
                onFallbackOrError = {}
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PglobBrandLogo(size = 84.dp)
            Text("PGLOB APP", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 22.sp)
            AvatarCircle(name = displayName.ifBlank { "Member" }, photoUri = photoUri, size = 72.dp)
            Text(
                if (displayName.isNotBlank()) "Unlock as $displayName" else "PGLOB APP Locked",
                color = WaTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            if (showForgotOtp) {
                OtpSixBoxVerification(
                    targetLabel = "Your Registered Phone / Email",
                    onVerified = {
                        showForgotOtp = false
                        settingNewPin = true
                        pinInput = ""
                        errorMsg = "Create a new 4-digit App & Pay PIN"
                    },
                    onChangeTarget = { showForgotOtp = false }
                )
            } else {
                FourBoxPinDisplay(pinInput)
                if (lockSeconds > 0) {
                    Text(
                        "Locked for 00:${lockSeconds.toString().padStart(2, '0')} — Use Biometrics or wait",
                        color = WaRed,
                        fontSize = 13.sp
                    )
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = WaRed, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4 && it.all(Char::isDigit)) {
                            pinInput = it
                            errorMsg = null
                        }
                    },
                    label = { Text(if (settingNewPin) "Enter New 4-Digit PIN" else "Enter 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    enabled = lockSeconds == 0,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        when {
                            settingNewPin && pinInput.length == 4 -> {
                                onResetPinWithOtp(pinInput)
                                onUnlockSuccess()
                            }
                            savedPin.isBlank() && pinInput.length == 4 -> {
                                onResetPinWithOtp(pinInput)
                                onUnlockSuccess()
                            }
                            pinInput == savedPin -> {
                                wrongCount = 0
                                onUnlockSuccess()
                            }
                            else -> {
                                wrongCount++
                                errorMsg = if (wrongCount >= 3) {
                                    lockSeconds = 60
                                    "Wrong PIN 3 times. Wait 60s or use Fingerprint / Face Unlock."
                                } else {
                                    "Wrong PIN (${3 - wrongCount} attempts left)"
                                }
                            }
                        }
                    },
                    enabled = pinInput.length == 4 && lockSeconds == 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
                ) {
                    Text(if (settingNewPin) "Save PIN & Unlock" else "Unlock with PIN", fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = {
                        launchBiometricPrompt(
                            context = context,
                            title = "Unlock PGLOB APP",
                            subtitle = "Fingerprint / Face ID / Device Lock",
                            onSuccess = onUnlockSuccess,
                            onFallbackOrError = { errorMsg = it }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric Unlock", tint = WaGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Use Fingerprint / Face Unlock", color = WaTextPrimary, fontWeight = FontWeight.Medium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { showForgotOtp = true }) {
                        Text("Forgot PIN? Use OTP", color = WaGreen)
                    }
                    TextButton(onClick = onSwitchAccount) {
                        Text("Switch Account", color = WaTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun PoliciesListDialog(
    initialDocId: String? = null,
    onDismiss: () -> Unit
) {
    var selectedDoc by remember {
        mutableStateOf(
            PolicyCatalog.allPolicies.firstOrNull { it.id == initialDocId } ?: PolicyCatalog.allPolicies.first()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WaCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = WaGreen)
                Spacer(Modifier.width(8.dp))
                Text(selectedDoc.title, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text(selectedDoc.body, color = WaTextPrimary, fontSize = 13.sp)
                    }
                    item {
                        HorizontalDivider(color = WaDivider)
                        Spacer(Modifier.height(4.dp))
                        Text("Other PGLOB APP Policies:", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    items(PolicyCatalog.allPolicies) { doc ->
                        Surface(
                            color = if (doc.id == selectedDoc.id) WaGreen.copy(alpha = 0.16f) else WaBackground,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedDoc = doc }
                        ) {
                            Text(
                                doc.title,
                                color = if (doc.id == selectedDoc.id) WaGreen else WaTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
            ) { Text("Got It", fontWeight = FontWeight.Medium) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    notifications: List<InAppNotificationItem>,
    onMarkAllRead: () -> Unit,
    onDelete: (String) -> Unit,
    onPayRequest: (InAppNotificationItem) -> Unit,
    onClickItem: (InAppNotificationItem) -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WaCard) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = WaGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Notifications", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                }
                TextButton(onClick = onMarkAllRead) {
                    Text("Mark All Read", color = WaGreen, fontWeight = FontWeight.Medium)
                }
            }

            if (notifications.isEmpty()) {
                Text("All caught up! No new notifications.", color = WaTextSecondary, modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notifications, key = { it.id }) { item ->
                        Surface(
                            color = if (item.isUnread) WaInput else WaBackground,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClickItem(item) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                        Text("${item.category} • ${item.timeLabel}", color = WaGreen, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WaTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(item.body, color = WaTextSecondary, fontSize = 14.sp)
                                if (item.category == "REQUEST" && item.requestAmount > 0.0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onPayRequest(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary),
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Pay Now", fontSize = 13.sp) }
                                        OutlinedButton(
                                            onClick = { onDelete(item.id) },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Decline", color = WaRed, fontSize = 13.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PglobAiAssistantSheet(
    aiState: AiUiState,
    initialPrompt: String = "",
    onAsk: (String) -> Unit,
    onInsertIntoChat: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var query by remember(initialPrompt) { mutableStateOf(initialPrompt) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WaCard
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "PGLOB AI", tint = WaGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("PGLOB AI Assistant", color = WaTextPrimary, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = WaTextPrimary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    "Translate" to "Translate this naturally into English, Spanish & French: ",
                    "Crush Rizz" to "Write a sweet, confident, respectful message for my crush on PGLOB APP.",
                    "Make CV" to "Write a clean modern 1-page CV template for a remote tech & sales specialist.",
                    "Summarize" to "Summarize the key points of this chat clearly in 3 bullet points."
                ).forEach { (label, preset) ->
                    Surface(
                        color = WaInput,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable {
                            query = preset
                            onAsk(preset)
                        }
                    ) {
                        Text(
                            label,
                            color = WaTextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Ask PGLOB AI anything...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Button(
                onClick = { onAsk(query) },
                enabled = query.isNotBlank() && aiState !is AiUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaTextPrimary)
            ) {
                Text("Ask PGLOB AI", fontWeight = FontWeight.Medium)
            }

            when (aiState) {
                is AiUiState.Idle -> {}
                is AiUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WaGreen, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Thinking with PGLOB AI...", color = WaTextSecondary)
                    }
                }
                is AiUiState.Streaming -> {
                    Surface(color = WaBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(aiState.text, color = WaTextPrimary, modifier = Modifier.padding(12.dp))
                    }
                }
                is AiUiState.Success -> {
                    Surface(color = WaBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item { Text(aiState.text, color = WaTextPrimary) }
                        }
                    }
                    if (onInsertIntoChat != null) {
                        OutlinedButton(
                            onClick = { onInsertIntoChat(aiState.text); onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WaGreen)
                            Spacer(Modifier.width(6.dp))
                            Text("Send AI Reply to Chat", color = WaGreen)
                        }
                    }
                }
                is AiUiState.Error -> {
                    Text(aiState.message, color = WaRed, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun VerifiedTick(verified: Boolean = true, isGold: Boolean = false) {
    if (verified) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Verified",
            tint = if (isGold) WaGold else WaBlueTick,
            modifier = Modifier.size(16.dp)
        )
    }
}
