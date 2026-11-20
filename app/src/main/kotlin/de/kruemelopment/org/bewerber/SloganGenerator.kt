package de.kruemelopment.org.bewerber

import java.util.Random

object SloganGenerator {

    val DEFAULT_SAVED_WORDS: List<String> = listOf(
        "Cola",
        "Käse",
        "Krümelopment Dev"
    )

    val DEFAULT_ADDITIONAL_WORDS: List<String> = listOf(
        "Cola",
        "Käse",
        "Bier",
        "Salami",
        "Fanta",
        "Sprite",
        "Wasser",
        "Döner",
        "Rosen"
    )

    private const val FALLBACK_TEMPLATE = "_ ist einfach perfekt"

    fun generate(
        word: String,
        templates: List<String>,
        additionalWords: List<String>,
        specificTemplate: String? = null,
        random: Random = Random()
    ): String {
        val cleanWord = word.trim().ifEmpty { DEFAULT_SAVED_WORDS.first() }
        val nonEmptyTemplates = templates.filter { it.isNotBlank() }

        val rawTemplate = when {
            !specificTemplate.isNullOrBlank() -> specificTemplate
            nonEmptyTemplates.isNotEmpty() -> nonEmptyTemplates[random.nextInt(nonEmptyTemplates.size)]
            else -> FALLBACK_TEMPLATE
        }

        val pool = additionalWords
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { DEFAULT_ADDITIONAL_WORDS }

        var chosenTildeWord: String? = null
        var result = rawTemplate.replace("_", cleanWord)

        if (result.contains("~")) {
            val candidates = pool.filterNot { it.equals(cleanWord, ignoreCase = true) }
                .ifEmpty { pool }
            val picked = candidates[random.nextInt(candidates.size)]
            chosenTildeWord = picked
            result = result.replace("~", picked)
        }

        if (result.contains("#")) {
            val candidatesExcludingBoth = pool.filterNot {
                it.equals(cleanWord, ignoreCase = true) ||
                    (chosenTildeWord != null && it.equals(chosenTildeWord, ignoreCase = true))
            }
            val candidatesExcludingTilde = if (chosenTildeWord != null) {
                pool.filterNot { it.equals(chosenTildeWord, ignoreCase = true) }
            } else {
                pool
            }
            val effectivePool = when {
                candidatesExcludingBoth.isNotEmpty() -> candidatesExcludingBoth
                candidatesExcludingTilde.isNotEmpty() -> candidatesExcludingTilde
                else -> pool
            }
            val picked = effectivePool[random.nextInt(effectivePool.size)]
            result = result.replace("#", picked)
        }

        return normalizeMultiline(result)
    }

    fun generateForRandomWord(
        words: List<String>,
        templates: List<String>,
        additionalWords: List<String>,
        random: Random = Random()
    ): Pair<String, String> {
        val cleanWords = words.map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { DEFAULT_SAVED_WORDS }
        val chosenWord = cleanWords[random.nextInt(cleanWords.size)]
        val slogan = generate(
            word = chosenWord,
            templates = templates,
            additionalWords = additionalWords,
            random = random
        )
        return Pair(chosenWord, slogan)
    }

    fun formatTemplatePreview(template: String): String {
        val replaced = template
            .replace("_", "(eingegebenes Wort)")
            .replace("~", "(zufälliges Wort)")
            .replace("#", "(zufälliges Wort)")
        return normalizeMultiline(replaced)
    }

    fun sanitizeFileName(rawName: String, maxLength: Int = 50): String {
        val safeMax = maxLength.coerceAtLeast(1)
        val cleaned = rawName
            .replace("\r\n", " ")
            .replace('\n', ' ')
            .replace('\r', ' ')
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("_+"), "_")
            .trim { it <= ' ' || it == '.' || it == '_' }

        val truncated = if (cleaned.length > safeMax) {
            cleaned.substring(0, safeMax).trim { it <= ' ' || it == '.' || it == '_' }
        } else {
            cleaned
        }

        return truncated.ifEmpty { "Werbespruch" }
    }

    private fun normalizeMultiline(text: String): String {
        if (!text.contains('\n') && !text.contains('\r')) {
            return text.trim()
        }
        return text
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
}
