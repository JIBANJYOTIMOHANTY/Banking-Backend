package com.learning.Project.service.Implementation;

import com.learning.Project.constants.MessageConstants;
import com.learning.Project.exceptions.CustomerAccountExceptions;
import com.learning.Project.model.Transaction;
import com.learning.Project.repository.CustomerAccountRepository;
import com.learning.Project.repository.TransactionRepository;
import com.learning.Project.service.TransactionService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerAccountRepository customerAccountRepository;

    TransactionServiceImpl(TransactionRepository transactionRepository,
            CustomerAccountRepository customerAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.customerAccountRepository = customerAccountRepository;
    }

    @Override
    public void recordTransaction(String accountNumber, String type, double amount, double postBalance) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Transaction tx = new Transaction(accountNumber, type, amount, postBalance, currentTime);
        transactionRepository.save(tx);
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber, String date) {
        customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(
                                MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        if (date != null && !date.isBlank()) {
            return transactionRepository.findByAccountNumberAndTimestampStartingWithOrderByTimestampAsc(accountNumber,
                    date);
        }
        return transactionRepository.findByAccountNumberOrderByTimestampAsc(accountNumber);
    }

    @Override
    @Transactional
    public void deleteTransactions(String accountNumber) {
        transactionRepository.deleteByAccountNumber(accountNumber);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
