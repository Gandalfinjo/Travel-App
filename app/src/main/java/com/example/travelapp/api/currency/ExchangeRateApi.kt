package com.example.travelapp.api.currency

import com.example.travelapp.BuildConfig
import com.example.travelapp.api.currency.models.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path


const val BASE_URL = "https://v6.exchangerate-api.com/"
const val EXCHANGE_RATE_API_KEY = BuildConfig.EXCHANGE_RATE_API_KEY

/**
 * Retrofit API interface for Exchange Rate API.
 *
 * Provides current exchange rates.
 */
interface ExchangeRateApi {
    /**
     * Fetches current exchange rates for a specified currency.
     *
     * @param apiKey Exchange Rate API key (default: pre-configured key)
     * @param baseCurrency Currency code (e.g. GBP, EUR, RSD)
     * @return [ExchangeRateResponse] containing result, base code and conversion rates
     */
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getRates(
        @Path("apiKey") apiKey: String = EXCHANGE_RATE_API_KEY,
        @Path("baseCurrency") baseCurrency: String
    ) : ExchangeRateResponse
}