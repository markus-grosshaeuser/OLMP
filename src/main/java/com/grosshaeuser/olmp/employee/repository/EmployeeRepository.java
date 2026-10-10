package com.grosshaeuser.olmp.employee.repository;

import com.grosshaeuser.olmp.employee.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT DISTINCT e FROM Employee e " +
            "JOIN e.roles r " +
            "WHERE r.name IN :roles AND " +
            "(lower(e.lastName) LIKE lower(concat('%',:searchTerm,'%')) OR " +
            "lower(e.firstName) LIKE lower(concat('%',:searchTerm,'%')) OR " +
            "lower(e.email) LIKE lower(concat('%',:searchTerm,'%')))")
    Slice<Employee> searchWithRolesIn(@Param("searchTerm") String searchTerm, @Param("roles") Set<String> roles, Pageable pageable);
}
