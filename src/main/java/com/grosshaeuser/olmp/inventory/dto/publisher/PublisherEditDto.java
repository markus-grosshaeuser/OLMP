package com.grosshaeuser.olmp.inventory.dto.publisher;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublisherEditDto extends PublisherCreateDto {

    @NotNull
    @Positive
    private Long id;
}

