package com.example.infinitebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.data.local.BookEntity
import com.example.infinitebook.ui.components.MetricChip
import com.example.infinitebook.ui.components.StudioTopBar
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

@Composable
fun StudioDashboardScreen(
    viewModel: BookStudioViewModel
) {
    val books by viewModel.books.collectAsState()

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "InfiniteBook AI",
                subtitle = "Long-Form Publishing Studio"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(StudioScreen.NEW_BOOK) },
                containerColor = GoldPrimary,
                contentColor = StudioObsidian,
                modifier = Modifier.testTag("create_new_book_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Book")
            }
        },
        containerColor = StudioObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Studio Master Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Editorial Publishing Desk",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ParchmentWhite,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                Text(
                                    text = "Infinite Chapters • 10,000+ Words/Ch • Multilingual • A4 PDF",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Create books of any scale, from 35-chapter epics to 1,000-page historical archives. Every chapter is sustained with deep continuity, contextual cartography, and publishing-grade typography.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ParchmentMuted,
                                lineHeight = 19.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(StudioScreen.NEW_BOOK) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_new_publication_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null,
                                tint = StudioObsidian,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start New Book Publication",
                                color = StudioObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANUSCRIPT PORTFOLIO (${books.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            if (books.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Publications Yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ParchmentWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Paste your complete book idea, characters, research notes, and length requirements to begin.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ParchmentMuted,
                                    lineHeight = 18.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.navigateTo(StudioScreen.NEW_BOOK) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Create First Book", color = StudioObsidian, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(books, key = { it.id }) { book ->
                    BookPortfolioCard(
                        book = book,
                        onOpen = { viewModel.selectBook(book.id) },
                        onExport = {
                            viewModel.selectBook(book.id)
                            viewModel.navigateTo(StudioScreen.PUBLISHING_EXPORT)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookPortfolioCard(
    book: BookEntity,
    onOpen: () -> Unit,
    onExport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("book_card_${book.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    if (book.subtitle.isNotBlank()) {
                        Text(
                            text = book.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldLight,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = "By ${book.author}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ParchmentMuted,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    color = if (book.status == "COMPLETED") EmeraldSuccess.copy(alpha = 0.2f) else GoldDark.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (book.status == "COMPLETED") EmeraldSuccess else GoldPrimary
                    )
                ) {
                    Text(
                        text = book.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (book.status == "COMPLETED") EmeraldSuccess else GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dashboard Specifications Grid
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip("Type", book.bookTypes.split(",").firstOrNull()?.trim() ?: "Book")
                MetricChip("Language", "${book.language} (${book.languageMode})")
                MetricChip("Reader Level", book.readerLevel)
                MetricChip("Chapters", "${book.numChapters}")
                MetricChip("Target Pages", "${book.targetPages} pp")
                MetricChip("Min Words/Ch", "${book.minWordsPerChapter}")
                MetricChip("Max Words/Ch", "${book.maxWordsPerChapter}")
                MetricChip("Illustrations", book.illustrationFrequency)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_chapter_studio_btn_${book.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = StudioObsidian,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Chapter Studio", color = StudioObsidian, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExport,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_pdf_btn_${book.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A4 PDF")
                }
            }
        }
    }
}
