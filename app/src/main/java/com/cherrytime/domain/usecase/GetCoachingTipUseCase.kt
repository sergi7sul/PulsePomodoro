package com.cherrytime.domain.usecase

import com.cherrytime.data.remote.GeminiCoachingService
import com.cherrytime.data.remote.GeminiKeyStore
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.Quote
import com.cherrytime.domain.repository.QuoteRepository
import javax.inject.Inject

class GetCoachingTipUseCase @Inject constructor(
    private val gemini: GeminiCoachingService,
    private val keyStore: GeminiKeyStore,
    private val quoteRepository: QuoteRepository,
) {
    suspend operator fun invoke(phase: Phase): Quote? {
        val apiKey = keyStore.getKey()
        if (apiKey != null) {
            val prompt = buildPrompt(phase)
            val result = gemini.getTip(apiKey, prompt)
            if (result.isSuccess) {
                return Quote(text = result.getOrThrow(), author = "AI Coach")
            }
        }
        // Fallback to local quote
        val tag = if (phase.isBreak) "rest" else "focus"
        return quoteRepository.getNextForTag(tag)
    }

    private fun buildPrompt(phase: Phase): String = when (phase) {
        Phase.WORK ->
            "Give me one concise (max 2 sentences) motivational coaching tip for someone starting a 25-minute focus session. Be direct and energizing."
        Phase.SHORT_BREAK ->
            "Give me one concise (max 2 sentences) relaxation tip for a 5-minute break after deep work. Be calming and restorative."
        Phase.LONG_BREAK ->
            "Give me one concise (max 2 sentences) wellness tip for a 15-minute long break. Suggest a light physical activity or mindfulness practice."
    }
}
