package com.ratelimit.ai.dto;

public class AiClassificationResponseDto {
    private String classification; // legit, suspicious, abuse
    private double confidence;
    private String reason;
    private int riskScore;

    public AiClassificationResponseDto() {}

    public AiClassificationResponseDto(String classification, double confidence, String reason, int riskScore) {
        this.classification = classification;
        this.confidence = confidence;
        this.reason = reason;
        this.riskScore = riskScore;
    }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
