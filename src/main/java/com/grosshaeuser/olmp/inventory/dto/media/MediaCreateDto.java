package com.grosshaeuser.olmp.inventory.dto.media;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediaCreateDto {

    @NotNull
    @Positive
    private Long mediaTypeId;

    @Positive
    private Long publisherId;

    @NotBlank
    @Size(max = 500)
    private String title;

    @Size(max = 500)
    private String subtitle;

    @Size(max = 100)
    private String edition;

    @Min(1)
    @Max(9999)
    private Short publicationYear;

    @Size(max = 10)
    private String languageCode;

    @Size(max = 100)
    String coverImageFileName;

    private String description;

    @NotNull
    private List<@NotNull @Valid MediaAuthorWriteDto> authors = new ArrayList<>();

    @NotNull
    private List<@NotNull @Valid MediaIdentifierWriteDto> identifiers = new ArrayList<>();

    @NotNull
    private List<@NotNull @Valid MediaInventoryItemDto> inventoryItems = new ArrayList<>();
}
