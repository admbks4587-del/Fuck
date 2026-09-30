package com.example.infinitebook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutlineDao {
    @Query("SELECT * FROM master_outlines WHERE bookId = :bookId ORDER BY chapterNumber ASC, sectionNumber ASC")
    fun getOutlineForBook(bookId: Long): Flow<List<OutlineEntity>>

    @Query("SELECT * FROM master_outlines WHERE bookId = :bookId ORDER BY chapterNumber ASC, sectionNumber ASC")
    suspend fun getOutlineForBookDirect(bookId: Long): List<OutlineEntity>

    @Query("SELECT * FROM master_outlines WHERE bookId = :bookId AND chapterNumber = :chapterNumber AND sectionNumber = :sectionNumber LIMIT 1")
    suspend fun getSectionDirect(bookId: Long, chapterNumber: Int, sectionNumber: Int): OutlineEntity?

    @Query("SELECT COUNT(*) FROM master_outlines WHERE bookId = :bookId")
    suspend fun getSectionCount(bookId: Long): Int

    @Query("SELECT COUNT(*) FROM master_outlines WHERE bookId = :bookId AND status = 'COMPLETED'")
    suspend fun getCompletedSectionCount(bookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutline(items: List<OutlineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(item: OutlineEntity): Long

    @Update
    suspend fun updateSection(item: OutlineEntity)

    @Query("DELETE FROM master_outlines WHERE bookId = :bookId")
    suspend fun deleteOutlineForBook(bookId: Long)
}
