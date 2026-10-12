package com.grosshaeuser.olmp.memberships.service;

import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDto;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipNumberGenerator membershipNumberGenerator;

    @Override
    @Transactional
    public Optional<Membership> createMembership(MembershipCreateDto dto) {
        return Optional.of(membershipRepository.save(
                Membership.builder()
                        .member(dto.getMember())
                        .membershipNumber(membershipNumberGenerator.generate())
                        .type(Membership.MembershipType.valueOf(dto.getMembershipType()))
                        .status(Membership.MembershipStatus.valueOf(dto.getMembershipStatus()))
                        .startDate(dto.getMembershipStartDate())
                        .expirationDate(dto.getMembershipExpirationDate())
                        .renewalDate(dto.getMembershipRenewalDate())
                        .cancellationDate(dto.getMembershipCancellationDate())
                        .build()));
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
}
