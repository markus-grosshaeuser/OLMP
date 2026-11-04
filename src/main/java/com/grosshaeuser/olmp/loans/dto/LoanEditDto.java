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

    @NotNull
    private OffsetDateTime dueDate;

    // Return timestamps are assigned by the service when a loan is returned.
    private OffsetDateTime returnDate;

    private String notes;

    // Supplied by the UI to detect edits made since the dialog was opened.
    private OffsetDateTime expectedUpdatedAt;
}
