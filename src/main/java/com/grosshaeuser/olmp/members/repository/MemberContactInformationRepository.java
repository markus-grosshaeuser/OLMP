package com.grosshaeuser.olmp.members.repository;

import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberContactInformationRepository extends JpaRepository<MemberContactInformation, Long> {

    Optional<MemberContactInformation> findByMemberId(Long memberId);
}
