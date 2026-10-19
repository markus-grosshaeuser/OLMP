package com.grosshaeuser.olmp.shared;

import java.time.LocalDate;
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


}
