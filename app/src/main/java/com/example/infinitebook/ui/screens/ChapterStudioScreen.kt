package com.example.infinitebook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import com.example.infinitebook.data.ai.Prompts
import com.example.ui.theme.SaffronAccent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.data.local.IllustrationEntity
import com.example.infinitebook.ui.components.CollapsibleSection
import com.example.infinitebook.ui.components.StudioTopBar
import com.example.infinitebook.ui.viewmodel.BookStudioViewModel
import com.example.infinitebook.ui.viewmodel.StudioScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioObsidian

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterStudioScreen(
    viewModel: BookStudioViewModel
) {
    val book by viewModel.selectedBook.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val selectedChapterNum by viewModel.selectedChapterNum.collectAsState()
    val generationProgress by viewModel.generationProgress.collectAsState()
    val illustrations by viewModel.illustrations.collectAsState()
    val outline by viewModel.outline.collectAsState()
    val duplicateReports by viewModel.duplicateReports.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()
    val auditPassed by viewModel.auditPassed.collectAsState()

    val currentChIllustrations = illustrations.filter { it.chapterNumber == selectedChapterNum }

    val activeChapter = currentChapter ?: chapters.firstOrNull { it.chapterNumber == selectedChapterNum }
    val minWords = book?.minWordsPerChapter ?: 10000
    val currentWords = activeChapter?.wordCount ?: 0
    val progressPct = minOf(1f, currentWords.toFloat() / maxOf(1, minWords))

    Scaffold(
        topBar = {
            StudioTopBar(
                title = book?.title ?: "Chapter Studio",
                subtitle = "Ch. $selectedChapterNum of ${book?.numChapters ?: 35} • ${book?.language}",
                onBackClick = { viewModel.navigateTo(StudioScreen.DASHBOARD) },
                actions = {
                    IconButton(
                        onClick = { viewModel.openFullBookPreview() },
                        modifier = Modifier.testTag("nav_preview_book_btn")
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = "Preview Book", tint = GoldPrimary)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(StudioScreen.CONTINUITY_INSPECTOR) },
                        modifier = Modifier.testTag("nav_continuity_btn")
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = "Continuity Engine", tint = GoldLight)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(StudioScreen.COVER_DESIGNER) },
                        modifier = Modifier.testTag("nav_cover_btn")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Cover Designer", tint = GoldLight)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(StudioScreen.PUBLISHING_EXPORT) },
                        modifier = Modifier.testTag("nav_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "A4 PDF Export", tint = GoldPrimary)
                    }
                }
            )
        },
        containerColor = StudioObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Chapter Selector Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chapters, key = { it.id }) { ch ->
                        val isSelected = ch.chapterNumber == selectedChapterNum
                        val isComplete = ch.wordCount >= minWords

                        Surface(
                            color = if (isSelected) GoldPrimary else StudioCardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoldLight else if (isComplete) EmeraldSuccess else StudioCardBorder
                            ),
                            modifier = Modifier
                                .clickable { viewModel.selectChapter(ch.chapterNumber) }
                                .testTag("select_chapter_${ch.chapterNumber}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isComplete) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isSelected) StudioObsidian else EmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "Ch. ${ch.chapterNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) StudioObsidian else ParchmentWhite
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${ch.wordCount}w",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSelected) StudioObsidian.copy(alpha = 0.8f) else ParchmentMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Word Count & Live Progress Monitor
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeChapter?.title ?: "Chapter $selectedChapterNum",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ParchmentWhite,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                if (!activeChapter?.subtitle.isNullOrBlank()) {
                                    Text(
                                        text = activeChapter?.subtitle ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GoldLight,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = if (Prompts.isMarathi(book?.language)) SaffronAccent.copy(alpha = 0.2f) else StudioCardBorder.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (Prompts.isMarathi(book?.language)) SaffronAccent else StudioCardBorder
                                    ),
                                    modifier = Modifier.testTag("selected_language_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = if (Prompts.isMarathi(book?.language)) SaffronAccent else GoldLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (Prompts.isMarathi(book?.language)) "भाषा: मराठी" else "भाषा: ${book?.language ?: "English"}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (Prompts.isMarathi(book?.language)) SaffronAccent else ParchmentWhite,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = if (currentWords >= minWords) EmeraldSuccess.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (currentWords >= minWords) EmeraldSuccess else AmberAccent
                                )
                            ) {
                                Text(
                                    text = if (currentWords >= minWords) "MIN THRESHOLD MET" else "CONTINUATION REQUIRED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (currentWords >= minWords) EmeraldSuccess else AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressPct },
                            color = GoldPrimary,
                            trackColor = StudioCardBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current: $currentWords words",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            )
                            Text(
                                text = "Target: $minWords – ${book?.maxWordsPerChapter ?: 100000} words (${(progressPct * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ParchmentMuted
                                )
                            )
                        }

                        if (generationProgress.isRunning) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = generationProgress.statusMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // STRICT UNIQUE-CONTENT ENGINE & FULL BOOK GENERATION DESK
            item {
                val completedPages = outline.count { it.status == "COMPLETED" }
                val targetPages = maxOf(1, book?.targetPages ?: 1)
                val outlineProgress = (completedPages.toFloat() / targetPages).coerceIn(0f, 1f)

                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STRICT UNIQUE-CONTENT ENGINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Surface(
                                color = if (auditPassed) EmeraldSuccess.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (auditPassed) EmeraldSuccess else AmberAccent)
                            ) {
                                Text(
                                    text = if (auditPassed) "100% UNIQUE • AUDIT PASSED" else "QUOTA: 450 WDS/PG",
                                    color = if (auditPassed) EmeraldSuccess else AmberAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Master Outline Quota: $completedPages / $targetPages Pages Generated (${(outlineProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentWhite, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { outlineProgress },
                            color = GoldPrimary,
                            trackColor = StudioCardBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startFullBookGeneration() },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("generate_full_book_btn")
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = StudioObsidian)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Generate Full Book",
                                    color = StudioObsidian,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.generateNextPage() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("generate_next_page_btn")
                            ) {
                                Text("+1 Page", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.runDuplicateAudit() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentMuted),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("run_audit_btn")
                            ) {
                                Text("Audit", fontSize = 13.sp)
                            }
                        }

                        if (duplicateReports.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = AmberAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${duplicateReports.size} Duplicate Warnings Detected",
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Duplicate check flagged similar paragraphs or repeated headings. Auto-fix will regenerate with alternative angles at temp 0.9.",
                                        color = ParchmentMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.autoFixDuplicates() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("auto_fix_duplicates_btn")
                                    ) {
                                        Text("Auto-Fix All Duplicates", color = StudioObsidian, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PRIMARY GENERATION & CONTINUATION CONTROLS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "EDITORIAL GENERATION DESK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (generationProgress.isRunning) {
                                Button(
                                    onClick = { viewModel.stopGeneration() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pause_generation_btn")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = StudioObsidian)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause Generation", color = StudioObsidian, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startGenerateChapter(selectedChapterNum) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("generate_chapter_btn")
                                ) {
                                    Icon(
                                        imageVector = if (currentWords == 0) Icons.Default.PlayArrow else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = StudioObsidian
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentWords == 0) "Generate Chapter" else "Continue Chapter",
                                        color = StudioObsidian,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.expandChapter(selectedChapterNum) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("expand_chapter_btn")
                                ) {
                                    Text("Expand")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.regenerateChapter(selectedChapterNum) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentMuted),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("regenerate_chapter_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Prominent Full Book Preview Button
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.openFullBookPreview() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("prominent_preview_book_button")
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = StudioObsidian)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preview Book (Full Page-by-Page Layout)",
                                color = StudioObsidian,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "STYLE & CONTINUITY TUNING ACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ParchmentMuted,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick style actions
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Increase Dialogue",
                                "Increase Historical Depth",
                                "Increase Description",
                                "Fix Continuity",
                                "Add Research",
                                "Add Illustration"
                            ).forEach { action ->
                                Surface(
                                    color = StudioDarkSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                                    modifier = Modifier
                                        .clickable { viewModel.applyStyleAction(action) }
                                        .testTag("style_action_${action.lowercase().replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = "✦ $action",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GoldLight,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chapter Blueprint & Plan Inspector
            item {
                CollapsibleSection(
                    title = "Chapter Blueprint & Thematic Goals",
                    subtitle = activeChapter?.plan?.take(60) ?: "Scene progression and historical goals"
                ) {
                    Text(
                        text = "PLAN: ${activeChapter?.plan ?: "No specific plan initialized."}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ParchmentWhite, lineHeight = 17.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SUMMARY: ${activeChapter?.summary ?: "Initial draft."}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, lineHeight = 17.sp)
                    )
                }
            }

            // Contextual Chapter Illustrations
            if (currentChIllustrations.isNotEmpty()) {
                item {
                    Text(
                        text = "CONTEXTUAL CHAPTER ILLUSTRATIONS & MAPS (${currentChIllustrations.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
                items(currentChIllustrations, key = { it.id }) { ill ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ill.visualType.contains("Map")) Icons.Default.Map else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${ill.figureNumber}: ${ill.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ParchmentWhite
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ill.caption,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ParchmentMuted,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            // Chapter Manuscript Prose
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MANUSCRIPT PROSE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "$currentWords words",
                                style = MaterialTheme.typography.labelSmall.copy(color = ParchmentMuted)
                            )
                        }
                        Divider(
                            color = StudioCardBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        if (activeChapter?.content.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = GoldDark,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Chapter ${selectedChapterNum} is ready to be written.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = ParchmentWhite)
                                    )
                                    Text(
                                        text = "Click 'Generate Chapter' to start composing long-form prose.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = activeChapter?.content ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ParchmentWhite,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 24.sp,
                                    fontSize = 14.5.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
