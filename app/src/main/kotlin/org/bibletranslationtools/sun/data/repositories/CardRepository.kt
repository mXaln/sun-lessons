package org.bibletranslationtools.sun.data.repositories

import org.bibletranslationtools.sun.data.dao.CardDao
import org.bibletranslationtools.sun.data.model.Card

class CardRepository(private val cardDao: CardDao) {

    suspend fun insert(card: Card) {
        cardDao.insert(card)
    }

    suspend fun delete(card: Card) {
        cardDao.delete(card)
    }

    suspend fun update(card: Card) {
        cardDao.update(card)
    }

    suspend fun get(id: String): Card? {
        return cardDao.get(id)
    }

    suspend fun getAllByLesson(lessonId: Int): List<Card> {
        return cardDao.getAllByLesson(lessonId)
    }

    suspend fun getAllPassed(): List<Card> {
        return cardDao.getAllPassed()
    }

    suspend fun getAllPassedCount(): Int {
        return cardDao.getAllPassedCount()
    }

}