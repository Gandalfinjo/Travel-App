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
 */
data class AiSuggestion(
    val destination: String,
    val description: String,
    val reason: String
)

data class AiSuggestionsUiState(
    val isLoading: Boolean = false,
    val suggestions: List<AiSuggestion> = emptyList(),
    val errorMessage: String? = null
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
                
                Format your response EXACTLY like this:
                DESTINATION: [name]
                DESCRIPTION: [description]
                REASON: [reason]
                ---
                (repeat for each suggestion)
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val suggestions = parseAiResponse(response.text ?: "")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    suggestions = suggestions
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

            for (line in lines) {
                when {
                    line.startsWith("DESTINATION:", ignoreCase = true) -> {
                        destination = line.substringAfter(":").trim()
                    }
                    line.startsWith("DESCRIPTION:", ignoreCase = true) -> {
                        description = line.substringAfter(":").trim()
                    }
                    line.startsWith("REASON:", ignoreCase = true) -> {
                        reason = line.substringAfter(":").trim()
                    }
                }
            }

            if (destination.isNotEmpty() && description.isNotEmpty() && reason.isNotEmpty()) {
                suggestions.add(AiSuggestion(destination, description, reason))
            }
        }

        return suggestions
    }
}