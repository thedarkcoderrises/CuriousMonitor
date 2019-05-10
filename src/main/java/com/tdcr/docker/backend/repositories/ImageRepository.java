package com.tdcr.docker.backend.repositories;

import com.tdcr.docker.backend.data.entity.ImageDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageDetails, String> {
}
