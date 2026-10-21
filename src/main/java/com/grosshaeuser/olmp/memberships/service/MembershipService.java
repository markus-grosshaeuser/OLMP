package com.grosshaeuser.olmp.memberships.service;

import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface MembershipService {
    Optional<Membership> createMembership(MembershipCreateDTO dto);

    HashMap<String, Integer> getMembershipPeriodsInMonths();

    Optional<Membership> findActiveByMemberId(Long memberId);

    List<Membership> findAllByMemberId(Long memberId);

    Optional<Membership> updateMembership(MembershipEditDTO dto);
}
