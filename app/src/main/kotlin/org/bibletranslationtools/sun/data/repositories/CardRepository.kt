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

    suspend fun getByLesson(lessonId: Int): List<Card> {
        return cardDao.getByLesson(lessonId)
    }

    suspend fun getAllLearned(): List<Card> {
        return cardDao.getAllLearned()
    }

    suspend fun countAllLearned(): Int {
        return cardDao.allLearnedCount()
    }

    suspend fun getAllTested(): List<Card> {
        return cardDao.getAllTested()
    }

    suspend fun countAllTested(): Int {
        return cardDao.allTestedCount()
    }

    suspend fun getByLessonCount(lessonId: Int): Int {
        return cardDao.getByLessonCount(lessonId)
    }

    suspend fun getLearnedByLesson(lessonId: Int): List<Card> {
        return cardDao.getLearnedByLesson(lessonId)
    }

    suspend fun getLearnedByLessonCount(lessonId: Int): Int {
        return cardDao.getLearnedByLessonCount(lessonId)
    }

    suspend fun getTestedByLesson(lessonId: Int): List<Card> {
        return cardDao.getTestedByLesson(lessonId)
    }

    suspend fun getTestedByLessonCount(lessonId: Int): Int {
        return cardDao.getTestedByLessonCount(lessonId)
    }

}