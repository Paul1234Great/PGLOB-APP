package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WallpaperOption
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaDoodleColor
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary

/**
 * Solid PGLOB Black (#0B141A) background used for ALL list and dashboard screens:
 * Chats, Updates, Communities, Calls, Settings, Contribution List, Pay List, Admin Dashboard.
 * Zero Earth, zero gradient, zero pattern.
 */
@Composable
fun PglobListBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WaBackground),
        content = content
    )
}

/**
 * Inside-Chat background (1-on-1, Group, and Contribution Chats):
 * Solid #0B141A with optional ultra-faint (4%) PGLOB doodle pattern (24dp icons spaced far apart),
 * plain #0B141A, OLED #000000, or custom gallery wallpaper. Zero Earth assets.
 */
@Composable
fun PglobChatBackground(
    wallpaperId: String,
    customUri: String = "",
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val baseColor = if (wallpaperId == WallpaperOption.PLAIN_BLACK.id) WaPureBlack else WaBackground
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        when (wallpaperId) {
            WallpaperOption.CUSTOM_URI.id -> {
                if (customUri.isNotBlank()) {
                    AsyncImage(
                        model = Uri.parse(customUri),
                        contentDescription = "Custom chat wallpaper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WaBackground.copy(alpha = 0.72f))
                    )
                } else {
                    SubtlePglobDoodleCanvas()
                }
            }

            WallpaperOption.SOLID_DARK.id, WallpaperOption.PLAIN_BLACK.id -> {
                // Solid #0B141A or #000000
            }

            else -> {
                // Default for chat: SOLID #0B141A + ultra-faint 4% PGLOB doodle icons
                SubtlePglobDoodleCanvas()
            }
        }
        content()
    }
}

@Composable
private fun SubtlePglobDoodleCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val iconPx = 24.dp.toPx()
        val spacing = 92.dp.toPx()
        val cols = (size.width / spacing).toInt() + 2
        val rows = (size.height / spacing).toInt() + 2
        // Very faint opacity (~4%) on #1A2A33 doodle glyphs over #0B141A
        val doodleTint = WaDoodleColor.copy(alpha = 0.28f)
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)

        for (r in 0..rows) {
            for (c in 0..cols) {
                val cx = c * spacing + if (r % 2 == 0) 24.dp.toPx() else 68.dp.toPx()
                val cy = r * spacing + 28.dp.toPx()
                val half = iconPx * 0.42f
                when ((r * 5 + c) % 5) {
                    // 0: Chat bubble
                    0 -> {
                        drawRoundRect(
                            color = doodleTint,
                            topLeft = Offset(cx - half, cy - half * 0.8f),
                            size = Size(iconPx * 0.84f, iconPx * 0.64f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            style = stroke
                        )
                    }
                    // 1: Smile circle
                    1 -> {
                        drawCircle(
                            color = doodleTint,
                            radius = half * 0.88f,
                            center = Offset(cx, cy),
                            style = stroke
                        )
                        drawArc(
                            color = doodleTint,
                            startAngle = 25f,
                            sweepAngle = 130f,
                            useCenter = false,
                            topLeft = Offset(cx - half * 0.5f, cy - half * 0.35f),
                            size = Size(half, half * 0.8f),
                            style = stroke
                        )
                    }
                    // 2: Phone handset arc
                    2 -> {
                        drawArc(
                            color = doodleTint,
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(cx - half, cy - half * 0.6f),
                            size = Size(iconPx * 0.78f, iconPx * 0.78f),
                            style = stroke
                        )
                    }
                    // 3: Camera outline
                    3 -> {
                        drawRoundRect(
                            color = doodleTint,
                            topLeft = Offset(cx - half, cy - half * 0.65f),
                            size = Size(iconPx * 0.8f, iconPx * 0.56f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = stroke
                        )
                        drawCircle(
                            color = doodleTint,
                            radius = half * 0.36f,
                            center = Offset(cx, cy - half * 0.05f),
                            style = stroke
                        )
                    }
                    // 4: Four-point diamond glyph
                    else -> {
                        val path = Path().apply {
                            moveTo(cx, cy - half)
                            lineTo(cx + half * 0.28f, cy - half * 0.28f)
                            lineTo(cx + half, cy)
                            lineTo(cx + half * 0.28f, cy + half * 0.28f)
                            lineTo(cx, cy + half)
                            lineTo(cx - half * 0.28f, cy + half * 0.28f)
                            lineTo(cx - half, cy)
                            lineTo(cx - half * 0.28f, cy - half * 0.28f)
                            close()
                        }
                        drawPath(path = path, color = doodleTint, style = stroke)
                    }
                }
            }
        }
    }
}

/**
 * Official PGLOB Logo:
 * - Solid Green rounded square (#00A884 by default, or #25D366 for launcher preview)
 * - Crisp White chat bubble outline with bottom-left tail
 * - Bold White "P" centered inside
 * - Zero stars, zero extra icons, zero blur, zero transparency.
 */
@Composable
fun PglobBrandLogo(
    size: Dp = 120.dp,
    modifier: Modifier = Modifier,
    containerColor: Color = WaGreen,
    shape: Shape = RoundedCornerShape((size.value * 0.24f).dp)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h * 0.47f
            val r = w * 0.31f
            val strokeWidth = (w * 0.078f).coerceAtLeast(2f)

            // White chat bubble outline with clean bottom-left speech tail
            val bubblePath = Path().apply {
                // Start at bottom-left tail base, curve around top, down right, back to tail tip
                moveTo(cx - r * 0.46f, cy + r * 0.84f)
                lineTo(cx - r * 0.92f, cy + r * 1.06f)
                lineTo(cx - r * 0.78f, cy + r * 0.56f)
                // Left side up
                cubicTo(
                    cx - r * 1.04f, cy + r * 0.20f,
                    cx - r * 1.04f, cy - r * 0.36f,
                    cx - r * 0.68f, cy - r * 0.68f
                )
                // Top curve across
                cubicTo(
                    cx - r * 0.30f, cy - r * 1.02f,
                    cx + r * 0.34f, cy - r * 1.02f,
                    cx + r * 0.70f, cy - r * 0.68f
                )
                // Right side down
                cubicTo(
                    cx + r * 1.06f, cy - r * 0.34f,
                    cx + r * 1.04f, cy + r * 0.34f,
                    cx + r * 0.66f, cy + r * 0.68f
                )
                // Bottom curve back to tail base
                cubicTo(
                    cx + r * 0.32f, cy + r * 0.98f,
                    cx - r * 0.14f, cy + r * 0.98f,
                    cx - r * 0.46f, cy + r * 0.84f
                )
                close()
            }
            drawPath(
                path = bubblePath,
                color = Color.White,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        Text(
            text = "P",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.39f).sp,
            lineHeight = (size.value * 0.42f).sp,
            letterSpacing = 0.sp
        )
    }
}

@Composable
fun PglobLogoWithTagline(
    logoSize: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PglobBrandLogo(size = logoSize)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "PGLOB",
            color = WaTextPrimary,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Chat • Pay • Earn",
            color = WaTextSecondary,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        )
    }
}
