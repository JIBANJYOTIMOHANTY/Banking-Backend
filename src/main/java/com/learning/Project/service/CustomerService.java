package com.learning.Project.service;

import java.util.List;

import com.learning.Project.model.CustomerAccount;

public interface CustomerService {

    CustomerAccount createAccount(CustomerAccount account);

    CustomerAccount deposit(String accountNumber, double amount);

    CustomerAccount withdraw(String accountNumber, double amount);

    CustomerAccount getAccount(String accountNumber);

    List<CustomerAccount> getAllAccounts();

    CustomerAccount updateAccountHolderData(CustomerAccount account);

    void deleteAccount(String accountNumber);

    void transfer(String sourceAccountNumber, String destAccountNumber, double amount);
}
