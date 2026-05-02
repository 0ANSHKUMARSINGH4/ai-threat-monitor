package com.ratelimit.ai.repository;

import com.ratelimit.ai.domain.AiClassification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiClassificationRepository extends JpaRepository<AiClassification, Long> {
    Optional<AiClassification> findTopByIpAddressOrderByEvaluatedAtDesc(String ipAddress);
}
