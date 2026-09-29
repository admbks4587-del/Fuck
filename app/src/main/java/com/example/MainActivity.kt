package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infinitebook.ui.screens.BookPreviewScreen
import com.example.infinitebook.ui.screens.ChapterStudioScreen
import com.example.infinitebook.ui.screens.ContinuityInspectorScreen
import com.example.infinitebook.ui.screens.CoverDesignerScreen
import com.example.infinitebook.ui.screens.NewBookSetupScreen
import com.example.infinitebook.ui.screens.PublishingExportScreen
import com.example.infinitebook.ui.screens.StudioDashboardScreen
import com.example.infinitebook.ui.viewmodel.BookStudioViewModel
import com.example.infinitebook.ui.viewmodel.StudioScreen
import com.example.ui.theme.InfiniteBookTheme
import com.example.ui.theme.StudioObsidian

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InfiniteBookTheme {
                val viewModel: BookStudioViewModel = viewModel()
                InfiniteBookApp(viewModel)
            }
        }
    }
}

@Composable
fun InfiniteBookApp(viewModel: BookStudioViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button smoothly
    BackHandler(enabled = currentScreen != StudioScreen.DASHBOARD) {
        when (currentScreen) {
            StudioScreen.NEW_BOOK -> viewModel.navigateTo(StudioScreen.DASHBOARD)
            StudioScreen.CHAPTER_STUDIO -> viewModel.navigateTo(StudioScreen.DASHBOARD)
            StudioScreen.CONTINUITY_INSPECTOR -> viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO)
            StudioScreen.PUBLISHING_EXPORT -> viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO)
            StudioScreen.COVER_DESIGNER -> viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO)
            StudioScreen.BOOK_PREVIEW -> viewModel.navigateTo(StudioScreen.CHAPTER_STUDIO)
            StudioScreen.DASHBOARD -> { /* Default behavior */ }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = StudioObsidian
    ) {
        when (currentScreen) {
            StudioScreen.DASHBOARD -> StudioDashboardScreen(viewModel)
            StudioScreen.NEW_BOOK -> NewBookSetupScreen(viewModel)
            StudioScreen.CHAPTER_STUDIO -> ChapterStudioScreen(viewModel)
            StudioScreen.CONTINUITY_INSPECTOR -> ContinuityInspectorScreen(viewModel)
            StudioScreen.PUBLISHING_EXPORT -> PublishingExportScreen(viewModel)
            StudioScreen.COVER_DESIGNER -> CoverDesignerScreen(viewModel)
            StudioScreen.BOOK_PREVIEW -> BookPreviewScreen(viewModel)
        }
    }
}
