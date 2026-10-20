package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaTypeCreateDto {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "[^\\p{Ll}]*", message = "must be uppercase")
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;
}

