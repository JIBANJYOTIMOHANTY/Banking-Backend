package com.learning.Project.service;

import com.learning.Project.model.Transaction;
import java.util.List;

public interface TransactionService {

    void recordTransaction(String accountNumber, String type, double amount, double postBalance);

    List<Transaction> getTransactionHistory(String accountNumber, String date);

    void deleteTransactions(String accountNumber);

    List<Transaction> getAllTransactions();
}
