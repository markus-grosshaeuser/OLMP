package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaTypeEditDto extends MediaTypeCreateDto {

    @NotNull
    @Positive
    private Long id;
}

