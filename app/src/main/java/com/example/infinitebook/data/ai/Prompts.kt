package com.example.infinitebook.data.ai

/**
 * Centralized Prompt Construction with Strict Language Enforcement.
 * Specifically forces authentic Marathi (मराठी देवनागरी) when Marathi is selected.
 */
object Prompts {

    const val MARATHI_FORCE_SYSTEM_PROMPT = """Tu ek Marathi lekhak ahes. Tula PUSTAK MARATHI madhech lihayche ahe. 
Sampurna pustak Shuddha Marathi Devanagari lipi madhe asle pahije.
English ek shabd suddha vapru nakos. Title suddha Marathi madhe de.
Tujhi bhasha kolhapuri / pune style authentic Marathi asavi."""

    fun isMarathi(language: String?): Boolean {
        if (language.isNullOrBlank()) return false
        val l = language.trim().lowercase()
        return l == "marathi" || l == "मराठी" || l.contains("marathi") || l.contains("मराठी")
    }

    fun getSystemPrompt(
        language: String,
        title: String = "",
        genre: String = "",
        readerLevel: String = "",
        additionalRules: String = ""
    ): String {
        return if (isMarathi(language)) {
            MARATHI_FORCE_SYSTEM_PROMPT
        } else {
            val langDirective = buildLanguageDirective(language)
            """$langDirective

You are InfiniteBook AI's Lead Author.
Book Title: "$title" (Genre: $genre).
Language: $language.
Reader Level: $readerLevel.

RULES:
1. Write 100% in $language.
2. $additionalRules
""".trimIndent()
        }
    }

    /**
     * Generation prompt prefix required at the start of every generation prompt:
     * - "LANGUAGE: ${selectedLanguage}. IMPORTANT: Write ENTIRE content in ${selectedLanguage} only. If Marathi selected, use ONLY मराठी देवनागरी."
     * - "Do NOT translate, write originally in selected language"
     */
    fun buildLanguageDirective(selectedLanguage: String): String {
        return """LANGUAGE: ${selectedLanguage}. IMPORTANT: Write ENTIRE content in ${selectedLanguage} only. If Marathi selected, use ONLY मराठी देवनागरी.
Do NOT translate, write originally in selected language."""
    }

    fun getLanguageChipText(language: String?): String {
        return if (isMarathi(language)) "भाषा: मराठी" else "भाषा: ${language ?: "English"}"
    }
}
