package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.employee.entity.EmployeeAccount;
import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import com.grosshaeuser.olmp.employee.repository.EmployeeAccountRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeAccountRepository employeeAccountRepository;

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public EmployeePrincipal loadUserByUsername(@NonNull String username) {
        EmployeeAccount account = employeeAccountRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Employee account not found"
                ));

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        account.getEmployee().getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            role.getPrivileges().forEach(privilege ->
                    authorities.add(new SimpleGrantedAuthority(privilege.getName()))
            );
        });

        return new EmployeePrincipal(account, authorities);
    }
}