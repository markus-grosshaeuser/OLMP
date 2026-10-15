package com.grosshaeuser.olmp.employees.repository;

import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeAccountRepository extends JpaRepository<EmployeeAccount, Long> {
    Optional<EmployeeAccount> findByUsername(String username);

    boolean existsByUsername(String username);
}
