package com.cherrytime.data.db.quote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun count(): Int

    @Query("""
        SELECT * FROM quotes
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY shownCount ASC, lastShownAt ASC
        LIMIT 1
    """)
    suspend fun getNextByTag(tag: String): QuoteEntity?

    @Query("""
        SELECT * FROM quotes
        ORDER BY shownCount ASC, lastShownAt ASC
        LIMIT 1
    """)
    suspend fun getNext(): QuoteEntity?

    @Query("UPDATE quotes SET shownCount = shownCount + 1, lastShownAt = :timestamp WHERE id = :id")
    suspend fun markShown(id: Long, timestamp: Long)
}
