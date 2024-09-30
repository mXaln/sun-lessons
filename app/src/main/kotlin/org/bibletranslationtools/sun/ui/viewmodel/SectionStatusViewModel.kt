package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.repositories.LessonRepository
import org.bibletranslationtools.sun.data.repositories.SentenceRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.ui.mapper.LessonMapper
import org.bibletranslationtools.sun.utils.Section

class SectionStatusViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val settingsRepository: SettingsRepository
    private val cardRepository: CardRepository
    private val sentenceRepository: SentenceRepository

    init {
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)
        val settingsDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingsDao)
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        sentenceRepository = SentenceRepository(sentenceDao, symbolDao)
        val cardDao = AppDatabase.getDatabase(application).getCardDao()
        cardRepository = CardRepository(cardDao)
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

    suspend fun sentencesByLessonCount(lessonId: Int): Int {
        return sentenceRepository.getByLessonCount(lessonId)
    }

    suspend fun getLastTestSession(): Section? {
        val lastLesson = settingsRepository.get("last_lesson")?.value?.toInt() ?: 1
        val lesson = lessonRepository.getWithData(lastLesson)
        val lessonData = lesson?.let(LessonMapper::map) ?: return null
        val hasSentences = lessonData.sentences.isNotEmpty()

        return when {
            hasSentences && lessonData.sentencesLearnedProgress == 100.0 -> Section.TEST_SENTENCES
            lessonData.cardsLearnedProgress == 100.0 -> Section.TEST_SYMBOLS
            else -> null
        }
    }
}