package com.tdcr.docker.backend.repositories;

import com.tdcr.docker.backend.data.entity.ContainerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<ContainerDetails, String> {
}
