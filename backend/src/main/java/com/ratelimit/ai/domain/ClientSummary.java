package com.ratelimit.ai.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_summary")
public class ClientSummary {

    @Id
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    private int totalRequests;
    private double requestsPerMinute;
    private int uniqueEndpoints;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClientStatus status;

    private LocalDateTime lastUpdated;

    public enum ClientStatus {
        LEGITIMATE, AUTOMATED, SUSPICIOUS, ABUSIVE
    }

    public ClientSummary() {}

    public ClientSummary(String ipAddress, int totalRequests, double requestsPerMinute, int uniqueEndpoints, ClientStatus status, LocalDateTime lastUpdated) {
        this.ipAddress = ipAddress;
        this.totalRequests = totalRequests;
        this.requestsPerMinute = requestsPerMinute;
        this.uniqueEndpoints = uniqueEndpoints;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Standard Getters and Setters
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
    public double getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(double requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    public int getUniqueEndpoints() { return uniqueEndpoints; }
    public void setUniqueEndpoints(int uniqueEndpoints) { this.uniqueEndpoints = uniqueEndpoints; }
    public ClientStatus getStatus() { return status; }
    public void setStatus(ClientStatus status) { this.status = status; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
