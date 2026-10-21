package com.grosshaeuser.olmp.members.dto;

import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class MemberEditDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MemberAccountEditDTO account;

    private boolean withContactInformation;
    @Nullable
    private MemberContactInformationEditDTO contact;

    private boolean withMembershipDetails;
    @Builder.Default
    private List<MembershipEditDTO> memberships = List.of();

}
