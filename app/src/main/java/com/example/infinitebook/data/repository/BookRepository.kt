package com.example.infinitebook.data.repository

import com.example.infinitebook.data.local.BookDao
import com.example.infinitebook.data.local.BookDataStore
import com.example.infinitebook.data.local.BookEntity
import com.example.infinitebook.data.local.ChapterEntity
import com.example.infinitebook.data.local.ContinuityRecordEntity
import com.example.infinitebook.data.local.IllustrationEntity
import com.example.infinitebook.data.local.OutlineDao
import com.example.infinitebook.data.local.OutlineEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
    private val outlineDao: OutlineDao,
    private val dataStore: BookDataStore? = null
) {

    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    fun getBook(id: Long): Flow<BookEntity?> = bookDao.getBookById(id)

    suspend fun getBookDirect(id: Long): BookEntity? = bookDao.getBookDirect(id)

    suspend fun createBook(book: BookEntity): Long = bookDao.insertBook(book)

    suspend fun updateBook(book: BookEntity) = bookDao.updateBook(book)

    suspend fun deleteBook(book: BookEntity) = bookDao.deleteBook(book)

    fun getChapters(bookId: Long): Flow<List<ChapterEntity>> = bookDao.getChaptersForBook(bookId)

    suspend fun getChaptersDirect(bookId: Long): List<ChapterEntity> = bookDao.getChaptersForBookDirect(bookId)

    fun getChapter(bookId: Long, chapterNumber: Int): Flow<ChapterEntity?> =
        bookDao.getChapter(bookId, chapterNumber)

    suspend fun getChapterDirect(bookId: Long, chapterNumber: Int): ChapterEntity? =
        bookDao.getChapterDirect(bookId, chapterNumber)

    suspend fun saveChapter(chapter: ChapterEntity): Long = bookDao.insertChapter(chapter)

    suspend fun saveChapters(chapters: List<ChapterEntity>) = bookDao.insertChapters(chapters)

    suspend fun updateChapter(chapter: ChapterEntity) = bookDao.updateChapter(chapter)

    fun getContinuityRecords(bookId: Long): Flow<List<ContinuityRecordEntity>> =
        bookDao.getContinuityRecords(bookId)

    suspend fun getContinuityRecordsDirect(bookId: Long): List<ContinuityRecordEntity> =
        bookDao.getContinuityRecordsDirect(bookId)

    suspend fun saveContinuityRecord(record: ContinuityRecordEntity): Long =
        bookDao.insertContinuityRecord(record)

    suspend fun saveContinuityRecords(records: List<ContinuityRecordEntity>) =
        bookDao.insertContinuityRecords(records)

    suspend fun updateContinuityRecord(record: ContinuityRecordEntity) =
        bookDao.updateContinuityRecord(record)

    suspend fun deleteContinuityRecord(record: ContinuityRecordEntity) =
        bookDao.deleteContinuityRecord(record)

    fun getIllustrations(bookId: Long): Flow<List<IllustrationEntity>> =
        bookDao.getIllustrationsForBook(bookId)

    suspend fun getIllustrationsDirect(bookId: Long): List<IllustrationEntity> =
        bookDao.getIllustrationsForBookDirect(bookId)

    fun getIllustrationsForChapter(bookId: Long, chapterNumber: Int): Flow<List<IllustrationEntity>> =
        bookDao.getIllustrationsForChapter(bookId, chapterNumber)

    suspend fun saveIllustration(illustration: IllustrationEntity): Long =
        bookDao.insertIllustration(illustration)

    suspend fun saveIllustrations(illustrations: List<IllustrationEntity>) =
        bookDao.insertIllustrations(illustrations)

    suspend fun deleteIllustration(illustration: IllustrationEntity) =
        bookDao.deleteIllustration(illustration)

    // Master Outline
    fun getOutline(bookId: Long): Flow<List<OutlineEntity>> =
        outlineDao.getOutlineForBook(bookId)

    suspend fun getOutlineDirect(bookId: Long): List<OutlineEntity> =
        outlineDao.getOutlineForBookDirect(bookId)

    suspend fun getOutlineSectionDirect(bookId: Long, chapterNumber: Int, sectionNumber: Int): OutlineEntity? =
        outlineDao.getSectionDirect(bookId, chapterNumber, sectionNumber)

    suspend fun saveOutline(items: List<OutlineEntity>) =
        outlineDao.insertOutline(items)

    suspend fun updateOutlineSection(item: OutlineEntity) =
        outlineDao.updateSection(item)

    suspend fun getSectionCount(bookId: Long): Int =
        outlineDao.getSectionCount(bookId)

    // Continuation State (DataStore)
    suspend fun saveContinuationState(bookId: Long, chapter: Int, section: Int, registryJson: String) {
        dataStore?.saveContinuationState(bookId, chapter, section, registryJson)
    }

    suspend fun getContinuationState(bookId: Long): Triple<Int, Int, String> {
        return dataStore?.getContinuationState(bookId) ?: Triple(1, 0, "")
    }

    suspend fun clearContinuationState(bookId: Long) {
        dataStore?.clearContinuationState(bookId)
    }
}
