package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.entity.Media;

import java.util.Objects;
import java.util.function.Function;

final class MappingChecks {

    private MappingChecks() {
    }

    static void sameId(Long expected, Long actual, String field) {
        if (expected == null || !expected.equals(actual)) {
            throw new IllegalArgumentException(field + " must match the target entity ID");
        }
    }

    static <T> void optionalReference(Long expected, T reference, Function<T, Long> id, String field) {
        if (expected == null) {
            if (reference == null) {
                return;
            }
        } else if (reference != null && expected.equals(id.apply(reference))) {
            return;
        }
        throw new IllegalArgumentException(field + " must match the resolved reference ID");
    }

    static void sameMedia(Media expected, Media actual) {
        Objects.requireNonNull(actual, "association media");
        if (expected != actual && (expected.getId() == null || !expected.getId().equals(actual.getId()))) {
            throw new IllegalArgumentException("Association belongs to a different media");
        }
    }
}
