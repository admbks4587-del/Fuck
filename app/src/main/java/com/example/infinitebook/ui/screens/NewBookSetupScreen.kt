package com.example.infinitebook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.data.model.BiographyFields
import com.example.infinitebook.data.model.BookStyleSettings
import com.example.infinitebook.ui.components.CollapsibleSection
import com.example.infinitebook.ui.components.DirectNumberInputField
import com.example.infinitebook.ui.components.IntensitySliderRow
import com.example.infinitebook.ui.components.MultiSelectChipGroup
import com.example.infinitebook.ui.components.SingleSelectChipGroup
import com.example.infinitebook.ui.components.StudioTopBar
import com.example.infinitebook.ui.viewmodel.BookStudioViewModel
import com.example.infinitebook.ui.viewmodel.StudioScreen
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioObsidian

@Composable
fun NewBookSetupScreen(
    viewModel: BookStudioViewModel
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Archival Fellow") }
    var largePrompt by remember { mutableStateOf("") }
    var referenceMaterial by remember { mutableStateOf("") }

    val allBookTypes = remember {
        listOf(
            "Fiction", "Novel", "Fantasy", "Mythology", "Historical Fiction", "Mystery",
            "Thriller", "Adventure", "Horror", "Science Fiction", "Romance", "Drama",
            "Philosophy", "Spirituality", "Religion", "Knowledge Book", "Educational Book",
            "History", "Biography", "Autobiography", "Memoir", "Documentary", "Research Book",
            "Science", "Technology", "Psychology", "Self-Help", "Poetry", "Short Stories",
            "Children’s Book", "Young Adult", "Reference Book", "Travel Book", "Cultural Book", "Custom"
        )
    }
    var selectedBookTypes by remember { mutableStateOf(setOf("Fiction", "Historical Fiction")) }

    val readerLevels = remember {
        listOf("Child", "Beginner", "Average", "Intermediate", "Advanced", "Expert", "All / General")
    }
    var selectedReaderLevel by remember { mutableStateOf("Average") }

    val languages = remember {
        listOf("English", "Marathi", "Hindi", "Sanskrit", "Gujarati", "Bengali", "Tamil", "Telugu", "Kannada", "Malayalam", "Urdu", "Punjabi", "Custom")
    }
    var selectedLanguage by remember { mutableStateOf("English") }

    val languageModes = remember { listOf("Monolingual", "Bilingual", "Multilingual") }
    var selectedLanguageMode by remember { mutableStateOf("Monolingual") }

    // Direct numeric input fields with hard defaults requested by user:
    var numChapters by remember { mutableIntStateOf(35) }
    var targetPages by remember { mutableIntStateOf(500) }
    var minWordsPerChapter by remember { mutableIntStateOf(10000) } // Default 10,000 words
    var maxWordsPerChapter by remember { mutableIntStateOf(100000) } // Default 100,000 words

    // Visuals
    val illustrationFrequencies = remember { listOf("None", "Minimal", "Normal", "Detailed", "Maximum") }
    var selectedIllFreq by remember { mutableStateOf("Normal") }

    val illustrationStyles = remember {
        listOf("Realistic", "Cinematic", "Fantasy", "Historical Painting", "Ink Sketch", "Ancient Manuscript", "Watercolor", "Technical Diagram", "Cartographic", "Scientific", "Custom")
    }
    var selectedIllStyle by remember { mutableStateOf("Cartographic") }

    // Biography fields
    var bioSubject by remember { mutableStateOf("") }
    var bioPeriod by remember { mutableStateOf("") }
    var bioPeople by remember { mutableStateOf("") }
    var bioEvents by remember { mutableStateOf("") }
    var bioAchievements by remember { mutableStateOf("") }
    var bioChallenges by remember { mutableStateOf("") }
    var bioContext by remember { mutableStateOf("") }
    var bioSources by remember { mutableStateOf("") }
    var bioChronology by remember { mutableStateOf("") }
    var bioPerspective by remember { mutableStateOf("Objective Historical") }

    val isBiography = selectedBookTypes.any { it in listOf("Biography", "Autobiography", "Memoir") }

    // Style Sliders (1-10)
    var styleSettings by remember { mutableStateOf(BookStyleSettings()) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "New Publication Setup",
                subtitle = "Master Architectural Blueprint",
                onBackClick = { viewModel.navigateTo(StudioScreen.DASHBOARD) }
            )
        },
        containerColor = StudioObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Quick preset button bar to load rich demo prompts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("Historical Epic (35 Ch)") {
                        title = "The Chronicles of Sahyadri"
                        subtitle = "A Comprehensive Historical Folio of the Maratha Fortresses"
                        author = "Prof. Raghuveer Sen"
                        largePrompt = "Write an expansive, multi-volume historical narrative chronicle of the 17th-century Maratha mountain bastions, architectural fortifications, secret underground cisterns, diplomatic treaties, and civilian mountain guilds across Raigad, Rajgad, and Sinhagad. Include rich architectural elevation diagrams, cartographic mountain passes, and authentic dialogue."
                        selectedBookTypes = setOf("Historical Fiction", "History", "Cultural Book")
                        selectedLanguage = "English"
                        numChapters = 35
                        targetPages = 500
                        minWordsPerChapter = 10000
                        maxWordsPerChapter = 100000
                        selectedIllStyle = "Cartographic"
                    }
                    PresetButton("Philosophical Sci-Fi") {
                        title = "The Axiom of Silence"
                        subtitle = "Cosmology and the Geometry of Consciousness"
                        author = "Dr. Maya K. Varma"
                        largePrompt = "A profound science fiction and philosophical exploration of an ancient radio telescope relay discovered beneath the Martian basalt plains, broadcasting non-repeating prime topologies that alter human linguistic cognition. Develop intricate characters, scientific depth, atmospheric isolation, and metaphysical inquiries."
                        selectedBookTypes = setOf("Science Fiction", "Philosophy", "Research Book")
                        selectedLanguage = "English"
                        numChapters = 24
                        targetPages = 420
                        minWordsPerChapter = 10000
                        maxWordsPerChapter = 80000
                        selectedIllStyle = "Technical Diagram"
                    }
                }
            }

            // Title, Subtitle, Author
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PUBLICATION IDENTIFIERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Book Title *", color = GoldLight) },
                            placeholder = { Text("e.g., The Cartographer of Whispering Stones", color = ParchmentMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentWhite,
                                unfocusedTextColor = ParchmentWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedContainerColor = StudioCardBg,
                                unfocusedContainerColor = StudioDarkSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = subtitle,
                            onValueChange = { subtitle = it },
                            label = { Text("Subtitle / Secondary Title", color = GoldLight) },
                            placeholder = { Text("e.g., An Archival Inquiry into the Third Realm", color = ParchmentMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentWhite,
                                unfocusedTextColor = ParchmentWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedContainerColor = StudioCardBg,
                                unfocusedContainerColor = StudioDarkSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Author / Compiler Name", color = GoldLight) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentWhite,
                                unfocusedTextColor = ParchmentWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedContainerColor = StudioCardBg,
                                unfocusedContainerColor = StudioDarkSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // CORE BOOK CREATION FLOW: Very large prompt box (no artificial limit)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE MASTER PROMPT (UNLIMITED EXTENT)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "${largePrompt.length} chars",
                                style = MaterialTheme.typography.labelSmall.copy(color = ParchmentMuted)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Paste your entire book premise, character rosters, historical references, lore documents, world rules, and writing requirements. No artificial prompt limit is imposed.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = largePrompt,
                            onValueChange = { largePrompt = it },
                            placeholder = {
                                Text(
                                    "Type or paste your complete book concept, plot architecture, character arcs, setting specifics, and thematic directives here...",
                                    color = ParchmentMuted,
                                    fontSize = 13.sp
                                )
                            },
                            minLines = 8,
                            maxLines = 25,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentWhite,
                                unfocusedTextColor = ParchmentWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedContainerColor = StudioCardBg,
                                unfocusedContainerColor = StudioDarkSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("master_prompt_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = referenceMaterial,
                            onValueChange = { referenceMaterial = it },
                            label = { Text("Supplemental Reference Material / Chronology Notes", color = GoldLight) },
                            placeholder = { Text("Transcripts, research notes, bibliographies, family trees, or historical logs...", color = ParchmentMuted) },
                            minLines = 3,
                            maxLines = 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentWhite,
                                unfocusedTextColor = ParchmentWhite,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardBorder,
                                focusedContainerColor = StudioCardBg,
                                unfocusedContainerColor = StudioDarkSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // CUSTOM BOOK LENGTH: DIRECT NUMERIC INPUT FIELDS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CUSTOM BOOK LENGTH & WORD COUNT TARGETS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Direct numeric inputs for exact chapter count and word constraints. No restrictive scrolling dropdowns.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DirectNumberInputField(
                                    label = "Number of Chapters *",
                                    value = numChapters,
                                    onValueChange = { numChapters = it },
                                    helperText = "e.g. 5, 10, 35, 100, 250+",
                                    testTag = "num_chapters_input"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DirectNumberInputField(
                                    label = "Target Total Pages *",
                                    value = targetPages,
                                    onValueChange = { targetPages = it },
                                    helperText = "e.g. 100, 250, 500, 1000+",
                                    testTag = "target_pages_input"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DirectNumberInputField(
                                    label = "Min Words/Chapter *",
                                    value = minWordsPerChapter,
                                    onValueChange = { minWordsPerChapter = it },
                                    helperText = "Default: 10,000 words",
                                    testTag = "min_words_input"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DirectNumberInputField(
                                    label = "Max Words/Chapter *",
                                    value = maxWordsPerChapter,
                                    onValueChange = { maxWordsPerChapter = it },
                                    helperText = "Default: 100,000 words",
                                    testTag = "max_words_input"
                                )
                            }
                        }
                    }
                }
            }

            // BOOK TYPE (Multi-select)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MultiSelectChipGroup(
                            title = "BOOK TYPE & GENRES (Multi-Select Allowed)",
                            options = allBookTypes,
                            selectedOptions = selectedBookTypes,
                            onToggle = { type ->
                                selectedBookTypes = if (selectedBookTypes.contains(type)) {
                                    if (selectedBookTypes.size > 1) selectedBookTypes - type else selectedBookTypes
                                } else {
                                    selectedBookTypes + type
                                }
                            }
                        )
                    }
                }
            }

            // READER LEVEL
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleSelectChipGroup(
                            title = "READER LEVEL (Depth, Vocabulary & Complexity)",
                            options = readerLevels,
                            selectedOption = selectedReaderLevel,
                            onSelect = { selectedReaderLevel = it }
                        )
                        Text(
                            text = "Controls vocabulary, sentence architecture, conceptual nuance, technical rigor, and narrative subtlety without age pigeonholing.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, fontSize = 11.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // BOOK LANGUAGE & MODES
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleSelectChipGroup(
                            title = "BOOK LANGUAGE (Universal Unicode Support)",
                            options = languages,
                            selectedOption = selectedLanguage,
                            onSelect = { selectedLanguage = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleSelectChipGroup(
                            title = "LANGUAGE MODE",
                            options = languageModes,
                            selectedOption = selectedLanguageMode,
                            onSelect = { selectedLanguageMode = it }
                        )
                    }
                }
            }

            // BIOGRAPHY SPECIALIZED MODE (Visible if Biography/Autobiography/Memoir is selected)
            if (isBiography) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BIOGRAPHY & MEMOIR SPECIALIZED SUITE",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = bioSubject,
                                onValueChange = { bioSubject = it },
                                label = { Text("Subject Person / Figure", color = GoldLight) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioPeriod,
                                onValueChange = { bioPeriod = it },
                                label = { Text("Life Period & Years (e.g., 1890–1954)", color = GoldLight) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioPeople,
                                onValueChange = { bioPeople = it },
                                label = { Text("Key Historical Influences & Associates", color = GoldLight) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioEvents,
                                onValueChange = { bioEvents = it },
                                label = { Text("Milestone Events & Turning Points", color = GoldLight) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // AUTOMATIC VISUALS CONTROLS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleSelectChipGroup(
                            title = "AUTOMATIC ILLUSTRATION FREQUENCY",
                            options = illustrationFrequencies,
                            selectedOption = selectedIllFreq,
                            onSelect = { selectedIllFreq = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleSelectChipGroup(
                            title = "ILLUSTRATION & CARTOGRAPHY STYLE",
                            options = illustrationStyles,
                            selectedOption = selectedIllStyle,
                            onSelect = { selectedIllStyle = it }
                        )
                    }
                }
            }

            // BOOK STYLE CONTROLS (Expandable 22 Sliders 1-10)
            item {
                CollapsibleSection(
                    title = "Book Style Controls & Depth Metrics (1–10)",
                    subtitle = "Tone, Dialogue, Pacing, Mystery, Lore, Research & World-Building"
                ) {
                    IntensitySliderRow("Pacing Intensity", styleSettings.pacing, { styleSettings = styleSettings.copy(pacing = it) })
                    IntensitySliderRow("Vocabulary Complexity", styleSettings.vocabularyComplexity, { styleSettings = styleSettings.copy(vocabularyComplexity = it) })
                    IntensitySliderRow("Dialogue Density", styleSettings.dialogueDensity, { styleSettings = styleSettings.copy(dialogueDensity = it) })
                    IntensitySliderRow("Description Density", styleSettings.descriptionDensity, { styleSettings = styleSettings.copy(descriptionDensity = it) })
                    IntensitySliderRow("Historical & Research Depth", styleSettings.historicalDepth, { styleSettings = styleSettings.copy(historicalDepth = it) })
                    IntensitySliderRow("World-Building Depth", styleSettings.worldBuildingDepth, { styleSettings = styleSettings.copy(worldBuildingDepth = it) })
                    IntensitySliderRow("Character Interiority & Depth", styleSettings.characterDepth, { styleSettings = styleSettings.copy(characterDepth = it) })
                    IntensitySliderRow("Philosophical Depth", styleSettings.philosophicalDepth, { styleSettings = styleSettings.copy(philosophicalDepth = it) })
                    IntensitySliderRow("Mystery & Suspense", styleSettings.mysteryIntensity, { styleSettings = styleSettings.copy(mysteryIntensity = it) })
                    IntensitySliderRow("Action Intensity", styleSettings.actionIntensity, { styleSettings = styleSettings.copy(actionIntensity = it) })
                    IntensitySliderRow("Emotional Resonance", styleSettings.emotionalIntensity, { styleSettings = styleSettings.copy(emotionalIntensity = it) })
                    IntensitySliderRow("Plot Complexity & Twists", styleSettings.plotComplexity, { styleSettings = styleSettings.copy(plotComplexity = it) })
                    IntensitySliderRow("Cultural & Regional Detail", styleSettings.culturalDetail, { styleSettings = styleSettings.copy(culturalDetail = it) })
                    IntensitySliderRow("Spiritual / Mythological Depth", styleSettings.spiritualMythologicalDepth, { styleSettings = styleSettings.copy(spiritualMythologicalDepth = it) })
                }
            }

            // INITIALIZE BUTTON
            item {
                Button(
                    onClick = {
                        val bio = BiographyFields(
                            subjectPerson = bioSubject,
                            lifePeriod = bioPeriod,
                            importantPeople = bioPeople,
                            majorEvents = bioEvents,
                            achievements = bioAchievements,
                            challenges = bioChallenges,
                            historicalContext = bioContext,
                            sourceMaterial = bioSources,
                            chronologyNotes = bioChronology,
                            desiredPerspective = bioPerspective
                        )

                        viewModel.createBook(
                            title = title.ifBlank { "Untitled Masterpiece" },
                            subtitle = subtitle,
                            author = author.ifBlank { "Author" },
                            prompt = largePrompt.ifBlank { "A grand long-form literary exploration of " + selectedBookTypes.joinToString(", ") },
                            referenceMaterial = referenceMaterial,
                            bookTypes = selectedBookTypes.toList(),
                            readerLevel = selectedReaderLevel,
                            language = selectedLanguage,
                            languageMode = selectedLanguageMode,
                            numChapters = numChapters,
                            targetPages = targetPages,
                            minWordsPerChapter = minWordsPerChapter,
                            maxWordsPerChapter = maxWordsPerChapter,
                            illustrationFrequency = selectedIllFreq,
                            illustrationStyle = selectedIllStyle,
                            styleSettings = styleSettings,
                            biographyFields = bio
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("initialize_book_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioObsidian,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Initialize Book Studio & Generate Blueprint",
                        color = StudioObsidian,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    Surface(
        color = StudioCardBg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = "⚡ $label",
            style = MaterialTheme.typography.bodySmall.copy(
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
