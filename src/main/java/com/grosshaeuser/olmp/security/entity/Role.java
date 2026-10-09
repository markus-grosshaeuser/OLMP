package com.grosshaeuser.olmp.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_privileges",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    @Builder.Default
    @Getter(AccessLevel.PRIVATE)
    private Set<Privilege> privileges = new HashSet<>();

    public void addPrivilege(Privilege privilege) {
        privileges.add(privilege);
        privilege.getRoles().add(this);
    }

    public void removePrivilege(Privilege privilege) {
        privileges.remove(privilege);
        privilege.getRoles().remove(this);
    }

    public boolean hasPrivilege(String privilegeName) {
        return privileges.stream()
                .anyMatch(privilege -> privilege.getName().equalsIgnoreCase(privilegeName));
    }

    public void setName(String name) {
        this.name = name == null ? null : name.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role role)) {
            return false;
        }
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
