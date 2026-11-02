
package com.grosshaeuser.olmp.reservations.dto;

import com.grosshaeuser.olmp.reservations.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEditDto {

    @NotNull
    private Long id;

    @NotNull
    private ReservationStatus status;

    private String notes;
}