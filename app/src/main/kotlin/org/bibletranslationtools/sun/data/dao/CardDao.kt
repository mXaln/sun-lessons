package org.bibletranslationtools.sun.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.bibletranslationtools.sun.data.model.Card

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Update
    suspend fun update(card: Card)

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun get(id: String): Card?

    @Query("SELECT * FROM cards WHERE passed = 1")
    suspend fun getAllPassed(): List<Card>

    @Query("SELECT COUNT(*) FROM cards WHERE passed = 1")
    suspend fun getAllPassedCount(): Int

    @Query("SELECT * FROM cards WHERE lesson_id = :lessonId")
    suspend fun getAllByLesson(lessonId: Int): List<Card>

}