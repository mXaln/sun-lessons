package org.bibletranslationtools.sun.data.model

interface TestCard {
    val id: String
    var correct: Boolean?
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}