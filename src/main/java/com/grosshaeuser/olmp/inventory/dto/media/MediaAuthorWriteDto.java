package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaAuthorWriteDto {

    @NotNull
    @Positive
    private Long authorId;

    @NotBlank
    @Size(max = 50)
    private String role = "AUTHOR";

    @NotNull
    @PositiveOrZero
    private Short sortOrder = 0;
}

