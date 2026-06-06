package com.learning.Project.service.Implementation;

import com.learning.Project.constants.MessageConstants;
import com.learning.Project.exceptions.BankAccountExceptions;
import com.learning.Project.model.Transaction;
import com.learning.Project.repository.BankAccountRepository;
import com.learning.Project.repository.TransactionRepository;
import com.learning.Project.service.TransactionService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public void recordTransaction(String accountNumber, String type, double amount, double postBalance) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Transaction tx = new Transaction(accountNumber, type, amount, postBalance, currentTime);
        transactionRepository.save(tx);
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber, String date) {
        bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        if (date != null && !date.isBlank()) {
            return transactionRepository.findByAccountNumberAndTimestampStartingWithOrderByTimestampAsc(accountNumber, date);
        }
        return transactionRepository.findByAccountNumberOrderByTimestampAsc(accountNumber);
    }

    @Override
    @Transactional
    public void deleteTransactions(String accountNumber) {
        transactionRepository.deleteByAccountNumber(accountNumber);
    }
}
