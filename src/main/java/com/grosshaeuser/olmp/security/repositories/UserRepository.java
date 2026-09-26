package com.grosshaeuser.olmp.security.repositories;

import com.grosshaeuser.olmp.security.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * This query solves the N+1 problem specifically for the login use case.
     * By using 'JOIN FETCH', we instruct JPA to fetch the user, their roles,
     * and the privileges within those roles in a single SQL JOIN query.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.privileges " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPrivileges(@Param("username") String username);

    // Standard findByUsername remains LAZY, which is perfect for
    // administrative tasks where roles aren't needed.
    Optional<User> findByUsername(String username);
}