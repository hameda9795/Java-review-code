package com.devmentor.infrastructure.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for application-specific health checks
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Add custom health checks here
        // For example: check AI service availability, database connection, etc.

        try {
            // Perform health checks
            boolean isHealthy = performHealthChecks();

            if (isHealthy) {
                return Health.up()
                        .withDetail("application", "DevMentor AI")
                        .withDetail("status", "All systems operational")
                        .build();
            } else {
                return Health.down()
                        .withDetail("application", "DevMentor AI")
                        .withDetail("status", "Some services degraded")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("application", "DevMentor AI")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private boolean performHealthChecks() {
        // Add your custom health check logic here
        // For now, just return true
        return true;
    }
}
