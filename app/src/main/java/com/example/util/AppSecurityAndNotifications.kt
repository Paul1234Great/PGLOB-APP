package com.example.util

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import java.util.Calendar

/**
 * Enables Android FLAG_SECURE while sensitive security, PIN, Vault, or Biometric UI is visible.
 */
@Composable
fun FlagSecureEffect(enabled: Boolean = true) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val window = (context as? Activity)?.window
        if (enabled && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (enabled && window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

data class InAppNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val category: String, // MESSAGE, PAY, SECURITY, CALL, REQUEST, STATUS, STATUS_LIKE, STATUS_REPLY, STATUS_RESHARE
    val timeLabel: String,
    val isUnread: Boolean = true,
    val requestAmount: Double = 0.0,
    val requestFrom: String = "",
    val targetChatId: String = "",
    val targetStatusId: String = ""
)

data class LinkedDeviceSession(
    val id: String,
    val deviceName: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)

object PglobNotificationHelper {
    private const val CHANNEL_ID = "pglob_app_alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "PGLOB APP Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Messages, PGLOB Pay transfers, calls, and security alerts"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun isQuietHoursNow(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 7
    }

    fun postLocalAlert(
        context: Context,
        title: String,
        body: String,
        notifEnabled: Boolean,
        dndEnabled: Boolean,
        showPreview: Boolean,
        vibrate: Boolean
    ) {
        if (!notifEnabled) return
        if (dndEnabled && isQuietHoursNow()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel(context)
        val displayBody = if (showPreview) body else "New notification inside PGLOB APP"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        if (!vibrate) {
            builder.setVibrate(longArrayOf(0L))
        }
        try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % 100_000L).toInt(), builder.build())
        } catch (_: SecurityException) {
        }
    }
}

object CountryPayCatalog {
    fun methodsForCountry(country: String): List<String> = when {
        country.contains("Nigeria", true) -> listOf(
            "PGLOB User", "Bank Transfer", "OPay", "PalmPay", "Moniepoint", "Kuda"
        )
        country.contains("Ghana", true) -> listOf(
            "PGLOB User", "MTN MoMo Ghana", "Vodafone Cash", "Ghana Bank"
        )
        country.contains("Kenya", true) -> listOf(
            "PGLOB User", "M-Pesa", "Airtel Money KE", "Kenya Bank"
        )
        country.contains("South Africa", true) -> listOf(
            "PGLOB User", "FNB / Capitec Bank", "Instant EFT"
        )
        country.contains("United Kingdom", true) -> listOf(
            "PGLOB User", "UK Bank (Faster Payments)", "Monzo / Revolut", "PayPal"
        )
        country.contains("United States", true) -> listOf(
            "PGLOB User", "US Bank (ACH)", "Cash App", "Zelle", "PayPal"
        )
        country.contains("India", true) -> listOf(
            "PGLOB User", "UPI ID", "PhonePe / GPay", "IMPS Bank"
        )
        country.contains("Senegal", true) || country.contains("Ivory", true) || country.contains("Cameroon", true) -> listOf(
            "PGLOB User", "Wave Mobile Money", "Orange Money", "MTN MoMo"
        )
        else -> listOf("PGLOB User", "Local Bank Transfer", "PayPal", "USDT Crypto")
    }

    fun banksForCountry(country: String): List<String> = when {
        country.contains("Nigeria", true) -> com.example.data.model.GlobalCatalogData.nigerianBanks
        country.contains("Ghana", true) -> listOf("GCB Bank", "Ecobank Ghana", "Fidelity Ghana", "MTN MoMo", "Telecel Cash")
        country.contains("Kenya", true) -> listOf("Safaricom M-Pesa", "KCB Bank", "Equity Bank", "NCBA Bank", "Co-op Bank")
        country.contains("South Africa", true) -> listOf("FNB", "Capitec Bank", "Standard Bank", "Absa", "Nedbank")
        country.contains("United Kingdom", true) -> listOf("Barclays", "HSBC UK", "Lloyds Bank", "Monzo", "Revolut", "Starling")
        country.contains("United States", true) -> listOf("Chase", "Bank of America", "Wells Fargo", "Citibank", "Chime")
        country.contains("India", true) -> listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Mahindra")
        else -> listOf("PGLOB Partner Bank", "International Wire", "OPay Global", "Moniepoint")
    }

    fun lookupAccountName(accountNumber: String, bankOrMethod: String, fallbackName: String): String {
        return when (accountNumber.trim()) {
            "2087491984" -> "PAUL ODOGUN • OPay"
            "1891342479" -> "PAUL ODOGUN • Access Bank"
            "1234567890" -> "AMINA YUSUF • GTBank"
            "0987654321" -> "MARCUS VANCE • First Bank"
            "5555555555" -> "KWAME MENSAH • Zenith / MoMo"
            else -> {
                val tag = fallbackName.uppercase().ifBlank { "VERIFIED MEMBER" }
                "$tag • $bankOrMethod"
            }
        }
    }

    fun largeTransferThreshold(currency: String): Double = when (currency) {
        "₦" -> 100_000.0
        "KSh" -> 25_000.0
        "₹" -> 15_000.0
        else -> 200.0
    }
}

object PolicyCatalog {
    data class PolicyDoc(val id: String, val title: String, val summary: String, val body: String)

    val allPolicies = listOf(
        PolicyDoc(
            id = "privacy",
            title = "Privacy Policy",
            summary = "Zero-trust protection, encryption, and your data control",
            body = """
                PGLOB APP Privacy Policy (Effective 2025–2026)

                1. Information We Protect: Your phone/email verification details, profile photo, and chat settings stay secured under your signed-in account.
                2. Zero-Permission Media: Selecting gallery photos uses Android Photo Picker without scanning unrelated files.
                3. Encryption & Security Controls: Personal chats and calls feature End-to-End security indicators, App Lock (Fingerprint/Face/PIN), Chat Lock folders, and screen capture protection (`FLAG_SECURE`) on PIN & Vault screens.
                4. Data Rights: You can export your account summary or delete your account anytime in Settings > Account & Privacy.
            """.trimIndent()
        ),
        PolicyDoc(
            id = "terms",
            title = "Terms of Service",
            summary = "Responsible global messaging, PGLOB Pay, Market & Dating rules",
            body = """
                PGLOB APP Terms of Service

                1. Eligibility: You must be 18+ to activate Dating Discover and PGLOB Pay transfers.
                2. Authentic Identity: Impersonation, spam, scamming, or sharing fraudulent payment requests results in immediate account suspension.
                3. PGLOB Pay & Coins: Coins and Diamonds earned in-app are governed by transparent conversion and anti-fraud rules.
                4. Respect Worldwide: Harassment, hate speech, and non-consensual content are strictly prohibited across Chats, Live Rooms, Reels, and Market.
            """.trimIndent()
        ),
        PolicyDoc(
            id = "payment",
            title = "Payment & Anti-Fraud Policy",
            summary = "Transaction PIN security, recipient verification & warning rules",
            body = """
                PGLOB PAY Security & Payment Policy

                • Every transfer, airtime purchase, data top-up, bill payment, withdrawal, gift, and market escrow purchase requires your 4-digit Transaction PIN or Fingerprint/Face verification.
                • PGLOB APP will NEVER call, email, or DM you asking for your PIN or 6-digit OTP.
                • 3 incorrect PIN attempts automatically pause PIN entry for 60 seconds (biometric verification remains available if enrolled).
                • First-time large transfers trigger an extra recipient verification step before settlement.
            """.trimIndent()
        ),
        PolicyDoc(
            id = "community",
            title = "Community & Safety Guidelines",
            summary = "Standards for Live Rooms, Communities, Market, Jobs & Dating",
            body = """
                PGLOB APP Community Guidelines

                • Keep Live Audio/Video rooms safe, welcoming, and legal in your region.
                • Market sellers must describe items honestly and use PGLOB Escrow.
                • Job posters must only advertise real opportunities without advance-fee scams.
                • Use the in-app Report & Block tools anytime—reports are routed directly to Admin Moderation.
            """.trimIndent()
        ),
        PolicyDoc(
            id = "cookies",
            title = "Storage & Data Policy",
            summary = "Local DataStore, Room offline queue, and Firebase cloud sync",
            body = """
                Storage & Offline Queue Policy

                • PGLOB APP stores your wallpaper, App Lock timeout, theme, and privacy toggles locally via Android DataStore.
                • Unsent offline messages are queued safely in local Room storage and auto-sent when connectivity resumes.
                • You can clear temporary cache or download a summary of your data from Settings > Storage and Data.
            """.trimIndent()
        )
    )
}

object PglobStrings {
    data class UiStrings(
        val appTitle: String,
        val tagline: String,
        val searchPlaceholder: String,
        val chats: String,
        val updates: String,
        val communities: String,
        val calls: String,
        val lockedChats: String,
        val archived: String,
        val welcomeBack: String,
        val unlockApp: String,
        val sendMoney: String,
        val translate: String,
        val askAi: String,
        val settings: String
    )

    fun forLanguage(lang: String): UiStrings = when (lang) {
        "Pidgin" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Yarn, Pay, Earn Coins, Find Love, Connect World",
            searchPlaceholder = "Find person or Ask PGLOB AI",
            chats = "Toris",
            updates = "Status",
            communities = "Groups",
            calls = "Calls",
            lockedChats = "Locked Toris",
            archived = "Hidden",
            welcomeBack = "Welcome Back",
            unlockApp = "Open PGLOB APP",
            sendMoney = "Send Dough",
            translate = "Translate",
            askAi = "Ask PGLOB AI",
            settings = "Settings"
        )
        "Yoruba" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Ìjíròrò, Ìsanwó, Èrè, Ìfẹ́, Ìbáṣepọ̀ Àgbáyé",
            searchPlaceholder = "Wá tàbí Béèrè lọ́wọ́ PGLOB AI",
            chats = "Àwọn Ìfọ̀rọ̀wérọ̀",
            updates = "Ìròyìn",
            communities = "Àgbájọ",
            calls = "Ìpè",
            lockedChats = "Ìfọ̀rọ̀wérọ̀ Tí A Tì",
            archived = "Tí A Fi Pamọ́",
            welcomeBack = "Kú Àbọ̀",
            unlockApp = "Ṣí PGLOB APP",
            sendMoney = "Firánṣẹ́ Owó",
            translate = "Túmmọ̀",
            askAi = "PGLOB AI",
            settings = "Ètò"
        )
        "Hausa" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Hira, Biya, Samu Kudi, Soyayya, Sadarwa Duniya",
            searchPlaceholder = "Nemo ko Tambayi PGLOB AI",
            chats = "Hira",
            updates = "Labarai",
            communities = "Kungiyoyi",
            calls = "Kira",
            lockedChats = "Hira Da Kulle",
            archived = "Ajiyayyu",
            welcomeBack = "Barka Da Dawowa",
            unlockApp = "Bude PGLOB APP",
            sendMoney = "Aika Kudi",
            translate = "Fassara",
            askAi = "PGLOB AI",
            settings = "Saituna"
        )
        "Igbo" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Nkata, Kwụọ Ụgwọ, Nweta Ego, Ịhụnanya, Jikọọ Ụwa",
            searchPlaceholder = "Chọọ ma ọ bụ Jụọ PGLOB AI",
            chats = "Nkata",
            updates = "Mmelite",
            communities = "Obodo",
            calls = "Oku",
            lockedChats = "Nkata Emechiri Emechi",
            archived = "Echekwara",
            welcomeBack = "Nnọọ",
            unlockApp = "Meghee PGLOB APP",
            sendMoney = "Zipu Ego",
            translate = "Tụgharịa",
            askAi = "PGLOB AI",
            settings = "Ntọala"
        )
        "French" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Discuter, Payer, Gagner, Rencontres, Monde Connecté",
            searchPlaceholder = "Rechercher ou Demander à PGLOB AI",
            chats = "Discussions",
            updates = "Actus",
            communities = "Communautés",
            calls = "Appels",
            lockedChats = "Discussions verrouillées",
            archived = "Archivées",
            welcomeBack = "Bon retour",
            unlockApp = "Déverrouiller PGLOB APP",
            sendMoney = "Envoyer",
            translate = "Traduire",
            askAi = "PGLOB AI",
            settings = "Paramètres"
        )
        "Spanish" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Chat, Pagos, Ganar, Citas, Conexión Global",
            searchPlaceholder = "Buscar o Preguntar a PGLOB AI",
            chats = "Chats",
            updates = "Estados",
            communities = "Comunidades",
            calls = "Llamadas",
            lockedChats = "Chats bloqueados",
            archived = "Archivados",
            welcomeBack = "Bienvenido de nuevo",
            unlockApp = "Desbloquear PGLOB APP",
            sendMoney = "Enviar Dinero",
            translate = "Traducir",
            askAi = "PGLOB AI",
            settings = "Ajustes"
        )
        "Portuguese" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Conversar, Pagar, Ganhar, Encontros, Conexão Mundial",
            searchPlaceholder = "Pesquisar ou Perguntar à PGLOB AI",
            chats = "Conversas",
            updates = "Atualizações",
            communities = "Comunidades",
            calls = "Chamadas",
            lockedChats = "Conversas bloqueadas",
            archived = "Arquivadas",
            welcomeBack = "Bem-vindo de volta",
            unlockApp = "Desbloquear PGLOB APP",
            sendMoney = "Enviar Dinheiro",
            translate = "Traduzir",
            askAi = "PGLOB AI",
            settings = "Configurações"
        )
        "Arabic" -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "دردشة، دفع، أرباح، تعارف، تواصل عالمي",
            searchPlaceholder = "ابحث أو اسأل PGLOB AI",
            chats = "الدردشات",
            updates = "المستجدات",
            communities = "المجتمعات",
            calls = "المكالمات",
            lockedChats = "دردشات مقفلة",
            archived = "المؤرشفة",
            welcomeBack = "مرحباً بعودتك",
            unlockApp = "فتح PGLOB APP",
            sendMoney = "إرسال مال",
            translate = "ترجمة",
            askAi = "PGLOB AI",
            settings = "الإعدادات"
        )
        else -> UiStrings(
            appTitle = "PGLOB APP",
            tagline = "Chat, Pay, Earn, Dating, World Connect",
            searchPlaceholder = "Ask PGLOB AI or Search",
            chats = "Chats",
            updates = "Updates",
            communities = "Communities",
            calls = "Calls",
            lockedChats = "Locked Chats",
            archived = "Archived",
            welcomeBack = "Welcome Back",
            unlockApp = "Unlock PGLOB APP",
            sendMoney = "Send Money",
            translate = "Translate",
            askAi = "PGLOB AI",
            settings = "Settings"
        )
    }
}

object StatusQuoteCodec {
    data class ParsedStatusQuote(
        val isForward: Boolean,
        val statusId: String,
        val ownerName: String,
        val ownerHandle: String,
        val mediaType: String,
        val mediaUri: String,
        val caption: String
    )

    fun encodeReply(
        statusId: String,
        ownerName: String,
        ownerHandle: String,
        mediaType: String,
        mediaUri: String,
        caption: String
    ): String = encode("STATUS_REPLY", statusId, ownerName, ownerHandle, mediaType, mediaUri, caption)

    fun encodeForward(
        statusId: String,
        ownerName: String,
        ownerHandle: String,
        mediaType: String,
        mediaUri: String,
        caption: String
    ): String = encode("STATUS_FORWARD", statusId, ownerName, ownerHandle, mediaType, mediaUri, caption)

    private fun encode(
        kind: String,
        statusId: String,
        ownerName: String,
        ownerHandle: String,
        mediaType: String,
        mediaUri: String,
        caption: String
    ): String {
        val cleanOwner = ownerName.replace("|", " ").replace("]", " ").take(40)
        val cleanHandle = ownerHandle.replace("|", " ").replace("]", " ").take(30)
        val cleanUri = mediaUri.replace("|", "").replace("]", "").take(180)
        val cleanCap = caption.take(180)
        return "[$kind|$statusId|$cleanOwner|$cleanHandle|$mediaType|$cleanUri]$cleanCap".take(500)
    }

    fun decodeOrNull(raw: String): ParsedStatusQuote? {
        if (!raw.startsWith("[STATUS_REPLY|") && !raw.startsWith("[STATUS_FORWARD|")) return null
        val endIdx = raw.indexOf(']')
        if (endIdx <= 0) return null
        val header = raw.substring(1, endIdx)
        val parts = header.split('|')
        if (parts.size < 6) return null
        return ParsedStatusQuote(
            isForward = parts[0] == "STATUS_FORWARD",
            statusId = parts[1],
            ownerName = parts[2],
            ownerHandle = parts[3],
            mediaType = parts[4],
            mediaUri = parts[5],
            caption = raw.substring(endIdx + 1)
        )
    }

    fun statusShareUrl(statusId: String, username: String, displayName: String): String {
        val slug = when {
            statusId == "st_zara123" -> "zara123"
            username.isNotBlank() -> username.lowercase().replace(Regex("[^a-z0-9_]"), "")
            else -> statusId.removePrefix("st_").ifBlank {
                displayName.lowercase().replace(Regex("[^a-z0-9]"), "").take(12).ifBlank { "status" }
            }
        }
        return "https://pglob.app/status/$slug"
    }
}

