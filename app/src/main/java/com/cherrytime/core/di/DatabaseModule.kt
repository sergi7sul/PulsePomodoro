package com.cherrytime.core.di

import android.content.Context
import androidx.room.Room
import com.cherrytime.data.db.CherryTimeDatabase
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.session.SessionDao
import com.cherrytime.data.repository.QuoteRepositoryImpl
import com.cherrytime.data.repository.TimerRepositoryImpl
import com.cherrytime.domain.repository.QuoteRepository
import com.cherrytime.domain.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CherryTimeDatabase =
        Room.databaseBuilder(context, CherryTimeDatabase::class.java, "cherrytime.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideSessionDao(db: CherryTimeDatabase): SessionDao = db.sessionDao()
    @Provides fun provideQuoteDao(db: CherryTimeDatabase): QuoteDao = db.quoteDao()

    @Provides
    @Singleton
    fun provideTimerRepository(impl: TimerRepositoryImpl): TimerRepository = impl

    @Provides
    @Singleton
    fun provideQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository = impl
}
