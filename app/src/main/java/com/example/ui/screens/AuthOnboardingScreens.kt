package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.LocalAppSettings
import com.example.data.model.GlobalCatalogData
import com.example.ui.components.AvatarCircle
import com.example.ui.components.CountryPickerSheet
import com.example.ui.components.FourBoxPinDisplay
import com.example.ui.components.OtpSixBoxVerification
import com.example.ui.components.PglobBrandLogo
import com.example.ui.components.PglobLogoWithTagline
import com.example.ui.components.PoliciesListDialog
import com.example.ui.components.VerifiedTick
import com.example.ui.components.createTempCameraUri
import com.example.ui.components.launchBiometricPrompt
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.PasswordStrength
import com.example.util.SecurityAndNumberCodec
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class WelcomeLocaleOption(
    val flag: String,
    val country: String,
    val language: String,
    val currency: String
)

private val WELCOME_LOCALES = listOf(
    WelcomeLocaleOption("🇳🇬", "Nigeria", "English", "₦"),
    WelcomeLocaleOption("🇺🇸", "United States", "English", "$"),
    WelcomeLocaleOption("🇬🇧", "United Kingdom", "English", "£"),
    WelcomeLocaleOption("🇬🇭", "Ghana", "English", "₵"),
    WelcomeLocaleOption("🇰🇪", "Kenya", "Swahili", "KSh"),
    WelcomeLocaleOption("🇮🇳", "India", "Hindi", "₹"),
    WelcomeLocaleOption("🇧🇷", "Brazil", "Portuguese", "R$")
)

fun attemptAutoSignIn(
    context: android.content.Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: android.content.Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, animationSpec = tween(durationMillis = 600)) }
        delay(2000L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            }
            .testTag("pglob_splash_screen")
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(logoScale.value)
                .testTag("pglob_splash_logo"),
            contentAlignment = Alignment.Center
        ) {
            PglobBrandLogo(
                size = 120.dp,
                shape = CircleShape
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .testTag("pglob_splash_from_footer"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PglobBrandLogo(
                size = 20.dp,
                shape = CircleShape
            )
            Text(
                text = "from PGLOB",
                color = Color(0xFF8696A0),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private enum class AuthSubMode {
    WELCOME, SIGN_UP, SIGN_UP_OTP, SIGN_IN, FORGOT_PASSWORD
}

private data class RegisteredPhoneAccount(
    val uid: String,
    val fullName: String,
    val phone: String,
    val countryName: String,
    val countryFlag: String,
    val currency: String,
    val email: String,
    val passwordHash: String,
    val pglobAccountNumber: String
)

private fun parseRegisteredAccounts(json: String): List<RegisteredPhoneAccount> = try {
    val arr = JSONArray(json)
    buildList {
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            add(
                RegisteredPhoneAccount(
                    uid = o.optString("uid"),
                    fullName = o.optString("fullName"),
                    phone = o.optString("phone"),
                    countryName = o.optString("countryName", "Nigeria"),
                    countryFlag = o.optString("countryFlag", "🇳🇬"),
                    currency = o.optString("currency", "₦"),
                    email = o.optString("email"),
                    passwordHash = o.optString("passwordHash"),
                    pglobAccountNumber = o.optString("pglobAccountNumber")
                )
            )
        }
    }
} catch (_: Exception) {
    emptyList()
}

private fun encodeRegisteredAccounts(list: List<RegisteredPhoneAccount>): String {
    val arr = JSONArray()
    list.forEach { acc ->
        arr.put(
            JSONObject().apply {
                put("uid", acc.uid)
                put("fullName", acc.fullName)
                put("phone", acc.phone)
                put("countryName", acc.countryName)
                put("countryFlag", acc.countryFlag)
                put("currency", acc.currency)
                put("email", acc.email)
                put("passwordHash", acc.passwordHash)
                put("pglobAccountNumber", acc.pglobAccountNumber)
            }
        )
    }
    return arr.toString()
}

@Composable
fun WelcomeAuthScreen(
    localSettings: LocalAppSettings = LocalAppSettings(),
    onSelectLocale: (country: String, language: String, currency: String) -> Unit = { _, _, _ -> },
    onForgetDeviceAccount: () -> Unit = {},
    onPhoneSignUpComplete: (
        uid: String,
        fullName: String,
        phone: String,
        countryName: String,
        countryFlag: String,
        currency: String,
        email: String,
        passwordHash: String,
        pglobAccountNumber: String,
        updatedRegisteredAccountsJson: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onPhoneSignInSuccess: (
        uid: String,
        fullName: String,
        phone: String,
        countryName: String,
        countryFlag: String,
        currency: String,
        email: String,
        passwordHash: String,
        pglobAccountNumber: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onFailedSignInAttempt: (attempts: Int, lockUntilMillis: Long) -> Unit = { _, _ -> },
    onPasswordResetComplete: (newHash: String, updatedRegisteredAccountsJson: String) -> Unit = { _, _ -> },
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var authMode by remember { mutableStateOf(AuthSubMode.WELCOME) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var showPoliciesDialog by remember { mutableStateOf(false) }
    var selectedLocale by remember { mutableStateOf(WELCOME_LOCALES.first()) }
    var showCountryPicker by remember { mutableStateOf(false) }

    // Country selection for Sign Up / Sign In / Forgot Password
    var selectedCountry by remember {
        mutableStateOf(
            GlobalCatalogData.countries.firstOrNull { it.name == localSettings.savedCountryName }
                ?: GlobalCatalogData.countries.first()
        )
    }

    // Sign Up state (7 ordered fields)
    var suPhone by remember { mutableStateOf("") }
    var suFullName by remember { mutableStateOf("") }
    var suEmailOptional by remember { mutableStateOf("") }
    var suPassword by remember { mutableStateOf("") }
    var suConfirmPassword by remember { mutableStateOf("") }
    var suPasswordVisible by remember { mutableStateOf(false) }
    var suReferralCode by remember { mutableStateOf("") }
    var suAgreedTerms by remember { mutableStateOf(false) }

    // OTP state
    var otpCountdownSec by remember { mutableIntStateOf(60) }
    var otpResendCount by remember { mutableIntStateOf(0) }
    var otpCallStatus by remember { mutableStateOf<String?>(null) }

    // Sign In state
    var siPhone by remember {
        mutableStateOf(
            localSettings.savedPhoneNumber.substringAfter(" ").ifBlank { "" }
        )
    }
    var siPassword by remember { mutableStateOf("") }
    var siPasswordVisible by remember { mutableStateOf(false) }
    var localNowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Forgot Password (3 steps)
    var fpStep by remember { mutableIntStateOf(1) }
    var fpPhone by remember { mutableStateOf("") }
    var fpEmailOptional by remember { mutableStateOf("") }
    var fpNewPassword by remember { mutableStateOf("") }
    var fpConfirmPassword by remember { mutableStateOf("") }
    var fpPasswordVisible by remember { mutableStateOf(false) }

    val hasReturningAccount = localSettings.hasAccountOnDevice && localSettings.savedDisplayName.isNotBlank()
    val registeredAccounts = remember(localSettings.registeredAccountsJson) {
        parseRegisteredAccounts(localSettings.registeredAccountsJson)
    }

    LaunchedEffect(authMode, otpCountdownSec) {
        if (authMode == AuthSubMode.SIGN_UP_OTP && otpCountdownSec > 0) {
            delay(1000L)
            otpCountdownSec--
        }
    }

    LaunchedEffect(localSettings.signInLockUntilMillis) {
        while (System.currentTimeMillis() < localSettings.signInLockUntilMillis) {
            localNowMillis = System.currentTimeMillis()
            delay(1000L)
        }
        localNowMillis = System.currentTimeMillis()
    }

    if (showCountryPicker) {
        CountryPickerSheet(
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                selectedCountry = country
                onSelectLocale(country.name, selectedLocale.language, country.currency)
            }
        )
    }

    if (showPoliciesDialog) {
        PoliciesListDialog(onDismiss = { showPoliciesDialog = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (authMode != AuthSubMode.WELCOME) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        errorMsg = null
                        successMsg = null
                        authMode = when (authMode) {
                            AuthSubMode.SIGN_UP_OTP -> AuthSubMode.SIGN_UP
                            AuthSubMode.FORGOT_PASSWORD -> if (fpStep > 1) { fpStep--; AuthSubMode.FORGOT_PASSWORD } else AuthSubMode.SIGN_IN
                            else -> AuthSubMode.WELCOME
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PglobWhite)
                    }
                    Text(
                        text = when (authMode) {
                            AuthSubMode.SIGN_UP -> "Create PGLOB Account"
                            AuthSubMode.SIGN_UP_OTP -> "Verify Your Number"
                            AuthSubMode.SIGN_IN -> "Sign In to PGLOB"
                            AuthSubMode.FORGOT_PASSWORD -> "Reset Password (Step $fpStep/3)"
                            AuthSubMode.WELCOME -> ""
                        },
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            } else {
                Spacer(Modifier.height(18.dp))
                PglobBrandLogo(
                    size = 100.dp,
                    shape = CircleShape,
                    modifier = Modifier.testTag("welcome_pglob_logo")
                )
                Spacer(Modifier.height(14.dp))

                if (hasReturningAccount) {
                    AvatarCircle(
                        name = localSettings.savedDisplayName,
                        size = 56.dp,
                        vipTier = "GOLD"
                    )
                    Text(
                        text = "Welcome back, ${localSettings.savedDisplayName}",
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                    if (localSettings.savedUsername.isNotBlank()) {
                        Text(
                            text = "@${localSettings.savedUsername}",
                            color = WaGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Welcome to PGLOB",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("welcome_title_text")
                    )
                }

                Text(
                    text = "Chat • Pay • Earn • Ajo • Market - All in One App",
                    color = Color(0xFFC0C9D1),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    WELCOME_LOCALES.forEach { loc ->
                        val isSelected = selectedLocale.flag == loc.flag
                        Surface(
                            color = WaCard,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) WaGreen else WaInput,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedLocale = loc
                                    selectedCountry = GlobalCatalogData.countries.firstOrNull { it.name == loc.country } ?: selectedCountry
                                    onSelectLocale(loc.country, loc.language, loc.currency)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(loc.flag, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Text(
                    text = "${selectedLocale.flag} ${selectedLocale.country} • ${selectedLocale.language} (${selectedLocale.currency})",
                    color = WaTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (errorMsg != null) {
                Surface(
                    color = PglobRed.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        errorMsg!!,
                        color = PglobRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (successMsg != null) {
                Surface(
                    color = WaGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        successMsg!!,
                        color = WaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            when (authMode) {
                AuthSubMode.WELCOME -> {
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "By continuing, you agree to our Privacy Policy and Terms",
                        color = Color(0xFF8696A0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPoliciesDialog = true }
                            .padding(vertical = 4.dp)
                            .testTag("welcome_terms_copy")
                    )

                    Button(
                        onClick = {
                            errorMsg = null
                            successMsg = null
                            authMode = AuthSubMode.SIGN_UP
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("create_account_signup_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("AGREE AND CONTINUE", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }

                    Button(
                        onClick = {
                            errorMsg = null
                            successMsg = null
                            authMode = AuthSubMode.SIGN_IN
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sign_in_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaInput, contentColor = WaTextPrimary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("I Already Have Account - Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isLoading = true
                                errorMsg = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMsg = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = { isLoading = false }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier.testTag("get_started_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = WaGreen, strokeWidth = 2.dp)
                            } else {
                                Text("Login with Google (Optional)", color = WaTextSecondary, fontSize = 13.sp)
                            }
                        }

                        TextButton(onClick = { showPoliciesDialog = true }) {
                            Text("Policies & Terms", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (hasReturningAccount) {
                        TextButton(onClick = onForgetDeviceAccount) {
                            Text("Not you? Switch Account", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                AuthSubMode.SIGN_UP -> {
                    // 1. Country Picker
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCountry.flag, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1. Country", color = WaTextSecondary, fontSize = 12.sp)
                                Text(
                                    "${selectedCountry.name} (${selectedCountry.dialCode} • ${selectedCountry.currency})",
                                    color = WaTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Select", color = WaGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Phone Number
                    val isPhoneValid = SecurityAndNumberCodec.isValidPhoneForCountry(selectedCountry.dialCode, suPhone)
                    val normalizedPreview = SecurityAndNumberCodec.normalizePhoneWithDial(selectedCountry.dialCode, suPhone)
                    OutlinedTextField(
                        value = suPhone,
                        onValueChange = { suPhone = it.filter(Char::isDigit).take(13); errorMsg = null },
                        label = { Text("2. Phone Number") },
                        prefix = { Text("${selectedCountry.dialCode} ", color = WaGreen, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signup_phone_input")
                    )

                    // 3. Full Real Name
                    OutlinedTextField(
                        value = suFullName,
                        onValueChange = { suFullName = it; errorMsg = null },
                        label = { Text("3. Full Real Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                    )

                    // 4. Email (Optional)
                    OutlinedTextField(
                        value = suEmailOptional,
                        onValueChange = { suEmailOptional = it },
                        label = { Text("4. Email (Optional) - You fit skip am") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 5. Password + Strength + Confirm Password
                    val pwdStrength = SecurityAndNumberCodec.validatePassword(suPassword)
                    OutlinedTextField(
                        value = suPassword,
                        onValueChange = { suPassword = it; errorMsg = null },
                        label = { Text("5. Password (min 8 chars + letter & number)") },
                        visualTransformation = if (suPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { suPasswordVisible = !suPasswordVisible }) {
                                Icon(
                                    if (suPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
                    )
                    if (suPassword.isNotEmpty()) {
                        val strengthColor = when (pwdStrength) {
                            PasswordStrength.STRONG -> WaGreen
                            PasswordStrength.GOOD -> PglobGold
                            else -> PglobRed
                        }
                        Text(
                            text = "Password strength: ${pwdStrength.label}",
                            color = strengthColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    OutlinedTextField(
                        value = suConfirmPassword,
                        onValueChange = { suConfirmPassword = it; errorMsg = null },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (suPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input")
                    )

                    // 6. Referral Code (Optional)
                    OutlinedTextField(
                        value = suReferralCode,
                        onValueChange = { suReferralCode = it.uppercase().take(16) },
                        label = { Text("6. Referral Code (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 7. Legal Agreement Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = suAgreedTerms,
                            onCheckedChange = { suAgreedTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = WaGreen, checkmarkColor = WaBackground)
                        )
                        Text("7. I accept Terms, Privacy & ", color = WaTextSecondary, fontSize = 13.sp)
                        Text(
                            "Anti-Fraud Rules",
                            color = WaGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { showPoliciesDialog = true }
                        )
                    }

                    Button(
                        onClick = {
                            val cleanName = suFullName.trim()
                            val normDigits = SecurityAndNumberCodec.digitsOnly(normalizedPreview)
                            val duplicate = registeredAccounts.any {
                                SecurityAndNumberCodec.digitsOnly(it.phone) == normDigits
                            }
                            when {
                                !isPhoneValid -> errorMsg = "Enter a valid ${selectedCountry.name} phone number."
                                duplicate -> errorMsg = "This phone number already has a PGLOB account. Sign In instead."
                                cleanName.length < 2 -> errorMsg = "Enter your full real name."
                                !SecurityAndNumberCodec.isPasswordAcceptable(suPassword) ->
                                    errorMsg = "Password must be 8+ chars with at least 1 letter and 1 number."
                                suPassword != suConfirmPassword -> errorMsg = "Password and Confirm Password do not match."
                                !suAgreedTerms -> errorMsg = "Please agree to Terms, Privacy & Anti-Fraud Rules."
                                else -> {
                                    errorMsg = null
                                    otpCountdownSec = 60
                                    otpCallStatus = null
                                    authMode = AuthSubMode.SIGN_UP_OTP
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("signup_continue_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Continue -> Verify Number", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                AuthSubMode.SIGN_UP_OTP -> {
                    val normalizedPhone = SecurityAndNumberCodec.normalizePhoneWithDial(selectedCountry.dialCode, suPhone)
                    val finalizeSignUp: () -> Unit = {
                        val newUid = "pg_${UUID.randomUUID().toString().replace("-", "").take(12)}"
                        val phoneSalt = SecurityAndNumberCodec.digitsOnly(normalizedPhone)
                        val pwdHash = SecurityAndNumberCodec.sha256Hash(phoneSalt, suPassword)
                        val pglobAcct = SecurityAndNumberCodec.generateDistinctAccountNumber(normalizedPhone, newUid)
                        val newAccount = RegisteredPhoneAccount(
                            uid = newUid,
                            fullName = suFullName.trim(),
                            phone = normalizedPhone,
                            countryName = selectedCountry.name,
                            countryFlag = selectedCountry.flag,
                            currency = selectedCountry.currency,
                            email = suEmailOptional.trim(),
                            passwordHash = pwdHash,
                            pglobAccountNumber = pglobAcct
                        )
                        val updatedList = (registeredAccounts + newAccount).distinctBy { it.phone }
                        onPhoneSignUpComplete(
                            newUid,
                            newAccount.fullName,
                            normalizedPhone,
                            selectedCountry.name,
                            selectedCountry.flag,
                            selectedCountry.currency,
                            newAccount.email,
                            pwdHash,
                            pglobAcct,
                            encodeRegisteredAccounts(updatedList)
                        )
                        onAuthSuccess()
                    }

                    Surface(color = WaCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Auto-Verify SMS / 6-Digit OTP",
                                color = WaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Sent to ${selectedCountry.flag} $normalizedPhone (Demo code: 123456)",
                                color = WaTextSecondary,
                                fontSize = 13.sp
                            )
                            OtpSixBoxVerification(
                                targetLabel = normalizedPhone,
                                onVerified = finalizeSignUp,
                                onChangeTarget = { authMode = AuthSubMode.SIGN_UP }
                            )
                            Button(
                                onClick = finalizeSignUp,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auto_verify_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Auto-Verify SMS Now (Instant)", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (otpResendCount < 3) {
                                            otpResendCount++
                                            otpCountdownSec = 60
                                            otpCallStatus = "Resent SMS code #${otpResendCount} (max 3/hr)"
                                        }
                                    },
                                    enabled = otpCountdownSec == 0 && otpResendCount < 3
                                ) {
                                    Text(
                                        if (otpCountdownSec > 0) "Resend in ${otpCountdownSec}s"
                                        else if (otpResendCount >= 3) "Resend limit reached (3/hr)"
                                        else "Resend OTP",
                                        color = if (otpCountdownSec == 0 && otpResendCount < 3) WaGreen else WaTextSecondary
                                    )
                                }
                                TextButton(onClick = {
                                    otpCallStatus = "Automated voice call initiated to $normalizedPhone — Code: 123456"
                                }) {
                                    Text("Call Me", color = PglobGold, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (otpCallStatus != null) {
                                Text(otpCallStatus!!, color = WaGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                AuthSubMode.SIGN_IN -> {
                    val locked = localNowMillis < localSettings.signInLockUntilMillis
                    val remainingMin = if (locked) (((localSettings.signInLockUntilMillis - localNowMillis) / 60_000L) + 1L) else 0L

                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCountry.flag, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text("${selectedCountry.name} (${selectedCountry.dialCode})", color = WaTextPrimary, modifier = Modifier.weight(1f))
                            Text("Change", color = WaGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = siPhone,
                        onValueChange = { siPhone = it.filter(Char::isDigit).take(13); errorMsg = null },
                        label = { Text("Phone Number") },
                        prefix = { Text("${selectedCountry.dialCode} ", color = WaGreen, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signin_phone_input")
                    )

                    OutlinedTextField(
                        value = siPassword,
                        onValueChange = { siPassword = it; errorMsg = null },
                        label = { Text("Password") },
                        visualTransformation = if (siPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { siPasswordVisible = !siPasswordVisible }) {
                                Icon(
                                    if (siPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("signin_password_input")
                    )

                    if (locked) {
                        Text(
                            "Too many wrong tries. Locked for $remainingMin min.",
                            color = PglobRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (locked) {
                                errorMsg = "Account temporarily locked. Try again in $remainingMin min."
                                return@Button
                            }
                            val normalizedPhone = SecurityAndNumberCodec.normalizePhoneWithDial(selectedCountry.dialCode, siPhone)
                            val digits = SecurityAndNumberCodec.digitsOnly(normalizedPhone)
                            val matched = registeredAccounts.firstOrNull {
                                SecurityAndNumberCodec.digitsOnly(it.phone) == digits ||
                                    SecurityAndNumberCodec.digitsOnly(it.phone).takeLast(10) == digits.takeLast(10)
                            } ?: if (SecurityAndNumberCodec.digitsOnly(localSettings.savedPhoneNumber) == digits &&
                                localSettings.passwordHash.isNotBlank()
                            ) {
                                RegisteredPhoneAccount(
                                    uid = localSettings.localSessionUid.ifBlank { "pg_${digits.takeLast(8)}" },
                                    fullName = localSettings.savedDisplayName.ifBlank { "PGLOB Member" },
                                    phone = localSettings.savedPhoneNumber,
                                    countryName = localSettings.savedCountryName,
                                    countryFlag = localSettings.savedCountryFlag,
                                    currency = localSettings.savedCurrency,
                                    email = localSettings.savedEmail,
                                    passwordHash = localSettings.passwordHash,
                                    pglobAccountNumber = localSettings.pglobAccountNumber.ifBlank {
                                        SecurityAndNumberCodec.generateDistinctAccountNumber(normalizedPhone, "pg_$digits")
                                    }
                                )
                            } else null

                            if (matched == null) {
                                errorMsg = "No account found for $normalizedPhone. Tap Create Account - Sign Up first."
                                return@Button
                            }

                            val salt = SecurityAndNumberCodec.digitsOnly(matched.phone)
                            val candidateHash = SecurityAndNumberCodec.sha256Hash(salt, siPassword)
                            if (candidateHash != matched.passwordHash) {
                                val nextTries = localSettings.failedSignInAttempts + 1
                                val lockUntil = if (nextTries >= 5) System.currentTimeMillis() + 15L * 60_000L else 0L
                                onFailedSignInAttempt(nextTries, lockUntil)
                                errorMsg = if (nextTries >= 5) {
                                    "5 wrong password attempts! Account locked for 15 minutes."
                                } else {
                                    "Wrong password (${5 - nextTries} attempts left)."
                                }
                            } else {
                                onPhoneSignInSuccess(
                                    matched.uid,
                                    matched.fullName,
                                    matched.phone,
                                    matched.countryName,
                                    matched.countryFlag,
                                    matched.currency,
                                    matched.email,
                                    matched.passwordHash,
                                    matched.pglobAccountNumber
                                )
                                onAuthSuccess()
                            }
                        },
                        enabled = !locked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("signin_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (localSettings.fingerprintSignInEnabled && hasReturningAccount) {
                        OutlinedButton(
                            onClick = {
                                launchBiometricPrompt(
                                    context = context,
                                    title = "Sign In with Fingerprint / Face ID",
                                    subtitle = "Unlock ${localSettings.savedDisplayName}'s PGLOB account",
                                    onSuccess = {
                                        val acct = localSettings.pglobAccountNumber.ifBlank {
                                            SecurityAndNumberCodec.generateDistinctAccountNumber(
                                                localSettings.savedPhoneNumber,
                                                localSettings.localSessionUid.ifBlank { "local" }
                                            )
                                        }
                                        onPhoneSignInSuccess(
                                            localSettings.localSessionUid.ifBlank { "pg_bio_user" },
                                            localSettings.savedDisplayName,
                                            localSettings.savedPhoneNumber,
                                            localSettings.savedCountryName,
                                            localSettings.savedCountryFlag,
                                            localSettings.savedCurrency,
                                            localSettings.savedEmail,
                                            localSettings.passwordHash,
                                            acct
                                        )
                                        onAuthSuccess()
                                    },
                                    onFallbackOrError = { errorMsg = it }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint Sign In", tint = WaGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Sign In with Fingerprint / Face ID", color = WaTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(onClick = {
                        errorMsg = null
                        successMsg = null
                        fpPhone = siPhone
                        fpStep = 1
                        authMode = AuthSubMode.FORGOT_PASSWORD
                    }) {
                        Text("Forgot Password?", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                AuthSubMode.FORGOT_PASSWORD -> {
                    when (fpStep) {
                        1 -> {
                            Text("Step 1: Verify Account Phone & Optional Email", color = WaTextSecondary, fontSize = 14.sp)
                            Surface(
                                color = WaCard,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true }
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedCountry.flag, fontSize = 22.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("${selectedCountry.name} (${selectedCountry.dialCode})", color = WaTextPrimary, modifier = Modifier.weight(1f))
                                    Text("Change", color = WaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedTextField(
                                value = fpPhone,
                                onValueChange = { fpPhone = it.filter(Char::isDigit).take(13); errorMsg = null },
                                label = { Text("Registered Phone Number") },
                                prefix = { Text("${selectedCountry.dialCode} ", color = WaGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = fpEmailOptional,
                                onValueChange = { fpEmailOptional = it },
                                label = { Text("Email Check (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (!SecurityAndNumberCodec.isValidPhoneForCountry(selectedCountry.dialCode, fpPhone)) {
                                        errorMsg = "Enter a valid phone number."
                                    } else {
                                        errorMsg = null
                                        fpStep = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                            ) {
                                Text("Continue to OTP", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> {
                            val fpFull = SecurityAndNumberCodec.normalizePhoneWithDial(selectedCountry.dialCode, fpPhone)
                            Text("Step 2: Enter 6-Digit Recovery OTP (123456)", color = WaTextSecondary, fontSize = 14.sp)
                            OtpSixBoxVerification(
                                targetLabel = fpFull,
                                onVerified = {
                                    errorMsg = null
                                    fpStep = 3
                                },
                                onChangeTarget = { fpStep = 1 }
                            )
                        }

                        else -> {
                            Text("Step 3: Create New Password", color = WaTextSecondary, fontSize = 14.sp)
                            OutlinedTextField(
                                value = fpNewPassword,
                                onValueChange = { fpNewPassword = it; errorMsg = null },
                                label = { Text("New Password (8+ chars, letter + number)") },
                                visualTransformation = if (fpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { fpPasswordVisible = !fpPasswordVisible }) {
                                        Icon(
                                            if (fpPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = fpConfirmPassword,
                                onValueChange = { fpConfirmPassword = it; errorMsg = null },
                                label = { Text("Confirm New Password") },
                                visualTransformation = if (fpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (!SecurityAndNumberCodec.isPasswordAcceptable(fpNewPassword)) {
                                        errorMsg = "Password must be at least 8 characters with letters and numbers."
                                        return@Button
                                    }
                                    if (fpNewPassword != fpConfirmPassword) {
                                        errorMsg = "Passwords do not match."
                                        return@Button
                                    }
                                    val fullPhone = SecurityAndNumberCodec.normalizePhoneWithDial(selectedCountry.dialCode, fpPhone)
                                    val digits = SecurityAndNumberCodec.digitsOnly(fullPhone)
                                    val newHash = SecurityAndNumberCodec.sha256Hash(digits, fpNewPassword)
                                    val updatedAccounts = registeredAccounts.map { acc ->
                                        val accDigits = SecurityAndNumberCodec.digitsOnly(acc.phone)
                                        if (accDigits == digits || accDigits.takeLast(10) == digits.takeLast(10)) {
                                            acc.copy(passwordHash = newHash)
                                        } else acc
                                    }
                                    onPasswordResetComplete(newHash, encodeRegisteredAccounts(updatedAccounts))
                                    siPhone = fpPhone
                                    siPassword = ""
                                    errorMsg = null
                                    successMsg = "Password Changed ✅ Sign in with your new password."
                                    authMode = AuthSubMode.SIGN_IN
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                            ) {
                                Text("Save New Password", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartOnboardingSetupScreen(
    defaultEmail: String,
    defaultName: String,
    onCheckUsername: suspend (String) -> Boolean,
    onConfigureSecurity: (pin: String, biometricEnabled: Boolean) -> Unit = { _, _ -> },
    onFinishSetup: (
        fullName: String,
        username: String,
        usernameVerified: Boolean,
        phone: String,
        email: String,
        country: String,
        countryFlag: String,
        currency: String,
        language: String,
        about: String,
        photoUri: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) } // 1: Phone/Email+OTP, 2: Profile+@username, 3: 4-Digit PIN + Biometric + Policies
    var usePhone by remember { mutableStateOf(true) }
    var selectedCountry by remember { mutableStateOf(GlobalCatalogData.countries.first()) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf(defaultEmail) }
    var pinPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showOtpBox by remember { mutableStateOf(false) }
    var contactVerified by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf(defaultName.ifBlank { "" }) }
    var username by remember { mutableStateOf("") }
    var usernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var usernameVerified by remember { mutableStateOf(false) }
    var showUsernameOtp by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf("Hey there! I am using PGLOB APP.") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Step 3 Security State
    var setupPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(true) }
    var agreedTerms by remember { mutableStateOf(false) }
    var showPoliciesDialog by remember { mutableStateOf(false) }
    var step3Error by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri.toString() }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok && tempCameraUri != null) photoUri = tempCameraUri.toString() }

    if (showCountryPicker) {
        CountryPickerSheet(
            onDismiss = { showCountryPicker = false },
            onSelect = { selectedCountry = it }
        )
    }

    if (showPoliciesDialog) {
        PoliciesListDialog(onDismiss = { showPoliciesDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PglobBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PglobLogoWithTagline(logoSize = 100.dp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (step) {
                    1 -> "Step 1/3 • Quick Verify"
                    2 -> "Step 2/3 • Profile & @User"
                    else -> "Step 3/3 • Strong Security"
                },
                color = WaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 28.sp
            )
            Text("PGLOB", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        LinearProgressIndicator(
            progress = { step / 3f },
            color = WaGreen,
            trackColor = WaInput,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Text(
            text = when (step) {
                1 -> "Choose Phone or Email verification (instant OTP 123456)"
                2 -> "Claim your unique @username and Gold Verified Tick ✔️"
                else -> "Create your 4-digit App & Pay PIN + Fingerprint / Face ID"
            },
            color = PglobGreyText,
            fontWeight = FontWeight.SemiBold
        )

        when (step) {
            1 -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { usePhone = true; showOtpBox = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (usePhone) PglobGold else PglobDarkGrey,
                            contentColor = if (usePhone) PglobBlack else PglobWhite
                        )
                    ) { Text("Phone Number", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { usePhone = false; showOtpBox = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!usePhone) PglobGold else PglobDarkGrey,
                            contentColor = if (!usePhone) PglobBlack else PglobWhite
                        )
                    ) { Text("Email", fontWeight = FontWeight.Bold) }
                }

                if (usePhone) {
                    Surface(
                        color = PglobDarkGrey,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showCountryPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCountry.flag, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text("${selectedCountry.name} (${selectedCountry.dialCode})", color = PglobWhite, modifier = Modifier.weight(1f))
                            Text("Change", color = PglobGold, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Phone Number") },
                        prefix = { Text("${selectedCountry.dialCode} ", color = PglobGold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinPassword,
                        onValueChange = { pinPassword = it },
                        label = { Text("App Recovery Password (6+ chars)") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!showOtpBox) {
                    Button(
                        onClick = { showOtpBox = true },
                        enabled = if (usePhone) phoneInput.length >= 6 else emailInput.contains("@"),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) { Text("Continue", fontWeight = FontWeight.ExtraBold) }
                } else {
                    val target = if (usePhone) "${selectedCountry.dialCode} $phoneInput" else emailInput
                    OtpSixBoxVerification(
                        targetLabel = target,
                        onVerified = {
                            contactVerified = true
                            step = 2
                        },
                        onChangeTarget = { showOtpBox = false }
                    )
                }

                TextButton(onClick = { step = 2 }, modifier = Modifier.align(Alignment.End)) {
                    Text("Skip to Profile Setup →", color = PglobGold, fontWeight = FontWeight.Bold)
                }
            }

            2 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AvatarCircle(name = fullName.ifBlank { "PG" }, photoUri = photoUri, size = 72.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Gallery")
                            }
                            OutlinedButton(onClick = {
                                val uri = createTempCameraUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Camera")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { raw ->
                        val cleaned = raw.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(20)
                        username = cleaned
                        usernameVerified = false
                        if (cleaned.length >= 3) {
                            scope.launch { usernameAvailable = onCheckUsername(cleaned) }
                        } else {
                            usernameAvailable = null
                        }
                    },
                    label = { Text("Unique Username (3-20 chars)") },
                    prefix = { Text("@", color = PglobGold) },
                    trailingIcon = {
                        if (usernameVerified) VerifiedTick(true, isGold = true)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (username.length >= 3 && usernameAvailable != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (usernameAvailable == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (usernameAvailable == true) PglobGreen else PglobRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (usernameAvailable == true) "Available! @$username is ready"
                            else "Username taken, try @${username}1",
                            color = if (usernameAvailable == true) PglobGreen else PglobRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (!usernameVerified && username.length >= 3 && usernameAvailable == true) {
                    OutlinedButton(
                        onClick = { showUsernameOtp = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PglobGold)
                    ) {
                        Text("Verify Username for Gold Tick ✔️")
                    }
                }

                if (showUsernameOtp && !usernameVerified) {
                    OtpSixBoxVerification(
                        targetLabel = "@$username",
                        onVerified = {
                            usernameVerified = true
                            showUsernameOtp = false
                        }
                    )
                }

                OutlinedTextField(
                    value = about,
                    onValueChange = { about = it },
                    label = { Text("About") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(
                        onClick = { showLanguageMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Language: $selectedLanguage  •  Currency: ${selectedCountry.currency}", color = PglobWhite)
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        GlobalCatalogData.languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = { selectedLanguage = lang; showLanguageMenu = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f).height(50.dp)) {
                        Text("Back", color = PglobWhite)
                    }
                    Button(
                        onClick = { step = 3 },
                        modifier = Modifier.weight(2f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) {
                        Text("Next: Security PIN →", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            else -> {
                Text("Create 4-Digit App & Pay PIN", color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                FourBoxPinDisplay(if (setupPin.length < 4) setupPin else confirmPin)

                OutlinedTextField(
                    value = setupPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { setupPin = it; step3Error = null } },
                    label = { Text("Create 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { confirmPin = it; step3Error = null } },
                    label = { Text("Confirm 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PglobGold)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Turn ON Fingerprint / Face ID?", color = PglobWhite, fontWeight = FontWeight.Bold)
                                Text("Quick unlock & instant Pay transfers", color = PglobGreyText, fontSize = 12.sp)
                            }
                        }
                        androidx.compose.material3.Switch(
                            checked = enableBiometrics,
                            onCheckedChange = { enableBiometrics = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = PglobBlack, checkedTrackColor = PglobGold)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreedTerms,
                        onCheckedChange = { agreedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = PglobGold, checkmarkColor = PglobBlack)
                    )
                    Text("I agree to PGLOB APP ", color = PglobGreyText, fontSize = 13.sp)
                    Text(
                        "Terms & Privacy Policy",
                        color = PglobGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { showPoliciesDialog = true }
                    )
                }

                if (step3Error != null) {
                    Text(step3Error!!, color = PglobRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { step = 2 }, modifier = Modifier.weight(1f).height(52.dp)) {
                        Text("Back", color = PglobWhite)
                    }
                    Button(
                        onClick = {
                            if (setupPin.isNotEmpty() && (setupPin.length != 4 || setupPin != confirmPin)) {
                                step3Error = "PIN and Confirm PIN must match (4 digits)"
                                return@Button
                            }
                            if (setupPin.length == 4) {
                                onConfigureSecurity(setupPin, enableBiometrics)
                            } else {
                                onConfigureSecurity("", enableBiometrics)
                            }
                            val finalPhone = if (phoneInput.isNotBlank()) "${selectedCountry.dialCode}$phoneInput" else ""
                            val finalUser = username.ifBlank { "pglob_${System.currentTimeMillis().toString().takeLast(5)}" }
                            onFinishSetup(
                                fullName.ifBlank { "Global Explorer" },
                                finalUser,
                                usernameVerified || contactVerified,
                                finalPhone,
                                emailInput,
                                selectedCountry.name,
                                selectedCountry.flag,
                                selectedCountry.currency,
                                selectedLanguage,
                                about,
                                photoUri
                            )
                        },
                        enabled = agreedTerms,
                        modifier = Modifier.weight(2f).height(52.dp).testTag("finish_setup_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                    ) {
                        Text("Finish & Enter World", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
