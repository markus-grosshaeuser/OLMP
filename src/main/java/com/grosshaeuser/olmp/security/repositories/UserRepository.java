package com.grosshaeuser.olmp.security.repositories;

import com.grosshaeuser.olmp.security.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.privileges " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPrivileges(@Param("username") String username);

}