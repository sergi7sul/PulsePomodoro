package com.cherrytime.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.quote.QuoteEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuoteDaoTest {

    private lateinit var db: CherryTimeDatabase
    private lateinit var dao: QuoteDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CherryTimeDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.quoteDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insertAll_and_count() = runTest {
        val quotes = listOf(
            QuoteEntity(text = "A", author = "X", tags = "focus"),
            QuoteEntity(text = "B", author = "Y", tags = "rest"),
        )
        dao.insertAll(quotes)
        assertThat(dao.count()).isEqualTo(2)
    }

    @Test
    fun getNextByTag_returnsMatchingTag() = runTest {
        dao.insertAll(listOf(
            QuoteEntity(text = "Focus quote", author = "A", tags = "focus"),
            QuoteEntity(text = "Rest quote", author = "B", tags = "rest"),
        ))
        val quote = dao.getNextByTag("focus")
        assertThat(quote).isNotNull()
        assertThat(quote!!.tags).contains("focus")
    }

    @Test
    fun getNextByTag_returnsLeastShown() = runTest {
        dao.insertAll(listOf(
            QuoteEntity(id = 1, text = "Q1", author = "A", tags = "focus", shownCount = 3),
            QuoteEntity(id = 2, text = "Q2", author = "B", tags = "focus", shownCount = 0),
        ))
        val next = dao.getNextByTag("focus")
        assertThat(next!!.text).isEqualTo("Q2")
    }

    @Test
    fun markShown_incrementsCount() = runTest {
        dao.insertAll(listOf(QuoteEntity(id = 1, text = "Q", author = "A", tags = "focus")))
        dao.markShown(id = 1, timestamp = 1000L)
        val next = dao.getNext()
        assertThat(next!!.shownCount).isEqualTo(1)
        assertThat(next.lastShownAt).isEqualTo(1000L)
    }

    @Test
    fun getNext_returnsNullWhenEmpty() = runTest {
        assertThat(dao.getNext()).isNull()
    }
}
