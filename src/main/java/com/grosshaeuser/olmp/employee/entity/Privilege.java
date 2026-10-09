package com.grosshaeuser.olmp.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "privileges")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void setName(String name) {
        this.name = name == null ? null : name.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Privilege privilege)) {
            return false;
        }
        return Objects.equals(name, privilege.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
