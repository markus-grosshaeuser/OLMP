package com.grosshaeuser.olmp.inventory;

import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCreateDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemEditDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorWriteDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierWriteDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    static Stream<InventoryItemCreateDto> itemForms() {
        InventoryItemEditDto edit = new InventoryItemEditDto();
        edit.setId(1L);
        return Stream.of(new InventoryItemCreateDto(), edit);
    }

    @ParameterizedTest
    @MethodSource("itemForms")
    void checksWithdrawalOrderingOnCreateAndEdit(InventoryItemCreateDto form) {
        form.setMediaId(1L);
        form.setInventoryNumber("COPY-1");
        form.setAcquiredAt(LocalDate.of(2026, 10, 17));
        form.setWithdrawnAt(form.getAcquiredAt().minusDays(1));

        assertThat(validator.validate(form))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("withdrawalDateValid");

        form.setWithdrawnAt(form.getAcquiredAt());
        assertThat(validator.validate(form)).isEmpty();

        form.setAcquiredAt(null);
        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void checksInheritedAndNestedConstraintsWhenEditingMedia() {
        MediaEditDto form = new MediaEditDto();
        form.setMediaTypeId(1L);
        form.setTitle(" ");
        MediaAuthorWriteDto author = new MediaAuthorWriteDto();
        author.setAuthorId(2L);
        author.setSortOrder((short) -1);
        form.getAuthors().add(author);
        MediaIdentifierWriteDto identifier = new MediaIdentifierWriteDto();
        identifier.setIdentifierType("isbn");
        identifier.setIdentifierValue("978-3-16-148410-0");
        form.getIdentifiers().add(identifier);

        assertThat(validator.validate(form))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("id", "title", "authors[0].sortOrder",
                        "identifiers[0].identifierType");

        form.setId(3L);
        form.setTitle("A catalogue title");
        author.setSortOrder((short) 0);
        identifier.setIdentifierType("ISBN");
        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void rejectsNullAuthorAndIdentifierEntries() {
        MediaEditDto form = new MediaEditDto();
        form.setId(1L);
        form.setMediaTypeId(2L);
        form.setTitle("A catalogue title");
        form.getAuthors().add(null);
        form.getIdentifiers().add(null);

        assertThat(validator.validate(form)).hasSize(2);

        form.getAuthors().clear();
        form.getIdentifiers().clear();
        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void rejectsDirectSelfParentingWhileAllowingRootsAndOtherParents() {
        LocationEditDto form = new LocationEditDto();
        form.setId(1L);
        form.setCode("SHELF-A");
        form.setName("Shelf A");
        form.setParentId(1L);

        assertThat(validator.validate(form))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("parentValid");

        form.setParentId(2L);
        assertThat(validator.validate(form)).isEmpty();

        form.setParentId(null);
        assertThat(validator.validate(form)).isEmpty();
    }
}
