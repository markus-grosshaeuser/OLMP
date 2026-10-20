package com.grosshaeuser.olmp.inventory.repository;

import com.grosshaeuser.olmp.inventory.entity.MediaAuthor;
import com.grosshaeuser.olmp.inventory.entity.MediaAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MediaAuthorRepository extends JpaRepository<MediaAuthor, MediaAuthorId> {

    @Query("""
            select ma from MediaAuthor ma
              join fetch ma.author
             where ma.media.id in :mediaIds
             order by ma.media.id, ma.sortOrder, ma.author.id, ma.id.role
            """)
    List<MediaAuthor> findAllWithAuthorByMediaIds(@Param("mediaIds") Collection<Long> mediaIds);

    void deleteByMediaId(Long id);
}
