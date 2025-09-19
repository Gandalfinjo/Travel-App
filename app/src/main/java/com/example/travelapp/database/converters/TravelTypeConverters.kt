package com.example.travelapp.database.converters

import androidx.room.TypeConverter
import com.example.travelapp.database.models.enums.NotificationType
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.models.enums.TripStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TravelTypeConverters {
    @TypeConverter
    fun fromTimestampMillis(timestampMillis: Long?): LocalDate? {
        return timestampMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    @TypeConverter
    fun toTimestampMillis(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    }

    @TypeConverter
    fun tripStatusFromString(value: String?): TripStatus? = value?.let { TripStatus.valueOf(it) }

    @TypeConverter
    fun tripStatusToString(status: TripStatus?): String? = status?.name

    @TypeConverter
    fun transportFromString(value: String?): TransportType? = value?.let { TransportType.valueOf(it) }

    @TypeConverter
    fun transportToString(t: TransportType?): String? = t?.name

    @TypeConverter
    fun notifTypeFromString(value: String?): NotificationType? = value?.let { NotificationType.valueOf(it) }

    @TypeConverter
    fun notifTypeToString(t: NotificationType?): String? = t?.name
}