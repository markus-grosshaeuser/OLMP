package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaIdentifierWriteDto {

    @Positive
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "[^\\p{Ll}]*", message = "must be uppercase")
    private String identifierType;

    @NotBlank
    @Size(max = 100)
    private String identifierValue;
}
