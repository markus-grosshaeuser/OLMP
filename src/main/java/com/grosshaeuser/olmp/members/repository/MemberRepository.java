package com.grosshaeuser.olmp.members.repository;

import com.grosshaeuser.olmp.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
