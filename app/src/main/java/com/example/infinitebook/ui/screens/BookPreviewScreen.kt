package com.example.infinitebook.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.ui.viewmodel.BookStudioViewModel
import com.example.infinitebook.ui.viewmodel.StudioScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPreviewScreen(
    viewModel: BookStudioViewModel
) {
    val context = LocalContext.current
    val book by viewModel.selectedBook.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val previewPageIndex by viewModel.previewPageIndex.collectAsState()
    val previewTotalPages by viewModel.previewTotalPages.collectAsState()
    val previewZoom by viewModel.previewZoom.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val chapterStartPages by viewModel.chapterStartPages.collectAsState()
    val isPreviewLoading by viewModel.isPreviewLoading.collectAsState()
    val exportedPdfFile by viewModel.exportedPdfFile.collectAsState()
    val pdfExportStatus by viewModel.pdfExportStatus.collectAsState()
    val pdfProgressPct by viewModel.pdfProgressPct.collectAsState()
    val downloadSuccessMessage by viewModel.downloadSuccessMessage.collectAsState()
    val saveProjectMessage by viewModel.saveProjectMessage.collectAsState()

    var showChapterJumpDialog by remember { mutableStateOf(false) }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Full Book Layout Preview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ParchmentWhite,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Page ${previewPageIndex + 1} of $previewTotalPages • ${book?.title ?: "Manuscript"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldPrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO) },
                        modifier = Modifier.testTag("preview_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Studio",
                            tint = ParchmentWhite
                        )
                    }
                },
                actions = {
                    // Zoom Out
                    IconButton(
                        onClick = { viewModel.setPreviewZoom(previewZoom - 0.25f) },
                        enabled = previewZoom > 0.75f,
                        modifier = Modifier.testTag("zoom_out_button")
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = GoldLight)
                    }

                    // Zoom indicator / reset
                    Surface(
                        color = StudioCardBg,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { viewModel.setPreviewZoom(1.0f) }
                    ) {
                        Text(
                            text = "${(previewZoom * 100).toInt()}%",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Zoom In
                    IconButton(
                        onClick = { viewModel.setPreviewZoom(previewZoom + 0.25f) },
                        enabled = previewZoom < 2.5f,
                        modifier = Modifier.testTag("zoom_in_button")
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = GoldLight)
                    }

                    // Chapter Jump
                    IconButton(
                        onClick = { showChapterJumpDialog = true },
                        modifier = Modifier.testTag("jump_chapter_button")
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Jump to Chapter", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioDarkSurface
                )
            )
        },
        bottomBar = {
            // Persistent Page Navigation & Action Desk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioDarkSurface)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Page slider and stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousPreviewPage() },
                        enabled = previewPageIndex > 0 && !isPreviewLoading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (previewPageIndex > 0) GoldPrimary else StudioCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("prev_page_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Page")
                    }

                    Surface(
                        color = StudioCardBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Page ${previewPageIndex + 1} of $previewTotalPages",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.nextPreviewPage() },
                        enabled = previewPageIndex < previewTotalPages - 1 && !isPreviewLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("next_page_button")
                    ) {
                        Text("Next Page", color = StudioObsidian, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = StudioObsidian, modifier = Modifier.size(16.dp))
                    }
                }

                if (previewTotalPages > 1) {
                    Slider(
                        value = previewPageIndex.toFloat(),
                        onValueChange = { viewModel.setPreviewPage(it.toInt()) },
                        valueRange = 0f..(previewTotalPages - 1).toFloat(),
                        steps = maxOf(0, previewTotalPages - 2),
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = StudioCardBorder
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // Action Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Generate Final PDF / Regenerate PDF
                    Button(
                        onClick = { viewModel.openFullBookPreview(forceRegenerate = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_generate_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StudioObsidian, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (exportedPdfFile != null) "Regenerate PDF" else "Generate Final PDF",
                            color = StudioObsidian,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }

                    // Download PDF button
                    Button(
                        onClick = {
                            val success = viewModel.downloadPdfToDevice()
                            if (exportedPdfFile != null) {
                                // Also trigger share intent for immediate access
                                val uri = viewModel.getShareablePdfUri(exportedPdfFile!!)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Open or Save A4 Book PDF"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_download_pdf_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = StudioObsidian, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF", color = StudioObsidian, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                // Secondary actions: Edit & Regenerate, Preview Again, Save Project
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_edit_regenerate_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit & Regenerate", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openFullBookPreview(forceRegenerate = false) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview Again", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveProject() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_save_project_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Project", fontSize = 11.sp)
                    }
                }
            }
        },
        containerColor = StudioObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioObsidian)
        ) {
            // Notification Banners (Download success or project saved)
            if (downloadSuccessMessage != null) {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearDownloadMessage() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = downloadSuccessMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (saveProjectMessage != null) {
                Surface(
                    color = GoldDark.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveProjectMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (pdfProgressPct in 1..99) {
                LinearProgressIndicator(
                    progress = { pdfProgressPct / 100f },
                    color = GoldPrimary,
                    trackColor = StudioCardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }

            // Main A4 Page Display Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPreviewLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = pdfExportStatus ?: "Rendering high-fidelity A4 book layout...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GoldLight)
                        )
                        Text(
                            text = "Typesetting typography, chapters, and illustrations...",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted)
                        )
                    }
                } else if (previewBitmap != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier
                            .shadow(12.dp, RoundedCornerShape(4.dp))
                            .testTag("preview_rendered_page_image")
                    ) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Page ${previewPageIndex + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Fallback to initiating preview
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Manuscript Ready for Preview",
                            style = MaterialTheme.typography.titleMedium.copy(color = ParchmentWhite, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Compile the complete A4 publication to inspect every page.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.openFullBookPreview(forceRegenerate = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Compile & Preview Book", color = StudioObsidian, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Jump to Chapter Dialog
    if (showChapterJumpDialog) {
        AlertDialog(
            onDismissRequest = { showChapterJumpDialog = false },
            containerColor = StudioDarkSurface,
            title = {
                Text(
                    text = "Jump to Chapter in Preview",
                    color = ParchmentWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(chapters, key = { it.id }) { ch ->
                        val startPage = chapterStartPages[ch.chapterNumber] ?: (ch.chapterNumber * 3)
                        Surface(
                            color = StudioCardBg,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.jumpToChapterPreview(ch.chapterNumber)
                                    showChapterJumpDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Chapter ${ch.chapterNumber}: ${ch.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ParchmentWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "${ch.wordCount} words",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ParchmentMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Surface(
                                    color = GoldDark.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Page $startPage",
                                        color = GoldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChapterJumpDialog = false }) {
                    Text("Close", color = GoldPrimary)
                }
            }
        )
    }
}
