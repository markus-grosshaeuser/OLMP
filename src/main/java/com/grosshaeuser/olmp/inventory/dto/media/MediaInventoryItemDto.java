package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MediaInventoryItemDto(
        @Positive Long id,
        String inventoryNumber,
        String barcode
) {
    public static MediaInventoryItemDto newCopy() {
        String uuid = UUID.randomUUID().toString();
        return new MediaInventoryItemDto(null, uuid, uuid);
    }
}
