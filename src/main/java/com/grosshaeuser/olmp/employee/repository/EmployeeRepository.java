package com.grosshaeuser.olmp.employee.repository;

import com.grosshaeuser.olmp.employee.entity.Employee;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @NonNull
    Optional<Employee> findById(@NonNull Long id);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT e FROM Employee e " +
            "JOIN e.roles r " +
            "WHERE r.name IN :roles AND " +
            "(lower(e.lastName) LIKE lower(concat('%',:searchTerm,'%')) OR " +
            "lower(e.firstName) LIKE lower(concat('%',:searchTerm,'%')) OR " +
            "lower(e.email) LIKE lower(concat('%',:searchTerm,'%')))")
    Page<Employee> searchWithRolesIn(@Param("searchTerm") String searchTerm, @Param("roles") Set<String> roles, Pageable pageable);
}
