package com.grosshaeuser.olmp.ui.internal_ui.shared.reservations;

import com.grosshaeuser.olmp.reservations.service.ReservationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class ReservationContext {
    private final ReservationService reservationService;
}
