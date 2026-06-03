package com.cherrytime.data.db.quote

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String,
    val tags: String,
    val shownCount: Int = 0,
    val lastShownAt: Long = 0L,
)
