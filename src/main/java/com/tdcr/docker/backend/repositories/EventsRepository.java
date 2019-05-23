package com.tdcr.docker.backend.repositories;

import com.tdcr.docker.backend.data.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsRepository extends JpaRepository<Event, Long> {
}
