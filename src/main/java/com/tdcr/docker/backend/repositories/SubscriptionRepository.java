package com.tdcr.docker.backend.repositories;

import com.tdcr.docker.backend.data.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
}
