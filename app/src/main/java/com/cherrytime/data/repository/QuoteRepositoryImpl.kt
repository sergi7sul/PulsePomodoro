package com.cherrytime.data.repository

import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.domain.model.Quote
import com.cherrytime.domain.repository.QuoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao,
) : QuoteRepository {

    override suspend fun getNextForTag(tag: String): Quote? {
        val entity = quoteDao.getNextByTag(tag) ?: quoteDao.getNext() ?: return null
        quoteDao.markShown(entity.id, System.currentTimeMillis())
        return Quote(text = entity.text, author = entity.author)
    }
}
