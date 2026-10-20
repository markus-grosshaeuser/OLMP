package com.grosshaeuser.olmp.inventory.repository;

import com.grosshaeuser.olmp.inventory.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaTypeRepository extends JpaRepository<MediaType, Long> {
}
