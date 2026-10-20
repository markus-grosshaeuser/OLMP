package com.grosshaeuser.olmp.inventory.repository;

import com.grosshaeuser.olmp.inventory.entity.MediaIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MediaIdentifierRepository extends JpaRepository<MediaIdentifier, Long> {

    List<MediaIdentifier> findAllByMediaId(long mediaId);

    List<MediaIdentifier> findAllByIdentifierTypeInAndIdentifierValueIn(
            Collection<String> identifierTypes, Collection<String> identifierValues);

    void deleteByMediaId(Long id);
}
