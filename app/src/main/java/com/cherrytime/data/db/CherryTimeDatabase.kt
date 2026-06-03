package com.cherrytime.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.quote.QuoteEntity
import com.cherrytime.data.db.session.SessionDao
import com.cherrytime.data.db.session.SessionEntity

@Database(
    entities = [SessionEntity::class, QuoteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CherryTimeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun quoteDao(): QuoteDao
}
