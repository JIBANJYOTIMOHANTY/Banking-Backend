package com.learning.Project.repository;

import com.learning.Project.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountNumberOrderByTimestampAsc(String accountNumber);

    List<Transaction> findByAccountNumberAndTimestampStartingWithOrderByTimestampAsc(String accountNumber,
            String datePrefix);

    void deleteByAccountNumber(String accountNumber);
}
