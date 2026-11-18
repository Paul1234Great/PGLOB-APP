package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pglobDataStore: DataStore<Preferences> by preferencesDataStore(name = "pglob_chat_prefs")

enum class WallpaperOption(val id: String, val label: String) {
    DOODLE_PGLOB("doodle_pglob", "PGLOB Subtle Doodle (#0B141A)"),
    SOLID_DARK("solid_dark", "PGLOB Solid Dark (#0B141A)"),
    PLAIN_BLACK("plain_black", "OLED Pure Black (#000000)"),
    CUSTOM_URI("custom_uri", "Custom Photo")
}

data class LocalAppSettings(
    val wallpaperId: String = WallpaperOption.DOODLE_PGLOB.id,
    val customWallpaperUri: String = "",
    val themeMode: String = "PGLOB_BLACK",
    val biometricEnabled: Boolean = false,
    val secretVaultPin: String = "",
    val secretVaultNotes: String = "",
    val blockedUserIds: Set<String> = emptySet(),
    val completedCourses: Set<String> = emptySet(),
    val lastSpinEpochDay: Long = 0L,
    val lastDailyLoginDay: Long = 0L,
    val payLockUntilMillis: Long = 0L,
    val dailyPayLimit: Double = 100000.0,
    // Security, Privacy, Appearance & Language fields
    val hasAccountOnDevice: Boolean = false,
    val savedDisplayName: String = "",
    val savedUsername: String = "",
    val appLockEnabled: Boolean = false,
    val appLockTimeoutSec: Int = 0, // 0 = Immediately, 60, 300, 3600
    val lockedChatIds: Set<String> = emptySet(),
    val hideLockedChats: Boolean = false,
    val favoriteChatIds: Set<String> = emptySet(),
    val disappearingHours: Int = 0, // 0 = OFF, 24, 168, 2160
    val readReceipts: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val lastSeenPrivacy: String = "Everyone",
    val profilePhotoPrivacy: String = "Everyone",
    val aboutPrivacy: String = "Everyone",
    val statusPrivacy: String = "My Contacts",
    val groupsPrivacy: String = "Everyone",
    val silenceUnknownCallers: Boolean = false,
    val liveLocationPrivacy: String = "Nobody",
    val twoStepEnabled: Boolean = false,
    val twoStepPin: String = "",
    val hideBalance: Boolean = false,
    val boldFontMode: Boolean = false,
    val appLanguage: String = "English",
    val notifMaster: Boolean = true,
    val notifMessages: Boolean = true,
    val notifGroups: Boolean = true,
    val notifPay: Boolean = true,
    val notifSecurity: Boolean = true,
    val notifPreview: Boolean = true,
    val notifVibrate: Boolean = true,
    val dndEnabled: Boolean = false,
    val lastPayAuthMillis: Long = 0L,
    val statusReactionsJson: String = "{}",
    val statusReshareCountsJson: String = "{}",
    val viewedStatusIds: Set<String> = emptySet(),
    val localStatusesJson: String = "[]",
    // v15 additions: Status favorites & trackers, Starred messages, Group Contributions, Admin & Sub-Admin
    val statusFavoritesIds: Set<String> = emptySet(),
    val statusTrackersJson: String = "[]",
    val starredMessageIds: Set<String> = emptySet(),
    val contributionsJson: String = "",
    val appSubAdminsJson: String = "",
    val groupSubAdminsJson: String = "[]",
    val adminLogsJson: String = "",
    val broadcastsJson: String = "",
    val appAdminConfigJson: String = "",
    // Persistent Login Forever + Phone/Password + Two-Number Separation
    val stayLoggedIn: Boolean = true,
    val isLoggedInSession: Boolean = false,
    val localSessionUid: String = "",
    val savedPhoneNumber: String = "",
    val savedCountryName: String = "Nigeria",
    val savedCountryFlag: String = "🇳🇬",
    val savedCurrency: String = "₦",
    val savedEmail: String = "",
    val passwordHash: String = "",
    val failedSignInAttempts: Int = 0,
    val signInLockUntilMillis: Long = 0L,
    val fingerprintSignInEnabled: Boolean = false,
    val registeredAccountsJson: String = "[]",
    val chattingPhoneNumber: String = "",
    val pglobAccountNumber: String = "",
    val chatNumberPrivacy: String = "My Contacts Only",
    val approvedChatNumbers: Set<String> = emptySet(),
    val accountNumberVisibility: String = "Show Only When I Share",
    val localWalletBalance: Double = 0.0,
    val localCoins: Long = 100L,
    val localDiamonds: Long = 1L,
    val localTransactionPin: String = "",
    val localVipTier: String = "FREE",
    val localAbout: String = "Hey there! I am using PGLOB APP.",
    val localBio: String = "",
    val localInterests: List<String> = emptyList(),
    val localPhotoUri: String = "",
    val deliveredOrderIds: Set<String> = emptySet(),
    val statusNotesJson: String = "[]",
    val dismissedExpiredNoteId: String = ""
)

class LocalSettingsStore(private val context: Context) {
    private object Keys {
        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SECRET_VAULT_PIN = stringPreferencesKey("secret_vault_pin")
        val SECRET_VAULT_NOTES = stringPreferencesKey("secret_vault_notes")
        val BLOCKED_USERS = stringPreferencesKey("blocked_users")
        val COMPLETED_COURSES = stringPreferencesKey("completed_courses")
        val LAST_SPIN_DAY = longPreferencesKey("last_spin_day")
        val LAST_DAILY_LOGIN_DAY = longPreferencesKey("last_daily_login_day")
        val PAY_LOCK_UNTIL = longPreferencesKey("pay_lock_until")
        val DAILY_PAY_LIMIT = stringPreferencesKey("daily_pay_limit")

        val HAS_ACCOUNT_ON_DEVICE = booleanPreferencesKey("has_account_on_device")
        val SAVED_DISPLAY_NAME = stringPreferencesKey("saved_display_name")
        val SAVED_USERNAME = stringPreferencesKey("saved_username")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TIMEOUT_SEC = intPreferencesKey("app_lock_timeout_sec")
        val LOCKED_CHAT_IDS = stringPreferencesKey("locked_chat_ids")
        val HIDE_LOCKED_CHATS = booleanPreferencesKey("hide_locked_chats")
        val FAVORITE_CHAT_IDS = stringPreferencesKey("favorite_chat_ids")
        val DISAPPEARING_HOURS = intPreferencesKey("disappearing_hours")
        val READ_RECEIPTS = booleanPreferencesKey("read_receipts")
        val SHOW_ONLINE = booleanPreferencesKey("show_online")
        val LAST_SEEN_PRIVACY = stringPreferencesKey("last_seen_privacy")
        val PROFILE_PHOTO_PRIVACY = stringPreferencesKey("profile_photo_privacy")
        val ABOUT_PRIVACY = stringPreferencesKey("about_privacy")
        val STATUS_PRIVACY = stringPreferencesKey("status_privacy")
        val GROUPS_PRIVACY = stringPreferencesKey("groups_privacy")
        val SILENCE_UNKNOWN_CALLERS = booleanPreferencesKey("silence_unknown_callers")
        val LIVE_LOCATION_PRIVACY = stringPreferencesKey("live_location_privacy")
        val TWO_STEP_ENABLED = booleanPreferencesKey("two_step_enabled")
        val TWO_STEP_PIN = stringPreferencesKey("two_step_pin")
        val HIDE_BALANCE = booleanPreferencesKey("hide_balance")
        val BOLD_FONT_MODE = booleanPreferencesKey("bold_font_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val NOTIF_MASTER = booleanPreferencesKey("notif_master")
        val NOTIF_MESSAGES = booleanPreferencesKey("notif_messages")
        val NOTIF_GROUPS = booleanPreferencesKey("notif_groups")
        val NOTIF_PAY = booleanPreferencesKey("notif_pay")
        val NOTIF_SECURITY = booleanPreferencesKey("notif_security")
        val NOTIF_PREVIEW = booleanPreferencesKey("notif_preview")
        val NOTIF_VIBRATE = booleanPreferencesKey("notif_vibrate")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val LAST_PAY_AUTH_MILLIS = longPreferencesKey("last_pay_auth_millis")
        val STATUS_REACTIONS_JSON = stringPreferencesKey("status_reactions_json")
        val STATUS_RESHARE_COUNTS_JSON = stringPreferencesKey("status_reshare_counts_json")
        val VIEWED_STATUS_IDS = stringPreferencesKey("viewed_status_ids")
        val LOCAL_STATUSES_JSON = stringPreferencesKey("local_statuses_json")

        val STATUS_FAVORITES_IDS = stringPreferencesKey("status_favorites_ids")
        val STATUS_TRACKERS_JSON = stringPreferencesKey("status_trackers_json")
        val STARRED_MESSAGE_IDS = stringPreferencesKey("starred_message_ids")
        val CONTRIBUTIONS_JSON = stringPreferencesKey("contributions_json")
        val APP_SUB_ADMINS_JSON = stringPreferencesKey("app_sub_admins_json")
        val GROUP_SUB_ADMINS_JSON = stringPreferencesKey("group_sub_admins_json")
        val ADMIN_LOGS_JSON = stringPreferencesKey("admin_logs_json")
        val BROADCASTS_JSON = stringPreferencesKey("broadcasts_json")
        val APP_ADMIN_CONFIG_JSON = stringPreferencesKey("app_admin_config_json")

        val STAY_LOGGED_IN = booleanPreferencesKey("stay_logged_in")
        val IS_LOGGED_IN_SESSION = booleanPreferencesKey("is_logged_in_session")
        val LOCAL_SESSION_UID = stringPreferencesKey("local_session_uid")
        val SAVED_PHONE_NUMBER = stringPreferencesKey("saved_phone_number")
        val SAVED_COUNTRY_NAME = stringPreferencesKey("saved_country_name")
        val SAVED_COUNTRY_FLAG = stringPreferencesKey("saved_country_flag")
        val SAVED_CURRENCY = stringPreferencesKey("saved_currency")
        val SAVED_EMAIL = stringPreferencesKey("saved_email")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val FAILED_SIGN_IN_ATTEMPTS = intPreferencesKey("failed_sign_in_attempts")
        val SIGN_IN_LOCK_UNTIL_MILLIS = longPreferencesKey("sign_in_lock_until_millis")
        val FINGERPRINT_SIGN_IN_ENABLED = booleanPreferencesKey("fingerprint_sign_in_enabled")
        val REGISTERED_ACCOUNTS_JSON = stringPreferencesKey("registered_accounts_json")
        val CHATTING_PHONE_NUMBER = stringPreferencesKey("chatting_phone_number")
        val PGLOB_ACCOUNT_NUMBER = stringPreferencesKey("pglob_account_number")
        val CHAT_NUMBER_PRIVACY = stringPreferencesKey("chat_number_privacy")
        val APPROVED_CHAT_NUMBERS = stringPreferencesKey("approved_chat_numbers")
        val ACCOUNT_NUMBER_VISIBILITY = stringPreferencesKey("account_number_visibility")
        val LOCAL_WALLET_BALANCE = stringPreferencesKey("local_wallet_balance")
        val LOCAL_COINS = longPreferencesKey("local_coins")
        val LOCAL_DIAMONDS = longPreferencesKey("local_diamonds")
        val LOCAL_TRANSACTION_PIN = stringPreferencesKey("local_transaction_pin")
        val LOCAL_VIP_TIER = stringPreferencesKey("local_vip_tier")
        val LOCAL_ABOUT = stringPreferencesKey("local_about")
        val LOCAL_BIO = stringPreferencesKey("local_bio")
        val LOCAL_INTERESTS = stringPreferencesKey("local_interests")
        val LOCAL_PHOTO_URI = stringPreferencesKey("local_photo_uri")
        val DELIVERED_ORDER_IDS = stringPreferencesKey("delivered_order_ids")
        val STATUS_NOTES_JSON = stringPreferencesKey("status_notes_json")
        val DISMISSED_EXPIRED_NOTE_ID = stringPreferencesKey("dismissed_expired_note_id")
    }

    val settingsFlow: Flow<LocalAppSettings> = context.pglobDataStore.data.map { prefs ->
        val rawWallpaper = prefs[Keys.WALLPAPER_ID] ?: WallpaperOption.DOODLE_PGLOB.id
        val normalizedWallpaper = if (WallpaperOption.entries.any { it.id == rawWallpaper }) {
            rawWallpaper
        } else {
            WallpaperOption.DOODLE_PGLOB.id
        }
        val rawTheme = prefs[Keys.THEME_MODE] ?: "PGLOB_BLACK"
        val normalizedTheme = if (rawTheme.isBlank() || rawTheme.contains("DARK", ignoreCase = true) && rawTheme != "PGLOB_BLACK" && rawTheme != "DIAMOND_DARK") {
            "PGLOB_BLACK"
        } else {
            rawTheme
        }
        LocalAppSettings(
            wallpaperId = normalizedWallpaper,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI] ?: "",
            themeMode = normalizedTheme,
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            secretVaultPin = prefs[Keys.SECRET_VAULT_PIN] ?: "",
            secretVaultNotes = prefs[Keys.SECRET_VAULT_NOTES] ?: "",
            blockedUserIds = (prefs[Keys.BLOCKED_USERS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            completedCourses = (prefs[Keys.COMPLETED_COURSES] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            lastSpinEpochDay = prefs[Keys.LAST_SPIN_DAY] ?: 0L,
            lastDailyLoginDay = prefs[Keys.LAST_DAILY_LOGIN_DAY] ?: 0L,
            payLockUntilMillis = prefs[Keys.PAY_LOCK_UNTIL] ?: 0L,
            dailyPayLimit = (prefs[Keys.DAILY_PAY_LIMIT] ?: "100000.0").toDoubleOrNull() ?: 100000.0,

            hasAccountOnDevice = prefs[Keys.HAS_ACCOUNT_ON_DEVICE] ?: false,
            savedDisplayName = prefs[Keys.SAVED_DISPLAY_NAME] ?: "",
            savedUsername = prefs[Keys.SAVED_USERNAME] ?: "",
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            appLockTimeoutSec = prefs[Keys.APP_LOCK_TIMEOUT_SEC] ?: 0,
            lockedChatIds = (prefs[Keys.LOCKED_CHAT_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            hideLockedChats = prefs[Keys.HIDE_LOCKED_CHATS] ?: false,
            favoriteChatIds = (prefs[Keys.FAVORITE_CHAT_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            disappearingHours = prefs[Keys.DISAPPEARING_HOURS] ?: 0,
            readReceipts = prefs[Keys.READ_RECEIPTS] ?: true,
            showOnlineStatus = prefs[Keys.SHOW_ONLINE] ?: true,
            lastSeenPrivacy = prefs[Keys.LAST_SEEN_PRIVACY] ?: "Everyone",
            profilePhotoPrivacy = prefs[Keys.PROFILE_PHOTO_PRIVACY] ?: "Everyone",
            aboutPrivacy = prefs[Keys.ABOUT_PRIVACY] ?: "Everyone",
            statusPrivacy = prefs[Keys.STATUS_PRIVACY] ?: "My Contacts",
            groupsPrivacy = prefs[Keys.GROUPS_PRIVACY] ?: "Everyone",
            silenceUnknownCallers = prefs[Keys.SILENCE_UNKNOWN_CALLERS] ?: false,
            liveLocationPrivacy = prefs[Keys.LIVE_LOCATION_PRIVACY] ?: "Nobody",
            twoStepEnabled = prefs[Keys.TWO_STEP_ENABLED] ?: false,
            twoStepPin = prefs[Keys.TWO_STEP_PIN] ?: "",
            hideBalance = prefs[Keys.HIDE_BALANCE] ?: false,
            boldFontMode = prefs[Keys.BOLD_FONT_MODE] ?: false,
            appLanguage = prefs[Keys.APP_LANGUAGE] ?: "English",
            notifMaster = prefs[Keys.NOTIF_MASTER] ?: true,
            notifMessages = prefs[Keys.NOTIF_MESSAGES] ?: true,
            notifGroups = prefs[Keys.NOTIF_GROUPS] ?: true,
            notifPay = prefs[Keys.NOTIF_PAY] ?: true,
            notifSecurity = prefs[Keys.NOTIF_SECURITY] ?: true,
            notifPreview = prefs[Keys.NOTIF_PREVIEW] ?: true,
            notifVibrate = prefs[Keys.NOTIF_VIBRATE] ?: true,
            dndEnabled = prefs[Keys.DND_ENABLED] ?: false,
            lastPayAuthMillis = prefs[Keys.LAST_PAY_AUTH_MILLIS] ?: 0L,
            statusReactionsJson = prefs[Keys.STATUS_REACTIONS_JSON] ?: "{}",
            statusReshareCountsJson = prefs[Keys.STATUS_RESHARE_COUNTS_JSON] ?: "{}",
            viewedStatusIds = (prefs[Keys.VIEWED_STATUS_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            localStatusesJson = prefs[Keys.LOCAL_STATUSES_JSON] ?: "[]",

            statusFavoritesIds = (prefs[Keys.STATUS_FAVORITES_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            statusTrackersJson = prefs[Keys.STATUS_TRACKERS_JSON] ?: "[]",
            starredMessageIds = (prefs[Keys.STARRED_MESSAGE_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            contributionsJson = prefs[Keys.CONTRIBUTIONS_JSON] ?: "",
            appSubAdminsJson = prefs[Keys.APP_SUB_ADMINS_JSON] ?: "",
            groupSubAdminsJson = prefs[Keys.GROUP_SUB_ADMINS_JSON] ?: "[]",
            adminLogsJson = prefs[Keys.ADMIN_LOGS_JSON] ?: "",
            broadcastsJson = prefs[Keys.BROADCASTS_JSON] ?: "",
            appAdminConfigJson = prefs[Keys.APP_ADMIN_CONFIG_JSON] ?: "",
            stayLoggedIn = prefs[Keys.STAY_LOGGED_IN] ?: true,
            isLoggedInSession = prefs[Keys.IS_LOGGED_IN_SESSION] ?: false,
            localSessionUid = prefs[Keys.LOCAL_SESSION_UID] ?: "",
            savedPhoneNumber = prefs[Keys.SAVED_PHONE_NUMBER] ?: "",
            savedCountryName = prefs[Keys.SAVED_COUNTRY_NAME] ?: "Nigeria",
            savedCountryFlag = prefs[Keys.SAVED_COUNTRY_FLAG] ?: "🇳🇬",
            savedCurrency = prefs[Keys.SAVED_CURRENCY] ?: "₦",
            savedEmail = prefs[Keys.SAVED_EMAIL] ?: "",
            passwordHash = prefs[Keys.PASSWORD_HASH] ?: "",
            failedSignInAttempts = prefs[Keys.FAILED_SIGN_IN_ATTEMPTS] ?: 0,
            signInLockUntilMillis = prefs[Keys.SIGN_IN_LOCK_UNTIL_MILLIS] ?: 0L,
            fingerprintSignInEnabled = prefs[Keys.FINGERPRINT_SIGN_IN_ENABLED] ?: false,
            registeredAccountsJson = prefs[Keys.REGISTERED_ACCOUNTS_JSON] ?: "[]",
            chattingPhoneNumber = prefs[Keys.CHATTING_PHONE_NUMBER] ?: "",
            pglobAccountNumber = prefs[Keys.PGLOB_ACCOUNT_NUMBER] ?: "",
            chatNumberPrivacy = prefs[Keys.CHAT_NUMBER_PRIVACY] ?: "My Contacts Only",
            approvedChatNumbers = (prefs[Keys.APPROVED_CHAT_NUMBERS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            accountNumberVisibility = prefs[Keys.ACCOUNT_NUMBER_VISIBILITY] ?: "Show Only When I Share",
            localWalletBalance = (prefs[Keys.LOCAL_WALLET_BALANCE] ?: "0.0").toDoubleOrNull() ?: 0.0,
            localCoins = prefs[Keys.LOCAL_COINS] ?: 100L,
            localDiamonds = prefs[Keys.LOCAL_DIAMONDS] ?: 1L,
            localTransactionPin = prefs[Keys.LOCAL_TRANSACTION_PIN] ?: "",
            localVipTier = prefs[Keys.LOCAL_VIP_TIER] ?: "FREE",
            localAbout = prefs[Keys.LOCAL_ABOUT] ?: "Hey there! I am using PGLOB APP.",
            localBio = prefs[Keys.LOCAL_BIO] ?: "",
            localInterests = com.example.util.ProfileCodec.decodeInterestsString(prefs[Keys.LOCAL_INTERESTS] ?: ""),
            localPhotoUri = prefs[Keys.LOCAL_PHOTO_URI] ?: "",
            deliveredOrderIds = (prefs[Keys.DELIVERED_ORDER_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toSet(),
            statusNotesJson = prefs[Keys.STATUS_NOTES_JSON] ?: "[]",
            dismissedExpiredNoteId = prefs[Keys.DISMISSED_EXPIRED_NOTE_ID] ?: ""
        )
    }

    suspend fun saveKnownAccountOnDevice(displayName: String, username: String) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.HAS_ACCOUNT_ON_DEVICE] = true
            if (displayName.isNotBlank()) prefs[Keys.SAVED_DISPLAY_NAME] = displayName
            if (username.isNotBlank()) prefs[Keys.SAVED_USERNAME] = username
        }
    }

    suspend fun clearKnownAccountOnDevice() {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.HAS_ACCOUNT_ON_DEVICE] = false
            prefs[Keys.SAVED_DISPLAY_NAME] = ""
            prefs[Keys.SAVED_USERNAME] = ""
        }
    }

    suspend fun setWallpaper(wallpaperId: String, customUri: String? = null) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_ID] = wallpaperId
            if (customUri != null) prefs[Keys.CUSTOM_WALLPAPER_URI] = customUri
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        context.pglobDataStore.edit { it[Keys.THEME_MODE] = themeMode }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.pglobDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAppLock(enabled: Boolean, timeoutSec: Int? = null) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.APP_LOCK_ENABLED] = enabled
            if (timeoutSec != null) prefs[Keys.APP_LOCK_TIMEOUT_SEC] = timeoutSec
        }
    }

    suspend fun toggleLockedChat(chatId: String) {
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.LOCKED_CHAT_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            if (current.contains(chatId)) current.remove(chatId) else current.add(chatId)
            prefs[Keys.LOCKED_CHAT_IDS] = current.joinToString(",")
        }
    }

    suspend fun toggleFavoriteChat(chatId: String) {
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.FAVORITE_CHAT_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            if (current.contains(chatId)) current.remove(chatId) else current.add(chatId)
            prefs[Keys.FAVORITE_CHAT_IDS] = current.joinToString(",")
        }
    }

    suspend fun setHideLockedChats(hide: Boolean) {
        context.pglobDataStore.edit { it[Keys.HIDE_LOCKED_CHATS] = hide }
    }

    suspend fun setDisappearingHours(hours: Int) {
        context.pglobDataStore.edit { it[Keys.DISAPPEARING_HOURS] = hours }
    }

    suspend fun updatePrivacy(
        readReceipts: Boolean? = null,
        showOnline: Boolean? = null,
        lastSeen: String? = null,
        profilePhoto: String? = null,
        about: String? = null,
        status: String? = null,
        groups: String? = null,
        silenceUnknownCallers: Boolean? = null,
        liveLocation: String? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            readReceipts?.let { prefs[Keys.READ_RECEIPTS] = it }
            showOnline?.let { prefs[Keys.SHOW_ONLINE] = it }
            lastSeen?.let { prefs[Keys.LAST_SEEN_PRIVACY] = it }
            profilePhoto?.let { prefs[Keys.PROFILE_PHOTO_PRIVACY] = it }
            about?.let { prefs[Keys.ABOUT_PRIVACY] = it }
            status?.let { prefs[Keys.STATUS_PRIVACY] = it }
            groups?.let { prefs[Keys.GROUPS_PRIVACY] = it }
            silenceUnknownCallers?.let { prefs[Keys.SILENCE_UNKNOWN_CALLERS] = it }
            liveLocation?.let { prefs[Keys.LIVE_LOCATION_PRIVACY] = it }
        }
    }

    suspend fun setTwoStep(enabled: Boolean, pin: String? = null) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.TWO_STEP_ENABLED] = enabled
            if (pin != null) prefs[Keys.TWO_STEP_PIN] = pin
        }
    }

    suspend fun setHideBalance(hide: Boolean) {
        context.pglobDataStore.edit { it[Keys.HIDE_BALANCE] = hide }
    }

    suspend fun setBoldFontMode(bold: Boolean) {
        context.pglobDataStore.edit { it[Keys.BOLD_FONT_MODE] = bold }
    }

    suspend fun setAppLanguage(lang: String) {
        context.pglobDataStore.edit { it[Keys.APP_LANGUAGE] = lang }
    }

    suspend fun updateNotificationPrefs(
        master: Boolean? = null,
        messages: Boolean? = null,
        groups: Boolean? = null,
        pay: Boolean? = null,
        security: Boolean? = null,
        preview: Boolean? = null,
        vibrate: Boolean? = null,
        dnd: Boolean? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            master?.let { prefs[Keys.NOTIF_MASTER] = it }
            messages?.let { prefs[Keys.NOTIF_MESSAGES] = it }
            groups?.let { prefs[Keys.NOTIF_GROUPS] = it }
            pay?.let { prefs[Keys.NOTIF_PAY] = it }
            security?.let { prefs[Keys.NOTIF_SECURITY] = it }
            preview?.let { prefs[Keys.NOTIF_PREVIEW] = it }
            vibrate?.let { prefs[Keys.NOTIF_VIBRATE] = it }
            dnd?.let { prefs[Keys.DND_ENABLED] = it }
        }
    }

    suspend fun setSecretVaultPin(pin: String) {
        context.pglobDataStore.edit { it[Keys.SECRET_VAULT_PIN] = pin }
    }

    suspend fun setSecretVaultNotes(notes: String) {
        context.pglobDataStore.edit { it[Keys.SECRET_VAULT_NOTES] = notes }
    }

    suspend fun toggleBlockUser(userId: String) {
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.BLOCKED_USERS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            if (current.contains(userId)) current.remove(userId) else current.add(userId)
            prefs[Keys.BLOCKED_USERS] = current.joinToString(",")
        }
    }

    suspend fun markCourseCompleted(courseId: String) {
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.COMPLETED_COURSES] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            current.add(courseId)
            prefs[Keys.COMPLETED_COURSES] = current.joinToString(",")
        }
    }

    suspend fun setLastSpinDay(day: Long) {
        context.pglobDataStore.edit { it[Keys.LAST_SPIN_DAY] = day }
    }

    suspend fun setLastDailyLoginDay(day: Long) {
        context.pglobDataStore.edit { it[Keys.LAST_DAILY_LOGIN_DAY] = day }
    }

    suspend fun setPayLockUntil(millis: Long) {
        context.pglobDataStore.edit { it[Keys.PAY_LOCK_UNTIL] = millis }
    }

    suspend fun setLastPayAuthMillis(millis: Long) {
        context.pglobDataStore.edit { it[Keys.LAST_PAY_AUTH_MILLIS] = millis }
    }

    suspend fun setDailyPayLimit(limit: Double) {
        context.pglobDataStore.edit { it[Keys.DAILY_PAY_LIMIT] = limit.toString() }
    }

    suspend fun clearLocalCacheOnly() {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.LAST_PAY_AUTH_MILLIS] = 0L
        }
    }

    suspend fun saveStatusReactionsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.STATUS_REACTIONS_JSON] = json }
    }

    suspend fun saveStatusReshareCountsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.STATUS_RESHARE_COUNTS_JSON] = json }
    }

    suspend fun markStatusViewed(statusId: String) {
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.VIEWED_STATUS_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            current.add(statusId)
            prefs[Keys.VIEWED_STATUS_IDS] = current.joinToString(",")
        }
    }

    suspend fun saveLocalStatusesJson(json: String) {
        context.pglobDataStore.edit { it[Keys.LOCAL_STATUSES_JSON] = json }
    }

    suspend fun toggleStatusFavorite(statusId: String): Boolean {
        var isFav = false
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.STATUS_FAVORITES_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            isFav = if (current.contains(statusId)) {
                current.remove(statusId)
                false
            } else {
                current.add(statusId)
                true
            }
            prefs[Keys.STATUS_FAVORITES_IDS] = current.joinToString(",")
        }
        return isFav
    }

    suspend fun saveStatusTrackersJson(json: String) {
        context.pglobDataStore.edit { it[Keys.STATUS_TRACKERS_JSON] = json }
    }

    suspend fun saveStatusNotesJson(json: String) {
        context.pglobDataStore.edit { it[Keys.STATUS_NOTES_JSON] = json }
    }

    suspend fun setDismissedExpiredNoteId(noteId: String) {
        context.pglobDataStore.edit { it[Keys.DISMISSED_EXPIRED_NOTE_ID] = noteId }
    }

    suspend fun toggleStarredMessage(messageId: String): Boolean {
        var starred = false
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.STARRED_MESSAGE_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            starred = if (current.contains(messageId)) {
                current.remove(messageId)
                false
            } else {
                current.add(messageId)
                true
            }
            prefs[Keys.STARRED_MESSAGE_IDS] = current.joinToString(",")
        }
        return starred
    }

    suspend fun saveContributionsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.CONTRIBUTIONS_JSON] = json }
    }

    suspend fun saveAppSubAdminsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.APP_SUB_ADMINS_JSON] = json }
    }

    suspend fun saveGroupSubAdminsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.GROUP_SUB_ADMINS_JSON] = json }
    }

    suspend fun saveAdminLogsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.ADMIN_LOGS_JSON] = json }
    }

    suspend fun saveBroadcastsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.BROADCASTS_JSON] = json }
    }

    suspend fun saveAppAdminConfigJson(json: String) {
        context.pglobDataStore.edit { it[Keys.APP_ADMIN_CONFIG_JSON] = json }
    }

    suspend fun savePhoneAuthSession(
        uid: String,
        displayName: String,
        phone: String,
        countryName: String,
        countryFlag: String,
        currency: String,
        email: String,
        passwordHash: String,
        pglobAccountNumber: String,
        registeredAccountsJson: String? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.HAS_ACCOUNT_ON_DEVICE] = true
            prefs[Keys.IS_LOGGED_IN_SESSION] = true
            prefs[Keys.LOCAL_SESSION_UID] = uid
            prefs[Keys.SAVED_DISPLAY_NAME] = displayName
            if ((prefs[Keys.SAVED_USERNAME] ?: "").isBlank()) {
                val baseHandle = displayName.lowercase().filter { it.isLetterOrDigit() }.take(10).ifBlank { "pglobuser" }
                prefs[Keys.SAVED_USERNAME] = "${baseHandle}_${phone.filter { it.isDigit() }.takeLast(4)}"
            }
            prefs[Keys.SAVED_PHONE_NUMBER] = phone
            prefs[Keys.CHATTING_PHONE_NUMBER] = phone
            prefs[Keys.SAVED_COUNTRY_NAME] = countryName
            prefs[Keys.SAVED_COUNTRY_FLAG] = countryFlag
            prefs[Keys.SAVED_CURRENCY] = currency
            prefs[Keys.SAVED_EMAIL] = email
            if (passwordHash.isNotBlank()) prefs[Keys.PASSWORD_HASH] = passwordHash
            prefs[Keys.PGLOB_ACCOUNT_NUMBER] = pglobAccountNumber
            prefs[Keys.FAILED_SIGN_IN_ATTEMPTS] = 0
            prefs[Keys.SIGN_IN_LOCK_UNTIL_MILLIS] = 0L
            if (registeredAccountsJson != null) {
                prefs[Keys.REGISTERED_ACCOUNTS_JSON] = registeredAccountsJson
            }
        }
    }

    suspend fun setStayLoggedIn(enabled: Boolean) {
        context.pglobDataStore.edit { it[Keys.STAY_LOGGED_IN] = enabled }
    }

    suspend fun setFingerprintSignInEnabled(enabled: Boolean) {
        context.pglobDataStore.edit { it[Keys.FINGERPRINT_SIGN_IN_ENABLED] = enabled }
    }

    suspend fun updatePasswordHash(newHash: String, registeredAccountsJson: String? = null) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.PASSWORD_HASH] = newHash
            prefs[Keys.FAILED_SIGN_IN_ATTEMPTS] = 0
            prefs[Keys.SIGN_IN_LOCK_UNTIL_MILLIS] = 0L
            if (registeredAccountsJson != null) prefs[Keys.REGISTERED_ACCOUNTS_JSON] = registeredAccountsJson
        }
    }

    suspend fun recordFailedSignInAttempt(attempts: Int, lockUntilMillis: Long) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.FAILED_SIGN_IN_ATTEMPTS] = attempts
            prefs[Keys.SIGN_IN_LOCK_UNTIL_MILLIS] = lockUntilMillis
        }
    }

    suspend fun updateRegisteredAccountsJson(json: String) {
        context.pglobDataStore.edit { it[Keys.REGISTERED_ACCOUNTS_JSON] = json }
    }

    suspend fun updateNumberPrivacy(
        chatNumberPrivacy: String? = null,
        approvedChatNumbers: Set<String>? = null,
        accountNumberVisibility: String? = null,
        pglobAccountNumber: String? = null,
        chattingPhoneNumber: String? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            chatNumberPrivacy?.let { prefs[Keys.CHAT_NUMBER_PRIVACY] = it }
            approvedChatNumbers?.let { prefs[Keys.APPROVED_CHAT_NUMBERS] = it.joinToString(",") }
            accountNumberVisibility?.let { prefs[Keys.ACCOUNT_NUMBER_VISIBILITY] = it }
            pglobAccountNumber?.let { prefs[Keys.PGLOB_ACCOUNT_NUMBER] = it }
            chattingPhoneNumber?.let { prefs[Keys.CHATTING_PHONE_NUMBER] = it }
        }
    }

    suspend fun updateLocalWalletAndCoins(
        coinsDelta: Long = 0L,
        diamondsDelta: Long = 0L,
        walletDelta: Double = 0.0,
        vipTier: String? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            val curCoins = prefs[Keys.LOCAL_COINS] ?: 100L
            val curDiamonds = prefs[Keys.LOCAL_DIAMONDS] ?: 1L
            val curWallet = (prefs[Keys.LOCAL_WALLET_BALANCE] ?: "0.0").toDoubleOrNull() ?: 0.0
            prefs[Keys.LOCAL_COINS] = (curCoins + coinsDelta).coerceAtLeast(0L)
            prefs[Keys.LOCAL_DIAMONDS] = (curDiamonds + diamondsDelta).coerceAtLeast(0L)
            prefs[Keys.LOCAL_WALLET_BALANCE] = (curWallet + walletDelta).coerceAtLeast(0.0).toString()
            if (vipTier != null) prefs[Keys.LOCAL_VIP_TIER] = vipTier
        }
    }

    suspend fun saveLocalProfileExtras(
        displayName: String? = null,
        phone: String? = null,
        email: String? = null,
        countryName: String? = null,
        countryFlag: String? = null,
        currency: String? = null,
        language: String? = null,
        about: String? = null,
        bio: String? = null,
        interests: List<String>? = null,
        photoUri: String? = null,
        transactionPin: String? = null
    ) {
        context.pglobDataStore.edit { prefs ->
            displayName?.let { prefs[Keys.SAVED_DISPLAY_NAME] = it }
            phone?.let {
                prefs[Keys.SAVED_PHONE_NUMBER] = it
                prefs[Keys.CHATTING_PHONE_NUMBER] = it
            }
            email?.let { prefs[Keys.SAVED_EMAIL] = it }
            countryName?.let { prefs[Keys.SAVED_COUNTRY_NAME] = it }
            countryFlag?.let { prefs[Keys.SAVED_COUNTRY_FLAG] = it }
            currency?.let { prefs[Keys.SAVED_CURRENCY] = it }
            language?.let { prefs[Keys.APP_LANGUAGE] = it }
            about?.let { prefs[Keys.LOCAL_ABOUT] = it }
            bio?.let { prefs[Keys.LOCAL_BIO] = com.example.util.ProfileCodec.sanitizeBio(it) }
            interests?.let { prefs[Keys.LOCAL_INTERESTS] = com.example.util.ProfileCodec.encodeInterestsString(it) }
            photoUri?.let { prefs[Keys.LOCAL_PHOTO_URI] = it }
            transactionPin?.let { prefs[Keys.LOCAL_TRANSACTION_PIN] = it }
        }
    }

    suspend fun markOrderDelivered(orderId: String) {
        if (orderId.isBlank()) return
        context.pglobDataStore.edit { prefs ->
            val current = (prefs[Keys.DELIVERED_ORDER_IDS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            current.add(orderId)
            prefs[Keys.DELIVERED_ORDER_IDS] = current.joinToString(",")
        }
    }

    suspend fun logoutExplicitSession(forgetDeviceAccount: Boolean = false) {
        context.pglobDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN_SESSION] = false
            prefs[Keys.LAST_PAY_AUTH_MILLIS] = 0L
            if (forgetDeviceAccount) {
                prefs[Keys.HAS_ACCOUNT_ON_DEVICE] = false
                prefs[Keys.SAVED_DISPLAY_NAME] = ""
                prefs[Keys.SAVED_USERNAME] = ""
            }
        }
    }
}
