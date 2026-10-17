package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "media_identifiers", uniqueConstraints = @UniqueConstraint(
        name = "uq_media_identifiers_type_value", columnNames = {"identifier_type", "identifier_value"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "[^\\p{Ll}]*", message = "must be uppercase")
    @Column(name = "identifier_type", nullable = false, length = 30)
    private String identifierType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "identifier_value", nullable = false, length = 100)
    private String identifierValue;

}
