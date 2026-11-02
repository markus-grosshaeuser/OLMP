package com.grosshaeuser.olmp.shared;

import com.grosshaeuser.olmp.inventory.entity.Media;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Function;

public final class MappingChecks {

    private MappingChecks() {
    }

    public static void sameId(Long expected, Long actual, String field) {
        if (expected == null || !expected.equals(actual)) {
            throw new IllegalArgumentException(field + " must match the target entity ID");
        }
    }

    public static <T> void optionalReference(Long expected, T reference, Function<T, Long> id, String field) {
        if (expected == null) {
            if (reference == null) {
                return;
            }
        } else if (reference != null && expected.equals(id.apply(reference))) {
            return;
        }
        throw new IllegalArgumentException(field + " must match the resolved reference ID");
    }

    public static void sameMedia(Media expected, Media actual) {
        Objects.requireNonNull(actual, "association media");
        if (expected != actual && (expected.getId() == null || !expected.getId().equals(actual.getId()))) {
            throw new IllegalArgumentException("Association belongs to a different media");
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
