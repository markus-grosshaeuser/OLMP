package com.grosshaeuser.olmp.loans.dto;

import com.grosshaeuser.olmp.employees.dto.EmployeeDTO;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemDetailDto;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.loans.LoanStatus;

import java.time.OffsetDateTime;

public record LoanDto(
        Long id,
        MemberDTO member,
        InventoryItemDetailDto inventoryItem,
        EmployeeDTO employee,
        OffsetDateTime loanDate,
        OffsetDateTime dueDate,
        OffsetDateTime returnDate,
        LoanStatus status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
