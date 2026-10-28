package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
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

import java.util.HashMap;
import java.util.List;
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
        if (dto.getMembershipExpirationDate() == null) {
            int period = getMembershipPeriodsInMonths().get(dto.getMembershipType());
            dto.setMembershipExpirationDate(dto.getMembershipStartDate().plusMonths(period));
        }
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
    public Optional<Membership> findActiveByMemberId(Long memberId) {
        checkPermission(SecurityConstants.Privileges.READ_MEMBERSHIP, "You have no permission to read memberships.");
        return membershipRepository.findActiveByMemberId(memberId);
    }

    @Override
    public List<Membership> findAllByMemberId(Long memberId) {
        checkPermission(SecurityConstants.Privileges.READ_MEMBERSHIP, "You have no permission to read memberships.");
        return membershipRepository.findAllByMemberIdOrderByStartDateDescIdDesc(memberId);
    }

    @Override
    @Transactional
    public Optional<Membership> updateMembership(MembershipEditDTO dto) {
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBERSHIP, "You have no permission to update memberships.");
        if (dto.getMembershipStatus().equals(MembershipStatus.CANCELLED.name())) {
            checkPermission(SecurityConstants.Privileges.CANCEL_MEMBERSHIP, "You have no permission to cancel memberships.");
        }
        return membershipRepository.findById(dto.getId())
                .map(membership -> {
                    membership.setType(MembershipType.valueOf(dto.getMembershipType()));
                    membership.setStatus(MembershipStatus.valueOf(dto.getMembershipStatus()));
                    membership.setExpirationDate(dto.getExpirationDate());
                    membership.setRenewalDate(dto.getRenewalDate());
                    membership.setCancellationDate(dto.getCancellationDate());
                    return membershipRepository.save(membership);
                });
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
