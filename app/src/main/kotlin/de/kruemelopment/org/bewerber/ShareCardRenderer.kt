package de.kruemelopment.org.bewerber

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import androidx.core.graphics.withRotation

object ShareCardRenderer {

    private const val CARD_SIZE = 1080
    private const val GALLERY_FOLDER = "individuelle Werbesprüche"
    private const val FILE_PROVIDER_AUTHORITY = "de.kruemelopment.org.bewerber.provider"

    fun renderShareCard(
        slogan: String,
        word: String = "",
        backgroundColorArgb: Int = 0xFF0A2E1A.toInt(),
        cardSurfaceColorArgb: Int = 0xFF124E2B.toInt(),
        accentColorArgb: Int = 0xFF04E762.toInt(),
        textColorArgb: Int = 0xFFFFFFFF.toInt()
    ): Bitmap {
        val bitmap = createBitmap(CARD_SIZE, CARD_SIZE)
        val canvas = Canvas(bitmap)
        val sizeF = CARD_SIZE.toFloat()

        // 1. Farbverlauf-Hintergrund
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                sizeF,
                sizeF,
                backgroundColorArgb,
                cardSurfaceColorArgb,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, sizeF, sizeF, bgPaint)

        // 2. Dekorative Material 3 Expressive Kreise & Pillen im Hintergrund
        val decoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(accentColorArgb, 28)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(940f, 150f, 240f, decoPaint)
        canvas.drawCircle(130f, 950f, 210f, decoPaint)

        val pillDecoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(accentColorArgb, 18)
            style = Paint.Style.FILL
        }
        canvas.withRotation(-18f, 540f, 540f) {
            drawRoundRect(RectF(140f, 180f, 940f, 340f), 80f, 80f, pillDecoPaint)
        }

        // 3. Abgerundete Hauptkarte mit feinem Akzent-Rahmen
        val cardRect = RectF(72f, 72f, 1008f, 1008f)
        val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(cardSurfaceColorArgb, 232)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 72f, 72f, cardFillPaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(accentColorArgb, 110)
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }
        canvas.drawRoundRect(cardRect, 72f, 72f, cardBorderPaint)

        // 4. Große stilisierte Anführungszeichen oben links
        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(accentColorArgb, 210)
            textSize = 180f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("„", 136f, 260f, quotePaint)

        //Kleiner dekorativer Akzentbalken unter dem Anführungszeichen
        val accentBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColorArgb
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(146f, 284f, 256f, 296f), 6f, 6f, accentBarPaint)

        // 5. Spruch zentriert mit StaticLayout.Builder
        val cleanSlogan = slogan.trim().ifEmpty { "Sprücheklopfer" }
        val dynamicTextSize = when {
            cleanSlogan.length <= 45 -> 64f
            cleanSlogan.length <= 90 -> 54f
            else -> 44f
        }

        val sloganPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorArgb
            textSize = dynamicTextSize
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }

        val textWidth = 768 // 1080 - 2 * 156 Padding
        val staticLayout = StaticLayout.Builder
            .obtain(cleanSlogan, 0, cleanSlogan.length, sloganPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(8f, 1.15f)
            .setIncludePad(false)
            .build()

        val textAreaTop = 320f
        val textAreaBottom = 840f
        val availableHeight = textAreaBottom - textAreaTop
        val textY = textAreaTop + ((availableHeight - staticLayout.height) / 2f).coerceAtLeast(0f)

        canvas.withTranslation(156f, textY) {
            staticLayout.draw(this)
        }

        // 6. Dezenter Pill-Badge Footer unten
        val cleanWord = word.trim()
        val footerText = if (cleanWord.isNotEmpty()) {
            "#$cleanWord  •  Sprücheklopfer"
        } else {
            "Sprücheklopfer"
        }

        val footerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorArgb
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val textVisualWidth = footerTextPaint.measureText(footerText)
        val pillWidth = (textVisualWidth + 88f).coerceAtMost(820f)
        val pillHeight = 64f
        val pillLeft = (sizeF - pillWidth) / 2f
        val pillTop = 884f
        val pillRect = RectF(pillLeft, pillTop, pillLeft + pillWidth, pillTop + pillHeight)

        val pillFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(backgroundColorArgb, 190)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillFillPaint)

        val pillStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(accentColorArgb, 140)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillStrokePaint)

        val fontMetrics = footerTextPaint.fontMetrics
        val baselineY = pillRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(footerText, sizeF / 2f, baselineY, footerTextPaint)

        return bitmap
    }

    fun saveSloganImageToGallery(
        context: Context,
        bitmap: Bitmap,
        slogan: String
    ): Result<Uri> = runCatching {
        val fileName = "${SloganGenerator.sanitizeFileName(slogan)}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val relativePath = "${Environment.DIRECTORY_PICTURES}${File.separator}$GALLERY_FOLDER"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("MediaStore-Eintrag konnte nicht erstellt werden")

            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IllegalStateException("PNG-Komprimierung fehlgeschlagen")
                }
                out.flush()
            } ?: throw IllegalStateException("OutputStream konnte nicht geöffnet werden")

            val publishValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(uri, publishValues, null, null)
            uri
        } else {
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val targetDir = File(picturesDir, GALLERY_FOLDER)
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IllegalStateException("Zielordner konnte nicht erstellt werden")
            }
            val imageFile = File(targetDir, fileName)
            FileOutputStream(imageFile).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IllegalStateException("PNG-Komprimierung fehlgeschlagen")
                }
                out.flush()
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                arrayOf("image/png"),
                null
            )
            Uri.fromFile(imageFile)
        }
    }

    fun shareSloganAsImage(
        context: Context,
        bitmap: Bitmap,
        slogan: String
    ): Result<Unit> = runCatching {
        val fileName = "${SloganGenerator.sanitizeFileName(slogan)}.png"
        val dir = context.getExternalFilesDir(null) ?: context.cacheDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val imageFile = File(dir, fileName)
        FileOutputStream(imageFile).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IllegalStateException("PNG-Komprimierung fehlgeschlagen")
            }
            out.flush()
        }

        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, slogan)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Werbespruch teilen über…").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun shareSloganAsText(context: Context, slogan: String) {
        val trimmed = slogan.trim()
        if (trimmed.isEmpty()) return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, trimmed)
        }
        val chooser = Intent.createChooser(shareIntent, "Spruch als Text teilen…").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(
            alpha.coerceIn(0, 255),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}
