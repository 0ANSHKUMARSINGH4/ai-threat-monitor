package com.ratelimit.ai.repository;

import com.ratelimit.ai.domain.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime timestamp);

    @Query("SELECT r.ipAddress, COUNT(r) as reqCount FROM RequestLog r WHERE r.timestamp > :since GROUP BY r.ipAddress")
    List<Object[]> countRequestsSinceGroupedByIp(@Param("since") LocalDateTime since);
}
