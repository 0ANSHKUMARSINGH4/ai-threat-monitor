package com.ratelimit.ai.controller;

import com.ratelimit.ai.domain.AiClassification;
import com.ratelimit.ai.domain.ClientSummary;
import com.ratelimit.ai.repository.AiClassificationRepository;
import com.ratelimit.ai.repository.ClientSummaryRepository;
import com.ratelimit.ai.repository.RequestLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ClientSummaryRepository clientSummaryRepository;
    private final AiClassificationRepository aiClassificationRepository;
    private final RequestLogRepository requestLogRepository;
    private final StringRedisTemplate redisTemplate;

    public AdminController(ClientSummaryRepository clientSummaryRepository, 
                           AiClassificationRepository aiClassificationRepository, 
                           RequestLogRepository requestLogRepository,
                           StringRedisTemplate redisTemplate) {
        this.clientSummaryRepository = clientSummaryRepository;
        this.aiClassificationRepository = aiClassificationRepository;
        this.requestLogRepository = requestLogRepository;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/clients")
    public List<Map<String, Object>> getAllClients() {
        var clients = clientSummaryRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClientSummary client : clients) {
            Map<String, Object> map = new HashMap<>();
            map.put("ipAddress", client.getIpAddress());
            map.put("status", client.getStatus() != null ? client.getStatus().name() : "LEGITIMATE");
            map.put("requestsPerMinute", client.getRequestsPerMinute());
            map.put("totalRequests", client.getTotalRequests());
            map.put("uniqueEndpoints", client.getUniqueEndpoints());
            
            aiClassificationRepository.findTopByIpAddressOrderByEvaluatedAtDesc(client.getIpAddress())
                .ifPresent(classification -> {
                    map.put("aiReason", classification.getReason() != null ? classification.getReason() : "No data");
                    map.put("riskScore", classification.getRiskScore() != null ? classification.getRiskScore() : 0);
                });
            
            result.add(map);
        }
        return result;
    }

    @GetMapping("/client/{ip}")
    public ResponseEntity<Map<String, Object>> getClientDetails(@PathVariable String ip) {
        var summaryOpt = clientSummaryRepository.findById(ip);
        if (summaryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summaryOpt.get());
        
        aiClassificationRepository.findTopByIpAddressOrderByEvaluatedAtDesc(ip)
                .ifPresent(classification -> response.put("aiClassification", classification));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/traffic")
    public Map<String, Object> getOverallTraffic() {
        var clients = clientSummaryRepository.findAll();
        long totalRequests = clients.stream()
                .mapToLong(ClientSummary::getTotalRequests)
                .sum();
        long abuseCount = clients.stream()
                .filter(c -> c.getStatus() == ClientSummary.ClientStatus.ABUSIVE)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients", clients.size());
        stats.put("totalRequests", totalRequests);
        stats.put("abuseClients", abuseCount);
        return stats;
    }

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return response;
    }

    @PostMapping("/unblock/{ip}")
    @Transactional
    public ResponseEntity<String> unblockClient(@PathVariable String ip) {
        var summaryOpt = clientSummaryRepository.findById(ip);
        if (summaryOpt.isPresent()) {
            ClientSummary summary = summaryOpt.get();
            summary.setStatus(ClientSummary.ClientStatus.LEGITIMATE);
            clientSummaryRepository.save(summary);

            AiClassification override = new AiClassification(
                    ip,
                    "LEGITIMATE",
                    1.0,
                    0,
                    "Manual Admin Override - Unblocked",
                    LocalDateTime.now()
            );
            aiClassificationRepository.save(override);
            
            redisTemplate.delete("ai:status:" + ip);
            redisTemplate.delete("window:" + ip);
            
            return ResponseEntity.ok("Client Unblocked");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/reclassify")
    @Transactional
    public ResponseEntity<Map<String, String>> reclassifyClient(
            @RequestBody Map<String, String> request) {
        
        String ip = request.get("ipAddress");
        String correctedLabel = request.get("label"); // "LEGITIMATE" or "ABUSIVE"
        String adminNote = request.get("note");
        
        if (ip == null || correctedLabel == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 1. Save labeled training example to DB
        AiClassification correction = new AiClassification(
            ip,
            correctedLabel,
            1.0,
            0,
            "Admin Reclassification: " + (adminNote != null ? adminNote : "No note"),
            LocalDateTime.now()
        );
        aiClassificationRepository.save(correction);
        
        // 2. Update client status immediately
        clientSummaryRepository.findById(ip).ifPresent(summary -> {
            summary.setStatus(ClientSummary.ClientStatus.valueOf(correctedLabel));
            clientSummaryRepository.save(summary);
        });
        
        // 3. Clear Redis cache so new label takes effect immediately
        redisTemplate.delete("ai:status:" + ip);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Reclassification saved. Training example recorded.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Map<String, String>> resetState() {
        log.info("Initiating NUCLEAR system environment reset...");
        
        // 1. Clear Redis (Try-Catch to prevent fatal failure if Redis blips)
        try {
            var keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Redis cache cleared ({} keys).", keys.size());
            }
        } catch (Exception e) {
            log.warn("Non-fatal: Redis reset failed: {}. Proceeding with Database clearance.", e.getMessage());
        }

        // 2. Atomic Database Batch Reset
        try {
            log.info("Performing Atomic Database clearance...");
            requestLogRepository.deleteAllInBatch();
            aiClassificationRepository.deleteAllInBatch();
            clientSummaryRepository.deleteAllInBatch();
            log.info("Database high-performance reset complete.");
        } catch (Exception e) {
            log.error("FATAL: Database nuclear reset failed: ", e);
            throw e; // Trigger rollback
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "System environment recalibrated successfully.");
        return ResponseEntity.ok(response);
    }
}
