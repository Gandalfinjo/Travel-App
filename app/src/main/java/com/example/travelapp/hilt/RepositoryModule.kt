package com.example.travelapp.hilt

import com.example.travelapp.api.currency.ExchangeRateApi
import com.example.travelapp.api.repositories.CurrencyRepository
import com.example.travelapp.database.dao.CachedRateDao
import com.example.travelapp.database.dao.ExpenseDao
import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.repositories.ExpenseRepository
import com.example.travelapp.database.repositories.TripRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCurrencyRepository(exchangeRateApi: ExchangeRateApi, cachedRateDao: CachedRateDao): CurrencyRepository =
        CurrencyRepository(exchangeRateApi, cachedRateDao)

    @Provides
    @Singleton
    fun provideExpenseRepository(expenseDao: ExpenseDao, currencyRepository: CurrencyRepository): ExpenseRepository =
        ExpenseRepository(expenseDao, currencyRepository)

    @Provides
    @Singleton
    fun provideTripRepository(tripDao: TripDao) =
        TripRepository(tripDao)
}