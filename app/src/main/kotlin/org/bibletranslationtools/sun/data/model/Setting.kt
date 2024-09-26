package org.bibletranslationtools.sun.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "settings", primaryKeys = ["name"])
data class Setting(
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "value")
    val value: String
) {
    companion object {
        const val VERSION = "version"
        const val LAST_SECTION = "last_section"
        const val LAST_LESSON = "last_lesson"
    }
}