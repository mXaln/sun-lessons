package org.bibletranslationtools.sun.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import org.bibletranslationtools.sun.data.model.Symbol

@Dao
interface SymbolDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(symbol: Symbol)

    @Delete
    suspend fun delete(symbol: Symbol)

    @Update
    suspend fun update(symbol: Symbol)
}