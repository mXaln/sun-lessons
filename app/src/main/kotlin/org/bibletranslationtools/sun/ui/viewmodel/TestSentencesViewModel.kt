package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.repositories.LessonRepository
import org.bibletranslationtools.sun.data.repositories.SentenceRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.utils.Section

class TestSentencesViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val sentenceRepository: SentenceRepository
    private val cardsRepository: CardRepository
    private val settingsRepository: SettingsRepository

    val lessonId = MutableStateFlow(1)
    val sentenceDone = MutableStateFlow(false)
    val mode = MutableStateFlow(LessonMode.NORMAL)

    private val _sentences = MutableStateFlow<List<SentenceWithSymbols>>(listOf())
    val sentences: StateFlow<List<SentenceWithSymbols>> = _sentences

    private val _cards = MutableStateFlow<List<Card>>(listOf())
    val cards: StateFlow<List<Card>> = _cards

    init {
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)

        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        sentenceRepository = SentenceRepository(sentenceDao, symbolDao)

        val cardDao = AppDatabase.getDatabase(application).getCardDao()
        cardsRepository = CardRepository(cardDao)

        val settingDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingDao)
    }

    fun loadSentences() {
        viewModelScope.launch {
            _sentences.value = sentenceRepository.getAllWithSymbols(lessonId.value)
        }
    }

    suspend fun updateSentence(sentence: Sentence) {
        sentenceRepository.update(sentence)

        val lastSection = Setting(Setting.LAST_SECTION, Section.TEST_SENTENCES.id)
        val lastLesson = Setting(Setting.LAST_LESSON, lessonId.value.toString())
        settingsRepository.insertOrUpdate(lastSection)
        settingsRepository.insertOrUpdate(lastLesson)
    }

    suspend fun getAllCards(): List<Card> {
        return cardsRepository.getByLesson(lessonId.value)
    }

    fun initializeLessonMode() {
        runBlocking {
            val all = sentenceRepository.getByLessonCount(lessonId.value)
            val done = sentenceRepository.getTestedByLessonCount(lessonId.value)

            if (all == done) {
                mode.value = LessonMode.REPEAT
            } else {
                mode.value = LessonMode.NORMAL
            }
        }
    }
}