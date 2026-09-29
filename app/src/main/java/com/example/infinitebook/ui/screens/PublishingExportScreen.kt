package com.example.infinitebook.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.data.model.IssueSeverity
import com.example.infinitebook.data.model.ManuscriptValidationIssue
import com.example.infinitebook.ui.components.CollapsibleSection
import com.example.infinitebook.ui.components.StudioTopBar
import com.example.infinitebook.ui.viewmodel.BookStudioViewModel
import com.example.infinitebook.ui.viewmodel.StudioScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonAccent
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

@Composable
fun PublishingExportScreen(
    viewModel: BookStudioViewModel
) {
    val context = LocalContext.current
    val book by viewModel.selectedBook.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val illustrations by viewModel.illustrations.collectAsState()
    val validationIssues by viewModel.validationIssues.collectAsState()
    val exportedPdfFile by viewModel.exportedPdfFile.collectAsState()
    val pdfExportStatus by viewModel.pdfExportStatus.collectAsState()
    val pdfProgressPct by viewModel.pdfProgressPct.collectAsState()

    val totalWords = chapters.sumOf { it.wordCount }
    val completedChapters = chapters.count { it.wordCount >= (book?.minWordsPerChapter ?: 10000) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "A4 Publishing & PDF Engine",
                subtitle = "${book?.title ?: "Publication"} • ${chapters.size} Chapters",
                onBackClick = { viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO) }
            )
        },
        containerColor = StudioObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Master PDF Generator Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "A4 Folio Book Publishing Engine",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ParchmentWhite,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                Text(
                                    text = "Full Book Layout • 595 x 842 pt • Unicode Embedded",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Compiles the entire publication in standard archival order: Deluxe Front Cover, Title & Imprint, Copyright, Dedication, Table of Contents, ${chapters.size} chapters with contextual illustrations, Glossary, and Deluxe Back Cover.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, lineHeight = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (pdfProgressPct in 1..99) {
                            LinearProgressIndicator(
                                progress = { pdfProgressPct / 100f },
                                color = GoldPrimary,
                                trackColor = StudioCardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pdfExportStatus ?: "Compiling PDF...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GoldLight,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.openFullBookPreview() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("prominent_preview_book_button_export")
                                ) {
                                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = StudioObsidian)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Preview Book (Page-by-Page Layout)",
                                        color = StudioObsidian,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.exportFinalPdf() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("generate_final_pdf_button")
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldLight)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Generate Final A4 PDF Manuscript",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }

                        if (exportedPdfFile != null && exportedPdfFile!!.exists()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PDF Generated Successfully!",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${exportedPdfFile!!.name} (${exportedPdfFile!!.length() / 1024} KB)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ParchmentWhite)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.downloadPdfToDevice()
                                                val uri = viewModel.getShareablePdfUri(exportedPdfFile!!)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Download or Open A4 Book PDF"))
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("download_share_pdf_btn")
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = StudioObsidian)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download PDF",
                                                color = StudioObsidian,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.openFullBookPreview() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("preview_after_export_btn")
                                        ) {
                                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = GoldLight)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Preview Again")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quality Control & Manuscript Validator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, tint = GoldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MANUSCRIPT QUALITY AUDIT",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "${validationIssues.size} Issues",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (validationIssues.isEmpty()) EmeraldSuccess else AmberAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Automated verification of minimum word counts, character continuity, language fidelity, and visual placements prior to publication.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, fontSize = 11.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (validationIssues.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All publication quality checks passed! Ready for A4 compilation.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = EmeraldSuccess)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                validationIssues.take(6).forEach { issue ->
                                    ValidationIssueRow(
                                        issue = issue,
                                        onAutoFix = { viewModel.autoFixIssue(issue) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Architectural Publishing Structure
            item {
                CollapsibleSection(
                    title = "Archival Publishing Structure (23 Sections)",
                    subtitle = "Standard publication layout order applied in PDF",
                    defaultExpanded = false
                ) {
                    val sections = listOf(
                        "1. Front Cover (Deluxe obsidian gold typography & crest)",
                        "2. Title Page (Formal imprint & metadata)",
                        "3. Subtitle Page",
                        "4. Copyright / Publication Page (Cataloging data)",
                        "5. Dedication",
                        "6. Epigraph",
                        "7. Foreword / Preface",
                        "8. Table of Contents / अनुक्रमणिका (Page references)",
                        "9. List of Illustrations & Cartography",
                        "10. Main Manuscript Chapters (${chapters.size} chapters)",
                        "11. Contextual Maps, Architectural Plates & Diagrams (${illustrations.size} plates)",
                        "12. Epilogue / Conclusion",
                        "13. Afterword",
                        "14. Glossary & Terms",
                        "15. Timeline & Chronology",
                        "16. Character Index & Lineage",
                        "17. Place & Realm Index",
                        "18. Subject Index",
                        "19. Research Notes & References",
                        "20. Acknowledgements",
                        "21. About the Author",
                        "22. About the Book & Press Note",
                        "23. Back Cover (Blurb, Quotes & ISBN Barcode)"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        sections.forEach { s ->
                            Text(
                                text = s,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ParchmentWhite,
                                    fontSize = 11.5.sp
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

@Composable
fun ValidationIssueRow(
    issue: ManuscriptValidationIssue,
    onAutoFix: () -> Unit
) {
    val icon = when (issue.severity) {
        IssueSeverity.ERROR -> Icons.Default.ErrorOutline
        IssueSeverity.WARNING -> Icons.Default.Warning
        IssueSeverity.INFO -> Icons.Default.Info
    }
    val color = when (issue.severity) {
        IssueSeverity.ERROR -> CrimsonAccent
        IssueSeverity.WARNING -> AmberAccent
        IssueSeverity.INFO -> GoldLight
    }

    Surface(
        color = StudioDarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite
                        )
                    )
                    Text(
                        text = issue.message,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ParchmentMuted,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            if (issue.fixAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAutoFix,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Auto-Fix", color = StudioObsidian, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
