package org.bibletranslationtools.sun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.bibletranslationtools.sun.data.dao.CardDao
import org.bibletranslationtools.sun.data.dao.LessonDao
import org.bibletranslationtools.sun.data.dao.SentenceDao
import org.bibletranslationtools.sun.data.dao.SettingsDao
import org.bibletranslationtools.sun.data.dao.SymbolDao
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Lesson
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.model.Symbol
import kotlin.concurrent.Volatile

@Database(
    entities = [
        Card::class,
        Lesson::class,
        Setting::class,
        Sentence::class,
        Symbol::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCardDao(): CardDao
    abstract fun getLessonDao(): LessonDao
    abstract fun getSettingDao(): SettingsDao
    abstract fun getSentenceDao(): SentenceDao
    abstract fun getSymbolDao(): SymbolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Room
                    .databaseBuilder(context, AppDatabase::class.java, "sun.db")
                    .build()
                INSTANCE!!
            }
        }
    }
}