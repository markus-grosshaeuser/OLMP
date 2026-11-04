package com.grosshaeuser.olmp.loans.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanBatchCreateDto {

    @NotBlank
    private String membershipNumber;

    @NotEmpty
    private List<@NotNull Long> inventoryItemIds;

    @NotNull
    private OffsetDateTime dueDate;

    private String notes;

    // Optional legacy input; the service records the authenticated employee.
    private Long employeeId;
}
