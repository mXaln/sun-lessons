package org.bibletranslationtools.sun.data.repositories

import org.bibletranslationtools.sun.data.dao.SettingsDao
import org.bibletranslationtools.sun.data.model.Setting

class SettingsRepository(private val settingDao: SettingsDao) {
    suspend fun insert(setting: Setting) {
        settingDao.insert(setting)
    }

    suspend fun delete(setting: Setting) {
        settingDao.delete(setting)
    }

    suspend fun update(setting: Setting) {
        settingDao.update(setting)
    }

    suspend fun insertOrUpdate(setting: Setting) {
        settingDao.get(setting.name)?.let {
            settingDao.update(setting)
        } ?: run {
            settingDao.insert(setting)
        }
    }

    suspend fun get(name: String): Setting? {
        return settingDao.get(name)
    }
}