package com.example.travelapp.hilt

import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing AI/ML dependencies.
 *
 * Configures and provides Google Gemini AI model for generating
 * personalized trip recommendations.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyBsGpf5-VxHl4j87Zll4mbUxmFaqWptb9w"
        )
    }
}