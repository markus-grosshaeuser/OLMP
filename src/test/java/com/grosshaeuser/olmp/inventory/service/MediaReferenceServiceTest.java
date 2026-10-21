package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Author;
import com.grosshaeuser.olmp.inventory.entity.Publisher;
import com.grosshaeuser.olmp.inventory.mappers.AuthorMapper;
import com.grosshaeuser.olmp.inventory.mappers.PublisherMapper;
import com.grosshaeuser.olmp.inventory.repository.AuthorRepository;
import com.grosshaeuser.olmp.inventory.repository.PublisherRepository;
import com.grosshaeuser.olmp.inventory.service.impl.MediaReferenceServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaReferenceServiceTest {
    private final AuthorRepository authors = mock(AuthorRepository.class);
    private final PublisherRepository publishers = mock(PublisherRepository.class);
    private final ValidatorFactory validation = Validation.buildDefaultValidatorFactory();
    private final MediaReferenceService service = new MediaReferenceServiceImpl(authors, publishers,
            new AuthorMapper(), new PublisherMapper(), validation.getValidator());

    @AfterEach
    void closeValidation() {
        validation.close();
    }

    @Test
    void updatesExistingAuthorWithoutReplacingIdentityAndNormalizesNames() {
        Author existing = Author.builder().id(2L).firstName("Ada").lastName("Lovelace").build();
        when(authors.findById(2L)).thenReturn(Optional.of(existing));
        when(authors.saveAndFlush(existing)).thenReturn(existing);
        AuthorEditDto dto = authorEdit();
        dto.setFirstName("  ");
        dto.setLastName(" King ");

        assertThat(service.updateAuthor(dto)).isEqualTo(new AuthorSummaryDto(2L, null, "King"));
        verify(authors).saveAndFlush(same(existing));
        assertThat(dto.getLastName()).isEqualTo(" King ");
    }

    @Test
    void updatesExistingPublisherWithoutReplacingIdentityAndAllowsClearingWebsite() {
        Publisher existing = Publisher.builder().id(9L).name("Existing").website("https://example.org").build();
        when(publishers.findById(9L)).thenReturn(Optional.of(existing));
        when(publishers.saveAndFlush(existing)).thenReturn(existing);
        PublisherEditDto dto = publisherEdit();
        dto.setName(" Renamed ");
        dto.setWebsite("  ");

        assertThat(service.updatePublisher(dto)).isEqualTo(new PublisherSummaryDto(9L, "Renamed", null));
        verify(publishers).saveAndFlush(same(existing));
        assertThat(dto.getName()).isEqualTo(" Renamed ");
    }

    @Test
    void rejectsMissingRecordsWithoutCreatingThem() {
        when(authors.findById(2L)).thenReturn(Optional.empty());
        when(publishers.findById(9L)).thenReturn(Optional.empty());
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateAuthor(authorEdit()))
                .withMessageContaining("no longer available");
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(publisherEdit()))
                .withMessageContaining("no longer available");
        verify(authors, never()).saveAndFlush(any());
        verify(publishers, never()).saveAndFlush(any());
    }

    @Test
    void validatesRequiredDataIdsNamesAndWebsiteBeforeAccessingRepositories() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateAuthor(null));
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(null));
        AuthorEditDto author = authorEdit();
        author.setId(null);
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateAuthor(author));
        author.setId(-1L);
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateAuthor(author));
        author.setId(2L);
        author.setLastName("  ");
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateAuthor(author));
        PublisherEditDto publisher = publisherEdit();
        publisher.setId(null);
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(publisher));
        publisher.setId(0L);
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(publisher));
        publisher.setId(9L);
        publisher.setName("  ");
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(publisher));
        publisher.setName("Valid");
        publisher.setWebsite("invalid URL");
        assertThatIllegalArgumentException().isThrownBy(() -> service.updatePublisher(publisher));
        verifyNoInteractions(authors, publishers);
    }

    private AuthorEditDto authorEdit() {
        AuthorEditDto dto = new AuthorEditDto();
        dto.setId(2L);
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        return dto;
    }

    private PublisherEditDto publisherEdit() {
        PublisherEditDto dto = new PublisherEditDto();
        dto.setId(9L);
        dto.setName("Existing");
        dto.setWebsite("https://example.org");
        return dto;
    }
}
