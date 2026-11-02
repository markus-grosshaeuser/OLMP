
package com.grosshaeuser.olmp.loans.dto;

import com.grosshaeuser.olmp.loans.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEditDto {

    @NotNull
    private Long id;

    @NotNull
    private LoanStatus status;

    private OffsetDateTime returnDate;

    private String notes;
}