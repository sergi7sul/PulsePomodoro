package com.cherrytime.domain.repository

import com.cherrytime.domain.model.Quote

interface QuoteRepository {
    suspend fun getNextForTag(tag: String): Quote?
}
