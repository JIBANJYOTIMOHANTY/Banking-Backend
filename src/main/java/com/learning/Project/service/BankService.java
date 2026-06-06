package com.learning.Project.service;

import java.util.List;

import com.learning.Project.model.BankAccount;

public interface BankService {

    BankAccount createAccount(BankAccount account);

    BankAccount deposit(String accountNumber, double amount);

    BankAccount withdraw(String accountNumber, double amount);

    BankAccount getAccount(String accountNumber);

    List<BankAccount> getAllAccounts();

    BankAccount updateAccountHolderData(BankAccount account);

    void deleteAccount(String accountNumber);

    void transfer(String sourceAccountNumber, String destAccountNumber, double amount);
}
