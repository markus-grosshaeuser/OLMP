package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class MediaAuthorId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private String role = "AUTHOR";
}
