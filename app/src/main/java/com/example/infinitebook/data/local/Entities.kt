package com.example.infinitebook.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val author: String = "Author",
    val prompt: String,
    val referenceMaterial: String = "",
    val bookTypes: String, // Comma-separated categories e.g. "Fiction, Mystery, Historical Fiction"
    val readerLevel: String = "Average", // Child, Beginner, Average, Intermediate, Advanced, Expert, All / General
    val language: String = "English",
    val languageMode: String = "Monolingual", // Monolingual, Bilingual, Multilingual
    val secondaryLanguage: String = "",
    val numChapters: Int = 12,
    val targetPages: Int = 250,
    val minWordsPerChapter: Int = 10000,
    val maxWordsPerChapter: Int = 100000,
    val illustrationFrequency: String = "Normal", // None, Minimal, Normal, Detailed, Maximum
    val illustrationStyle: String = "Realistic", // Realistic, Cinematic, Fantasy, Historical Painting, Ink Sketch, Ancient Manuscript, Watercolor, Technical Diagram, Cartographic, Scientific, Custom
    val styleSettingsJson: String = "",
    val biographyFieldsJson: String = "",
    val frontMatterJson: String = "",
    val backMatterJson: String = "",
    val coverFrontPrompt: String = "",
    val coverBackBlurb: String = "",
    val coverTheme: String = "Royal Leather", // Royal Leather, Obsidian Gold, Vintage Parchment, Cosmic Fantasy, Minimal Modern
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, ARCHIVED
    val currentChapterIndex: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId", "chapterNumber"], unique = true)]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterNumber: Int,
    val title: String,
    val subtitle: String = "",
    val summary: String = "",
    val plan: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val status: String = "PENDING", // PENDING, GENERATING, COMPLETED, REVIEW_NEEDED
    val continuationCount: Int = 0,
    val continuityNotes: String = "",
    val researchNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "continuity_records",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class ContinuityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val category: String, // CHARACTER, LOCATION, MAP, ARTIFACT, SYMBOL, EVENT, CHRONOLOGY, FAMILY_TREE, ORGANIZATION, WORLD_RULE, TECHNOLOGY, MYTHOLOGY, CLUE, MYSTERY, DIALOGUE_FACT, SUMMARY, VISUAL_REF
    val name: String,
    val details: String,
    val firstIntroducedChapter: Int = 1,
    val lastReferencedChapter: Int = 1,
    val status: String = "ACTIVE" // ACTIVE, RESOLVED, EVOLVED
)

@Entity(
    tableName = "illustrations",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId", "chapterNumber"])]
)
data class IllustrationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterNumber: Int,
    val title: String,
    val caption: String,
    val figureNumber: String, // e.g. "Figure 3.1"
    val visualType: String, // Map, World Map, Route, Architectural, Character, Artifact, Timeline, Diagram, Scene
    val style: String,
    val prompt: String,
    val imageUri: String = "",
    val paragraphAnchor: Int = 0, // In which section/paragraph to embed in chapter
    val createdAt: Long = System.currentTimeMillis()
)
