package com.cherrytime.data.db.stretch

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StretchExerciseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<StretchExerciseEntity>)

    @Query("SELECT * FROM stretch_exercises ORDER BY id ASC")
    suspend fun getAll(): List<StretchExerciseEntity>

    @Query("SELECT COUNT(*) FROM stretch_exercises")
    suspend fun count(): Int
}
