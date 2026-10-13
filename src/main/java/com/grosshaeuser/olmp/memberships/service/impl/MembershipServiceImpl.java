package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import com.grosshaeuser.olmp.memberships.service.MembershipNumberGenerator;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
    public Optional<Membership> createMembership(MembershipCreateDTO dto) {
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

    @Override
    public Optional<Membership> findByMemberId(Long memberId) {
        return membershipRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<Membership> updateMembership(MembershipEditDTO dto) {
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
}
