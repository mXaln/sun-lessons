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

    suspend fun getAll(lessonId: Int): List<Sentence> {
        return sentenceDao.getAll(lessonId)
    }

    suspend fun getAllWithSymbols(lessonId: Int): List<SentenceWithSymbols> {
        return sentenceDao.getAllWithSymbols(lessonId)
    }

    suspend fun getAllPassedWithSymbols(): List<SentenceWithSymbols> {
        return sentenceDao.getAllPassedWithSymbols()
    }

    suspend fun getAllCount(lessonId: Int): Int {
        return sentenceDao.getAllCount(lessonId)
    }

    suspend fun getAllPassedCount(): Int {
        return sentenceDao.getAllPassedCount()
    }

}