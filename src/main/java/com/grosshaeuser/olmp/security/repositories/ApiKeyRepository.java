package com.grosshaeuser.olmp.security.repositories;

import com.grosshaeuser.olmp.security.entities.ApiKey;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    @NullMarked
    Optional<ApiKey> findById(UUID id);

    @Query("SELECT a FROM ApiKey a " +
            "JOIN FETCH a.user u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.privileges p " +
            "WHERE a.id = :id")
    Optional<ApiKey> findByIdWithFullUserContext(@Param("id") UUID id);
}
