package com.example.infinitebook.data.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfPreviewRenderer(private val file: File) : AutoCloseable {

    private var pfd: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    init {
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pfd?.let { descriptor ->
                renderer = PdfRenderer(descriptor)
            }
        } catch (e: Exception) {
            Log.e("PdfPreviewRenderer", "Failed to open PDF renderer", e)
        }
    }

    val pageCount: Int
        get() = renderer?.pageCount ?: 0

    suspend fun renderPage(pageIndex: Int, scale: Float = 1.35f): Bitmap? = withContext(Dispatchers.IO) {
        val r = renderer ?: return@withContext null
        if (pageIndex < 0 || pageIndex >= r.pageCount) return@withContext null

        try {
            val page = r.openPage(pageIndex)
            val w = maxOf(100, (page.width * scale).toInt())
            val h = maxOf(140, (page.height * scale).toInt())
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        } catch (e: Exception) {
            Log.e("PdfPreviewRenderer", "Error rendering page $pageIndex", e)
            null
        }
    }

    override fun close() {
        try {
            renderer?.close()
            pfd?.close()
        } catch (e: Exception) {
            Log.e("PdfPreviewRenderer", "Error closing renderer", e)
        }
    }
}
