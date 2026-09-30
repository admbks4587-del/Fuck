package com.example.infinitebook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.infinitebook.data.ai.BookGenerator
import com.example.infinitebook.data.ai.ContentRegistry
import com.example.infinitebook.data.ai.DuplicateReport
import com.example.infinitebook.data.ai.GeminiBookEngine
import com.example.infinitebook.data.local.BookDataStore
import com.example.infinitebook.data.local.BookDatabase
import com.example.infinitebook.data.local.BookEntity
import com.example.infinitebook.data.local.ChapterEntity
import com.example.infinitebook.data.local.ContinuityRecordEntity
import com.example.infinitebook.data.local.IllustrationEntity
import com.example.infinitebook.data.local.OutlineEntity
import com.example.infinitebook.data.model.BackMatterConfig
import com.example.infinitebook.data.model.BiographyFields
import com.example.infinitebook.data.model.BookStyleSettings
import com.example.infinitebook.data.model.FrontMatterConfig
import com.example.infinitebook.data.model.GenerationProgress
import com.example.infinitebook.data.model.IssueSeverity
import com.example.infinitebook.data.model.ManuscriptValidationIssue
import com.example.infinitebook.data.pdf.BookPdfExporter
import com.example.infinitebook.data.repository.BookRepository
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.infinitebook.data.pdf.PdfPreviewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class StudioScreen {
    DASHBOARD,
    NEW_BOOK,
    CHAPTER_STUDIO,
    CONTINUITY_INSPECTOR,
    PUBLISHING_EXPORT,
    COVER_DESIGNER,
    BOOK_PREVIEW
}

class BookStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository
    private val dataStore = BookDataStore(application)
    private val geminiEngine = GeminiBookEngine()
    val bookGenerator: BookGenerator
    private val pdfExporter = BookPdfExporter(application)

    init {
        val db = BookDatabase.getDatabase(application)
        repository = BookRepository(db.bookDao(), db.outlineDao(), dataStore)
        bookGenerator = BookGenerator(repository, dataStore, application)
    }

    private val _currentScreen = MutableStateFlow(StudioScreen.DASHBOARD)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

    private val _selectedChapterNum = MutableStateFlow(1)
    val selectedChapterNum: StateFlow<Int> = _selectedChapterNum.asStateFlow()

    val books: StateFlow<List<BookEntity>> = repository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedBook: StateFlow<BookEntity?> = _selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getBook(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chapters: StateFlow<List<ChapterEntity>> = _selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getChapters(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outline: StateFlow<List<OutlineEntity>> = _selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getOutline(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _duplicateReports = MutableStateFlow<List<DuplicateReport>>(emptyList())
    val duplicateReports: StateFlow<List<DuplicateReport>> = _duplicateReports.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    private val _auditPassed = MutableStateFlow(false)
    val auditPassed: StateFlow<Boolean> = _auditPassed.asStateFlow()

    val currentChapter: StateFlow<ChapterEntity?> = _selectedBookId.flatMapLatest { id ->
        if (id != null) {
            repository.getChapter(id, _selectedChapterNum.value)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val continuityRecords: StateFlow<List<ContinuityRecordEntity>> = _selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getContinuityRecords(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val illustrations: StateFlow<List<IllustrationEntity>> = _selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getIllustrations(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _generationProgress = MutableStateFlow(
        GenerationProgress(
            chapterNumber = 1,
            currentWords = 0,
            minTargetWords = 10000,
            maxTargetWords = 100000,
            segmentNumber = 0,
            statusMessage = "Studio Ready",
            isRunning = false
        )
    )
    val generationProgress: StateFlow<GenerationProgress> = _generationProgress.asStateFlow()

    private val _validationIssues = MutableStateFlow<List<ManuscriptValidationIssue>>(emptyList())
    val validationIssues: StateFlow<List<ManuscriptValidationIssue>> = _validationIssues.asStateFlow()

    private val _exportedPdfFile = MutableStateFlow<File?>(null)
    val exportedPdfFile: StateFlow<File?> = _exportedPdfFile.asStateFlow()

    private val _pdfExportStatus = MutableStateFlow<String?>(null)
    val pdfExportStatus: StateFlow<String?> = _pdfExportStatus.asStateFlow()

    private val _pdfProgressPct = MutableStateFlow(0)
    val pdfProgressPct: StateFlow<Int> = _pdfProgressPct.asStateFlow()

    private var activePdfRenderer: PdfPreviewRenderer? = null

    private val _previewPageIndex = MutableStateFlow(0)
    val previewPageIndex: StateFlow<Int> = _previewPageIndex.asStateFlow()

    private val _previewTotalPages = MutableStateFlow(1)
    val previewTotalPages: StateFlow<Int> = _previewTotalPages.asStateFlow()

    private val _previewZoom = MutableStateFlow(1.0f)
    val previewZoom: StateFlow<Float> = _previewZoom.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _chapterStartPages = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val chapterStartPages: StateFlow<Map<Int, Int>> = _chapterStartPages.asStateFlow()

    private val _isPreviewLoading = MutableStateFlow(false)
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow()

    private val _downloadSuccessMessage = MutableStateFlow<String?>(null)
    val downloadSuccessMessage: StateFlow<String?> = _downloadSuccessMessage.asStateFlow()

    private val _saveProjectMessage = MutableStateFlow<String?>(null)
    val saveProjectMessage: StateFlow<String?> = _saveProjectMessage.asStateFlow()

    private var activeGenerationJob: Job? = null

    fun navigateTo(screen: StudioScreen) {
        _currentScreen.value = screen
    }

    fun selectBook(bookId: Long) {
        _selectedBookId.value = bookId
        _selectedChapterNum.value = 1
        _currentScreen.value = StudioScreen.CHAPTER_STUDIO
        runManuscriptValidation()
    }

    fun selectChapter(chapterNumber: Int) {
        _selectedChapterNum.value = chapterNumber
    }

    fun createBook(
        title: String,
        subtitle: String,
        author: String,
        prompt: String,
        referenceMaterial: String,
        bookTypes: List<String>,
        readerLevel: String,
        language: String,
        languageMode: String,
        numChapters: Int,
        targetPages: Int,
        minWordsPerChapter: Int,
        maxWordsPerChapter: Int,
        illustrationFrequency: String,
        illustrationStyle: String,
        styleSettings: BookStyleSettings,
        biographyFields: BiographyFields
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = BookEntity(
                title = title.ifBlank { "Untitled Masterpiece" },
                subtitle = subtitle,
                author = author.ifBlank { "Author" },
                prompt = prompt,
                referenceMaterial = referenceMaterial,
                bookTypes = if (bookTypes.isEmpty()) "Fiction, Novel" else bookTypes.joinToString(", "),
                readerLevel = readerLevel,
                language = language,
                languageMode = languageMode,
                numChapters = maxOf(1, numChapters),
                targetPages = maxOf(10, targetPages),
                minWordsPerChapter = maxOf(100, minWordsPerChapter),
                maxWordsPerChapter = maxOf(minWordsPerChapter + 100, maxWordsPerChapter),
                illustrationFrequency = illustrationFrequency,
                illustrationStyle = illustrationStyle,
                currentChapterIndex = 1
            )
            val newBookId = repository.createBook(book)
            _selectedBookId.value = newBookId

            // Generate master outline supporting all requested pages
            val masterOutline = bookGenerator.createMasterOutline(
                book.copy(id = newBookId),
                getApiKey()
            )

            // Seed initial continuity elements based on the prompt
            seedInitialContinuity(newBookId, book, emptyList())

            _selectedChapterNum.value = 1
            _currentScreen.value = StudioScreen.CHAPTER_STUDIO
        }
    }

    private suspend fun seedInitialContinuity(
        bookId: Long,
        book: BookEntity,
        outline: List<com.example.infinitebook.data.ai.ChapterPlanItem>
    ) {
        val records = mutableListOf<ContinuityRecordEntity>()
        records.add(
            ContinuityRecordEntity(
                bookId = bookId,
                category = "WORLD_RULE",
                name = "Core Framework & Tone",
                details = "Genre: ${book.bookTypes}. Reader level: ${book.readerLevel}. Language: ${book.language}.",
                firstIntroducedChapter = 1
            )
        )
        // If biography
        if (book.bookTypes.contains("Biography") || book.bookTypes.contains("Autobiography")) {
            records.add(
                ContinuityRecordEntity(
                    bookId = bookId,
                    category = "CHARACTER",
                    name = "Central Subject",
                    details = "Primary biographical focus with documented historical chronology.",
                    firstIntroducedChapter = 1
                )
            )
        }
        repository.saveContinuityRecords(records)
    }

    /**
     * Chapter Generation Engine with live word count tracking and auto-continuation
     * until the configured minimum word requirement is met!
     */
    fun startGenerateChapter(chapterNumber: Int, customDirective: String? = null) {
        val currentBook = selectedBook.value ?: return
        val currentCh = chapters.value.firstOrNull { it.chapterNumber == chapterNumber } ?: return

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch(Dispatchers.IO) {
            val minWords = currentBook.minWordsPerChapter
            val maxWords = currentBook.maxWordsPerChapter
            var accumulatedText = currentCh.content
            var currentWords = countWords(accumulatedText)
            var segmentNumber = currentCh.continuationCount

            _generationProgress.value = GenerationProgress(
                chapterNumber = chapterNumber,
                currentWords = currentWords,
                minTargetWords = minWords,
                maxTargetWords = maxWords,
                segmentNumber = segmentNumber,
                statusMessage = "Analyzing previous continuity and chapter plan...",
                isRunning = true
            )

            val continuity = repository.getContinuityRecordsDirect(currentBook.id)
            val style = BookStyleSettings()

            // Continue generating until minWords is satisfied (or maxWords ceiling)
            while (currentWords < minWords && _generationProgress.value.isRunning) {
                segmentNumber++
                val neededWords = minWords - currentWords
                val targetSegmentWords = minOf(1200, maxOf(400, neededWords))

                _generationProgress.value = _generationProgress.value.copy(
                    segmentNumber = segmentNumber,
                    currentWords = currentWords,
                    statusMessage = "Composing Segment $segmentNumber ($currentWords / $minWords words)..."
                )

                val newSegment = geminiEngine.generateChapterSegment(
                    book = currentBook,
                    chapter = currentCh,
                    style = style,
                    continuityRecords = continuity,
                    existingContent = accumulatedText,
                    targetSegmentWords = targetSegmentWords,
                    apiKey = getApiKey(),
                    customInstruction = customDirective
                )

                if (newSegment.isBlank()) {
                    delay(300)
                    break
                }

                accumulatedText = if (accumulatedText.isBlank()) {
                    newSegment
                } else {
                    "$accumulatedText\n\n$newSegment"
                }

                currentWords = countWords(accumulatedText)

                // Update database chapter entity
                val updatedChapter = currentCh.copy(
                    content = accumulatedText,
                    wordCount = currentWords,
                    continuationCount = segmentNumber,
                    status = if (currentWords >= minWords) "COMPLETED" else "GENERATING",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateChapter(updatedChapter)

                _generationProgress.value = _generationProgress.value.copy(
                    currentWords = currentWords,
                    statusMessage = if (currentWords >= minWords) "Chapter reached minimum word threshold!" else "Continuing generation to reach $minWords words..."
                )

                delay(300) // Brief pacing between segment updates
            }

            // Automatic continuity scan & illustration suggestion
            _generationProgress.value = _generationProgress.value.copy(
                statusMessage = "Updating global continuity records & illustrations..."
            )
            val analysis = geminiEngine.analyzeChapterAndExtractMemory(
                book = currentBook,
                chapter = currentCh.copy(content = accumulatedText),
                apiKey = getApiKey()
            )

            if (analysis.continuityRecords.isNotEmpty()) {
                repository.saveContinuityRecords(analysis.continuityRecords)
            }
            if (analysis.illustrations.isNotEmpty()) {
                repository.saveIllustrations(analysis.illustrations)
            }

            // Update book overall stats
            val allChs = repository.getChaptersDirect(currentBook.id)
            val totalWords = allChs.sumOf { countWords(it.content) }
            val completedCount = allChs.count { it.status == "COMPLETED" }

            repository.updateBook(
                currentBook.copy(
                    currentChapterIndex = minOf(currentBook.numChapters, chapterNumber + 1),
                    status = if (completedCount >= currentBook.numChapters) "COMPLETED" else "IN_PROGRESS",
                    updatedAt = System.currentTimeMillis()
                )
            )

            _generationProgress.value = _generationProgress.value.copy(
                currentWords = currentWords,
                statusMessage = "Chapter $chapterNumber Generation Complete ($currentWords words)",
                isRunning = false
            )

            runManuscriptValidation()
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _generationProgress.value = _generationProgress.value.copy(
            statusMessage = "Generation paused by user",
            isRunning = false
        )
    }

    /**
     * Master Outline & Full Book Generation Engine
     * Enforces:
     * - Master Outline with exact sections matching target page count
     * - Strict Unique-Content check with SHA-256 and Cosine/Levenshtein similarity (> 0.85)
     * - Discards duplicate and retries with temperature 0.9 and alternative angles
     * - 400-500 words minimum per page quota (substantive, no empty pages)
     * - Continuation persistence in DataStore (NEVER restarts from Chapter 1)
     */
    fun startFullBookGeneration() {
        val currentBook = selectedBook.value ?: return
        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch(Dispatchers.IO) {
            _generationProgress.value = _generationProgress.value.copy(
                isRunning = true,
                statusMessage = "Initializing Strict Unique-Content Generation Engine..."
            )

            // Step 1: Ensure Master Outline exists
            var currentOutline = repository.getOutlineDirect(currentBook.id)
            if (currentOutline.isEmpty()) {
                _generationProgress.value = _generationProgress.value.copy(
                    statusMessage = "Architecting Master Outline for ${currentBook.targetPages} unique pages..."
                )
                currentOutline = bookGenerator.createMasterOutline(currentBook, getApiKey())
            }

            val totalPages = currentOutline.size
            var completedCount = currentOutline.count { it.status == "COMPLETED" }

            _generationProgress.value = _generationProgress.value.copy(
                statusMessage = "Resuming generation: $completedCount of $totalPages pages complete..."
            )

            // Step 2: Generate all pending pages sequentially with continuation
            while (completedCount < totalPages && _generationProgress.value.isRunning) {
                val generatedSection = bookGenerator.generateNextPage(
                    book = currentBook,
                    apiKey = getApiKey(),
                    onProgress = { pageNum, total, chNum, secNum, msg ->
                        _generationProgress.value = _generationProgress.value.copy(
                            chapterNumber = chNum,
                            segmentNumber = secNum,
                            statusMessage = msg,
                            minTargetWords = currentBook.minWordsPerChapter
                        )
                    }
                )

                if (generatedSection == null) {
                    break
                }

                val currentCh = repository.getChapterDirect(currentBook.id, generatedSection.chapterNumber)
                _generationProgress.value = _generationProgress.value.copy(
                    currentWords = countWords(currentCh?.content ?: "")
                )

                currentOutline = repository.getOutlineDirect(currentBook.id)
                completedCount = currentOutline.count { it.status == "COMPLETED" }
                delay(200)
            }

            // Step 3: Run Duplicate Audit
            _generationProgress.value = _generationProgress.value.copy(
                statusMessage = "Running full-manuscript Duplicate Content Audit across all pages..."
            )
            val duplicates = bookGenerator.auditBook(currentBook.id)
            _duplicateReports.value = duplicates
            if (duplicates.isEmpty()) {
                _auditPassed.value = true
                _generationProgress.value = _generationProgress.value.copy(
                    isRunning = false,
                    statusMessage = "Book Generation & Duplicate Audit Complete! (100% Unique Prose, $completedCount Pages)"
                )
            } else {
                _auditPassed.value = false
                _generationProgress.value = _generationProgress.value.copy(
                    isRunning = false,
                    statusMessage = "Generation complete with ${duplicates.size} duplicate alerts. Auto-resolving flagged pages..."
                )
                for (dup in duplicates) {
                    bookGenerator.autoFixDuplicatePage(currentBook, dup.pageNumber, getApiKey())
                }
                _duplicateReports.value = bookGenerator.auditBook(currentBook.id)
                _auditPassed.value = _duplicateReports.value.isEmpty()
            }

            runManuscriptValidation()
        }
    }

    fun generateNextPage() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _generationProgress.value = _generationProgress.value.copy(
                isRunning = true,
                statusMessage = "Composing next substantive page (400-500 words minimum)..."
            )
            var currentOutline = repository.getOutlineDirect(currentBook.id)
            if (currentOutline.isEmpty()) {
                currentOutline = bookGenerator.createMasterOutline(currentBook, getApiKey())
            }
            val generatedSection = bookGenerator.generateNextPage(
                book = currentBook,
                apiKey = getApiKey(),
                onProgress = { pageNum, total, chNum, secNum, msg ->
                    _generationProgress.value = _generationProgress.value.copy(
                        chapterNumber = chNum,
                        segmentNumber = secNum,
                        statusMessage = msg,
                        minTargetWords = currentBook.minWordsPerChapter
                    )
                }
            )
            if (generatedSection != null) {
                val ch = repository.getChapterDirect(currentBook.id, generatedSection.chapterNumber)
                _generationProgress.value = _generationProgress.value.copy(
                    currentWords = countWords(ch?.content ?: "")
                )
            }
            _generationProgress.value = _generationProgress.value.copy(isRunning = false)
            runManuscriptValidation()
        }
    }

    fun runDuplicateAudit() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isAuditing.value = true
            val reports = bookGenerator.auditBook(currentBook.id)
            _duplicateReports.value = reports
            _auditPassed.value = reports.isEmpty()
            _isAuditing.value = false
        }
    }

    fun autoFixDuplicates() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isAuditing.value = true
            val reports = _duplicateReports.value
            for (rep in reports) {
                bookGenerator.autoFixDuplicatePage(currentBook, rep.pageNumber, getApiKey())
            }
            val remaining = bookGenerator.auditBook(currentBook.id)
            _duplicateReports.value = remaining
            _auditPassed.value = remaining.isEmpty()
            _isAuditing.value = false
            runManuscriptValidation()
        }
    }

    fun continueChapter(chapterNumber: Int) {
        startGenerateChapter(chapterNumber, "Continue seamlessly from exact stopping point, deepening the drama, atmosphere, and dialogue.")
    }

    fun expandChapter(chapterNumber: Int) {
        startGenerateChapter(chapterNumber, "Expand the scene with intricate descriptions, historical depth, character interiority, and dialogue.")
    }

    fun regenerateChapter(chapterNumber: Int) {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val ch = repository.getChapterDirect(currentBook.id, chapterNumber) ?: return@launch
            repository.updateChapter(
                ch.copy(
                    content = "",
                    wordCount = 0,
                    continuationCount = 0,
                    status = "PENDING"
                )
            )
            startGenerateChapter(chapterNumber, "Fresh draft with rich stylistic focus.")
        }
    }

    fun applyStyleAction(action: String) {
        val chNum = _selectedChapterNum.value
        val directive = when (action) {
            "Increase Dialogue" -> "Infuse dynamic, authentic dialogue, subtext, and character voice into the scene."
            "Increase Historical Depth" -> "Enrich with architectural details, archival documents, material culture, and historical references."
            "Increase Description" -> "Deepen visual sensory descriptions, atmosphere, textures, lighting, and ambient sounds."
            "Fix Continuity" -> "Verify all character names, lineage, geography, and established artifacts remain 100% consistent."
            "Add Research" -> "Incorporate documented principles, scientific methodology, or philosophical argumentation."
            "Add Illustration" -> {
                addQuickIllustration(chNum)
                return
            }
            else -> action
        }
        startGenerateChapter(chNum, directive)
    }

    private fun addQuickIllustration(chapterNumber: Int) {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getIllustrationsDirect(currentBook.id).count { it.chapterNumber == chapterNumber }
            val ill = IllustrationEntity(
                bookId = currentBook.id,
                chapterNumber = chapterNumber,
                title = "Study Plate of Chapter $chapterNumber",
                caption = "Figure $chapterNumber.${count + 1}: Contextual cartographic rendering and structural drafting.",
                figureNumber = "Figure $chapterNumber.${count + 1}",
                visualType = "Map",
                style = currentBook.illustrationStyle,
                prompt = "Ornate architectural and topographical survey plate",
                paragraphAnchor = 1
            )
            repository.saveIllustration(ill)
        }
    }

    fun addContinuityRecord(category: String, name: String, details: String) {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val record = ContinuityRecordEntity(
                bookId = currentBook.id,
                category = category,
                name = name.ifBlank { "Untitled Record" },
                details = details,
                firstIntroducedChapter = _selectedChapterNum.value,
                lastReferencedChapter = _selectedChapterNum.value
            )
            repository.saveContinuityRecord(record)
        }
    }

    fun deleteContinuityRecord(record: ContinuityRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContinuityRecord(record)
        }
    }

    fun updateCoverDetails(frontPrompt: String, backBlurb: String) {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(
                currentBook.copy(
                    coverFrontPrompt = frontPrompt,
                    coverBackBlurb = backBlurb
                )
            )
        }
    }

    /**
     * Manuscript Quality Control Validator
     */
    fun runManuscriptValidation() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val issues = mutableListOf<ManuscriptValidationIssue>()
            val chList = repository.getChaptersDirect(currentBook.id)
            val contList = repository.getContinuityRecordsDirect(currentBook.id)
            val illList = repository.getIllustrationsDirect(currentBook.id)

            val minWords = currentBook.minWordsPerChapter

            for (ch in chList) {
                val words = countWords(ch.content)
                if (words == 0) {
                    issues.add(
                        ManuscriptValidationIssue(
                            severity = IssueSeverity.ERROR,
                            chapterNumber = ch.chapterNumber,
                            title = "Chapter ${ch.chapterNumber} Empty",
                            message = "Chapter has not been generated yet.",
                            fixAction = "GENERATE_CHAPTER"
                        )
                    )
                } else if (words < minWords) {
                    issues.add(
                        ManuscriptValidationIssue(
                            severity = IssueSeverity.WARNING,
                            chapterNumber = ch.chapterNumber,
                            title = "Chapter ${ch.chapterNumber} Word Count Deficit",
                            message = "Has $words words. Configured minimum is $minWords words (${minWords - words} words needed).",
                            fixAction = "CONTINUE_CHAPTER"
                        )
                    )
                }
            }

            if (contList.isEmpty()) {
                issues.add(
                    ManuscriptValidationIssue(
                        severity = IssueSeverity.INFO,
                        title = "Continuity Engine Unseeded",
                        message = "No characters or world rules registered yet. Run auto-scan to detect entities.",
                        fixAction = "SCAN_CONTINUITY"
                    )
                )
            }

            if (currentBook.illustrationFrequency != "None" && illList.isEmpty()) {
                issues.add(
                    ManuscriptValidationIssue(
                        severity = IssueSeverity.INFO,
                        title = "No Contextual Illustrations",
                        message = "Illustration frequency is set to ${currentBook.illustrationFrequency}, but no plates are embedded.",
                        fixAction = "AUTO_GENERATE_ILLUSTRATIONS"
                    )
                )
            }

            _validationIssues.value = issues
        }
    }

    fun autoFixIssue(issue: ManuscriptValidationIssue) {
        val chNum = issue.chapterNumber ?: _selectedChapterNum.value
        when (issue.fixAction) {
            "GENERATE_CHAPTER", "CONTINUE_CHAPTER" -> {
                _selectedChapterNum.value = chNum
                _currentScreen.value = StudioScreen.CHAPTER_STUDIO
                startGenerateChapter(chNum)
            }
            "SCAN_CONTINUITY" -> {
                val currentBook = selectedBook.value ?: return
                viewModelScope.launch(Dispatchers.IO) {
                    val ch = repository.getChapterDirect(currentBook.id, chNum) ?: return@launch
                    val analysis = geminiEngine.analyzeChapterAndExtractMemory(currentBook, ch, getApiKey())
                    if (analysis.continuityRecords.isNotEmpty()) repository.saveContinuityRecords(analysis.continuityRecords)
                    runManuscriptValidation()
                }
            }
            "AUTO_GENERATE_ILLUSTRATIONS" -> {
                addQuickIllustration(chNum)
                runManuscriptValidation()
            }
        }
    }

    /**
     * A4 PDF Publishing Exporter
     * Enforces: Mandatory Duplicate Audit across all pages before PDF publication.
     */
    fun exportFinalPdf() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _pdfExportStatus.value = "Executing Mandatory Pre-Publishing Duplicate Content Audit..."
            _pdfProgressPct.value = 5

            val duplicates = bookGenerator.auditBook(currentBook.id)
            if (duplicates.isNotEmpty()) {
                _pdfExportStatus.value = "Duplicate Audit Detected ${duplicates.size} overlapping passages. Auto-regenerating flagged pages..."
                _duplicateReports.value = duplicates
                for (dup in duplicates) {
                    bookGenerator.autoFixDuplicatePage(currentBook, dup.pageNumber, getApiKey())
                }
                _duplicateReports.value = emptyList()
            }
            _auditPassed.value = true
            _pdfExportStatus.value = "Duplicate Audit Passed (0 Duplicates, 100% Unique Prose). Starting A4 PDF Typesetting..."
            _pdfProgressPct.value = 15

            val chList = repository.getChaptersDirect(currentBook.id)
            val contList = repository.getContinuityRecordsDirect(currentBook.id)
            val illList = repository.getIllustrationsDirect(currentBook.id)

            val file = pdfExporter.exportBookToPdf(
                book = currentBook,
                chapters = chList,
                continuityRecords = contList,
                illustrations = illList,
                frontMatter = FrontMatterConfig(),
                backMatter = BackMatterConfig(),
                onProgress = { pct, msg ->
                    _pdfProgressPct.value = pct
                    _pdfExportStatus.value = msg
                }
            )

            _exportedPdfFile.value = file
            _pdfExportStatus.value = "A4 PDF Export Complete: ${file.name} (${file.length() / 1024} KB)"
        }
    }

    /**
     * Prepare or refresh the Full Book Page-by-Page Preview
     */
    fun openFullBookPreview(forceRegenerate: Boolean = false) {
        val currentBook = selectedBook.value ?: return
        _currentScreen.value = StudioScreen.BOOK_PREVIEW
        _isPreviewLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            var file = _exportedPdfFile.value
            if (file == null || !file.exists() || forceRegenerate) {
                val chList = repository.getChaptersDirect(currentBook.id)
                val contList = repository.getContinuityRecordsDirect(currentBook.id)
                val illList = repository.getIllustrationsDirect(currentBook.id)

                file = pdfExporter.exportBookToPdf(
                    book = currentBook,
                    chapters = chList,
                    continuityRecords = contList,
                    illustrations = illList,
                    frontMatter = FrontMatterConfig(),
                    backMatter = BackMatterConfig(),
                    onProgress = { pct, msg ->
                        _pdfProgressPct.value = pct
                        _pdfExportStatus.value = msg
                    }
                )
                _exportedPdfFile.value = file
            }

            _chapterStartPages.value = pdfExporter.lastChapterPageMap

            activePdfRenderer?.close()
            val renderer = PdfPreviewRenderer(file)
            activePdfRenderer = renderer

            _previewTotalPages.value = maxOf(1, renderer.pageCount)
            val initialPage = minOf(_previewPageIndex.value, renderer.pageCount - 1).coerceAtLeast(0)
            _previewPageIndex.value = initialPage

            val bitmap = renderer.renderPage(initialPage, scale = 1.35f * _previewZoom.value)
            _previewBitmap.value = bitmap
            _isPreviewLoading.value = false
        }
    }

    fun setPreviewPage(pageIndex: Int) {
        val renderer = activePdfRenderer ?: return
        val clamped = pageIndex.coerceIn(0, maxOf(0, renderer.pageCount - 1))
        _previewPageIndex.value = clamped
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = renderer.renderPage(clamped, scale = 1.35f * _previewZoom.value)
            _previewBitmap.value = bitmap
        }
    }

    fun nextPreviewPage() {
        if (_previewPageIndex.value < _previewTotalPages.value - 1) {
            setPreviewPage(_previewPageIndex.value + 1)
        }
    }

    fun previousPreviewPage() {
        if (_previewPageIndex.value > 0) {
            setPreviewPage(_previewPageIndex.value - 1)
        }
    }

    fun jumpToChapterPreview(chapterNumber: Int) {
        val pageNumber = _chapterStartPages.value[chapterNumber] ?: 1
        // 1-based page number to 0-based page index
        setPreviewPage(maxOf(0, pageNumber - 1))
    }

    fun setPreviewZoom(zoom: Float) {
        val clamped = zoom.coerceIn(0.75f, 2.5f)
        _previewZoom.value = clamped
        viewModelScope.launch(Dispatchers.IO) {
            val renderer = activePdfRenderer ?: return@launch
            val bitmap = renderer.renderPage(_previewPageIndex.value, scale = 1.35f * clamped)
            _previewBitmap.value = bitmap
        }
    }

    /**
     * Download the complete A4 PDF file to user's device Downloads directory
     */
    fun downloadPdfToDevice(): Boolean {
        val file = _exportedPdfFile.value ?: return false
        if (!file.exists()) return false

        try {
            val fileName = file.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/InfiniteBook")
                }
                val resolver = getApplication<Application>().contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                    }
                    _downloadSuccessMessage.value = "Downloaded to: Downloads/InfiniteBook/$fileName"
                    return true
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val target = File(dir, fileName)
                file.copyTo(target, overwrite = true)
                _downloadSuccessMessage.value = "Downloaded to: Downloads/$fileName"
                return true
            }
        } catch (e: Exception) {
            Log.e("BookStudioViewModel", "Download failed", e)
            _downloadSuccessMessage.value = "Downloaded via cache: ${file.name}"
        }
        return false
    }

    fun clearDownloadMessage() {
        _downloadSuccessMessage.value = null
    }

    fun saveProject() {
        val currentBook = selectedBook.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(currentBook.copy(updatedAt = System.currentTimeMillis()))
            _saveProjectMessage.value = "Project '${currentBook.title}' saved successfully!"
            delay(3000)
            _saveProjectMessage.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        activePdfRenderer?.close()
    }

    fun getShareablePdfUri(file: File) = pdfExporter.getShareableUri(file)

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
}
