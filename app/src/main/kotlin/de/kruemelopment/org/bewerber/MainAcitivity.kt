package de.kruemelopment.org.bewerber

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.kruemelopment.org.bewerber.ui.MainScreen
import de.kruemelopment.org.bewerber.ui.theme.BewerberTheme
import java.util.Locale

class MainAcitivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private val viewModel: BewerberViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)
        handleIncomingIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            BewerberTheme(
                themeMode = uiState.themeMode,
                useDynamicColor = uiState.useDynamicColor
            ) {
                MainScreen(
                    viewModel = viewModel,
                    onSpeakSlogan = ::speak
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val word = intent.getStringExtra("wort")
        val slogan = intent.getStringExtra("spruch")
        if (!word.isNullOrBlank() || !slogan.isNullOrBlank()) {
            viewModel.applyFromIntent(word, slogan)
            intent.removeExtra("wort")
            intent.removeExtra("spruch")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.GERMAN)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speak(text: String) {
        if (text.isBlank()) return
        if (!isTtsReady || tts == null) {
            viewModel.showStatus("Sprachausgabe wird initialisiert oder ist nicht verfügbar.", true)
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "slogan_tts")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
