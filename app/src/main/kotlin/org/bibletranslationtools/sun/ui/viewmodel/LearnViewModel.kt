package org.bibletranslationtools.sun.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.sun.data.AppDatabase
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.utils.Constants

class LearnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    private val mutableCards = MutableStateFlow<List<Card>>(listOf())

    val cards: StateFlow<List<Card>> = mutableCards

    val lessonId = MutableStateFlow(1)
    val part = MutableStateFlow(Constants.PART_ONE)

    init {
        val dao = AppDatabase.getDatabase(application).getCardDao()
        repository = CardRepository(dao)
    }

    fun loadCards(): Job {
        return viewModelScope.launch {
            mutableCards.value = repository.getAllByLesson(lessonId.value)
                .filter { it.part == part.value }
        }
    }

    fun saveCard(card: Card): Job {
        return viewModelScope.launch {
            repository.update(card)
            mutableCards.value = mutableCards.value
        }
    }

}