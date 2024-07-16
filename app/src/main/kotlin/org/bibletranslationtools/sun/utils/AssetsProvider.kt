package org.bibletranslationtools.sun.utils

import android.content.Context
import android.graphics.drawable.Drawable

class AssetsProvider {
    companion object {
        fun readText(context: Context, fileName: String): String? {
            return try {
                context.assets.open(fileName).use { inputStream ->
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    String(buffer, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                null
            }
        }

        fun readDrawable(context: Context, fileName: String): Drawable? {
            return try {
                context.assets.open(fileName).use { inputStream ->
                    Drawable.createFromStream(inputStream, null)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}