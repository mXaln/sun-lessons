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
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.repositories.SentenceRepository

class LearnSentencesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SentenceRepository

    private val _sentences = MutableStateFlow<List<SentenceWithSymbols>>(listOf())
    val sentences: StateFlow<List<SentenceWithSymbols>> = _sentences
    val flipState = MutableStateFlow(EasyFlipView.FlipState.FRONT_SIDE)
    val lessonId = MutableStateFlow(1)

    init {
        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        repository = SentenceRepository(sentenceDao, symbolDao)
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
        }
    }
}