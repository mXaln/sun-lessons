package org.bibletranslationtools.sun.data.repositories

import org.bibletranslationtools.sun.data.dao.SentenceDao
import org.bibletranslationtools.sun.data.dao.SymbolDao
import org.bibletranslationtools.sun.data.model.Sentence
import org.bibletranslationtools.sun.data.model.SentenceWithSymbols
import org.bibletranslationtools.sun.data.model.Symbol

class SentenceRepository(
    private val sentenceDao: SentenceDao,
    private val symbolDao: SymbolDao
) {
    suspend fun insert(sentence: Sentence) {
        sentenceDao.insert(sentence)
    }

    suspend fun delete(sentence: Sentence) {
        sentenceDao.delete(sentence)
    }

    suspend fun update(sentence: Sentence) {
        sentenceDao.update(sentence)
    }

    suspend fun insert(symbol: Symbol) {
        symbolDao.insert(symbol)
    }

    suspend fun get(id: String): Sentence? {
        return sentenceDao.get(id)
    }

    suspend fun getByLesson(lessonId: Int): List<Sentence> {
        return sentenceDao.getByLesson(lessonId)
    }

    suspend fun getAllWithSymbols(lessonId: Int): List<SentenceWithSymbols> {
        return sentenceDao.getByLessonWithSymbols(lessonId)
    }

    suspend fun getAllLearnedWithSymbols(): List<SentenceWithSymbols> {
        return sentenceDao.getAllLearnedWithSymbols()
    }

    suspend fun allLearnedCount(): Int {
        return sentenceDao.allLearnedCount()
    }

    suspend fun getAllTestedWithSymbols(): List<SentenceWithSymbols> {
        return sentenceDao.getAllTestedWithSymbols()
    }

    suspend fun allTestedCount(): Int {
        return sentenceDao.allTestedCount()
    }

    suspend fun getByLessonCount(lessonId: Int): Int {
        return sentenceDao.getByLessonCount(lessonId)
    }

    suspend fun getLearnedByLessonCount(lessonId: Int): Int {
        return sentenceDao.getLearnedByLessonCount(lessonId)
    }

    suspend fun getTestedByLessonCount(lessonId: Int): Int {
        return sentenceDao.getTestedByLessonCount(lessonId)
    }
}