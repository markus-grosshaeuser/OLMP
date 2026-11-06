package com.grosshaeuser.olmp.reservations.service.impl;

import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.reservations.ReservationStatus;
import com.grosshaeuser.olmp.reservations.dto.*;
import com.grosshaeuser.olmp.reservations.entity.Reservation;
import com.grosshaeuser.olmp.reservations.mappers.ReservationMapper;
import com.grosshaeuser.olmp.reservations.repository.ReservationRepository;
import com.grosshaeuser.olmp.reservations.service.ReservationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('" + ROOT_ADMIN + "', '" + ADMIN + "', '" + STAFF + "')")
public class ReservationServiceImpl implements ReservationService {
    private static final Set<String> SORT_PROPERTIES = Set.of("id", "member.lastName", "media.title", "reservationDate", "status");

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final EntityManager entityManager;
    private final Validator validator;

    @Override
    public List<ReservationSummaryDto> findReservations(String query, ReservationStatus status, Pageable pageable) {
        Sort sort = Sort.by(pageable.getSort().stream()
                .filter(order -> SORT_PROPERTIES.contains(order.getProperty())).toList());
        if (sort.isUnsorted()) {
            sort = Sort.by("reservationDate");
        }
        return reservationRepository.findAll(filter(query, status), PageRequest.of(pageable.getPageNumber(),
                        pageable.getPageSize(), sort.and(Sort.by("id"))))
                .stream().map(reservationMapper::toSummary).toList();
    }

    @Override
    public long countReservations(String query, ReservationStatus status) {
        return reservationRepository.count(filter(query, status));
    }

    private Specification<Reservation> filter(String query, ReservationStatus status) {
        String text = query == null ? "" : query.strip();
        String pattern = searchPattern(text);
        return (root, criteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!text.isEmpty()) {
                var member = root.join("member");
                var memberships = criteria.subquery(Long.class);
                var membership = memberships.from(Membership.class);
                memberships.select(membership.get("member").get("id"))
                        .where(cb.like(cb.lower(membership.get("membershipNumber")), pattern, '!'));
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(member.get("firstName"), " "), member.get("lastName"))), pattern, '!'),
                        cb.like(cb.lower(root.join("media").get("title")), pattern, '!'),
                        member.get("id").in(memberships)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Override
    public Optional<ReservationDto> findReservation(long id) {
        return reservationRepository.findById(id).map(reservationMapper::toDto);
    }

    @Override
    public List<ReservationMediaDto> findMedia(String query, Pageable pageable) {
        return entityManager.createQuery("""
                select new com.grosshaeuser.olmp.reservations.dto.ReservationMediaDto(m.id, m.title, t.name, m.publicationYear)
                from Media m join m.mediaType t
                where lower(m.title) like :pattern escape '!'
                   or lower(m.subtitle) like :pattern escape '!'
                   or exists (select i.id from MediaIdentifier i where i.media = m
                              and lower(i.identifierValue) like :pattern escape '!')
                order by m.title, m.id
                """, ReservationMediaDto.class)
                .setParameter("pattern", searchPattern(query))
                .setFirstResult(Math.toIntExact(pageable.getOffset())).setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    @Override
    @Transactional
    public List<ReservationSummaryDto> createReservations(ReservationBatchCreateDto dto) {
        validate(dto);
        if (new HashSet<>(dto.getMediaIds()).size() != dto.getMediaIds().size()) {
            throw new IllegalArgumentException("A title may only appear once in a batch.");
        }
        Membership membership = entityManager.createQuery("select m from Membership m where m.membershipNumber = :number", Membership.class)
                .setParameter("number", dto.getMembershipNumber().strip()).getResultStream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No membership found for this number."));
        LocalDate today = LocalDate.now();
        if (membership.getStatus() != MembershipStatus.ACTIVE || membership.getStartDate().isAfter(today)
                || (membership.getExpirationDate() != null && membership.getExpirationDate().isBefore(today))
                || (membership.getCancellationDate() != null && !membership.getCancellationDate().isAfter(today))) {
            throw new IllegalArgumentException("This membership is not currently active.");
        }
        // Lock titles in a stable order so concurrent batches cannot create duplicate pending reservations.
        List<Media> titles = dto.getMediaIds().stream().sorted().map(id -> {
            Media media = entityManager.find(Media.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (media == null) {
                throw new IllegalArgumentException("A title no longer exists: " + id);
            }
            return media;
        }).toList();
        for (Media media : titles) {
            if (reservationRepository.existsByMemberIdAndMediaIdAndStatus(membership.getMember().getId(), media.getId(), ReservationStatus.PENDING)) {
                throw new IllegalArgumentException("This member already has a pending reservation for " + media.getTitle() + ".");
            }
        }
        OffsetDateTime now = OffsetDateTime.now();
        return reservationRepository.saveAll(titles.stream().map(media -> Reservation.builder()
                        .member(membership.getMember()).media(media).reservationDate(now)
                        .status(ReservationStatus.PENDING).notes(dto.getNotes()).build()).toList())
                .stream().map(reservationMapper::toSummary).toList();
    }

    @Override
    @Transactional
    public void updateReservation(ReservationEditDto dto) {
        validate(dto);
        Reservation reservation = entityManager.find(Reservation.class, dto.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (reservation == null) {
            throw new IllegalArgumentException("This reservation no longer exists.");
        }
        entityManager.refresh(reservation, LockModeType.PESSIMISTIC_WRITE);
        if (dto.getExpectedUpdatedAt() != null && !dto.getExpectedUpdatedAt().isEqual(reservation.getUpdatedAt())) {
            throw new OptimisticLockingFailureException("This reservation was changed by another employee. Reopen its details and try again.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING && dto.getStatus() != reservation.getStatus()) {
            throw new IllegalArgumentException("This reservation is closed and cannot change status. Create a new reservation instead.");
        }
        reservationMapper.update(dto, reservation);
    }

    private String searchPattern(String query) {
        String text = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return "%" + text.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private void validate(Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Reservation details are required.");
        }
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            var violation = violations.iterator().next();
            throw new IllegalArgumentException(violation.getPropertyPath() + ": " + violation.getMessage());
        }
    }
}
