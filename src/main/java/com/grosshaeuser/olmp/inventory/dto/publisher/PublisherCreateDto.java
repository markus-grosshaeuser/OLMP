package com.grosshaeuser.olmp.inventory.dto.publisher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublisherCreateDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String website;
}

