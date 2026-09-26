package com.grosshaeuser.olmp.security.repositories;

import com.grosshaeuser.olmp.security.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Fetches a role by its name and eagerly loads its privileges in a single query.
     * This is highly efficient for authorization checks where you need to validate
     * the privileges associated with a specific role.
     */
    @Query("SELECT r FROM Role r " +
            "LEFT JOIN FETCH r.privileges " +
            "WHERE r.name = :name")
    Optional<Role> findByNameWithPrivileges(@Param("name") String name);

    /**
     * Standard find by name. Use this when you only need the Role entity itself
     * (e.g., for a simple dropdown in an admin UI) and don't need to inspect its privileges.
     */
    Optional<Role> findByName(String name);
}