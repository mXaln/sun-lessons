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
import org.bibletranslationtools.sun.data.repositories.CardRepository
import org.bibletranslationtools.sun.data.model.Card
import org.bibletranslationtools.sun.data.model.Setting
import org.bibletranslationtools.sun.data.repositories.SettingsRepository
import org.bibletranslationtools.sun.utils.Section

class LearnSymbolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    private val settingsRepository: SettingsRepository
    private val mutableCards = MutableStateFlow<List<Card>>(listOf())

    val flipState = MutableStateFlow(EasyFlipView.FlipState.FRONT_SIDE)
    val cards: StateFlow<List<Card>> = mutableCards
    val lessonId = MutableStateFlow(1)

    init {
        val dao = AppDatabase.getDatabase(application).getCardDao()
        repository = CardRepository(dao)
        val settingDao = AppDatabase.getDatabase(application).getSettingDao()
        settingsRepository = SettingsRepository(settingDao)
    }

    fun loadCards(): Job {
        return viewModelScope.launch {
            mutableCards.value = repository.getByLesson(lessonId.value)
        }
    }

    fun saveCard(card: Card): Job {
        return viewModelScope.launch {
            repository.update(card)
            mutableCards.value = mutableCards.value

            val lastSection = Setting(Setting.LAST_SECTION, Section.LEARN_SYMBOLS.id)
            val lastLesson = Setting(Setting.LAST_LESSON, lessonId.value.toString())
            settingsRepository.insertOrUpdate(lastSection)
            settingsRepository.insertOrUpdate(lastLesson)
        }
    }

}