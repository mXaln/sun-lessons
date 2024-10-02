package org.bibletranslationtools.sun.ui.model

import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Lesson
import org.bibletranslationtools.sun.data.model.Sentence
import java.util.Objects

data class LessonModel(
    val lesson: Lesson,
    val cards: List<Card>,
    val sentences: List<Sentence>
) {
    var isAvailable = false
    var isSelected = false

    val cardsLearned get() = cards.count { it.learned }
    val cardsLearnedProgress get() = cardsLearned.toDouble() / cards.size * 100

    val cardsTested get() = cards.count { it.tested }
    val cardsTestedProgress get() = cardsTested.toDouble() / cards.size * 100

    val sentencesLearned get() = sentences.count { it.learned }
    val sentencesLearnedProgress get() = run {
        // If there are no sentences, return 100% progress
        if (sentences.isNotEmpty()) {
            sentencesLearned.toDouble() / sentences.size * 100
        } else {
            100.0
        }
    }

    val sentencesTested get() = sentences.count { it.tested }
    val sentencesTestedProgress get() = run {
        // If there are no sentences, return 100% progress
        if (sentences.isNotEmpty()) {
            sentencesTested.toDouble() / sentences.size * 100
        } else {
            100.0
        }
    }

    val totalProgress: Double
        get() {
            // Size times 2, because we have learned and tested cards/sentences
            val total = (cards.size * 2) + (sentences.size * 2)
            val completed = cardsLearned + cardsTested + sentencesLearned + sentencesTested
            return (completed.toDouble() / total) * 100
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val lessonModel = other as LessonModel
        return lesson == lessonModel.lesson &&
                totalProgress == lessonModel.totalProgress &&
                isAvailable == lessonModel.isAvailable &&
                isSelected == lessonModel.isSelected
    }

    override fun hashCode(): Int {
        return Objects.hash(lesson, totalProgress, isAvailable, isSelected)
    }
}