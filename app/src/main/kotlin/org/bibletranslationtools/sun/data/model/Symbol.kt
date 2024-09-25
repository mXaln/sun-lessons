package org.bibletranslationtools.sun.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.bibletranslationtools.sun.utils.AnswerType
import java.util.Objects

@Entity(tableName = "symbols")
data class Symbol(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "sort")
    val sort: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sentence_id")
    var sentenceId: String? = null
) {
    @Ignore
    var selected = false
    @Ignore
    var correct: Boolean? = null
    @Ignore
    var type: AnswerType = AnswerType.OPTION

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val symbol = other as Symbol
        return id == symbol.id &&
                sort == symbol.sort &&
                name == symbol.name &&
                sentenceId == symbol.sentenceId &&
                selected == symbol.selected &&
                correct == symbol.correct &&
                type == symbol.type
    }

    override fun hashCode(): Int {
        return Objects.hash(id, sort, name, sentenceId, selected, correct, type)
    }
}
