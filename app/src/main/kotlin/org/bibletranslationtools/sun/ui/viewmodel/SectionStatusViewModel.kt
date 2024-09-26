package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.LessonRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.utils.Section

class SectionStatusViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val settingsRepository: SettingsRepository

    init {
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)
        val settingsDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingsDao)
    }

    suspend fun getNextLesson(id: Int): Int {
        val lessons = lessonRepository.getAll().map { it.id }
        val current = lessons.indexOf(id)
        var next = 1
        if (current < lessons.size - 1) {
            next = lessons[current + 1]
        }
        return next
    }

    suspend fun saveSectionStatus(lessonId: Int, section: Section) {
        val lastSection = Setting(Setting.LAST_SECTION, section.id)
        val lastLesson = Setting(Setting.LAST_LESSON, lessonId.toString())
        settingsRepository.insertOrUpdate(lastSection)
        settingsRepository.insertOrUpdate(lastLesson)
    }
}