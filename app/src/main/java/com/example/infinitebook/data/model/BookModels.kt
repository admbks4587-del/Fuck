package com.example.infinitebook.data.model

data class BookStyleSettings(
    val narrativePerspective: String = "Third Person Limited",
    val writingTone: String = "Epic & Grand",
    val pacing: Int = 7,
    val vocabularyComplexity: Int = 8,
    val dialogueDensity: Int = 6,
    val descriptionDensity: Int = 8,
    val historicalDepth: Int = 7,
    val researchDepth: Int = 7,
    val worldBuildingDepth: Int = 9,
    val characterDepth: Int = 8,
    val philosophicalDepth: Int = 7,
    val mysteryIntensity: Int = 6,
    val suspenseIntensity: Int = 7,
    val actionIntensity: Int = 6,
    val emotionalIntensity: Int = 8,
    val horrorIntensity: Int = 2,
    val romanceIntensity: Int = 3,
    val twistFrequency: Int = 6,
    val plotComplexity: Int = 8,
    val scientificTechnicalDepth: Int = 5,
    val culturalDetail: Int = 8,
    val spiritualMythologicalDepth: Int = 7,
    val endingStyle: String = "Climactic & Resonant"
)

data class BiographyFields(
    val subjectPerson: String = "",
    val lifePeriod: String = "",
    val importantPeople: String = "",
    val majorEvents: String = "",
    val achievements: String = "",
    val challenges: String = "",
    val historicalContext: String = "",
    val sourceMaterial: String = "",
    val chronologyNotes: String = "",
    val desiredPerspective: String = "Objective Historical" // First Person Memoir, Objective Historical, Narrative Nonfiction, Intimate Biographical
)

data class FrontMatterConfig(
    val includeFrontCover: Boolean = true,
    val includeTitlePage: Boolean = true,
    val includeSubtitlePage: Boolean = true,
    val includeCopyrightPage: Boolean = true,
    val includeDedication: Boolean = true,
    val dedicationText: String = "For those who seek boundless worlds within the quiet turning of pages.",
    val includeEpigraph: Boolean = true,
    val epigraphQuote: String = "\"Words can be like X-rays, if you use them properly—they’ll go through anything.\" — Aldous Huxley",
    val includeForeword: Boolean = true,
    val forewordTitle: String = "Foreword & Publisher's Note",
    val forewordText: String = "",
    val includeTableOfContents: Boolean = true,
    val includeListOfIllustrations: Boolean = true
)

data class BackMatterConfig(
    val includeEpilogue: Boolean = true,
    val epilogueContent: String = "",
    val includeAfterword: Boolean = true,
    val afterwordContent: String = "",
    val includeGlossary: Boolean = true,
    val includeTimeline: Boolean = true,
    val includeCharacterIndex: Boolean = true,
    val includePlaceIndex: Boolean = true,
    val includeSubjectIndex: Boolean = true,
    val includeResearchNotes: Boolean = true,
    val includeAcknowledgements: Boolean = true,
    val acknowledgementsText: String = "The author expresses sincere gratitude to historians, literary editors, and creators whose legacy inspires this volume.",
    val includeAboutAuthor: Boolean = true,
    val aboutAuthorText: String = "",
    val includeAboutBook: Boolean = true,
    val aboutBookText: String = "",
    val includeBackCover: Boolean = true
)

data class ManuscriptValidationIssue(
    val severity: IssueSeverity, // ERROR, WARNING, INFO
    val chapterNumber: Int? = null,
    val title: String,
    val message: String,
    val fixAction: String? = null
)

enum class IssueSeverity {
    ERROR,
    WARNING,
    INFO
}

data class GenerationProgress(
    val chapterNumber: Int,
    val currentWords: Int,
    val minTargetWords: Int,
    val maxTargetWords: Int,
    val segmentNumber: Int,
    val statusMessage: String,
    val isRunning: Boolean = false
)
