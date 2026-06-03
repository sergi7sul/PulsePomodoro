package com.cherrytime.data.db

import android.content.Context
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.quote.QuoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val quoteDao: QuoteDao,
) {
    suspend fun seedIfNeeded() {
        if (quoteDao.count() > 0) return
        val json = context.assets.open("quotes.json").bufferedReader().readText()
        val array = JSONArray(json)
        val quotes = (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            QuoteEntity(
                text = obj.getString("text"),
                author = obj.getString("author"),
                tags = obj.getString("tags"),
            )
        }
        quoteDao.insertAll(quotes)
    }
}
