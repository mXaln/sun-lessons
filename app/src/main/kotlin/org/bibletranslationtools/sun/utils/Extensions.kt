package org.bibletranslationtools.sun.utils

import android.content.Intent
import android.os.Bundle

enum class Section(val id: String) {
    LEARN_SYMBOLS("learn_symbols"),
    TEST_SYMBOLS("test_symbols"),
    LEARN_SENTENCES("learn_sentences"),
    TEST_SENTENCES("test_sentences"),
    TEST_ALL("test_all");

    companion object {
        private val map = entries.toTypedArray().associateBy { it.id.lowercase() }
        fun of(id: String) = map[id.lowercase()] ?: LEARN_SYMBOLS
    }
}

enum class AnswerType {
    OPTION,
    ANSWER
}

inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, default: T) =
    getInt(key).let { if (it >= 0) enumValues<T>()[it] else default }

fun <T : Enum<T>> Bundle.putEnum(key: String, value: T) =
    putInt(key, value.ordinal)

inline fun <reified T : Enum<T>> Intent.getEnumExtra(key: String, default: T) =
    getIntExtra(key, default.ordinal).let { enumValues<T>()[it] }

fun <T : Enum<T>> Intent.putEnumExtra(key: String, value: T) =
    putExtra(key, value.ordinal)