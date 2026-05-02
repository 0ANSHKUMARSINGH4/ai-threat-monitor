package com.ratelimit.ai.service;

import com.ratelimit.ai.domain.ClientSummary;
import com.ratelimit.ai.domain.RequestLog;
import com.ratelimit.ai.repository.AiClassificationRepository;
import com.ratelimit.ai.repository.ClientSummaryRepository;
import com.ratelimit.ai.repository.RequestLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BehaviorAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(BehaviorAnalysisService.class);

    private final RequestLogRepository requestLogRepository;
    private final ClientSummaryRepository clientSummaryRepository;
    private final AiClassificationRepository aiClassificationRepository;
    private final AiAbuseDetectionService aiAbuseDetectionService;

    public BehaviorAnalysisService(RequestLogRepository requestLogRepository,
                                   ClientSummaryRepository clientSummaryRepository,
                                   AiClassificationRepository aiClassificationRepository,
                                   AiAbuseDetectionService aiAbuseDetectionService) {
        this.requestLogRepository = requestLogRepository;
        this.clientSummaryRepository = clientSummaryRepository;
        this.aiClassificationRepository = aiClassificationRepository;
        this.aiAbuseDetectionService = aiAbuseDetectionService;
    }

    @Scheduled(fixedRate = 30000)
    public void analyzeTrafficBehavior() {
        log.info("Starting scheduled traffic behavior analysis...");
        
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        List<Object[]> counts = requestLogRepository.countRequestsSinceGroupedByIp(oneMinuteAgo);
        
        for (Object[] row : counts) {
            String ip = (String) row[0];
            Long reqCount = (Long) row[1];
            
            List<RequestLog> recentLogs = requestLogRepository.findByIpAddressAndTimestampAfter(ip, oneMinuteAgo);
            
            int uniqueEndpoints = recentLogs.stream()
                .map(RequestLog::getEndpoint)
                .collect(Collectors.toSet()).size();
            
            ClientSummary summary = clientSummaryRepository.findById(ip)
                .orElse(new ClientSummary(ip, 0, 0.0, 0, ClientSummary.ClientStatus.LEGITIMATE, LocalDateTime.now()));
            
            summary.setRequestsPerMinute(reqCount.doubleValue());
            summary.setUniqueEndpoints(uniqueEndpoints);
            summary.setTotalRequests(summary.getTotalRequests() + reqCount.intValue());
            summary.setLastUpdated(LocalDateTime.now());
            
            clientSummaryRepository.save(summary);
            aiAbuseDetectionService.evaluateBehavior(summary);
            updateClientStatusFromAi(summary);
        }
    }

    private void updateClientStatusFromAi(ClientSummary summary) {
        aiClassificationRepository.findTopByIpAddressOrderByEvaluatedAtDesc(summary.getIpAddress())
            .ifPresent(classification -> {
                try {
                    ClientSummary.ClientStatus newStatus = ClientSummary.ClientStatus.valueOf(classification.getClassification().toUpperCase());
                    summary.setStatus(newStatus);
                    clientSummaryRepository.save(summary);
                } catch (IllegalArgumentException e) {
                    log.error("Unknown classification status: {}", classification.getClassification());
                }
            });
    }
}
