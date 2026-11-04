package com.grosshaeuser.olmp.loans.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanBatchReturnDto {

    @NotEmpty
    private List<@NotNull Long> inventoryItemIds;

    // Optional legacy input; employee identity is taken from the security context.
    private Long employeeId;
}
