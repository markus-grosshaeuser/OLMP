package com.grosshaeuser.olmp.memberships.repository;

import com.grosshaeuser.olmp.memberships.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByMemberId(Long memberId);
}
