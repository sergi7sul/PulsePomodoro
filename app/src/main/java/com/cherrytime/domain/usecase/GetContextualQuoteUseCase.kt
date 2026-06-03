package com.cherrytime.domain.usecase

import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.Quote
import com.cherrytime.domain.repository.QuoteRepository
import java.util.Calendar
import javax.inject.Inject

class GetContextualQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(phase: Phase): Quote? {
        val tag = selectTag(phase)
        return repository.getNextForTag(tag)
    }

    private fun selectTag(phase: Phase): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            !phase.isBreak -> if (hour < 12) "energy" else "focus"
            else -> if (hour < 15) "rest" else "mindfulness"
        }
    }
}
