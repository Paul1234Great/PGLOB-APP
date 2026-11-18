package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ContributionCodec {
    data class DecodedCard(
        val groupId: String,
        val title: String,
        val subtitle: String,
        val badge: String
    )

    fun encode(groupId: String, title: String, subtitle: String, badge: String): String {
        val g = groupId.replace("|", "").replace("]", "").take(48)
        val t = title.replace("|", " ").replace("]", " ").take(80)
        val s = subtitle.replace("|", " ").replace("]", " ").take(120)
        val b = badge.replace("|", " ").replace("]", " ").take(32)
        return "[CONTRIBUTION|$g|$t|$s|$b]".take(480)
    }

    fun decodeOrNull(raw: String): DecodedCard? {
        if (!raw.startsWith("[CONTRIBUTION|")) return null
        val end = raw.indexOf(']')
        if (end <= 0) return null
        val parts = raw.substring(1, end).split('|')
        if (parts.size < 5) return null
        return DecodedCard(
            groupId = parts[1],
            title = parts[2],
            subtitle = parts[3],
            badge = parts[4]
        )
    }
}

object PdfReceiptGenerator {
    fun generateAndShareReceipt(
        context: Context,
        title: String,
        lines: List<String>,
        filePrefix: String = "pglob_receipt"
    ): Result<File> = try {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val bgPaint = Paint().apply { color = Color.rgb(11, 20, 26) }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val headerPaint = Paint().apply { color = Color.rgb(0, 168, 132) }
        canvas.drawRect(0f, 0f, 595f, 96f, headerPaint)

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("PGLOB APP • ESCROW RECEIPT", 36f, 52f, brandPaint)

        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(233, 237, 239)
            textSize = 13f
        }
        canvas.drawText("Verified Digital Contribution & Payment Record", 36f, 76f, subHeaderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 215, 0)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title.take(46), 36f, 148f, titlePaint)

        val linePaint = Paint().apply {
            color = Color.rgb(34, 45, 52)
            strokeWidth = 2f
        }
        canvas.drawLine(36f, 168f, 559f, 168f, linePaint)

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(233, 237, 239)
            textSize = 14f
        }
        var y = 205f
        lines.forEach { row ->
            if (y < 760f) {
                canvas.drawText(row.take(68), 36f, y, bodyPaint)
                y += 30f
            }
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(134, 150, 160)
            textSize = 11f
        }
        canvas.drawLine(36f, 775f, 559f, 775f, linePaint)
        canvas.drawText("Secured by PGLOB Escrow • Do not share your PIN or OTP with anyone.", 36f, 804f, footerPaint)

        pdf.finishPage(page)

        val safePrefix = filePrefix.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24)
        val outFile = File(context.cacheDir, "${safePrefix}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { fos ->
            pdf.writeTo(fos)
        }
        pdf.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Download / Share PDF Receipt")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        Result.success(outFile)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
