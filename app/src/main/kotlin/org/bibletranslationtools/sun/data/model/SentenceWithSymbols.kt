package org.bibletranslationtools.sun.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SentenceWithSymbols(
    @Embedded val sentence: Sentence,
    @Relation(
        parentColumn = "id",
        entityColumn = "sentence_id"
    )
    val symbols: List<Symbol>
)
