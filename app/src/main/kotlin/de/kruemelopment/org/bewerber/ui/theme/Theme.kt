package de.kruemelopment.org.bewerber.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import de.kruemelopment.org.bewerber.ThemeMode

internal val BewerberDarkColorScheme = darkColorScheme(
    primary = Color(0xFF28F078),
    onPrimary = Color(0xFF003915),
    primaryContainer = Color(0xFF005221),
    onPrimaryContainer = Color(0xFF74FF9E),
    secondary = Color(0xFF8CD6B1),
    onSecondary = Color(0xFF003823),
    secondaryContainer = Color(0xFF005235),
    onSecondaryContainer = Color(0xFFA8F2CC),
    tertiary = Color(0xFF67D9E8),
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFF9EEFFD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1511),
    onBackground = Color(0xFFDDE4DD),
    surface = Color(0xFF0E1511),
    onSurface = Color(0xFFDDE4DD),
    surfaceVariant = Color(0xFF3E4A41),
    onSurfaceVariant = Color(0xFFBDCABA),
    surfaceContainerLowest = Color(0xFF09100C),
    surfaceContainerLow = Color(0xFF161D19),
    surfaceContainer = Color(0xFF1A211D),
    surfaceContainerHigh = Color(0xFF232C26),
    surfaceContainerHighest = Color(0xFF2E3731),
    outline = Color(0xFF879488),
    outlineVariant = Color(0xFF3E4A41)
)

internal val BewerberLightColorScheme = lightColorScheme(
    primary = Color(0xFF006D2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6BFF9B),
    onPrimaryContainer = Color(0xFF00210A),
    secondary = Color(0xFF256A4B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFABF2CA),
    onSecondaryContainer = Color(0xFF002112),
    tertiary = Color(0xFF006874),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF9EEFFD),
    onTertiaryContainer = Color(0xFF001F24),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5FBF5),
    onBackground = Color(0xFF171D19),
    surface = Color(0xFFF5FBF5),
    onSurface = Color(0xFF171D19),
    surfaceVariant = Color(0xFFD9E6DA),
    onSurfaceVariant = Color(0xFF3E4A41),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5EF),
    surfaceContainer = Color(0xFFE9F0EA),
    surfaceContainerHigh = Color(0xFFE3ECE4),
    surfaceContainerHighest = Color(0xFFDDE6DE),
    outline = Color(0xFF6E7A70),
    outlineVariant = Color(0xFFBDCABA)
)

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

val ExpressiveTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

internal const val PREFS_SETTINGS = "settings"
internal const val KEY_THEME_MODE = "theme_mode"
internal const val KEY_DYNAMIC_COLOR = "dynamic_color"
internal const val KEY_LEGACY_NIGHT_MODE = "mode"

internal fun readThemeSettings(context: Context): Pair<ThemeMode, Boolean> {
    val settingsPrefs = context.getSharedPreferences(PREFS_SETTINGS, 0)
    val savedThemeName = settingsPrefs.getString(KEY_THEME_MODE, null)
    val themeMode = when {
        savedThemeName != null -> runCatching { ThemeMode.valueOf(savedThemeName) }
            .getOrDefault(ThemeMode.SYSTEM)
        settingsPrefs.contains(KEY_LEGACY_NIGHT_MODE) -> {
            if (settingsPrefs.getBoolean(KEY_LEGACY_NIGHT_MODE, false)) {
                ThemeMode.DARK
            } else {
                ThemeMode.LIGHT
            }
        }
        else -> ThemeMode.SYSTEM
    }
    val useDynamicColor = settingsPrefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    return Pair(themeMode, useDynamicColor)
}

internal fun resolveBewerberColorSchemes(
    context: Context,
    useDynamicColor: Boolean
): Pair<ColorScheme, ColorScheme> {
    return if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Pair(dynamicLightColorScheme(context), dynamicDarkColorScheme(context))
    } else {
        Pair(BewerberLightColorScheme, BewerberDarkColorScheme)
    }
}

@Composable
fun BewerberTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val (lightScheme, darkScheme) = resolveBewerberColorSchemes(context, useDynamicColor)
    val colorScheme = if (darkTheme) darkScheme else lightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content
    )
}
