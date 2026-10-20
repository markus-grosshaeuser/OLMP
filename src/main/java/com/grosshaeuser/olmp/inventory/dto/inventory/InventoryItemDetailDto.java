package com.grosshaeuser.olmp.inventory.dto.inventory;

import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaReferenceDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record InventoryItemDetailDto(
        Long id,
        MediaReferenceDto media,
        String inventoryNumber,
        String barcode,
        InventoryItemCondition condition,
        LocationSummaryDto location,
        LocalDate acquiredAt,
        BigDecimal acquisitionCost,
        LocalDate withdrawnAt,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
