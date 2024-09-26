package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.repositories.SentenceRepository

class GlobalTestViewModel(application: Application) : AndroidViewModel(application) {
    private val cardRepository: CardRepository
    private val sentenceRepository: SentenceRepository

    private val mutableCardsCount = MutableStateFlow(0)
    val cardsCount: StateFlow<Int> = mutableCardsCount

    private val mutableSentencesCount = MutableStateFlow(0)
    val sentencesCount: StateFlow<Int> = mutableSentencesCount

    init {
        val cardDao = AppDatabase.getDatabase(application).getCardDao()
        cardRepository = CardRepository(cardDao)
        val sentenceDao = AppDatabase.getDatabase(application).getSentenceDao()
        val symbolDao = AppDatabase.getDatabase(application).getSymbolDao()
        sentenceRepository = SentenceRepository(sentenceDao, symbolDao)
    }

    fun loadAllTestedCardsCount() {
        viewModelScope.launch {
            mutableCardsCount.value = cardRepository.countAllTested()
        }
    }

    fun loadAllTestedSentencesCount() {
        viewModelScope.launch {
            mutableSentencesCount.value = sentenceRepository.allTestedCount()
        }
    }
}