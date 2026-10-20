package com.grosshaeuser.olmp.inventory.repository;

import com.grosshaeuser.olmp.inventory.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
}
