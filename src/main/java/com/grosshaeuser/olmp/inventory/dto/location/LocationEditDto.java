package com.grosshaeuser.olmp.inventory.dto.location;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationEditDto extends LocationCreateDto {

    @NotNull
    @Positive
    private Long id;

    @AssertTrue(message = "a location cannot be its own parent")
    public boolean isParentValid() {
        return id == null || !id.equals(getParentId());
    }
}

