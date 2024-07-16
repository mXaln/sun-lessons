package org.bibletranslationtools.sun.utils

object TallyMarkConverter {
    fun toText(number: Int): String {
        var text = ""
        val fivesCount = number / 5

        for (i in 1..fivesCount) {
            text += "5"
        }

        val remainder = number % 5

        if (remainder > 0) {
            text += number % 5
        }

        return text
    }
}