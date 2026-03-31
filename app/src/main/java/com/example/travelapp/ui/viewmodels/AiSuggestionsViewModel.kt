package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.repositories.TripRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents a single AI-generated travel destination suggestion.
 *
 * @property destination Name of the suggested destination
 * @property description Brief description of the destination (2-3 sentences)
 * @property reason Explanation of why this matches the user's travel history
 * @property estimatedBudget User's estimated budget for which to provide a suggestion
 * @property currency Currency in which to set the budget (EUR currently fixed)
 * @property transport Transport which the AI suggests to use to the destination
 */
data class AiSuggestion(
    val destination: String,
    val description: String,
    val reason: String,
    val estimatedBudget: String = "",
    val currency: String = "EUR",
    val transport: String = "OTHER"
)

data class AiSuggestionsUiState(
    val isLoading: Boolean = false,
    val suggestions: List<AiSuggestion> = emptyList(),
    val errorMessage: String? = null,
    val customPrompt: String = "",
    val hasGeneratedOnce: Boolean = false
)

/**
 * ViewModel for AI-powered travel destination suggestions.
 *
 * Uses Google Gemini AI to analyze user's past trips and generate
 * personalized recommendations for future destinations.
 */
@HiltViewModel
class AiSuggestionsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val generativeModel: GenerativeModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiSuggestionsUiState())
    val uiState: StateFlow<AiSuggestionsUiState> = _uiState.asStateFlow()

    /**
     * Sets the custom prompt in the UI State
     *
     * @param prompt Custom prompt to set
     */
    fun onCustomPromptChange(prompt: String) {
        _uiState.update { it.copy(customPrompt = prompt) }
    }

    /**
     * Generates AI-powered destination suggestions based on user's trip history.
     *
     * Analyzes past trips (locations, dates, budgets) and uses Gemini AI to
     * suggest 3 new destinations that match the user's travel patterns.
     *
     * @param userId ID of the user for whom to generate suggestions
     */
    fun generateSuggestions(userId: Int) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val trips = tripRepository.getUserTrips(userId).first()

            if (trips.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No previous trips found. Take your first trip to get AI suggestions!"
                    )
                }

                return@launch
            }

            val tripsSummary = trips.joinToString("\n") { trip ->
                "- ${trip.location} (${trip.startDate} to ${trip.endDate}, Budget: ${trip.budget} ${trip.currency})"
            }

            val prompt = """
                Based on these previous trips:
                $tripsSummary
                
                Suggest 3 NEW travel destinations that would match the user's preferences.
                For each destination, provide:
                1. Destination name
                2. Brief description (2-3 sentences)
                3. Why it matches their travel history
                4. Estimated budget in EUR
                5. Transport to the destination
                
                Format your response EXACTLY like this:
                DESTINATION: [name]
                DESCRIPTION: [description]
                REASON: [reason]
                BUDGET: [number only, no currency symbol]
                CURRENCY: EUR
                TRANSPORT: [one of: PLANE, CAR, TRAIN, BUS, OTHER]
                ---
                (repeat for each suggestion)
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val suggestions = parseAiResponse(response.text ?: "")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    suggestions = suggestions,
                    hasGeneratedOnce = true
                )
            }
        }
        catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to generate suggestions: ${e.message}"
                )
            }
        }
    }

    /**
     * Generates AI-powered custom destination suggestions based on user's custom prompt.
     */
    fun generateCustomSuggestions() = viewModelScope.launch {
        val prompt = _uiState.value.customPrompt
        if (prompt.isBlank()) return@launch

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val aiPrompt = """
                The user is looking for travel destination suggestions with these preferences:
                "$prompt"
                
                Suggest 3 travel destinations that best match these preferences.
                For each destination, provide:
                1. Destination name
                2. Brief description (2-3 sentences)
                3. Why it matches the user's preferences
                4. Estimated budget in EUR
                5. Transport to the destination
                
                Format your response EXACTLY like this:
                DESTINATION: [name]
                DESCRIPTION: [description]
                REASON: [reason]
                BUDGET: [number only, no currency symbol]
                CURRENCY: EUR
                TRANSPORT: [one of: PLANE, CAR, TRAIN, BUS, OTHER]
                ---
                (repeat for each suggestion)
            """.trimIndent()

            val response = generativeModel.generateContent(aiPrompt)
            val suggestions = parseAiResponse(response.text ?: "")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    suggestions = suggestions,
                    hasGeneratedOnce = true
                )
            }
        }
        catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to generate suggestions: ${e.message}"
                )
            }
        }
    }

    /**
     * Parses AI response text into structured AiSuggestion objects.
     *
     * Expects response format:
     * DESTINATION: [AiSuggestion.destination]
     * DESCRIPTION: [AiSuggestion.description]
     * REASON: [AiSuggestion.reason]
     * BUDGET: [AiSuggestion.estimatedBudget]
     * CURRENCY: [AiSuggestion.currency]
     * TRANSPORT: [AiSuggestion.transport]
     * ---
     *
     * @param text Raw AI response text
     * @return List of parsed suggestions (may be empty if parsing fails)
     */
    private fun parseAiResponse(text: String): List<AiSuggestion> {
        val suggestions = mutableListOf<AiSuggestion>()

        val blocks = text.split("---").filter { it.isNotBlank() }

        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            var destination = ""
            var description = ""
            var reason = ""
            var budget = ""
            var currency = "EUR"
            var transport = "OTHER"

            for (line in lines) {
                when {
                    line.startsWith("DESTINATION:", ignoreCase = true) ->
                        destination = line.substringAfter(":").trim()
                    line.startsWith("DESCRIPTION:", ignoreCase = true) ->
                        description = line.substringAfter(":").trim()
                    line.startsWith("REASON:", ignoreCase = true) ->
                        reason = line.substringAfter(":").trim()
                    line.startsWith("BUDGET:", ignoreCase = true) ->
                        budget = line.substringAfter(":").trim()
                    line.startsWith("CURRENCY:", ignoreCase = true) ->
                        currency = line.substringAfter(":").trim()
                    line.startsWith("TRANSPORT:", ignoreCase = true) ->
                        transport = line.substringAfter(":").trim()
                }
            }

            if (destination.isNotEmpty() && description.isNotEmpty() && reason.isNotEmpty()) {
                suggestions.add(AiSuggestion(destination, description, reason, budget, currency, transport))
            }
        }

        return suggestions
    }
}