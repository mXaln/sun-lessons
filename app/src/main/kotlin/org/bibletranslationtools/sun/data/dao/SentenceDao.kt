package org.bibletranslationtools.sun.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols

@Dao
interface SentenceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sentence: Sentence)

    @Delete
    suspend fun delete(sentence: Sentence)

    @Update
    suspend fun update(sentence: Sentence)

    @Transaction
    @Query("SELECT * FROM sentences WHERE id = :id")
    suspend fun get(id: String): Sentence?

    @Transaction
    @Query("SELECT * FROM sentences WHERE lesson_id = :lessonId")
    suspend fun getByLesson(lessonId: Int): List<Sentence>

    @Transaction
    @Query("SELECT * FROM sentences WHERE lesson_id = :lessonId")
    suspend fun getByLessonWithSymbols(lessonId: Int): List<SentenceWithSymbols>

    @Transaction
    @Query("SELECT * FROM sentences WHERE tested = 1")
    suspend fun getAllTestedWithSymbols(): List<SentenceWithSymbols>

    @Transaction
    @Query("SELECT COUNT(*) FROM sentences WHERE tested = 1")
    suspend fun allTestedCount(): Int

    @Transaction
    @Query("SELECT * FROM sentences WHERE learned = 1")
    suspend fun getAllLearnedWithSymbols(): List<SentenceWithSymbols>

    @Transaction
    @Query("SELECT COUNT(*) FROM sentences WHERE learned = 1")
    suspend fun allLearnedCount(): Int

    @Transaction
    @Query("SELECT * FROM sentences WHERE learned = 1 AND lesson_id = :lessonId")
    suspend fun getLearnedByLesson(lessonId: Int): List<Sentence>

    @Transaction
    @Query("SELECT COUNT(*) FROM sentences WHERE lesson_id = :lessonId")
    suspend fun getByLessonCount(lessonId: Int): Int

    @Transaction
    @Query("SELECT COUNT(*) FROM sentences WHERE learned = 1 AND lesson_id = :lessonId")
    suspend fun getLearnedByLessonCount(lessonId: Int): Int

    @Transaction
    @Query("SELECT COUNT(*) FROM sentences WHERE tested = 1 AND lesson_id = :lessonId")
    suspend fun getTestedByLessonCount(lessonId: Int): Int
}