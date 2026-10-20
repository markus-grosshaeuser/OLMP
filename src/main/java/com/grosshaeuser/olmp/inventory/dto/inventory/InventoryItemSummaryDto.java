package com.grosshaeuser.olmp.inventory.dto.inventory;

import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;

import java.time.LocalDate;

public record InventoryItemSummaryDto(
        Long id,
        Long mediaId,
        String mediaTitle,
        String inventoryNumber,
        String barcode,
        InventoryItemCondition condition,
        LocationSummaryDto location,
        LocalDate withdrawnAt
) {
}

