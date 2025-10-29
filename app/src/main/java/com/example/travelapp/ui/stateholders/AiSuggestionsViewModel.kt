package com.example.travelapp.ui.stateholders

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

@HiltViewModel
class AiSuggestionsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val generativeModel: GenerativeModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiSuggestionsUiState())
    val uiState: StateFlow<AiSuggestionsUiState> = _uiState.asStateFlow()

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