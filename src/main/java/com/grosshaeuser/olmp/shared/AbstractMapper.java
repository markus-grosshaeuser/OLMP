package com.grosshaeuser.olmp.shared;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public abstract class AbstractMapper {
    public static String saveParsedDateStringOrDefault(LocalDate date, String defaultString) {
        return saveParsedDateStringOrNull(date) != null ? saveParsedDateStringOrNull(date) : defaultString;
    }

    public static String saveParsedDateStringOrNull(LocalDate date) {
        if (date == null) {
            return null;
        }
        try {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate saveParsedDateOrDefault(String dateString, LocalDate defaultDate) {
        return saveParsedDateOrNull(dateString) != null ? saveParsedDateOrNull(dateString) : defaultDate;
    }

    public static LocalDate saveParsedDateOrNull(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime saveOffsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDateTime();
    }

    public static OffsetDateTime saveLocalDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
    }
}
