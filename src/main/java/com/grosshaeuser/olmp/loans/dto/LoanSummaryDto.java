package com.grosshaeuser.olmp.loans.dto;

import com.grosshaeuser.olmp.loans.LoanStatus;
import java.time.OffsetDateTime;

public record LoanSummaryDto(
        Long id,
        String memberName,
        String inventoryNumber,
        String mediaTitle,
        OffsetDateTime dueDate,
        LoanStatus status
) {
}
