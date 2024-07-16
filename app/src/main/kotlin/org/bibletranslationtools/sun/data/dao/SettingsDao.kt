package org.bibletranslationtools.sun.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.bibletranslationtools.sun.data.model.Setting

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Delete
    suspend fun delete(setting: Setting)

    @Update
    suspend fun update(setting: Setting)

    @Query("SELECT * FROM settings WHERE name = :name")
    suspend fun get(name: String): Setting?
}