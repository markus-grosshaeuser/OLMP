package com.grosshaeuser.olmp.members.repository;

import com.grosshaeuser.olmp.members.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m " +
            "WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Slice<Member> search(String searchTerm, Pageable pageable);
}
