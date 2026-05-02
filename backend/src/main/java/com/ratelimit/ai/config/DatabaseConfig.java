package com.ratelimit.ai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${DB_HOST:}")
    private String dbHost;

    @Value("${DB_NAME:}")
    private String dbName;

    @Value("${DB_USERNAME:}")
    private String dbUsername;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Initializing Render Production DataSource...");
        
        HikariConfig config = new HikariConfig();
        
        // Priority 1: DATABASE_URL (Standard Render/PaaS variable)
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            log.info("Found DATABASE_URL. Attempting to parse...");
            try {
                parseDatabaseUrl(databaseUrl, config);
            } catch (Exception e) {
                log.error("Failed to parse DATABASE_URL: {}. Falling back to individual variables.", e.getMessage());
            }
        }
        
        // Priority 2: Individual variables if DATABASE_URL was missing or failed
        if (config.getJdbcUrl() == null) {
            log.info("Using individual DB environment variables...");
            String host = (dbHost == null || dbHost.isEmpty()) ? "localhost" : dbHost;
            String name = (dbName == null || dbName.isEmpty()) ? "postgres" : dbName;
            
            String jdbcUrl = String.format("jdbc:postgresql://%s:5432/%s?sslmode=require", host, name);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUsername);
            config.setPassword(dbPassword);
        }

        // Connection Pool Tuning (Free Tier Optimization)
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(4); // Keep it low for Render Free tier
        config.setMinimumIdle(1);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(30000); // 30 seconds wait
        config.setPoolName("RenderPostgresPool");
        
        // Resilience settings
        config.addDataSourceProperty("socketTimeout", "30");
        config.addDataSourceProperty("tcpKeepAlive", "true");

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error("CRITICAL: DataSource creation failed! App will start in a degraded state. Error: {}", e.getMessage());
            // Return a lazy/dummy config if needed, but Hikari usually handles this if initialization-fail-timeout is set
            config.setInitializationFailTimeout(-1); // Don't crash the JVM if DB is down at boot
            return new HikariDataSource(config);
        }
    }

    private void parseDatabaseUrl(String url, HikariConfig config) 
            throws URISyntaxException {
        URI dbUri = new URI(url);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        int port = dbUri.getPort();
        if (port == -1) {
            port = 5432; // Default PostgreSQL port
        }
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" 
            + port + dbUri.getPath() + "?sslmode=require";

        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        log.info("Parsed JDBC URL: jdbc:postgresql://{}:{}{}", 
            dbUri.getHost(), port, dbUri.getPath());
    }
}
