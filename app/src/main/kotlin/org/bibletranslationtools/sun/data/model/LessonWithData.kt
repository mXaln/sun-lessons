package org.bibletranslationtools.sun.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class LessonWithData(
    @Embedded val lesson: Lesson,
    @Relation(
        entity = Card::class,
        parentColumn = "id",
        entityColumn = "lesson_id"
    )
    val cards: List<Card>,
    @Relation(
        entity = Sentence::class,
        parentColumn = "id",
        entityColumn = "lesson_id"
    )
    val sentences: List<Sentence>
)