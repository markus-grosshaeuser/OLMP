package com.grosshaeuser.olmp.memberships.repository;

import com.grosshaeuser.olmp.memberships.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findAllByMemberIdOrderByStartDateDescIdDesc(Long memberId);

    @Query("SELECT m FROM Membership m WHERE m.member.id = :memberId AND m.status = 'ACTIVE'")
    Optional<Membership> findActiveByMemberId(Long memberId);
}
