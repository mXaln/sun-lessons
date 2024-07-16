package org.bibletranslationtools.sun.preferences

import android.content.Context
import android.content.SharedPreferences

class UserSharePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}