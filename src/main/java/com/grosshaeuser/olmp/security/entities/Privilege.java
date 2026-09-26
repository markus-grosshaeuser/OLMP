package com.grosshaeuser.olmp.security.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "privileges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @ManyToMany(mappedBy = "privileges")
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
        role.getPrivileges().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getPrivileges().remove(this);
    }
}