package com.learning.Project.actuator;

import com.learning.Project.repository.CustomerAccountRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class BankSystemHealthIndicator implements HealthIndicator {

    private final CustomerAccountRepository repository;

    BankSystemHealthIndicator(CustomerAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        try {
            long count = repository.count();
            return Health.up()
                    .withDetail("message", "Bank system database is online and reachable")
                    .withDetail("totalAccountsCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("message", "Failed to reach bank system database")
                    .build();
        }
    }
}
