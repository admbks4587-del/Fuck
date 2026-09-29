package com.example.infinitebook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // Books
    @Query("SELECT * FROM books ORDER BY updatedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookDirect(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    // Chapters
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    suspend fun getChaptersForBookDirect(bookId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterNumber = :chapterNumber LIMIT 1")
    fun getChapter(bookId: Long, chapterNumber: Int): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterNumber = :chapterNumber LIMIT 1")
    suspend fun getChapterDirect(bookId: Long, chapterNumber: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersForBook(bookId: Long)

    // Continuity
    @Query("SELECT * FROM continuity_records WHERE bookId = :bookId ORDER BY category ASC, name ASC")
    fun getContinuityRecords(bookId: Long): Flow<List<ContinuityRecordEntity>>

    @Query("SELECT * FROM continuity_records WHERE bookId = :bookId ORDER BY category ASC, name ASC")
    suspend fun getContinuityRecordsDirect(bookId: Long): List<ContinuityRecordEntity>

    @Query("SELECT * FROM continuity_records WHERE bookId = :bookId AND category = :category ORDER BY name ASC")
    fun getContinuityByCategory(bookId: Long, category: String): Flow<List<ContinuityRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinuityRecord(record: ContinuityRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinuityRecords(records: List<ContinuityRecordEntity>)

    @Update
    suspend fun updateContinuityRecord(record: ContinuityRecordEntity)

    @Delete
    suspend fun deleteContinuityRecord(record: ContinuityRecordEntity)

    // Illustrations
    @Query("SELECT * FROM illustrations WHERE bookId = :bookId ORDER BY chapterNumber ASC, id ASC")
    fun getIllustrationsForBook(bookId: Long): Flow<List<IllustrationEntity>>

    @Query("SELECT * FROM illustrations WHERE bookId = :bookId ORDER BY chapterNumber ASC, id ASC")
    suspend fun getIllustrationsForBookDirect(bookId: Long): List<IllustrationEntity>

    @Query("SELECT * FROM illustrations WHERE bookId = :bookId AND chapterNumber = :chapterNumber ORDER BY id ASC")
    fun getIllustrationsForChapter(bookId: Long, chapterNumber: Int): Flow<List<IllustrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIllustration(illustration: IllustrationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIllustrations(illustrations: List<IllustrationEntity>)

    @Update
    suspend fun updateIllustration(illustration: IllustrationEntity)

    @Delete
    suspend fun deleteIllustration(illustration: IllustrationEntity)
}
