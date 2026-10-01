package com.example

import com.example.infinitebook.data.ai.GeminiBookEngine
import com.example.infinitebook.data.ai.Prompts
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verification test for Marathi Language generation:
 * 1. Confirms Marathi language detection
 * 2. Confirms exact Marathi force system prompt is used
 * 3. Confirms generation prompt directive contains strict Devanagari enforcement
 * 4. Generates 1 sample chapter in Marathi and verifies authentic Devanagari content
 */
class MarathiGenerationTest {

    @Test
    fun testMarathiDetection() {
        assertTrue(Prompts.isMarathi("Marathi"))
        assertTrue(Prompts.isMarathi("marathi"))
        assertTrue(Prompts.isMarathi("मराठी"))
        assertTrue(Prompts.isMarathi("Marathi (मराठी)"))
        assertFalse(Prompts.isMarathi("English"))
    }

    @Test
    fun testMarathiForceSystemPrompt() {
        val expected = """Tu ek Marathi lekhak ahes. Tula PUSTAK MARATHI madhech lihayche ahe. 
Sampurna pustak Shuddha Marathi Devanagari lipi madhe asle pahije.
English ek shabd suddha vapru nakos. Title suddha Marathi madhe de.
Tujhi bhasha kolhapuri / pune style authentic Marathi asavi."""

        val actual = Prompts.getSystemPrompt("Marathi")
        assertEquals(expected, actual)
    }

    @Test
    fun testGenerationPromptDirectives() {
        val directive = Prompts.buildLanguageDirective("Marathi")
        assertTrue(directive.contains("LANGUAGE: Marathi. IMPORTANT: Write ENTIRE content in Marathi only. If Marathi selected, use ONLY मराठी देवनागरी."))
        assertTrue(directive.contains("Do NOT translate, write originally in selected language."))
    }

    @Test
    fun testGenerateSampleChapterInMarathi() = runBlocking {
        val engine = GeminiBookEngine()
        val (sampleChapter, prose) = engine.generateSampleChapterInMarathi(
            bookTitle = "सह्याद्रीचे अग्निकंकण",
            chapterNumber = 1
        )

        assertNotNull(sampleChapter)
        assertTrue(sampleChapter.content.isNotBlank())
        assertTrue("Prose must contain authentic Marathi Devanagari text", prose.contains("सह्याद्री") || prose.contains("मराठी"))
        assertTrue("Prose must have sufficient length", sampleChapter.wordCount > 100)
        assertEquals("COMPLETED", sampleChapter.status)
    }
}
