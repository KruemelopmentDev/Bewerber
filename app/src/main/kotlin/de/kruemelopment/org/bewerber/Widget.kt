package de.kruemelopment.org.bewerber

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
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
import androidx.glance.appwidget.state.getAppWidgetState
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
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import de.kruemelopment.org.bewerber.ui.theme.readThemeSettings
import de.kruemelopment.org.bewerber.ui.theme.resolveBewerberColorSchemes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

class Widget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BewerberGlanceWidget()
}

class BewerberGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val currentPrefs = runCatching {
            getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        }.getOrNull()

        val existingWord = currentPrefs?.get(KEY_WORD)?.takeIf { it.isNotBlank() }
        val existingSlogan = currentPrefs?.get(KEY_SLOGAN)?.takeIf { it.isNotBlank() }
        val lastUpdated = currentPrefs?.get(KEY_LAST_UPDATED) ?: 0L
        val now = System.currentTimeMillis()

        val needsNewEntry = existingSlogan == null ||
            (lastUpdated > 0L && now - lastUpdated >= AUTO_REFRESH_INTERVAL_MS)

        val (fallbackWord, fallbackSlogan) = if (needsNewEntry) {
            val entry = withContext(Dispatchers.IO) {
                generateRandomEntry(context)
            }
            runCatching {
                updateAppWidgetState(context, id) { prefs ->
                    prefs[KEY_WORD] = entry.first
                    prefs[KEY_SLOGAN] = entry.second
                    prefs[KEY_LAST_UPDATED] = now
                }
            }
            entry
        } else {
            Pair(existingWord.orEmpty(), existingSlogan)
        }

        provideContent {
            val prefs = currentState<Preferences>()
            // Referencing KEY_THEME_SYNC ensures recomposition when app theme settings change.
            @Suppress("UNUSED_VARIABLE")
            val themeSync = prefs[KEY_THEME_SYNC]
            val word = prefs[KEY_WORD]?.takeIf { it.isNotBlank() } ?: fallbackWord
            val slogan = prefs[KEY_SLOGAN]?.takeIf { it.isNotBlank() } ?: fallbackSlogan
            val (themeMode, useDynamicColor) = readThemeSettings(context)

            GlanceTheme {
                WidgetContent(
                    context = context,
                    word = word,
                    slogan = slogan,
                    themeMode = themeMode,
                    useDynamicColor = useDynamicColor
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        word: String,
        slogan: String,
        themeMode: ThemeMode,
        useDynamicColor: Boolean
    ) {
        val size = LocalSize.current
        val fontSize = computeAdaptiveFontSize(size, slogan.length)

        val (lightScheme, darkScheme) = resolveBewerberColorSchemes(context, useDynamicColor)
        val systemNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        val dayScheme = when (themeMode) {
            ThemeMode.SYSTEM -> lightScheme
            ThemeMode.LIGHT -> lightScheme
            ThemeMode.DARK -> darkScheme
        }
        val nightScheme = when (themeMode) {
            ThemeMode.SYSTEM -> darkScheme
            ThemeMode.LIGHT -> lightScheme
            ThemeMode.DARK -> darkScheme
        }
        val activeScheme = when (themeMode) {
            ThemeMode.SYSTEM -> if (systemNight) darkScheme else lightScheme
            ThemeMode.LIGHT -> lightScheme
            ThemeMode.DARK -> darkScheme
        }

        val cardBackgroundColor = ColorProvider(
            day = dayScheme.surfaceContainerHigh,
            night = nightScheme.surfaceContainerHigh
        )
        val textColor = ColorProvider(
            day = dayScheme.onSurface,
            night = nightScheme.onSurface
        )
        val accentColor = ColorProvider(
            day = dayScheme.primary,
            night = nightScheme.primary
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
        private val KEY_LAST_UPDATED = longPreferencesKey("widget_last_updated")
        private val KEY_THEME_SYNC = longPreferencesKey("widget_theme_sync")
        private const val AUTO_REFRESH_INTERVAL_MS = 25 * 60 * 1000L

        fun generateRandomEntry(context: Context): Pair<String, String> {
            val myDB = DataBaseHelper(context)
            val (savedWords, additionalWords, customTemplates) = try {
                val (saved, additional) = try {
                    myDB.ensureDefaultWords()
                } catch (_: Exception) {
                    Pair(SloganGenerator.DEFAULT_SAVED_WORDS, SloganGenerator.DEFAULT_ADDITIONAL_WORDS)
                }
                val custom = try {
                    myDB.getCustomTemplates()
                } catch (_: Exception) {
                    emptyList()
                }
                Triple(saved, additional, custom)
            } finally {
                runCatching { myDB.close() }
            }

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
                prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
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

        suspend fun syncThemeAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(BewerberGlanceWidget::class.java)
            val now = System.currentTimeMillis()
            for (glanceId in ids) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[KEY_THEME_SYNC] = now
                }
                BewerberGlanceWidget().update(context, glanceId)
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
