package com.grosshaeuser.olmp.security.repositories;

import com.grosshaeuser.olmp.security.entities.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByName(String name);
}
