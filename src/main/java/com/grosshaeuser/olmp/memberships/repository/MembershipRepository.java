package com.grosshaeuser.olmp.memberships.repository;

import com.grosshaeuser.olmp.memberships.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
