package org.bibletranslationtools.sun.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import java.util.Objects

@Entity(tableName = "cards", primaryKeys = ["id"])
data class Card(
    @ColumnInfo(name = "id")
    override val id: String,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "primary")
    val primary: String,
    @ColumnInfo(name = "secondary")
    val secondary: String,
    @ColumnInfo(name = "learned")
    var learned: Boolean = false,
    @ColumnInfo(name = "tested")
    var tested: Boolean = false,
    @ColumnInfo(name = "lesson_id")
    var lessonId: Int? = null,
) : TestCard {
    @Ignore
    override var correct: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val card = other as Card
        return id == card.id &&
                symbol == card.symbol &&
                learned == card.learned &&
                tested == card.tested &&
                lessonId == card.lessonId
    }

    override fun hashCode(): Int {
        return Objects.hash(id, symbol, learned, tested, lessonId)
    }
}

data class Answer(
    override var correct: Boolean?
) : TestCard {

    override val id: String
        get() = if (correct == true) CORRECT else INCORRECT

    private companion object {
        const val CORRECT = "correct"
        const val INCORRECT = "incorrect"
    }
}