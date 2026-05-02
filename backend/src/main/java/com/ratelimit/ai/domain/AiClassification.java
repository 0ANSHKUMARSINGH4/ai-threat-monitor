package com.ratelimit.ai.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_classification")
public class AiClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(nullable = false, length = 20)
    private String classification; 

    private Double confidence;
    private Integer riskScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    public AiClassification() {}

    public AiClassification(String ipAddress, String classification, Double confidence, Integer riskScore, String reason, LocalDateTime evaluatedAt) {
        this.ipAddress = ipAddress;
        this.classification = classification;
        this.confidence = confidence;
        this.riskScore = riskScore;
        this.reason = reason;
        this.evaluatedAt = evaluatedAt;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
