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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infinitebook.data.local.ContinuityRecordEntity
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContinuityInspectorScreen(
    viewModel: BookStudioViewModel
) {
    val book by viewModel.selectedBook.collectAsState()
    val records by viewModel.continuityRecords.collectAsState()

    val categories = remember {
        listOf("ALL", "CHARACTER", "LOCATION", "MAP", "ARTIFACT", "SYMBOL", "EVENT", "CHRONOLOGY", "WORLD_RULE", "CLUE", "MYSTERY")
    }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredRecords = records.filter { record ->
        val matchesCategory = (selectedCategory == "ALL" || record.category == selectedCategory)
        val matchesQuery = (searchQuery.isBlank() || record.name.contains(searchQuery, ignoreCase = true) || record.details.contains(searchQuery, ignoreCase = true))
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "Continuity & Lore Engine",
                subtitle = "${book?.title ?: "Manuscript"} • ${records.size} Entities Tracked",
                onBackClick = { viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = StudioObsidian,
                modifier = Modifier.testTag("add_continuity_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entity")
            }
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
                // Banner description
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Extension, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Persistent Manuscript Memory",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ParchmentWhite
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Maintains persistent records for characters, appearances, relationships, locations, maps, artifacts, world rules, and clues so names and lore never wander across chapters.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentMuted, lineHeight = 17.sp)
                        )
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search characters, places, artifacts, rules...", color = ParchmentMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldLight) },
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

            // Category filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            color = if (isSelected) GoldPrimary else StudioCardBg,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoldLight else StudioCardBorder
                            ),
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("cat_filter_${cat.lowercase()}")
                        ) {
                            Text(
                                text = cat.replace('_', ' '),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StudioObsidian else ParchmentWhite,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No continuity records found for this category.\nClick '+' to define a new character, place, or world rule.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ParchmentMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    ContinuityRecordCard(
                        record = record,
                        onDelete = { viewModel.deleteContinuityRecord(record) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddContinuityDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { cat, name, details ->
                viewModel.addContinuityRecord(cat, name, details)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContinuityRecordCard(
    record: ContinuityRecordEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GoldDark.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Text(
                            text = record.category.replace('_', ' '),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite
                        )
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ParchmentMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.details,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ParchmentWhite.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "First Introduced: Ch. ${record.firstIntroducedChapter}  •  Status: ${record.status}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ParchmentMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun AddContinuityDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, name: String, details: String) -> Unit
) {
    val categories = remember {
        listOf("CHARACTER", "LOCATION", "MAP", "ARTIFACT", "SYMBOL", "EVENT", "CHRONOLOGY", "WORLD_RULE", "CLUE", "MYSTERY")
    }
    var selectedCat by remember { mutableStateOf("CHARACTER") }
    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Text("Add Continuity Record", color = ParchmentWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", color = GoldLight, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCat
                        Surface(
                            color = if (isSelected) GoldPrimary else StudioCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier.clickable { selectedCat = cat }
                        ) {
                            Text(
                                text = cat.replace('_', ' '),
                                color = if (isSelected) StudioObsidian else ParchmentWhite,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name / Subject *", color = GoldLight) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentWhite,
                        unfocusedTextColor = ParchmentWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Traits, Lore, Appearance & Relations *", color = GoldLight) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentWhite,
                        unfocusedTextColor = ParchmentWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(selectedCat, name, details)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Save to Memory", color = StudioObsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ParchmentMuted)
            }
        }
    )
}
