package com.grosshaeuser.olmp.members.repository;

import com.grosshaeuser.olmp.members.entity.MemberAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberAccountRepository extends JpaRepository<MemberAccount, Long> {
    Optional<MemberAccount> findByEmail(String email);
}
