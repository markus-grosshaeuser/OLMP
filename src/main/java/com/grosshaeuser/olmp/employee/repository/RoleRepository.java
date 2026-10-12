package com.grosshaeuser.olmp.employee.repository;

import com.grosshaeuser.olmp.employee.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Set<Role> findAllByNameIn(Set<String> names);
}
