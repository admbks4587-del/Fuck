package com.example.infinitebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CoverDesignerScreen(
    viewModel: BookStudioViewModel
) {
    val book by viewModel.selectedBook.collectAsState()

    var activeTab by remember { mutableStateOf("FRONT") } // FRONT or BACK
    var customFrontPrompt by remember(book) { mutableStateOf(book?.coverFrontPrompt ?: "") }
    var customBackBlurb by remember(book) {
        mutableStateOf(
            if (!book?.coverBackBlurb.isNullOrBlank()) book!!.coverBackBlurb else "An extraordinary exploration of ${book?.bookTypes}. Conceived through the boundless imagination of ${book?.author} and engineered by InfiniteBook AI, this comprehensive volume delivers expansive world-building, intricate continuity, and unparalleled narrative depth. Written in ${book?.language} for ${book?.readerLevel?.lowercase()} readers, it stands as an enduring monument to long-form storytelling."
        )
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Cover & Spine Designer",
                subtitle = "${book?.title ?: "Cover Studio"} • A4 Standard Ratio",
                onBackClick = { viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO) }
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
                // Front / Back toggle tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton("FRONT COVER", activeTab == "FRONT") { activeTab = "FRONT" }
                    TabButton("BACK COVER & BLURB", activeTab == "BACK") { activeTab = "BACK" }
                }
            }

            // Live A4 Visual Cover Canvas
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeTab == "FRONT") {
                        FrontCoverPreviewCard(
                            title = book?.title ?: "Untitled Masterpiece",
                            subtitle = book?.subtitle ?: "An Archival Publication",
                            author = book?.author ?: "Author",
                            genre = book?.bookTypes ?: "Fiction",
                            language = book?.language ?: "English"
                        )
                    } else {
                        BackCoverPreviewCard(
                            title = book?.title ?: "Untitled Masterpiece",
                            blurb = customBackBlurb,
                            author = book?.author ?: "Author"
                        )
                    }
                }
            }

            // Controls
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (activeTab == "FRONT") "FRONT COVER ART DIRECTIVES" else "BACK COVER SYNOPSIS & PRAISE BLURB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeTab == "FRONT") {
                            OutlinedTextField(
                                value = customFrontPrompt,
                                onValueChange = { customFrontPrompt = it },
                                label = { Text("Cover Visual Identity Prompt", color = GoldLight) },
                                placeholder = { Text("e.g. Ornate gold celestial astrolabe, mountain fortresses, dark leatherette texture...", color = ParchmentMuted) },
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = customBackBlurb,
                                onValueChange = { customBackBlurb = it },
                                label = { Text("Back Cover Synopsis & Endorsement Blurb", color = GoldLight) },
                                minLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ParchmentWhite,
                                    unfocusedTextColor = ParchmentWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.updateCoverDetails(customFrontPrompt, customBackBlurb)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_cover_details_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = StudioObsidian)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Cover Settings", color = StudioObsidian, fontWeight = FontWeight.Bold)
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
private fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) GoldPrimary else StudioCardBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldLight else StudioCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) StudioObsidian else ParchmentWhite,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FrontCoverPreviewCard(
    title: String,
    subtitle: String,
    author: String,
    genre: String,
    language: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C111D)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary),
        modifier = Modifier
            .width(260.dp)
            .aspectRatio(1f / 1.414f) // Standard A4 ratio
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.dp, GoldDark, RoundedCornerShape(4.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top badge & genre
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "★ INFINITEBOOK AI MASTERWORK ★",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontSize = 6.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = genre.uppercase().replace(",", " • ").take(30),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ParchmentMuted,
                            fontSize = 7.sp
                        )
                    )
                }

                // Center Emblem & Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldPrimary,
                                fontSize = 8.5.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Author & Edition Bottom
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WRITTEN BY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ParchmentMuted,
                            fontSize = 6.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = author.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite,
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "A4 ARCHIVAL EDITION • $language",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldDark,
                            fontSize = 5.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BackCoverPreviewCard(
    title: String,
    blurb: String,
    author: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C111D)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary),
        modifier = Modifier
            .width(260.dp)
            .aspectRatio(1f / 1.414f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.dp, GoldDark, RoundedCornerShape(4.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 0.5.sp
                    )
                )

                Text(
                    text = blurb.take(300),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ParchmentWhite.copy(alpha = 0.9f),
                        fontSize = 7.5.sp,
                        lineHeight = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\"A monumental achievement in literary world-building.\"",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 7.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Mock Barcode
                    Surface(
                        color = Color.White,
                        modifier = Modifier
                            .width(80.dp)
                            .height(22.dp)
                    ) {
                        Text(
                            text = "||| | ||| || |||",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "ISBN 978-0-998877-01-4",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ParchmentMuted,
                            fontSize = 6.sp
                        )
                    )
                }
            }
        }
    }
}
