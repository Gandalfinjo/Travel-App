package com.example.travelapp.api.repositories

import com.example.travelapp.api.currency.ExchangeRateApi
import com.example.travelapp.database.dao.CachedRateDao
import com.example.travelapp.database.models.CachedRate
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours

@Singleton
class CurrencyRepository @Inject constructor(
    private val exchangeRateApi: ExchangeRateApi,
    private val cachedRateDao: CachedRateDao
) {
    // Supported currencies
    companion object {
        val SUPPORTED_CURRENCIES = listOf(
            "EUR", "USD", "GBP", "CHF", "RSD", "HUF",
            "RON", "BGN", "RUB", "TRY", "JPY", "CNY",
            "AUD", "CAD"
        )
    }

    /**
     * Converts an amount from one currency to another.
     * Uses cached rates if available and less than 24 hours old.
     * Falls back to stale cache if API call fails.
     * Returns original amount if no cache available and API fails.
     *
     * @param amount Amount to convert
     * @param from Source currency code
     * @param to Target currency code
     * @return Converted amount, or original amount if conversion unavailable
     */
    suspend fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount

        val pair = "${from}_${to}"

        // Check cache first
        val cached = cachedRateDao.getRate(pair)
        val now = System.currentTimeMillis()

        if (cached != null && now - cached.timestamp < CACHE_VALIDITY_MS) {
            return amount * cached.rate
        }

        // Try fetching fresh rates
        return try {
            val response = exchangeRateApi.getRates(baseCurrency = from)

            if (response.result == "success") {
                // Save all rates for this base currency to cache
                val rates = response.conversionRates.map { (currency, rate) ->
                    CachedRate(
                        currencyPair = "${from}_${currency}",
                        rate = rate,
                        timestamp = now
                    )
                }

                cachedRateDao.deleteRatesForBase(from)
                cachedRateDao.insertAll(rates)

                val rate = response.conversionRates[to] ?: 1.0
                amount * rate
            }
            else {
                // API returned error, use stale cache if available
                cached?.let { amount * it.rate } ?: amount
            }
        }
        catch (_: Exception) {
            // Network error, use stale cache if available
            cached?.let { amount * it.rate } ?: amount
        }
    }

    /**
     * Converts a list of amounts from different currencies to a target currency.
     * Fetches rates for each unique source currency.
     */
    suspend fun convertAll(
        amounts: List<Pair<Double, String>>,
        to: String
    ): Double {
        return amounts.sumOf { (amount, from) -> convert(amount, from, to) }
    }
}