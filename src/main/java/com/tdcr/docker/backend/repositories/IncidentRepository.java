package com.tdcr.docker.backend.repositories;

import com.tdcr.docker.backend.data.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, String> {
}
