package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

enum class PglobThemeMode(val displayName: String) {
    PGLOB_BLACK("PGLOB Black"),
    GOLD("Gold + Dark"),
    BLACK_WHITE("Monochrome Dark"),
    DIAMOND_BLACK("Diamond Cyan"),
    ROYAL_PURPLE("Royal Violet"),
    GOLD_ONLY("Gold Accent")
}

private val PglobBlackColorScheme = darkColorScheme(
    primary = WaGreen,
    onPrimary = WaBackground,
    secondary = WaGold,
    onSecondary = WaBackground,
    tertiary = WaBlueTick,
    background = WaBackground,
    onBackground = WaTextPrimary,
    surface = WaCard,
    onSurface = WaTextPrimary,
    surfaceVariant = WaInput,
    onSurfaceVariant = WaTextSecondary,
    outline = WaDivider,
    error = WaRed,
    onError = WaTextPrimary
)

private val GoldDarkColorScheme = darkColorScheme(
    primary = WaGreen,
    onPrimary = WaBackground,
    secondary = WaGold,
    onSecondary = WaBackground,
    tertiary = WaBlueTick,
    background = WaBackground,
    onBackground = WaTextPrimary,
    surface = WaCard,
    onSurface = WaTextPrimary,
    surfaceVariant = WaInput,
    onSurfaceVariant = WaTextSecondary,
    outline = WaDivider,
    error = WaRed,
    onError = WaTextPrimary
)

private val BlackWhiteColorScheme = darkColorScheme(
    primary = WaTextPrimary,
    onPrimary = WaBackground,
    secondary = WaGreen,
    onSecondary = WaBackground,
    tertiary = WaBlueTick,
    background = WaBackground,
    onBackground = WaTextPrimary,
    surface = WaCard,
    onSurface = WaTextPrimary,
    surfaceVariant = WaInput,
    onSurfaceVariant = WaTextSecondary,
    outline = WaDivider,
    error = WaRed,
    onError = WaTextPrimary
)

private val DiamondDarkColorScheme = darkColorScheme(
    primary = PglobDiamondCyan,
    onPrimary = WaBackground,
    secondary = WaGreen,
    onSecondary = WaBackground,
    tertiary = WaGold,
    background = WaBackground,
    onBackground = WaTextPrimary,
    surface = WaCard,
    onSurface = WaTextPrimary,
    surfaceVariant = WaInput,
    onSurfaceVariant = WaTextSecondary,
    outline = WaDivider,
    error = WaRed,
    onError = WaTextPrimary
)

private val RoyalPurpleColorScheme = darkColorScheme(
    primary = PglobRoyalPurpleAccent,
    onPrimary = WaTextPrimary,
    secondary = WaGreen,
    onSecondary = WaBackground,
    tertiary = WaGold,
    background = Color(0xFF0E091A),
    onBackground = WaTextPrimary,
    surface = Color(0xFF150E24),
    onSurface = WaTextPrimary,
    surfaceVariant = Color(0xFF211836),
    onSurfaceVariant = Color(0xFFB5AFC7),
    outline = Color(0xFF2B2242),
    error = WaRed,
    onError = WaTextPrimary
)

private val GoldOnlyColorScheme = darkColorScheme(
    primary = WaGold,
    onPrimary = WaBackground,
    secondary = WaGreen,
    onSecondary = WaBackground,
    tertiary = WaBlueTick,
    background = WaBackground,
    onBackground = WaTextPrimary,
    surface = WaCard,
    onSurface = WaTextPrimary,
    surfaceVariant = WaInput,
    onSurfaceVariant = WaTextSecondary,
    outline = WaDivider,
    error = WaRed,
    onError = WaTextPrimary
)

private fun buildTypography(boldFontMode: Boolean): Typography {
    val bodyWeight = if (boldFontMode) FontWeight.SemiBold else FontWeight.Medium
    return Typography(
        headlineLarge = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = Typography.bodyLarge.copy(fontWeight = bodyWeight),
        bodyMedium = Typography.bodyMedium.copy(fontWeight = bodyWeight),
        labelLarge = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        labelMedium = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        labelSmall = Typography.labelSmall.copy(fontWeight = FontWeight.Medium)
    )
}

@Composable
fun PglobChatTheme(
    themeMode: PglobThemeMode = PglobThemeMode.PGLOB_BLACK,
    boldFontMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when (themeMode) {
        PglobThemeMode.PGLOB_BLACK -> PglobBlackColorScheme
        PglobThemeMode.GOLD -> GoldDarkColorScheme
        PglobThemeMode.BLACK_WHITE -> BlackWhiteColorScheme
        PglobThemeMode.DIAMOND_BLACK -> DiamondDarkColorScheme
        PglobThemeMode.ROYAL_PURPLE -> RoyalPurpleColorScheme
        PglobThemeMode.GOLD_ONLY -> GoldOnlyColorScheme
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = buildTypography(boldFontMode),
        content = content
    )
}
