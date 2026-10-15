package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.mapper.MembershipMapper;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import com.grosshaeuser.olmp.memberships.service.MembershipNumberGenerator;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipNumberGenerator membershipNumberGenerator;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public Optional<Membership> createMembership(MembershipCreateDTO dto) {
        checkPermission(SecurityConstants.Privileges.CREATE_MEMBERSHIP, "You have no permission to create a membership.");
        return Optional.of(membershipRepository.save(MembershipMapper.createMembershipFromMembershipCreateDto(dto, membershipNumberGenerator.generate())));
    }

    @Override
    public HashMap<String, Integer> getMembershipPeriodsInMonths() {
        HashMap<String, Integer> membershipPeriods = new HashMap<>();
        membershipPeriods.put("STANDARD", 12);
        membershipPeriods.put("STUDENT", 6);
        membershipPeriods.put("SENIOR", 12);
        membershipPeriods.put("PREMIUM", 12);
        return membershipPeriods;
    }

    @Override
    public Optional<Membership> findByMemberId(Long memberId) {
        checkPermission(SecurityConstants.Privileges.READ_MEMBERSHIP, "You have no permission to read memberships.");
        return membershipRepository.findByMemberId(memberId);
    }

    @Override
    @Transactional
    public Optional<Membership> updateMembership(MembershipEditDTO dto) {
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBERSHIP, "You have no permission to update memberships.");
        if (dto.getMembershipStatus().equals(Membership.MembershipStatus.CANCELLED.name())) {
            checkPermission(SecurityConstants.Privileges.CANCEL_MEMBERSHIP, "You have no permission to cancel memberships.");
        }
        return membershipRepository.findById(dto.getId())
                .map(membership -> {
                    membership.setType(Membership.MembershipType.valueOf(dto.getMembershipType()));
                    membership.setStatus(Membership.MembershipStatus.valueOf(dto.getMembershipStatus()));
                    membership.setExpirationDate(LocalDate.parse(dto.getExpirationDate()));
                    membership.setRenewalDate(!dto.getRenewalDate().isBlank() ? LocalDate.parse(dto.getRenewalDate()) : null);
                    membership.setCancellationDate(!dto.getCancellationDate().isBlank() ? LocalDate.parse(dto.getCancellationDate()) : null);
                    return membershipRepository.save(membership);
                });
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
