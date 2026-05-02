package com.ratelimit.ai.repository;

import com.ratelimit.ai.domain.ClientSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSummaryRepository extends JpaRepository<ClientSummary, String> {
}
