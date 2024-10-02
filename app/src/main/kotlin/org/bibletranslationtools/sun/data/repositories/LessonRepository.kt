package org.bibletranslationtools.sun.data.repositories

import org.bibletranslationtools.sun.data.dao.LessonDao
import org.bibletranslationtools.sun.data.model.Lesson
import org.bibletranslationtools.sun.data.model.LessonWithData

class LessonRepository(private val lessonDao: LessonDao) {
    suspend fun insert(lesson: Lesson) {
        return lessonDao.insert(lesson)
    }

    suspend fun delete(lesson: Lesson) {
        lessonDao.delete(lesson)
    }

    suspend fun update(lesson: Lesson) {
        lessonDao.update(lesson)
    }

    suspend fun getAll(): List<Lesson> {
        return lessonDao.getAll()
    }

    suspend fun getAllWithData(): List<LessonWithData> {
        return lessonDao.getAllWithData()
    }

    suspend fun getWithData(id: Int): LessonWithData? {
        return lessonDao.getWithData(id)
    }

    suspend fun get(id: Int): Lesson? {
        return lessonDao.get(id)
    }

}