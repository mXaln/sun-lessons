package org.bibletranslationtools.sun.ui.mapper

import org.bibletranslationtools.sun.data.model.LessonWithData
import org.bibletranslationtools.sun.ui.model.LessonModel

object LessonMapper {
    fun map(lesson: LessonWithData): LessonModel {
        return LessonModel(
            lesson.lesson,
            lesson.cards,
            lesson.sentences
        )
    }
}