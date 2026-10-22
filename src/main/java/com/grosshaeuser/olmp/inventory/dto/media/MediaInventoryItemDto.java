package com.grosshaeuser.olmp.inventory.dto.media;

import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MediaInventoryItemDto(
        @Positive Long id,
        @Size(max = 100) String inventoryNumber,
        @Size(max = 100) String barcode,
        @NotNull InventoryItemCondition condition,
        @Positive Long locationId,
        LocalDate acquiredAt,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal acquisitionCost,
        LocalDate withdrawnAt,
        String notes
) {
    public MediaInventoryItemDto(Long id, String inventoryNumber, String barcode) {
        this(id, inventoryNumber, barcode, InventoryItemCondition.GOOD, null, null, null, null, null);
    }

    @AssertTrue(message = "withdrawal date must be on or after acquisition date")
    public boolean isWithdrawalDateValid() {
        return withdrawnAt == null || acquiredAt == null || !withdrawnAt.isBefore(acquiredAt);
    }

    public static class MediaInventoryItemDtoBuilder {
        private InventoryItemCondition condition = InventoryItemCondition.GOOD;
    }
}
