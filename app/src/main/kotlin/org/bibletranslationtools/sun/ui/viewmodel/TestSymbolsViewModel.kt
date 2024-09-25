package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Lesson
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.LessonRepository
import org.bibletranslationtools.sun.data.repositories.SentenceRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.utils.Section

class TestSymbolsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CardRepository
    private val sentenceRepository: SentenceRepository
    private val lessonRepository: LessonRepository
    private val settingsRepository: SettingsRepository

    private val mutableCards = MutableStateFlow<List<Card>>(listOf())
    val cards: StateFlow<List<Card>> = mutableCards

    val questionDone = MutableStateFlow(false)
    val lessonId = MutableStateFlow(1)
    val isGlobal = MutableStateFlow(false)

    init {
        val cardDao = AppDatabase.getDatabase(application).getCardDao()
        repository = CardRepository(cardDao)
        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        sentenceRepository = SentenceRepository(sentenceDao, symbolDao)
        val lessonDao = AppDatabase.getDatabase(application).getLessonDao()
        lessonRepository = LessonRepository(lessonDao)
        val settingDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingDao)
    }

    fun loadLessonCards() {
        viewModelScope.launch {
            mutableCards.value = repository.getAllByLesson(lessonId.value)
        }
    }

    fun loadAllPassedCards() {
        viewModelScope.launch {
            mutableCards.value = repository.getAllTested()
        }
    }

    suspend fun getSentencesCount(): Int {
        return viewModelScope
            .async {
                sentenceRepository.countAll(lessonId.value)
            }
            .await()
    }

    suspend fun updateCard(card: Card) {
        repository.update(card)

        val lastSection = Setting("last_section", Section.TEST_SYMBOLS.id)
        val lastLesson = Setting("last_lesson", lessonId.value.toString())
        settingsRepository.insertOrUpdate(lastSection)
        settingsRepository.insertOrUpdate(lastLesson)
    }

    suspend fun getAllLessons(): List<Lesson> {
        return lessonRepository.getAll()
    }
}