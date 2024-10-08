package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.repositories.LessonRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.ui.mapper.LessonMapper
import org.bibletranslationtools.sun.ui.model.LessonModel

class TrackProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val settingsRepository: SettingsRepository

    val lessons: StateFlow<List<LessonModel>> get() = mutableLessons
    private val mutableLessons = MutableStateFlow<List<LessonModel>>(listOf())

    init {
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)
        val settingsDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingsDao)
    }

    fun loadLessons(): Job {
        return viewModelScope.launch {
            val lessons = lessonRepository.getAllWithData().map(LessonMapper::map)
            lessons.forEachIndexed { index, lesson ->
                lesson.isAvailable = lessonAvailable(lessons, index)
            }
            mutableLessons.value = lessons
        }
    }

    suspend fun getLastLesson(): Int {
        return settingsRepository.get("last_lesson")?.value?.toInt() ?: 1
    }

    private fun lessonAvailable(lessons: List<LessonModel>, position: Int): Boolean {
        if (position == 0) return true
        val prevLesson = lessons[position - 1]
        return prevLesson.totalProgress == 100.0
    }

}