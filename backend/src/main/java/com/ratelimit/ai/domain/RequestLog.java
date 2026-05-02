package com.ratelimit.ai.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs", indexes = {@Index(name = "idx_ip_ts", columnList = "ip_address, timestamp")})
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String endpoint;

    @Column(length = 255)
    private String payloadHash;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public RequestLog() {}

    public RequestLog(String ipAddress, String endpoint, String payloadHash, String userAgent, LocalDateTime timestamp) {
        this.ipAddress = ipAddress;
        this.endpoint = endpoint;
        this.payloadHash = payloadHash;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
