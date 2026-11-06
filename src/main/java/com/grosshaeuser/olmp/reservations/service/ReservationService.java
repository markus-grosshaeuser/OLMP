package com.grosshaeuser.olmp.reservations.service;

import com.grosshaeuser.olmp.reservations.ReservationStatus;
import com.grosshaeuser.olmp.reservations.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
    List<ReservationSummaryDto> findReservations(String query, ReservationStatus status, Pageable pageable);

    long countReservations(String query, ReservationStatus status);

    Optional<ReservationDto> findReservation(long id);

    List<ReservationMediaDto> findMedia(String query, Pageable pageable);

    List<ReservationSummaryDto> createReservations(ReservationBatchCreateDto dto);

    void updateReservation(ReservationEditDto dto);
}
