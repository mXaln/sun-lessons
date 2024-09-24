package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.model.Lesson
import org.bibletranslationtools.sun.data.repositories.LessonRepository

class SectionStatusViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository

    init {
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)
    }

    suspend fun getAllLessons(): List<Lesson> {
        return lessonRepository.getAll()
    }
}