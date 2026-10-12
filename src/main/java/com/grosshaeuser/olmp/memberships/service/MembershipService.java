package com.grosshaeuser.olmp.memberships.service;

import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDto;
import com.grosshaeuser.olmp.memberships.entity.Membership;

import java.util.HashMap;
import java.util.Optional;

public interface MembershipService {
    Optional<Membership> createMembership(MembershipCreateDto dto);

    HashMap<String, Integer> getMembershipPeriodsInMonths();
}
