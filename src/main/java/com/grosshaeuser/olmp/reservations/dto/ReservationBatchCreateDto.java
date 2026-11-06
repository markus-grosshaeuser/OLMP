
package com.grosshaeuser.olmp.reservations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationBatchCreateDto {

    @NotBlank
    @Size(max = 50)
    private String membershipNumber;

    @NotEmpty
    private List<@NotNull @Positive Long> mediaIds;

    private String notes;
}
