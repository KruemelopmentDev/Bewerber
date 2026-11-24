package de.kruemelopment.org.bewerber

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Canvas
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import de.kruemelopment.org.bewerber.ui.theme.KEY_DYNAMIC_COLOR
import de.kruemelopment.org.bewerber.ui.theme.KEY_LEGACY_NIGHT_MODE
import de.kruemelopment.org.bewerber.ui.theme.KEY_THEME_MODE
import de.kruemelopment.org.bewerber.ui.theme.PREFS_SETTINGS
import de.kruemelopment.org.bewerber.ui.theme.readThemeSettings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class HistoryEntry(val word: String, val slogan: String)

enum class UndoType { SAVED_WORD, ADDITIONAL_WORD, FAVORITE_SLOGAN, CUSTOM_TEMPLATE }

data class UndoAction(val type: UndoType, val value: String, val extra: String = "")

data class BewerberUiState(
    val inputWord: String = "",
    val currentSlogan: String = "",
    val history: List<HistoryEntry> = emptyList(),
    val historyIndex: Int = -1,
    val savedWords: List<String> = emptyList(),
    val additionalWords: List<String> = emptyList(),
    val favoriteSlogans: List<String> = emptyList(),
    val customTemplates: List<String> = emptyList(),
    val builtInTemplates: List<String> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val termsAccepted: Boolean = true,
    val statusMessage: String? = null,
    val isStatusError: Boolean = false,
    val lastUndoAction: UndoAction? = null
) {
    val canGoBackInHistory: Boolean get() = historyIndex > 0
    val canGoForwardInHistory: Boolean get() = historyIndex in 0 until (history.size - 1)
    val isCurrentSloganFavorite: Boolean
        get() = currentSlogan.isNotBlank() && favoriteSlogans.contains(currentSlogan)
    val allTemplates: List<String> get() = (customTemplates + builtInTemplates).distinct()
}

class BewerberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DataBaseHelper(application)

    private val _uiState = MutableStateFlow(BewerberUiState())
    val uiState: StateFlow<BewerberUiState> = _uiState.asStateFlow()

    init {
        val (themeMode, useDynamicColor) = readThemeSettings(application)

        val builtInTemplates = runCatching {
            application.resources.getStringArray(R.array.sprueche).toList().distinct()
        }.getOrDefault(emptyList())

        val (savedWords, additionalWords) = db.ensureDefaultWords()
        val favoriteSlogans = db.getFavoriteSlogans()
        val customTemplates = db.getCustomTemplates()

        _uiState.value = BewerberUiState(
            savedWords = savedWords,
            additionalWords = additionalWords,
            favoriteSlogans = favoriteSlogans,
            customTemplates = customTemplates,
            builtInTemplates = builtInTemplates,
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            termsAccepted = true
        )

        updateDynamicShortcuts()
    }

    fun onInputWordChanged(newWord: String) {
        _uiState.update { it.copy(inputWord = newWord) }
    }

    fun generateSlogan(wordOverride: String? = null, specificTemplate: String? = null) {
        val state = _uiState.value
        val targetWord = (wordOverride ?: state.inputWord).trim()

        if (targetWord.isEmpty()) {
            showStatus("Gib zuerst ein Wort ein oder wähle einen Quick-Chip!", isError = true)
            return
        }

        val slogan = SloganGenerator.generate(
            word = targetWord,
            templates = state.allTemplates,
            additionalWords = state.additionalWords,
            specificTemplate = specificTemplate
        )

        val insertedAdditional = db.insertWordIfNotExists(
            targetWord,
            DataBaseHelper.TABLE_ADDITIONAL
        )
        val updatedAdditionalWords = if (insertedAdditional) {
            db.getWords(DataBaseHelper.TABLE_ADDITIONAL)
        } else {
            state.additionalWords
        }

        val updatedHistory = (state.history + HistoryEntry(word = targetWord, slogan = slogan))
            .takeLast(MAX_HISTORY_SIZE)

        _uiState.update {
            it.copy(
                inputWord = targetWord,
                currentSlogan = slogan,
                history = updatedHistory,
                historyIndex = updatedHistory.lastIndex,
                additionalWords = updatedAdditionalWords,
                statusMessage = null,
                isStatusError = false,
                lastUndoAction = null
            )
        }

        if (insertedAdditional) {
            notifyWidgets()
        }
    }

    fun selectQuickWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { it.copy(inputWord = trimmed) }
        generateSlogan(wordOverride = trimmed)
    }

    fun applyFromIntent(word: String?, slogan: String?) {
        val cleanWord = word?.trim().orEmpty()
        val cleanSlogan = slogan?.trim().orEmpty()

        if (cleanSlogan.isNotEmpty()) {
            val state = _uiState.value
            val effectiveWord = cleanWord.ifEmpty { state.inputWord }
            val updatedHistory = (
                state.history + HistoryEntry(word = effectiveWord, slogan = cleanSlogan)
                ).takeLast(MAX_HISTORY_SIZE)

            _uiState.update {
                it.copy(
                    inputWord = effectiveWord,
                    currentSlogan = cleanSlogan,
                    history = updatedHistory,
                    historyIndex = updatedHistory.lastIndex
                )
            }
        } else if (cleanWord.isNotEmpty()) {
            selectQuickWord(cleanWord)
        }
    }

    fun navigateHistoryBack() {
        val state = _uiState.value
        if (!state.canGoBackInHistory) return
        val newIndex = state.historyIndex - 1
        val entry = state.history[newIndex]
        _uiState.update {
            it.copy(
                historyIndex = newIndex,
                inputWord = entry.word,
                currentSlogan = entry.slogan
            )
        }
    }

    fun navigateHistoryForward() {
        val state = _uiState.value
        if (!state.canGoForwardInHistory) return
        val newIndex = state.historyIndex + 1
        val entry = state.history[newIndex]
        _uiState.update {
            it.copy(
                historyIndex = newIndex,
                inputWord = entry.word,
                currentSlogan = entry.slogan
            )
        }
    }

    fun saveCurrentInputWord() {
        addSavedWord(_uiState.value.inputWord)
    }

    fun addSavedWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) {
            showStatus("Gib zuerst ein Wort ein, bevor du es speicherst!", isError = true)
            return
        }

        val added = db.insertWordIfNotExists(trimmed, DataBaseHelper.TABLE_SAVED_WORDS)
        if (added) {
            val updatedSaved = db.getWords(DataBaseHelper.TABLE_SAVED_WORDS)
            _uiState.update {
                it.copy(
                    savedWords = updatedSaved,
                    statusMessage = "„$trimmed“ zu gespeicherten Wörtern hinzugefügt",
                    isStatusError = false,
                    lastUndoAction = null
                )
            }
            updateDynamicShortcuts()
            notifyWidgets()
        } else {
            showStatus("„$trimmed“ ist bereits gespeichert", isError = true)
        }
    }

    fun addAdditionalWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) {
            showStatus("Gib zuerst ein Ergänzungswort ein!", isError = true)
            return
        }

        val added = db.insertWordIfNotExists(trimmed, DataBaseHelper.TABLE_ADDITIONAL)
        if (added) {
            val updatedAdditional = db.getWords(DataBaseHelper.TABLE_ADDITIONAL)
            _uiState.update {
                it.copy(
                    additionalWords = updatedAdditional,
                    statusMessage = "Ergänzungswort „$trimmed“ hinzugefügt",
                    isStatusError = false,
                    lastUndoAction = null
                )
            }
            notifyWidgets()
        } else {
            showStatus("„$trimmed“ ist bereits vorhanden", isError = true)
        }
    }

    fun deleteSavedWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        db.deleteWord(trimmed, DataBaseHelper.TABLE_SAVED_WORDS)
        val (savedWords, additionalWords) = db.ensureDefaultWords()
        _uiState.update {
            it.copy(
                savedWords = savedWords,
                additionalWords = additionalWords,
                statusMessage = "„$trimmed“ gelöscht",
                isStatusError = false,
                lastUndoAction = UndoAction(UndoType.SAVED_WORD, trimmed)
            )
        }
        updateDynamicShortcuts()
        notifyWidgets()
    }

    fun deleteAdditionalWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        db.deleteWord(trimmed, DataBaseHelper.TABLE_ADDITIONAL)
        val (savedWords, additionalWords) = db.ensureDefaultWords()
        _uiState.update {
            it.copy(
                savedWords = savedWords,
                additionalWords = additionalWords,
                statusMessage = "„$trimmed“ gelöscht",
                isStatusError = false,
                lastUndoAction = UndoAction(UndoType.ADDITIONAL_WORD, trimmed)
            )
        }
        notifyWidgets()
    }

    fun toggleCurrentSloganFavorite() {
        val state = _uiState.value
        val slogan = state.currentSlogan.trim()
        if (slogan.isEmpty()) {
            showStatus("Erstelle zuerst einen Werbespruch!", isError = true)
            return
        }

        if (db.isFavoriteSlogan(slogan)) {
            db.removeFavoriteSlogan(slogan)
            val updatedFavorites = db.getFavoriteSlogans()
            _uiState.update {
                it.copy(
                    favoriteSlogans = updatedFavorites,
                    statusMessage = "Aus Favoriten entfernt",
                    isStatusError = false,
                    lastUndoAction = UndoAction(
                        type = UndoType.FAVORITE_SLOGAN,
                        value = slogan,
                        extra = state.inputWord
                    )
                )
            }
        } else {
            db.addFavoriteSlogan(slogan, state.inputWord)
            val updatedFavorites = db.getFavoriteSlogans()
            _uiState.update {
                it.copy(
                    favoriteSlogans = updatedFavorites,
                    statusMessage = "Zu Favoriten hinzugefügt",
                    isStatusError = false,
                    lastUndoAction = null
                )
            }
        }
    }

    fun removeFavoriteSlogan(slogan: String) {
        val trimmed = slogan.trim()
        if (trimmed.isEmpty()) return
        db.removeFavoriteSlogan(trimmed)
        val updatedFavorites = db.getFavoriteSlogans()
        _uiState.update {
            it.copy(
                favoriteSlogans = updatedFavorites,
                statusMessage = "Favorit entfernt",
                isStatusError = false,
                lastUndoAction = UndoAction(UndoType.FAVORITE_SLOGAN, trimmed)
            )
        }
    }

    fun selectFavoriteSlogan(slogan: String) {
        val trimmed = slogan.trim()
        if (trimmed.isEmpty()) return
        val state = _uiState.value
        val updatedHistory = (
            state.history + HistoryEntry(word = state.inputWord, slogan = trimmed)
            ).takeLast(MAX_HISTORY_SIZE)
        _uiState.update {
            it.copy(
                currentSlogan = trimmed,
                history = updatedHistory,
                historyIndex = updatedHistory.lastIndex
            )
        }
    }

    fun addCustomTemplate(template: String) {
        val raw = template.trim()
        if (raw.isEmpty()) {
            showStatus("Gib zuerst eine eigene Spruch-Vorlage ein!", isError = true)
            return
        }

        val normalized = if (raw.contains("_")) raw else "$raw _"
        val added = db.addCustomTemplate(normalized)
        if (added) {
            val updatedTemplates = db.getCustomTemplates()
            val message = if (raw.contains("_")) {
                "Eigene Spruch-Vorlage gespeichert"
            } else {
                "Vorlage gespeichert (Platzhalter „_“ automatisch ergänzt)"
            }
            _uiState.update {
                it.copy(
                    customTemplates = updatedTemplates,
                    statusMessage = message,
                    isStatusError = false,
                    lastUndoAction = null
                )
            }
        } else {
            showStatus("Diese Vorlage existiert bereits", isError = true)
        }
    }

    fun deleteCustomTemplate(template: String) {
        val trimmed = template.trim()
        if (trimmed.isEmpty()) return
        db.removeCustomTemplate(trimmed)
        val updatedTemplates = db.getCustomTemplates()
        _uiState.update {
            it.copy(
                customTemplates = updatedTemplates,
                statusMessage = "Eigene Vorlage gelöscht",
                isStatusError = false,
                lastUndoAction = UndoAction(UndoType.CUSTOM_TEMPLATE, trimmed)
            )
        }
    }

    fun undoLastDelete() {
        val action = _uiState.value.lastUndoAction ?: return
        when (action.type) {
            UndoType.SAVED_WORD -> {
                db.insertWordIfNotExists(action.value, DataBaseHelper.TABLE_SAVED_WORDS)
                val saved = db.getWords(DataBaseHelper.TABLE_SAVED_WORDS)
                _uiState.update {
                    it.copy(
                        savedWords = saved,
                        statusMessage = "„${action.value}“ wiederhergestellt",
                        isStatusError = false,
                        lastUndoAction = null
                    )
                }
                updateDynamicShortcuts()
                notifyWidgets()
            }
            UndoType.ADDITIONAL_WORD -> {
                db.insertWordIfNotExists(action.value, DataBaseHelper.TABLE_ADDITIONAL)
                val additional = db.getWords(DataBaseHelper.TABLE_ADDITIONAL)
                _uiState.update {
                    it.copy(
                        additionalWords = additional,
                        statusMessage = "„${action.value}“ wiederhergestellt",
                        isStatusError = false,
                        lastUndoAction = null
                    )
                }
                notifyWidgets()
            }
            UndoType.FAVORITE_SLOGAN -> {
                db.addFavoriteSlogan(action.value, action.extra)
                val favorites = db.getFavoriteSlogans()
                _uiState.update {
                    it.copy(
                        favoriteSlogans = favorites,
                        statusMessage = "Favorit wiederhergestellt",
                        isStatusError = false,
                        lastUndoAction = null
                    )
                }
            }
            UndoType.CUSTOM_TEMPLATE -> {
                db.addCustomTemplate(action.value)
                val custom = db.getCustomTemplates()
                _uiState.update {
                    it.copy(
                        customTemplates = custom,
                        statusMessage = "Eigene Vorlage wiederhergestellt",
                        isStatusError = false,
                        lastUndoAction = null
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        val app = getApplication<Application>()
        app.getSharedPreferences(PREFS_SETTINGS, 0)
            .edit {
                putString(KEY_THEME_MODE, mode.name)
                    .putBoolean(KEY_LEGACY_NIGHT_MODE, mode == ThemeMode.DARK)
            }
        _uiState.update { it.copy(themeMode = mode) }
        notifyWidgetsThemeChanged()
    }

    fun setUseDynamicColor(enabled: Boolean) {
        val app = getApplication<Application>()
        app.getSharedPreferences(PREFS_SETTINGS, 0)
            .edit {
                putBoolean(KEY_DYNAMIC_COLOR, enabled)
            }
        _uiState.update { it.copy(useDynamicColor = enabled) }
        notifyWidgetsThemeChanged()
    }

    fun showStatus(message: String, isError: Boolean = false) {
        _uiState.update {
            it.copy(
                statusMessage = message,
                isStatusError = isError,
                lastUndoAction = null
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update {
            it.copy(
                statusMessage = null,
                isStatusError = false,
                lastUndoAction = null
            )
        }
    }

    fun updateDynamicShortcuts() {
        runCatching {
            val app = getApplication<Application>()
            val shortcutManager = app.getSystemService(ShortcutManager::class.java) ?: return
            val words = _uiState.value.savedWords
                .filter { it.isNotBlank() }
                .takeLast(3)

            if (words.isEmpty()) {
                shortcutManager.removeAllDynamicShortcuts()
                return
            }

            val icon = createShortcutIcon(app)
            val shortcuts = words.mapIndexed { index, word ->
                val shortcutIntent = Intent(app, MainAcitivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    putExtra("wort", word)
                }
                val builder = ShortcutInfo.Builder(app, "id${index + 1}")
                    .setShortLabel(word.take(25))
                    .setLongLabel(word.take(50))
                    .setIntent(shortcutIntent)
                if (icon != null) {
                    builder.setIcon(icon)
                }
                builder.build()
            }
            shortcutManager.dynamicShortcuts = shortcuts
        }
    }

    private fun createShortcutIcon(app: Application): Icon? = runCatching {
        val drawable = ContextCompat.getDrawable(app, R.mipmap.ic_launcher_round)
            ?: ContextCompat.getDrawable(app, R.mipmap.ic_launcher)
            ?: return null
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = createBitmap(width, height)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        Icon.createWithBitmap(bmp)
    }.getOrNull()

    private fun notifyWidgets() {
        viewModelScope.launch {
            runCatching {
                BewerberGlanceWidget.refreshAll(getApplication())
            }
        }
    }

    private fun notifyWidgetsThemeChanged() {
        viewModelScope.launch {
            runCatching {
                BewerberGlanceWidget.syncThemeAll(getApplication())
            }
        }
    }

    override fun onCleared() {
        runCatching { db.close() }
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 50
    }
}
