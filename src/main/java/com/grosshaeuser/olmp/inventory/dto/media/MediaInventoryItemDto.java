package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record MediaInventoryItemDto(
        @Positive Long id,
        String inventoryNumber,
        String barcode
) {
}
