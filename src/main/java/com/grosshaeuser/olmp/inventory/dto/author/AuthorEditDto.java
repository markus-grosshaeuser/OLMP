package com.grosshaeuser.olmp.inventory.dto.author;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorEditDto extends AuthorCreateDto {

    @NotNull
    @Positive
    private Long id;
}

