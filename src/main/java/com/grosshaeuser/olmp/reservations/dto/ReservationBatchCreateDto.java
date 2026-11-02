
package com.grosshaeuser.olmp.reservations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationBatchCreateDto {

    @NotBlank
    private String membershipNumber;

    @NotEmpty
    private List<Long> mediaIds;

    private String notes;
}