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
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.SentenceRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.ui.model.LessonMode
import org.bibletranslationtools.sun.utils.Section
import kotlin.math.min

class LearnSentencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SentenceRepository
    private val settingsRepository: SettingsRepository

    private val _sentences = MutableStateFlow<List<SentenceWithSymbols>>(listOf())
    val sentences: StateFlow<List<SentenceWithSymbols>> = _sentences

    val lessonId = MutableStateFlow(1)
    val mode = MutableStateFlow(LessonMode.NORMAL)

    init {
        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        repository = SentenceRepository(sentenceDao, symbolDao)
        val settingDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingDao)
    }

    fun loadSentences(): Job {
        return viewModelScope.launch {
            _sentences.value = repository.getAllWithSymbols(lessonId.value)
        }
    }

    fun saveSentence(sentence: SentenceWithSymbols): Job {
        return viewModelScope.launch {
            repository.update(sentence.sentence)
            _sentences.value = _sentences.value

            val lastSection = Setting(Setting.LAST_SECTION, Section.LEARN_SENTENCES.id)
            val lastLesson = Setting(Setting.LAST_LESSON, lessonId.value.toString())
            settingsRepository.insertOrUpdate(lastSection)
            settingsRepository.insertOrUpdate(lastLesson)
        }
    }

    fun saveLastPosition(position: Int) {
        runBlocking {
            val lastSentence = Setting(Setting.LAST_SENTENCE, position.toString())
            settingsRepository.insertOrUpdate(lastSentence)
        }
    }

    fun getLastPosition(): Int {
        return runBlocking {
            val pos = settingsRepository.get(Setting.LAST_SENTENCE)?.value?.toInt() ?: 0
            min(pos, _sentences.value.size - 1)
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