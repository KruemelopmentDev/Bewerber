package de.kruemelopment.org.bewerber

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import de.kruemelopment.org.bewerber.ui.theme.BewerberDarkColorScheme
import de.kruemelopment.org.bewerber.ui.theme.BewerberLightColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import androidx.core.graphics.createBitmap

class Widget : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = BewerberGlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                BewerberGlanceWidget.refreshAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class BewerberGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (fallbackWord, fallbackSlogan) = withContext(Dispatchers.IO) {
            generateRandomEntry(context)
        }

        provideContent {
            val prefs = currentState<Preferences>()
            val word = prefs[KEY_WORD] ?: fallbackWord
            val slogan = prefs[KEY_SLOGAN] ?: fallbackSlogan

            GlanceTheme {
                WidgetContent(
                    context = context,
                    word = word,
                    slogan = slogan
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        word: String,
        slogan: String
    ) {
        val size = LocalSize.current
        val fontSize = computeAdaptiveFontSize(size, slogan.length)

        val lightScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            BewerberLightColorScheme
        }
        val darkScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            BewerberDarkColorScheme
        }

        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val activeScheme = if (isNight) darkScheme else lightScheme

        val cardBackgroundColor = ColorProvider(
            day = lightScheme.surfaceContainerHigh,
            night = darkScheme.surfaceContainerHigh
        )
        val textColor = ColorProvider(
            day = lightScheme.onSurface,
            night = darkScheme.onSurface
        )
        val accentColor = ColorProvider(
            day = lightScheme.primary,
            night = darkScheme.primary
        )

        val decorBitmap = createCardDecorBitmap(
            widthDp = size.width.value.coerceAtLeast(120f),
            heightDp = size.height.value.coerceAtLeast(56f),
            primaryArgb = activeScheme.primary.copy(alpha = 0.08f).toArgb(),
            tertiaryArgb = activeScheme.tertiary.copy(alpha = 0.06f).toArgb()
        )

        val openAppIntent = Intent(context, MainAcitivity::class.java).apply {
            putExtra("wort", word)
            putExtra("spruch", slogan)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(cardBackgroundColor)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(openAppIntent))
        ) {
            Image(
                provider = ImageProvider(decorBitmap),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.fillMaxSize()
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = slogan,
                    style = TextStyle(
                        color = textColor,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .cornerRadius(16.dp)
                        .clickable(actionRunCallback<RefreshWidgetAction>())
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.refresh),
                        contentDescription = context.getString(R.string.widget_refresh_desc),
                        colorFilter = ColorFilter.tint(accentColor),
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            }
        }
    }

    private fun createCardDecorBitmap(
        widthDp: Float,
        heightDp: Float,
        primaryArgb: Int,
        tertiaryArgb: Int
    ): Bitmap {
        val scale = 2f
        val widthPx = (widthDp * scale).toInt().coerceIn(200, 800)
        val heightPx = (heightDp * scale).toInt().coerceIn(100, 800)
        val bitmap = createBitmap(widthPx, heightPx)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val minDim = min(widthPx, heightPx).toFloat()

        paint.color = primaryArgb
        canvas.drawCircle(widthPx * 0.9f, heightPx * 0.12f, minDim * 0.48f, paint)

        paint.color = tertiaryArgb
        canvas.drawCircle(widthPx * 0.08f, heightPx * 0.88f, minDim * 0.36f, paint)

        return bitmap
    }

    private fun computeAdaptiveFontSize(size: DpSize, textLength: Int): TextUnit {
        val compact = size.height < 90.dp || size.width < 180.dp
        return when {
            compact && textLength > 70 -> 13.sp
            compact && textLength > 45 -> 14.sp
            compact -> 16.sp
            textLength > 90 -> 14.sp
            textLength > 60 -> 16.sp
            textLength > 35 -> 18.sp
            else -> 20.sp
        }
    }

    companion object {
        private val KEY_WORD = stringPreferencesKey("widget_word")
        private val KEY_SLOGAN = stringPreferencesKey("widget_slogan")

        fun generateRandomEntry(context: Context): Pair<String, String> {
            val myDB = DataBaseHelper(context)
            val (savedWords, additionalWords) = try {
                myDB.ensureDefaultWords()
            } catch (_: Exception) {
                Pair(SloganGenerator.DEFAULT_SAVED_WORDS, SloganGenerator.DEFAULT_ADDITIONAL_WORDS)
            }
            val customTemplates = try {
                myDB.getCustomTemplates()
            } catch (_: Exception) {
                emptyList()
            }
            myDB.close()

            val builtInTemplates = context.resources.getStringArray(R.array.sprueche).toList()
            val allTemplates = customTemplates + builtInTemplates

            return SloganGenerator.generateForRandomWord(
                words = savedWords,
                templates = allTemplates,
                additionalWords = additionalWords
            )
        }

        suspend fun refreshWidget(context: Context, glanceId: GlanceId) {
            val (word, slogan) = withContext(Dispatchers.IO) {
                generateRandomEntry(context)
            }
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[KEY_WORD] = word
                prefs[KEY_SLOGAN] = slogan
            }
            BewerberGlanceWidget().update(context, glanceId)
        }

        suspend fun refreshAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(BewerberGlanceWidget::class.java)
            for (glanceId in ids) {
                refreshWidget(context, glanceId)
            }
        }
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        BewerberGlanceWidget.refreshWidget(context, glanceId)
    }
}
