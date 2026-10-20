package com.grosshaeuser.olmp.inventory.dto.inventory;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryItemCreateDto {

    @NotNull
    @Positive
    private Long mediaId;

    @NotBlank
    @Size(max = 100)
    private String inventoryNumber;

    @Size(max = 100)
    private String barcode;

    @NotNull
    private InventoryItemCondition condition = InventoryItemCondition.GOOD;

    @Positive
    private Long locationId;

    private LocalDate acquiredAt;

    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    private BigDecimal acquisitionCost;

    private LocalDate withdrawnAt;

    private String notes;

    @AssertTrue(message = "withdrawal date must be on or after acquisition date")
    public boolean isWithdrawalDateValid() {
        return withdrawnAt == null || acquiredAt == null || !withdrawnAt.isBefore(acquiredAt);
    }
}

