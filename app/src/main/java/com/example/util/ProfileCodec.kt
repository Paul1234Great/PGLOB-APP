package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ProfileCodec {
    const val MAX_BIO_LENGTH = 200
    const val MAX_INTERESTS_COUNT = 10
    const val MAX_INTEREST_LENGTH = 28
    private const val MAX_PHOTO_DATA_URL_LENGTH = 55_000

    val SUGGESTED_INTERESTS: List<String> = listOf(
        "Ajo Savings",
        "Afrobeats",
        "Tech & AI",
        "Business",
        "Football",
        "Fintech & Pay",
        "Travel",
        "Fashion",
        "Food & Cooking",
        "Music",
        "Movies",
        "Gaming",
        "Fitness",
        "Photography",
        "Design",
        "Startups",
        "Reading",
        "Faith & Community"
    )

    fun sanitizeBio(raw: String): String = raw.take(MAX_BIO_LENGTH)

    fun formatBioCounter(raw: String): String =
        "${raw.take(MAX_BIO_LENGTH).length}/$MAX_BIO_LENGTH"

    fun normalizeInterestTag(raw: String): String =
        raw.replace("|", " ")
            .replace("\n", " ")
            .trim()
            .replace(Regex("\\s+"), " ")
            .take(MAX_INTEREST_LENGTH)

    fun addInterest(current: List<String>, rawTag: String): List<String> {
        val clean = normalizeInterestTag(rawTag)
        if (clean.isBlank()) return current
        if (current.any { it.equals(clean, ignoreCase = true) }) return current
        if (current.size >= MAX_INTERESTS_COUNT) return current
        return current + clean
    }

    fun removeInterest(current: List<String>, tag: String): List<String> =
        current.filterNot { it.equals(tag, ignoreCase = true) }

    fun encodeInterestsString(interests: List<String>): String =
        interests.mapNotNull { normalizeInterestTag(it).takeIf { clean -> clean.isNotBlank() } }
            .distinctBy { it.lowercase() }
            .take(MAX_INTERESTS_COUNT)
            .joinToString("|")

    fun decodeInterestsString(raw: String): List<String> =
        raw.split("|")
            .mapNotNull { normalizeInterestTag(it).takeIf { clean -> clean.isNotBlank() } }
            .distinctBy { it.lowercase() }
            .take(MAX_INTERESTS_COUNT)

    fun compressProfileImageToDataUrlOrFallback(context: Context, uri: Uri): String {
        val raw = uri.toString()
        if (raw.startsWith("data:image/")) {
            return raw.take(MAX_PHOTO_DATA_URL_LENGTH)
        }
        return try {
            val resolver = context.contentResolver
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, boundsOpts) }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
                return raw.take(500)
            }
            var sample = 1
            val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
            while (maxDim / sample > 400) {
                sample *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return raw.take(500)

            val targetMax = 180
            val scale = minOf(
                targetMax.toFloat() / decoded.width.coerceAtLeast(1),
                targetMax.toFloat() / decoded.height.coerceAtLeast(1),
                1f
            )
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                decoded
            }

            val baos = ByteArrayOutputStream()
            var quality = 72
            var encodedUrl = ""
            while (quality >= 35) {
                baos.reset()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                val candidate = "data:image/jpeg;base64,$b64"
                if (candidate.length <= MAX_PHOTO_DATA_URL_LENGTH) {
                    encodedUrl = candidate
                    break
                }
                quality -= 15
            }
            if (scaled !== decoded) scaled.recycle()
            decoded.recycle()
            encodedUrl.ifBlank { raw.take(500) }
        } catch (_: Throwable) {
            raw.take(500)
        }
    }
}
