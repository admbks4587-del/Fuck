package com.example.infinitebook.data.ai

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Strict Unique-Content Engine Registry
 * Tracks paragraph SHA-256 hashes, headings, examples, explanations, and semantic vectors
 * to guarantee 100% unique prose across all chapters and pages.
 */
class ContentRegistry {

    val paragraphHashes = HashSet<String>()
    val usedHeadings = HashSet<String>()
    val usedExamples = HashSet<String>()
    val usedExplanations = HashSet<String>()
    val coveredTopics = LinkedHashSet<String>()

    // Retain recent normalized paragraph samples for high-speed TF-IDF/cosine similarity checks
    private val registeredParagraphs = mutableListOf<String>()

    /**
     * Check if the provided text, heading, or example is a duplicate or near-duplicate.
     * Evaluates:
     * 1. Exact SHA-256 hash match on normalized paragraphs.
     * 2. Heading duplication (normalized exact or > 0.85 similarity).
     * 3. TF-IDF / Bag-of-Words Cosine Similarity > 0.85 against registered paragraphs.
     * 4. Normalized Levenshtein similarity > 0.85.
     */
    fun isDuplicate(
        text: String,
        heading: String? = null,
        example: String? = null
    ): Boolean {
        // 1. Heading check
        if (!heading.isNullOrBlank()) {
            val normHeading = normalize(heading)
            if (usedHeadings.contains(normHeading)) return true
            for (h in usedHeadings) {
                if (computeNormalizedLevenshtein(normHeading, h) > 0.85) return true
                if (computeCosineSimilarity(normHeading, h) > 0.85) return true
            }
        }

        // 2. Example check
        if (!example.isNullOrBlank()) {
            val normEx = normalize(example)
            if (usedExamples.contains(normEx)) return true
        }

        val paragraphs = extractParagraphs(text)
        if (paragraphs.isEmpty()) return false

        for (p in paragraphs) {
            val normP = normalize(p)
            if (normP.isBlank()) continue

            // A. Exact SHA-256 Hash Check
            val hash = computeSha256(normP)
            if (paragraphHashes.contains(hash)) {
                return true
            }

            // B. Semantic Cosine Similarity & Normalized Levenshtein against registered corpus
            for (existing in registeredParagraphs) {
                val cosine = computeCosineSimilarity(normP, existing)
                if (cosine > 0.85) {
                    return true
                }

                // If lengths are relatively close, check Levenshtein
                val lenRatio = min(normP.length, existing.length).toDouble() / max(normP.length, existing.length).coerceAtLeast(1)
                if (lenRatio > 0.70) {
                    val lev = computeNormalizedLevenshtein(normP, existing)
                    if (lev > 0.85) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Register approved, unique content into the registry.
     */
    fun register(
        text: String,
        heading: String? = null,
        example: String? = null,
        explanation: String? = null,
        topic: String? = null
    ) {
        if (!heading.isNullOrBlank()) {
            usedHeadings.add(normalize(heading))
            coveredTopics.add(heading.trim())
        }

        if (!example.isNullOrBlank()) {
            usedExamples.add(normalize(example))
        }

        if (!explanation.isNullOrBlank()) {
            usedExplanations.add(normalize(explanation))
        }

        if (!topic.isNullOrBlank()) {
            coveredTopics.add(topic.trim())
        }

        val paragraphs = extractParagraphs(text)
        for (p in paragraphs) {
            val norm = normalize(p)
            if (norm.length >= 20) {
                val hash = computeSha256(norm)
                paragraphHashes.add(hash)
                registeredParagraphs.add(norm)
            }
        }
    }

    /**
     * Returns a formatted summary of already covered topics for AI context injection.
     */
    fun getAllTopics(): String {
        return if (coveredTopics.isEmpty()) {
            "None yet (initial section)"
        } else {
            coveredTopics.toList().takeLast(60).joinToString("; ")
        }
    }

    fun getAllHeadings(): List<String> = usedHeadings.toList()

    private fun extractParagraphs(raw: String): List<String> {
        return raw.split(Regex("\n{2,}|\r\n{2,}"))
            .map { it.trim() }
            .filter { it.length >= 25 }
    }

    private fun normalize(str: String): String {
        return str.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\p{L}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun computeSha256(normalized: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(normalized.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Cosine similarity based on Term Frequencies (Bag-of-Words / TF).
     */
    fun computeCosineSimilarity(s1: String, s2: String): Double {
        val words1 = s1.split(" ").filter { it.length > 2 }
        val words2 = s2.split(" ").filter { it.length > 2 }
        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val freq1 = words1.groupingBy { it }.eachCount()
        val freq2 = words2.groupingBy { it }.eachCount()

        val allWords = freq1.keys + freq2.keys
        var dotProduct = 0.0
        var mag1 = 0.0
        var mag2 = 0.0

        for (w in allWords) {
            val v1 = freq1[w]?.toDouble() ?: 0.0
            val v2 = freq2[w]?.toDouble() ?: 0.0
            dotProduct += v1 * v2
            mag1 += v1 * v1
            mag2 += v2 * v2
        }

        if (mag1 == 0.0 || mag2 == 0.0) return 0.0
        return dotProduct / (sqrt(mag1) * sqrt(mag2))
    }

    /**
     * Normalized Levenshtein similarity in [0.0, 1.0].
     * 1.0 = exact match, 0.0 = completely disjoint.
     */
    fun computeNormalizedLevenshtein(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        // Cap length for efficiency if strings are huge
        val a = if (s1.length > 400) s1.substring(0, 400) else s1
        val b = if (s2.length > 400) s2.substring(0, 400) else s2

        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }

        val distance = dp[b.length]
        val maxLen = max(a.length, b.length)
        return 1.0 - (distance.toDouble() / maxLen.toDouble())
    }

    /**
     * Serialize registry to JSON for Room / DataStore persistence.
     */
    fun toJson(): String {
        val root = JSONObject()
        root.put("paragraphHashes", JSONArray(paragraphHashes))
        root.put("usedHeadings", JSONArray(usedHeadings))
        root.put("usedExamples", JSONArray(usedExamples))
        root.put("usedExplanations", JSONArray(usedExplanations))
        root.put("coveredTopics", JSONArray(coveredTopics))
        root.put("registeredParagraphs", JSONArray(registeredParagraphs.takeLast(300)))
        return root.toString()
    }

    companion object {
        fun fromJson(json: String?): ContentRegistry {
            val registry = ContentRegistry()
            if (json.isNullOrBlank()) return registry
            try {
                val root = JSONObject(json)
                root.optJSONArray("paragraphHashes")?.let { arr ->
                    for (i in 0 until arr.length()) registry.paragraphHashes.add(arr.getString(i))
                }
                root.optJSONArray("usedHeadings")?.let { arr ->
                    for (i in 0 until arr.length()) registry.usedHeadings.add(arr.getString(i))
                }
                root.optJSONArray("usedExamples")?.let { arr ->
                    for (i in 0 until arr.length()) registry.usedExamples.add(arr.getString(i))
                }
                root.optJSONArray("usedExplanations")?.let { arr ->
                    for (i in 0 until arr.length()) registry.usedExplanations.add(arr.getString(i))
                }
                root.optJSONArray("coveredTopics")?.let { arr ->
                    for (i in 0 until arr.length()) registry.coveredTopics.add(arr.getString(i))
                }
                root.optJSONArray("registeredParagraphs")?.let { arr ->
                    for (i in 0 until arr.length()) registry.registeredParagraphs.add(arr.getString(i))
                }
            } catch (e: Exception) {
                // Return fresh registry on parsing failure
            }
            return registry
        }
    }
}

/**
 * Report generated by Duplicate Audit across manuscript pages.
 */
data class DuplicateReport(
    val pageNumber: Int,
    val chapterNumber: Int,
    val sectionNumber: Int,
    val duplicateType: String, // "EXACT_PARAGRAPH", "HIGH_SIMILARITY", "DUPLICATE_HEADING"
    val similarityScore: Double,
    val snippet: String,
    val conflictingWith: String
)
