package com.grosshaeuser.olmp.inventory.dto.publisher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
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
    @URL(message = "Enter a valid URL, for example https://example.org")
    private String website;
}
