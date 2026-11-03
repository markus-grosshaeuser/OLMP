
package com.grosshaeuser.olmp.reservations.mappers;

import com.grosshaeuser.olmp.shared.MappingChecks;
import com.grosshaeuser.olmp.inventory.mappers.MediaMapper;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.reservations.dto.ReservationDto;
import com.grosshaeuser.olmp.reservations.dto.ReservationEditDto;
import com.grosshaeuser.olmp.reservations.dto.ReservationSummaryDto;
import com.grosshaeuser.olmp.reservations.entity.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ReservationMapper {

    private final MemberMapper memberMapper;
    private final MediaMapper mediaMapper;

    public ReservationMapper(MemberMapper memberMapper, MediaMapper mediaMapper) {
        this.memberMapper = memberMapper;
        this.mediaMapper = mediaMapper;
    }

    public ReservationDto toDto(Reservation source) {
        return source == null ? null : new ReservationDto(
                source.getId(),
                memberMapper.mapMemberToMemberDTO(source.getMember()),
                mediaMapper.toSummary(source.getMedia(), List.of()),
                source.getReservationDate(),
                source.getStatus(),
                source.getNotes(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    public ReservationSummaryDto toSummary(Reservation source) {
        if (source == null) {
            return null;
        }
        return new ReservationSummaryDto(
                source.getId(),
                source.getMember().getFirstName() + " " + source.getMember().getLastName(),
                source.getMedia().getTitle(),
                source.getReservationDate(),
                source.getStatus()
        );
    }

    public ReservationEditDto toEditDto(Reservation source) {
        if (source == null) {
            return null;
        }
        return ReservationEditDto.builder()
                .id(source.getId())
                .status(source.getStatus())
                .notes(source.getNotes())
                .build();
    }

    public void update(ReservationEditDto source, Reservation target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        MappingChecks.sameId(source.getId(), target.getId(), "id");

        target.setStatus(source.getStatus());
        target.setNotes(source.getNotes());
    }
}