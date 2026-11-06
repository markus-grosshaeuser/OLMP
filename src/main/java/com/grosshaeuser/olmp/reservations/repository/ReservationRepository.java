package com.grosshaeuser.olmp.reservations.repository;

import com.grosshaeuser.olmp.reservations.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jspecify.annotations.NonNull;
import com.grosshaeuser.olmp.reservations.ReservationStatus;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"member", "media"})
    Page<Reservation> findAll(@NonNull Specification<Reservation> specification, @NonNull Pageable pageable);

    boolean existsByMemberIdAndMediaIdAndStatus(Long memberId, Long mediaId, ReservationStatus status);
}
