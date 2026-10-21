package com.grosshaeuser.olmp.inventory.repository;

import com.grosshaeuser.olmp.inventory.entity.Media;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    String SEARCH_FILTER = """
             where (lower(m.title) like :pattern escape '!'
                or lower(m.subtitle) like :pattern escape '!')
               and (:publisherId is null or m.publisher.id = :publisherId)
               and (:authorId is null or exists (
                   select 1 from MediaAuthor ma where ma.media = m and ma.author.id = :authorId
               ))
            """;

    @Query("""
            select m.id as id, m.title as title, m.publicationYear as publicationYear,
                   mt.id as mediaTypeId, mt.code as mediaTypeCode, mt.name as mediaTypeName,
                   p.id as publisherId, p.name as publisherName, p.website as publisherWebsite
              from Media m
              join m.mediaType mt
              left join m.publisher p
            """ + SEARCH_FILTER)
    List<SummaryRow> search(@Param("pattern") String pattern, @Param("authorId") Long authorId,
                            @Param("publisherId") Long publisherId, Pageable pageable);

    @Query("select count(m) from Media m " + SEARCH_FILTER)
    long countSearch(@Param("pattern") String pattern, @Param("authorId") Long authorId,
                     @Param("publisherId") Long publisherId);

    @EntityGraph(attributePaths = {"mediaType", "publisher"})
    @Query("select m from Media m where m.id = :id")
    Optional<Media> findDetailedById(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Media m where m.id = :id")
    Optional<Media> findForUpdate(@Param("id") long id);

    interface SummaryRow {
        Long getId();
        String getTitle();
        Short getPublicationYear();
        Long getMediaTypeId();
        String getMediaTypeCode();
        String getMediaTypeName();
        Long getPublisherId();
        String getPublisherName();
        String getPublisherWebsite();
    }
}
