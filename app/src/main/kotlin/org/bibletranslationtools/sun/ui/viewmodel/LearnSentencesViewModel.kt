package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wajahatkarim3.easyflipview.EasyFlipView
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.SentenceRepository
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.utils.Section

class LearnSentencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SentenceRepository
    private val settingsRepository: SettingsRepository

    private val _sentences = MutableStateFlow<List<SentenceWithSymbols>>(listOf())
    val sentences: StateFlow<List<SentenceWithSymbols>> = _sentences
    val flipState = MutableStateFlow(EasyFlipView.FlipState.FRONT_SIDE)
    val lessonId = MutableStateFlow(1)

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

            val lastSection = Setting("last_section", Section.LEARN_SENTENCES.id)
            val lastLesson = Setting("last_lesson", lessonId.value.toString())
            settingsRepository.insertOrUpdate(lastSection)
            settingsRepository.insertOrUpdate(lastLesson)
        }
    }
}