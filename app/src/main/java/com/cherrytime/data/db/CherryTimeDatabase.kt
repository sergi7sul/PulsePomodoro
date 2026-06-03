package com.cherrytime.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.quote.QuoteEntity
import com.cherrytime.data.db.session.SessionDao
import com.cherrytime.data.db.session.SessionEntity
import com.cherrytime.data.db.stretch.StretchExerciseDao
import com.cherrytime.data.db.stretch.StretchExerciseEntity

@Database(
    entities = [SessionEntity::class, QuoteEntity::class, StretchExerciseEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class CherryTimeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun quoteDao(): QuoteDao
    abstract fun stretchExerciseDao(): StretchExerciseDao
}
