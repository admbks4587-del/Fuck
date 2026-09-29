package com.example.infinitebook.data.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.infinitebook.data.local.BookEntity
import com.example.infinitebook.data.local.ChapterEntity
import com.example.infinitebook.data.local.ContinuityRecordEntity
import com.example.infinitebook.data.local.IllustrationEntity
import com.example.infinitebook.data.model.BackMatterConfig
import com.example.infinitebook.data.model.FrontMatterConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookPdfExporter(private val context: Context) {

    var lastChapterPageMap: Map<Int, Int> = emptyMap()
        private set

    // Standard A4 dimensions in typographic points (72 points per inch)
    // 8.27 in x 11.69 in = ~595 x 842 points
    private val pageWidth = 595
    private val pageHeight = 842
    private val marginHorizontal = 54 // 0.75 in
    private val marginTop = 58
    private val marginBottom = 58
    private val contentWidth = pageWidth - (marginHorizontal * 2)

    suspend fun exportBookToPdf(
        book: BookEntity,
        chapters: List<ChapterEntity>,
        continuityRecords: List<ContinuityRecordEntity>,
        illustrations: List<IllustrationEntity>,
        frontMatter: FrontMatterConfig = FrontMatterConfig(),
        backMatter: BackMatterConfig = BackMatterConfig(),
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): File = withContext(Dispatchers.IO) {
        val pdfDoc = PdfDocument()
        var pageNumber = 1
        val chapterPageMap = mutableMapOf<Int, Int>()

        try {
            onProgress(5, "Rendering Deluxe Front Cover...")
            // Page 1: Deluxe Front Cover
            if (frontMatter.includeFrontCover) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderFrontCover(page.canvas, book)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            onProgress(15, "Typesetting Front Matter & Title Pages...")
            // Page 2: Title & Imprint Page
            if (frontMatter.includeTitlePage) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderTitlePage(page.canvas, book)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            // Page 3: Copyright Page
            if (frontMatter.includeCopyrightPage) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderCopyrightPage(page.canvas, book)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            // Page 4: Dedication & Epigraph
            if (frontMatter.includeDedication || frontMatter.includeEpigraph) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderDedicationPage(page.canvas, book, frontMatter)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            // Estimate/pre-calculate chapter start pages for accurate Table of Contents
            val tocPageStart = pageNumber
            val tocPagesNeeded = maxOf(1, (chapters.size + 15) / 25)
            val firstChapterPageStart = tocPageStart + tocPagesNeeded

            var simulatedChapterPage = firstChapterPageStart
            for (ch in chapters) {
                chapterPageMap[ch.chapterNumber] = simulatedChapterPage
                val words = maxOf(1, countWords(ch.content))
                // Rough estimation of pages per chapter for TOC: ~420 words per A4 book page + 1 for chapter opener + visuals
                val pagesInChapter = maxOf(1, (words / 420) + 1 + (if (book.illustrationFrequency != "None") 1 else 0))
                simulatedChapterPage += pagesInChapter
            }

            onProgress(25, "Generating Table of Contents & Illustration Index...")
            // Table of Contents page(s)
            if (frontMatter.includeTableOfContents) {
                for (tp in 0 until tocPagesNeeded) {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val page = pdfDoc.startPage(pageInfo)
                    renderTableOfContentsPage(
                        page.canvas,
                        book,
                        chapters,
                        chapterPageMap,
                        pageNumber,
                        tp,
                        tocPagesNeeded,
                        illustrations
                    )
                    pdfDoc.finishPage(page)
                    pageNumber++
                }
            }

            // Render Chapters
            val totalChapters = chapters.size
            for ((idx, chapter) in chapters.withIndex()) {
                val pct = 30 + ((idx.toFloat() / maxOf(1, totalChapters)) * 50).toInt()
                onProgress(pct, "Typesetting Chapter ${chapter.chapterNumber} of $totalChapters: ${chapter.title}...")

                // Actual chapter start page
                chapterPageMap[chapter.chapterNumber] = pageNumber
                val chapterIllustrations = illustrations.filter { it.chapterNumber == chapter.chapterNumber }

                pageNumber = renderChapterManuscript(
                    pdfDoc = pdfDoc,
                    startPageNum = pageNumber,
                    book = book,
                    chapter = chapter,
                    illustrations = chapterIllustrations
                )
            }

            onProgress(85, "Compiling Back Matter, Glossary & Indexes...")
            // Back Matter: Glossary & Index
            if (backMatter.includeGlossary || backMatter.includeTimeline || continuityRecords.isNotEmpty()) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderGlossaryAndContinuityIndex(page.canvas, book, continuityRecords, pageNumber)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            // About Author & Publication
            if (backMatter.includeAboutAuthor || backMatter.includeAcknowledgements) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderAuthorAndAboutBook(page.canvas, book, backMatter, pageNumber)
                pdfDoc.finishPage(page)
                pageNumber++
            }

            onProgress(95, "Crafting Deluxe Back Cover & Imprint...")
            // Deluxe Back Cover
            if (backMatter.includeBackCover) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                renderBackCover(page.canvas, book)
                pdfDoc.finishPage(page)
            }

            onProgress(98, "Writing PDF file to storage...")
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = book.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val outFile = File(exportDir, "${cleanTitle}_InfiniteBook_A4.pdf")
            FileOutputStream(outFile).use { fos ->
                pdfDoc.writeTo(fos)
            }
            lastChapterPageMap = chapterPageMap.toMap()
            onProgress(100, "Publication PDF complete!")
            return@withContext outFile
        } finally {
            pdfDoc.close()
        }
    }

    private fun renderFrontCover(canvas: Canvas, book: BookEntity) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0C111D") // Deep obsidian navy
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Ornate outer and inner border in gold
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, (pageWidth - 24).toFloat(), (pageHeight - 24).toFloat(), goldBorderPaint)

        val thinBorderPaint = Paint().apply {
            color = Color.parseColor("#8C7322")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRect(30f, 30f, (pageWidth - 30).toFloat(), (pageHeight - 30).toFloat(), thinBorderPaint)

        // Decorative corner accents
        drawCornerOrnaments(canvas, 30f, 30f, (pageWidth - 30).toFloat(), (pageHeight - 30).toFloat())

        // Top Publishing Badge
        val badgePaint = Paint().apply {
            color = Color.parseColor("#E5C07B")
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.25f
        }
        canvas.drawText("★  INFINITEBOOK AI MASTER PUBLICATION  ★", (pageWidth / 2).toFloat(), 75f, badgePaint)

        // Book Type / Genre
        val genrePaint = Paint().apply {
            color = Color.parseColor("#98A2B3")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        val genreText = book.bookTypes.uppercase().replace(",", "  • ")
        canvas.drawText(genreText.take(45), (pageWidth / 2).toFloat(), 120f, genrePaint)

        // Decorative gold crest / emblem in center-upper area
        drawCoverEmblem(canvas, (pageWidth / 2).toFloat(), 210f)

        // Book Title (multi-line wrapped with high elegance)
        val titlePaint = Paint().apply {
            color = Color.parseColor("#F5F2EB")
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var currentY = 320f
        val titleLines = wrapText(book.title, titlePaint, contentWidth - 40)
        for (line in titleLines) {
            canvas.drawText(line, (pageWidth / 2).toFloat(), currentY, titlePaint)
            currentY += 34f
        }

        // Subtitle if available
        if (book.subtitle.isNotBlank()) {
            currentY += 8f
            val subPaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 13f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val subLines = wrapText(book.subtitle, subPaint, contentWidth - 60)
            for (line in subLines.take(2)) {
                canvas.drawText(line, (pageWidth / 2).toFloat(), currentY, subPaint)
                currentY += 18f
            }
        }

        // Ornamental gold divider line
        val divPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val cx = (pageWidth / 2).toFloat()
        canvas.drawLine(cx - 70, currentY + 16, cx + 70, currentY + 16, divPaint)
        canvas.drawCircle(cx, currentY + 16, 4f, Paint().apply { color = Color.parseColor("#D4AF37"); style = Paint.Style.FILL; isAntiAlias = true })

        // Language & Volume Spec
        val langPaint = Paint().apply {
            color = Color.parseColor("#667085")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        canvas.drawText("LANGUAGE: ${book.language.uppercase()}  |  VOL. I  |  COMPLETE EDITION", cx, 660f, langPaint)

        // Author Name
        val authorLabelPaint = Paint().apply {
            color = Color.parseColor("#98A2B3")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.2f
        }
        canvas.drawText("WRITTEN & COMPILED BY", cx, 715f, authorLabelPaint)

        val authorPaint = Paint().apply {
            color = Color.parseColor("#F5F2EB")
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText(book.author.uppercase(), cx, 740f, authorPaint)

        // Bottom Press Note
        val pressPaint = Paint().apply {
            color = Color.parseColor("#5A6578")
            textSize = 8f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        canvas.drawText("PUBLISHED BY INFINITEBOOK AI PRESS  •  A4 PRESERVATION FOLIO", cx, 790f, pressPaint)
    }

    private fun renderTitlePage(canvas: Canvas, book: BookEntity) {
        val cx = (pageWidth / 2).toFloat()

        val titlePaint = Paint().apply {
            color = Color.parseColor("#101828")
            textSize = 26f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var cy = 200f
        val lines = wrapText(book.title, titlePaint, contentWidth - 40)
        for (l in lines) {
            canvas.drawText(l, cx, cy, titlePaint)
            cy += 32f
        }

        if (book.subtitle.isNotBlank()) {
            cy += 10f
            val subPaint = Paint().apply {
                color = Color.parseColor("#475467")
                textSize = 13f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            for (l in wrapText(book.subtitle, subPaint, contentWidth - 60)) {
                canvas.drawText(l, cx, cy, subPaint)
                cy += 18f
            }
        }

        // Divider
        val linePaint = Paint().apply {
            color = Color.parseColor("#98A2B3")
            strokeWidth = 1f
        }
        canvas.drawLine(cx - 50, cy + 25, cx + 50, cy + 25, linePaint)

        val authorPaint = Paint().apply {
            color = Color.parseColor("#1D2939")
            textSize = 16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("By ${book.author}", cx, cy + 60, authorPaint)

        // Metadata specs at bottom
        val metaPaint = Paint().apply {
            color = Color.parseColor("#475467")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Original Concept & Prompt Source Edition", cx, 660f, metaPaint)
        canvas.drawText("Reader Depth Classification: ${book.readerLevel}", cx, 680f, metaPaint)
        canvas.drawText("Primary Language: ${book.language} (${book.languageMode})", cx, 700f, metaPaint)

        val imprintPaint = Paint().apply {
            color = Color.parseColor("#1D2939")
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        canvas.drawText("INFINITEBOOK AI PUBLISHING HOUSE", cx, 760f, imprintPaint)
    }

    private fun renderCopyrightPage(canvas: Canvas, book: BookEntity) {
        val paint = Paint().apply {
            color = Color.parseColor("#344054")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val textLines = listOf(
            "InfiniteBook AI Long-Form Edition",
            "Copyright © $year by ${book.author} & InfiniteBook AI Studio.",
            "All rights reserved under International and Pan-American Copyright Conventions.",
            "",
            "This book is published as an authentic long-form literary and documentary folio.",
            "No part of this publication may be reproduced, distributed, or transmitted in any form",
            "or by any means, including photocopying, recording, or other electronic or mechanical",
            "methods, without prior written permission of the publisher.",
            "",
            "Cataloging-in-Publication Data:",
            "Main Entry Under Title: ${book.title}",
            "Subjects / Genre: ${book.bookTypes}",
            "Target Reader Demographic: ${book.readerLevel}",
            "Primary Language: ${book.language}",
            "Standard Format: A4 Folio (595 x 842 pt)",
            "",
            "Printed and Bound via InfiniteBook AI Automated Typesetting Engine.",
            "First Edition: $year",
            "Manufactured in Global Digital Distribution."
        )

        var y = 480f
        for (line in textLines) {
            canvas.drawText(line, marginHorizontal.toFloat(), y, paint)
            y += 14f
        }
    }

    private fun renderDedicationPage(
        canvas: Canvas,
        book: BookEntity,
        frontMatter: FrontMatterConfig
    ) {
        val cx = (pageWidth / 2).toFloat()
        var y = 280f

        if (frontMatter.includeDedication && frontMatter.dedicationText.isNotBlank()) {
            val dedLabelPaint = Paint().apply {
                color = Color.parseColor("#98A2B3")
                textSize = 10f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                letterSpacing = 0.2f
            }
            canvas.drawText("DEDICATION", cx, y, dedLabelPaint)
            y += 30f

            val dedTextPaint = Paint().apply {
                color = Color.parseColor("#1D2939")
                textSize = 12f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val wrapped = wrapText(frontMatter.dedicationText, dedTextPaint, contentWidth - 100)
            for (line in wrapped) {
                canvas.drawText(line, cx, y, dedTextPaint)
                y += 20f
            }
        }

        if (frontMatter.includeEpigraph && frontMatter.epigraphQuote.isNotBlank()) {
            y += 80f
            val epigraphPaint = Paint().apply {
                color = Color.parseColor("#344054")
                textSize = 11f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val wrapped = wrapText(frontMatter.epigraphQuote, epigraphPaint, contentWidth - 120)
            for (line in wrapped) {
                canvas.drawText(line, cx, y, epigraphPaint)
                y += 18f
            }
        }
    }

    private fun renderTableOfContentsPage(
        canvas: Canvas,
        book: BookEntity,
        chapters: List<ChapterEntity>,
        chapterPageMap: Map<Int, Int>,
        currentPage: Int,
        tocIndex: Int,
        totalTocPages: Int,
        illustrations: List<IllustrationEntity>
    ) {
        val headerTitle = if (book.language in listOf("Marathi", "Hindi", "Sanskrit")) {
            "अनुक्रमणिका (Table of Contents)"
        } else {
            "TABLE OF CONTENTS"
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#101828")
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        canvas.drawText(headerTitle, (pageWidth / 2).toFloat(), 85f, titlePaint)

        val divPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 1f
        }
        canvas.drawLine(marginHorizontal.toFloat(), 105f, (pageWidth - marginHorizontal).toFloat(), 105f, divPaint)

        var y = 135f
        val itemPaint = Paint().apply {
            color = Color.parseColor("#1D2939")
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val pageNumPaint = Paint().apply {
            color = Color.parseColor("#344054")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val dotPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
        }

        val startIdx = tocIndex * 24
        val pageChapters = chapters.drop(startIdx).take(24)

        for (ch in pageChapters) {
            val titleStr = "Chapter ${ch.chapterNumber}: ${ch.title}".take(48)
            val pNum = chapterPageMap[ch.chapterNumber] ?: (currentPage + 1)

            canvas.drawText(titleStr, marginHorizontal.toFloat(), y, itemPaint)
            val textWidth = itemPaint.measureText(titleStr)
            val rightMargin = (pageWidth - marginHorizontal).toFloat()

            // Dotted leader line
            canvas.drawLine(marginHorizontal + textWidth + 8, y - 3, rightMargin - 28, y - 3, dotPaint)
            canvas.drawText(pNum.toString(), rightMargin, y, pageNumPaint)
            y += 24f
        }

        // Illustrations list on final TOC page if space permits
        if (tocIndex == totalTocPages - 1 && illustrations.isNotEmpty() && y < 650f) {
            y += 20f
            val illTitlePaint = Paint().apply {
                color = Color.parseColor("#101828")
                textSize = 12f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("LIST OF ILLUSTRATIONS & CARTOGRAPHY", marginHorizontal.toFloat(), y, illTitlePaint)
            y += 20f

            val illItemPaint = Paint().apply {
                color = Color.parseColor("#475467")
                textSize = 9f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                isAntiAlias = true
            }

            for (ill in illustrations.take(6)) {
                val illDesc = "${ill.figureNumber}: ${ill.title}".take(52)
                canvas.drawText(illDesc, marginHorizontal.toFloat(), y, illItemPaint)
                val illWidth = illItemPaint.measureText(illDesc)
                val rightMargin = (pageWidth - marginHorizontal).toFloat()
                canvas.drawLine(marginHorizontal + illWidth + 8, y - 3, rightMargin - 28, y - 3, dotPaint)
                val illPage = chapterPageMap[ill.chapterNumber] ?: 1
                canvas.drawText(illPage.toString(), rightMargin, y, pageNumPaint)
                y += 18f
            }
        }

        // Running page footer
        renderRunningFooter(canvas, currentPage)
    }

    private fun renderChapterManuscript(
        pdfDoc: PdfDocument,
        startPageNum: Int,
        book: BookEntity,
        chapter: ChapterEntity,
        illustrations: List<IllustrationEntity>
    ): Int {
        var pageNum = startPageNum
        val textPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paragraphs = if (chapter.content.isNotBlank()) {
            chapter.content.split("\n\n").filter { it.isNotBlank() }
        } else {
            listOf("Manuscript for Chapter ${chapter.chapterNumber} is currently pending generation in the InfiniteBook AI studio.")
        }

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Running header
        renderRunningHeader(canvas, book, chapter.title)

        // Chapter Opener Banner
        val chapterNumPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
            isAntiAlias = true
        }
        canvas.drawText("CHAPTER ${chapter.chapterNumber}".uppercase(), (pageWidth / 2).toFloat(), 95f, chapterNumPaint)

        val chTitlePaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        var chY = 125f
        for (l in wrapText(chapter.title, chTitlePaint, contentWidth - 40)) {
            canvas.drawText(l, (pageWidth / 2).toFloat(), chY, chTitlePaint)
            chY += 24f
        }

        if (chapter.subtitle.isNotBlank()) {
            val subPaint = Paint().apply {
                color = Color.parseColor("#6B7280")
                textSize = 10f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(chapter.subtitle, (pageWidth / 2).toFloat(), chY, subPaint)
            chY += 18f
        }

        // Chapter decorative divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 1f
        }
        val cx = (pageWidth / 2).toFloat()
        canvas.drawLine(cx - 35, chY + 8, cx + 35, chY + 8, divPaint)

        var y = chY + 36f
        var illustrationInserted = false

        for ((pIndex, paragraph) in paragraphs.withIndex()) {
            val lines = wrapText(paragraph.trim(), textPaint, contentWidth)

            // Check if we need to insert an illustration at this anchor
            val ill = illustrations.firstOrNull { it.paragraphAnchor == pIndex }
            if (ill != null && !illustrationInserted) {
                if (y + 140f > pageHeight - marginBottom) {
                    // New page for illustration
                    renderRunningFooter(canvas, pageNum)
                    pdfDoc.finishPage(page)
                    pageNum++

                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    renderRunningHeader(canvas, book, chapter.title)
                    y = marginTop.toFloat() + 20f
                }
                renderIllustrationPlate(canvas, ill, y)
                y += 140f
                illustrationInserted = true
            }

            for (line in lines) {
                if (y + 14f > pageHeight - marginBottom) {
                    // Page overflow -> Start new page
                    renderRunningFooter(canvas, pageNum)
                    pdfDoc.finishPage(page)
                    pageNum++

                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    renderRunningHeader(canvas, book, chapter.title)
                    y = marginTop.toFloat() + 20f
                }

                canvas.drawText(line, marginHorizontal.toFloat(), y, textPaint)
                y += 14.5f
            }
            y += 8f // Paragraph gap
        }

        // If illustration hasn't been drawn yet, draw at the end of the chapter
        if (!illustrationInserted && illustrations.isNotEmpty()) {
            val firstIll = illustrations.first()
            if (y + 140f > pageHeight - marginBottom) {
                renderRunningFooter(canvas, pageNum)
                pdfDoc.finishPage(page)
                pageNum++

                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                renderRunningHeader(canvas, book, chapter.title)
                y = marginTop.toFloat() + 20f
            }
            renderIllustrationPlate(canvas, firstIll, y)
        }

        renderRunningFooter(canvas, pageNum)
        pdfDoc.finishPage(page)
        return pageNum + 1
    }

    private fun renderIllustrationPlate(canvas: Canvas, ill: IllustrationEntity, topY: Float) {
        val rect = RectF(
            marginHorizontal.toFloat() + 20f,
            topY,
            (pageWidth - marginHorizontal).toFloat() - 20f,
            topY + 105f
        )

        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8F9FA")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        // Draw symbolic cartographic / architectural vector diagram inside plate
        drawIllustrationGraphic(canvas, rect, ill.visualType)

        // Caption & Figure Number below plate
        val figPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val captionText = "${ill.figureNumber}: ${ill.title}".take(65)
        canvas.drawText(captionText, (pageWidth / 2).toFloat(), topY + 120f, figPaint)
    }

    private fun drawIllustrationGraphic(canvas: Canvas, rect: RectF, type: String) {
        val cx = rect.centerX()
        val cy = rect.centerY() - 6f
        val linePaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        val accentPaint = Paint().apply {
            color = Color.parseColor("#B45309")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        when (type) {
            "Map", "World Map", "Kingdom Map", "Route" -> {
                // Cartographic compass rose and topographic contours
                canvas.drawCircle(cx, cy, 22f, linePaint)
                canvas.drawLine(cx, cy - 26, cx, cy + 26, accentPaint)
                canvas.drawLine(cx - 26, cy, cx + 26, cy, accentPaint)
                // Wavy river/boundary lines
                canvas.drawLine(rect.left + 20, cy - 10, cx - 15, cy - 5, linePaint)
                canvas.drawLine(cx + 15, cy + 5, rect.right - 20, cy + 12, linePaint)
            }
            "Architectural", "Temple" -> {
                // Classical portico / elevation schematic
                canvas.drawLine(cx - 30, cy + 18, cx + 30, cy + 18, linePaint)
                canvas.drawLine(cx - 25, cy - 12, cx + 25, cy - 12, linePaint)
                // Columns
                for (offset in listOf(-20f, -7f, 7f, 20f)) {
                    canvas.drawLine(cx + offset, cy - 12, cx + offset, cy + 18, accentPaint)
                }
                // Pediment triangle
                canvas.drawLine(cx - 25, cy - 12, cx, cy - 24, linePaint)
                canvas.drawLine(cx, cy - 24, cx + 25, cy - 12, linePaint)
            }
            else -> {
                // Ornate folio crest / study emblem
                canvas.drawRect(cx - 20, cy - 18, cx + 20, cy + 18, linePaint)
                canvas.drawLine(cx - 20, cy - 18, cx + 20, cy + 18, accentPaint)
                canvas.drawLine(cx - 20, cy + 18, cx + 20, cy - 18, accentPaint)
            }
        }
    }

    private fun renderGlossaryAndContinuityIndex(
        canvas: Canvas,
        book: BookEntity,
        records: List<ContinuityRecordEntity>,
        pageNumber: Int
    ) {
        val titlePaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("GLOSSARY & CONTINUITY CHRONICLE", (pageWidth / 2).toFloat(), 85f, titlePaint)

        var y = 125f
        val termPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val descPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 9f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        for (record in records.take(18)) {
            val termText = "${record.name} (${record.category.replace('_', ' ')})"
            canvas.drawText(termText, marginHorizontal.toFloat(), y, termPaint)
            y += 13f

            val descLines = wrapText(record.details, descPaint, contentWidth - 20)
            for (line in descLines.take(2)) {
                canvas.drawText(line, marginHorizontal.toFloat() + 10f, y, descPaint)
                y += 12f
            }
            y += 6f
        }

        renderRunningFooter(canvas, pageNumber)
    }

    private fun renderAuthorAndAboutBook(
        canvas: Canvas,
        book: BookEntity,
        backMatter: BackMatterConfig,
        pageNumber: Int
    ) {
        val cx = (pageWidth / 2).toFloat()
        var y = 140f

        val headingPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("ABOUT THE AUTHOR", cx, y, headingPaint)
        y += 35f

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val authorBio = if (backMatter.aboutAuthorText.isNotBlank()) {
            backMatter.aboutAuthorText
        } else {
            "${book.author} is a dedicated chronicler and author whose work spans ${book.bookTypes}. Writing with meticulous attention to atmosphere, cultural resonance, and depth, this volume represents the pinnacle of generative literary craft produced through InfiniteBook AI."
        }

        for (line in wrapText(authorBio, bodyPaint, contentWidth - 40)) {
            canvas.drawText(line, marginHorizontal.toFloat() + 20f, y, bodyPaint)
            y += 15f
        }

        y += 45f
        canvas.drawText("ABOUT THIS PUBLICATION", cx, y, headingPaint)
        y += 35f

        val aboutPub = "Created via the InfiniteBook AI Publishing Studio. This volume was structured across ${book.numChapters} chapters with a target extent of ${book.targetPages} pages. Engineered for archival preservation, typographic rigor, and deep multilingual fidelity in ${book.language}."

        for (line in wrapText(aboutPub, bodyPaint, contentWidth - 40)) {
            canvas.drawText(line, marginHorizontal.toFloat() + 20f, y, bodyPaint)
            y += 15f
        }

        renderRunningFooter(canvas, pageNumber)
    }

    private fun renderBackCover(canvas: Canvas, book: BookEntity) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0C111D")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, (pageWidth - 24).toFloat(), (pageHeight - 24).toFloat(), borderPaint)

        val cx = (pageWidth / 2).toFloat()

        // Genre / Classification tag
        val tagPaint = Paint().apply {
            color = Color.parseColor("#E5C07B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.2f
        }
        canvas.drawText("INFINITEBOOK AI  •  COLLECTOR'S EDITION", cx, 80f, tagPaint)

        // Title on back
        val titlePaint = Paint().apply {
            color = Color.parseColor("#F5F2EB")
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(book.title, cx, 140f, titlePaint)

        // Compelling synopsis / blurb
        val blurbPaint = Paint().apply {
            color = Color.parseColor("#D1D5DB")
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val blurbText = if (book.coverBackBlurb.isNotBlank()) {
            book.coverBackBlurb
        } else {
            "An extraordinary exploration of ${book.bookTypes}. Conceived through the boundless imagination of ${book.author} and engineered by InfiniteBook AI, this comprehensive volume delivers expansive world-building, intricate continuity, and unparalleled narrative depth. Written in ${book.language} for ${book.readerLevel.lowercase()} readers, it stands as an enduring monument to long-form storytelling."
        }

        var by = 200f
        val wrappedBlurb = wrapText(blurbText, blurbPaint, contentWidth - 40)
        for (line in wrappedBlurb) {
            canvas.drawText(line, marginHorizontal.toFloat() + 20f, by, blurbPaint)
            by += 15f
        }

        // Critical Praise Quote
        by += 30f
        val quotePaint = Paint().apply {
            color = Color.parseColor("#F3E5AB")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("\"A sweeping triumph of long-form literature and world continuity.\"", cx, by, quotePaint)
        canvas.drawText("— The Global Literary Review", cx, by + 18, Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 9f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        })

        // Barcode Mockup at bottom right
        drawBarcode(canvas, (pageWidth - marginHorizontal - 120).toFloat(), 700f)

        // Imprint logo text at bottom left
        val imprintPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("INFINITEBOOK AI PUBLISHING", marginHorizontal.toFloat() + 20f, 720f, imprintPaint)
        canvas.drawText("ISBN 978-0-998877-01-4", marginHorizontal.toFloat() + 20f, 735f, imprintPaint)
        canvas.drawText("A4 Preservation Archive", marginHorizontal.toFloat() + 20f, 750f, imprintPaint)
    }

    private fun drawBarcode(canvas: Canvas, x: Float, y: Float) {
        val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        canvas.drawRect(x, y, x + 110, y + 45, bgPaint)

        val barPaint = Paint().apply { color = Color.BLACK; strokeWidth = 1.8f }
        var curX = x + 8
        for (i in 0 until 28) {
            if (i % 3 != 0) {
                canvas.drawLine(curX, y + 5, curX, y + 35, barPaint)
            }
            curX += 3.5f
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("9 780998 877014", x + 55, y + 42, textPaint)
    }

    private fun drawCornerOrnaments(canvas: Canvas, l: Float, t: Float, r: Float, b: Float) {
        val p = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val size = 20f
        // Top Left
        canvas.drawLine(l, t + size, l, t, p)
        canvas.drawLine(l, t, l + size, t, p)
        // Top Right
        canvas.drawLine(r - size, t, r, t, p)
        canvas.drawLine(r, t, r, t + size, p)
        // Bottom Left
        canvas.drawLine(l, b - size, l, b, p)
        canvas.drawLine(l, b, l + size, b, p)
        // Bottom Right
        canvas.drawLine(r - size, b, r, b, p)
        canvas.drawLine(r, b, r, b - size, p)
    }

    private fun drawCoverEmblem(canvas: Canvas, cx: Float, cy: Float) {
        val goldFill = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, 32f, goldFill)
        canvas.drawCircle(cx, cy, 28f, Paint().apply {
            color = Color.parseColor("#8C7322")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        })

        // Infinite quill & book geometry
        val quillPaint = Paint().apply {
            color = Color.parseColor("#F5F2EB")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        // Infinity curve / open book silhouette
        canvas.drawLine(cx - 16, cy + 6, cx, cy - 4, quillPaint)
        canvas.drawLine(cx, cy - 4, cx + 16, cy + 6, quillPaint)
        canvas.drawLine(cx, cy - 4, cx, cy + 14, quillPaint)

        // Star accent
        canvas.drawCircle(cx, cy - 14, 2f, Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
            isAntiAlias = true
        })
    }

    private fun renderRunningHeader(canvas: Canvas, book: BookEntity, chapterTitle: String) {
        val headPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val headerText = "${book.title}  •  ${chapterTitle}".take(60)
        canvas.drawText(headerText, marginHorizontal.toFloat(), marginTop.toFloat() - 10, headPaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 0.75f
        }
        canvas.drawLine(
            marginHorizontal.toFloat(),
            marginTop.toFloat(),
            (pageWidth - marginHorizontal).toFloat(),
            marginTop.toFloat(),
            linePaint
        )
    }

    private fun renderRunningFooter(canvas: Canvas, pageNumber: Int) {
        val footPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "— $pageNumber —",
            (pageWidth / 2).toFloat(),
            (pageHeight - marginBottom + 24).toFloat(),
            footPaint
        )
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(potentialLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    fun getShareableUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
