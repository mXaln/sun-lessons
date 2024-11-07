package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.utils.Section
import kotlin.math.min

class LearnSymbolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    private val settingsRepository: SettingsRepository

    private val _cards = MutableStateFlow<List<Card>>(listOf())
    val cards: StateFlow<List<Card>> = _cards

    val lessonId = MutableStateFlow(1)
    val mode = MutableStateFlow(LessonMode.NORMAL)

    init {
        val dao = AppDatabase.getDatabase(application).getCardDao()
        repository = CardRepository(dao)
        val settingDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingDao)
    }

    fun loadCards(): Job {
        return viewModelScope.launch {
            _cards.value = repository.getByLesson(lessonId.value)
        }
    }

    fun saveCard(card: Card): Job {
        return viewModelScope.launch {
            repository.update(card)
            _cards.value = _cards.value

            val lastSection = Setting(Setting.LAST_SECTION, Section.LEARN_SYMBOLS.id)
            val lastLesson = Setting(Setting.LAST_LESSON, lessonId.value.toString())
            settingsRepository.insertOrUpdate(lastSection)
            settingsRepository.insertOrUpdate(lastLesson)
        }
    }

    fun saveLastPosition(position: Int) {
        runBlocking {
            val lastSymbol = Setting(Setting.LAST_SYMBOL, position.toString())
            settingsRepository.insertOrUpdate(lastSymbol)
        }
    }

    fun getLastPosition(): Int {
        return runBlocking {
            val pos = settingsRepository.get(Setting.LAST_SYMBOL)?.value?.toInt() ?: 0
            min(pos, _cards.value.size - 1)
        }
    }

    fun initializeLessonMode() {
        runBlocking {
            val all = repository.getByLessonCount(lessonId.value)
            val done = repository.getLearnedByLessonCount(lessonId.value)

            if (all == done) {
                mode.value = LessonMode.REPEAT
            } else {
                mode.value = LessonMode.NORMAL
            }
        }
    }
}