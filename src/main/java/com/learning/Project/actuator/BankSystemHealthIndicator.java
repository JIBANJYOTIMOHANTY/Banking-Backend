package com.learning.Project.actuator;

import com.learning.Project.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class BankSystemHealthIndicator implements HealthIndicator {

    @Autowired
    private BankAccountRepository repository;

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
