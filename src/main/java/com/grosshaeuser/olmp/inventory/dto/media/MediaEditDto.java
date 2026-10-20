package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaEditDto extends MediaCreateDto {

    @NotNull
    @Positive
    private Long id;
}

