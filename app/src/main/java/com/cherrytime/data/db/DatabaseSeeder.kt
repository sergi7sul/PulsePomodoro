package com.cherrytime.data.db

import android.content.Context
import com.cherrytime.data.db.quote.QuoteDao
import com.cherrytime.data.db.quote.QuoteEntity
import com.cherrytime.data.db.stretch.StretchExerciseDao
import com.cherrytime.data.db.stretch.StretchExerciseEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val quoteDao: QuoteDao,
    private val stretchDao: StretchExerciseDao,
) {
    suspend fun seedIfNeeded() {
        seedQuotes()
        seedExercises()
    }

    private suspend fun seedQuotes() {
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

    private suspend fun seedExercises() {
        if (stretchDao.count() > 0) return
        val exercises = listOf(
            StretchExerciseEntity(1, "Neck Roll", "Slowly roll your head in a full circle. Keep your shoulders relaxed.", 30),
            StretchExerciseEntity(2, "Shoulder Roll", "Roll both shoulders forward 5 times, then backward 5 times.", 30),
            StretchExerciseEntity(3, "Chest Opener", "Clasp your hands behind your back and gently squeeze your shoulder blades together.", 30),
            StretchExerciseEntity(4, "Forward Fold", "Stand up and hinge at the hips, letting your upper body hang toward the floor.", 30),
            StretchExerciseEntity(5, "Side Stretch", "Raise your right arm overhead and lean gently to the left. Hold, then switch sides.", 40),
            StretchExerciseEntity(6, "Wrist Circles", "Extend your arms and rotate your wrists in slow circles — 5 each direction.", 30),
            StretchExerciseEntity(7, "Spinal Twist", "Sit tall and gently rotate your torso to the right, then to the left.", 40),
        )
        stretchDao.insertAll(exercises)
    }
}
