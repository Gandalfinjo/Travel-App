package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.database.models.CachedRate

@Dao
interface CachedRateDao {
    @Query("SELECT * FROM cached_rates WHERE currencyPair = :pair")
    suspend fun getRate(pair: String): CachedRate?

    @Query("SELECT * FROM cached_rates WHERE currencyPair LIKE :base || '_%'")
    suspend fun getRatesForBase(base: String): List<CachedRate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<CachedRate>)

    @Query("DELETE FROM cached_rates WHERE currencyPair LIKE :base || '_%'")
    suspend fun deleteRatesForBase(base: String)
}