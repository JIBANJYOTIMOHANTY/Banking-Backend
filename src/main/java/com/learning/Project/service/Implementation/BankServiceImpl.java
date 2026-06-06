package com.learning.Project.service.Implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.Project.model.BankAccount;
import com.learning.Project.repository.BankAccountRepository;
import com.learning.Project.service.BankService;
import com.learning.Project.validation.BankAccountValidator;
import com.learning.Project.exceptions.BankAccountExceptions;
import com.learning.Project.constants.MessageConstants;
import io.micrometer.core.instrument.MeterRegistry;
import com.learning.Project.service.TransactionService;

@Service
public class BankServiceImpl implements BankService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MeterRegistry meterRegistry;

    private synchronized String generateAccountNumber() {
        Optional<BankAccount> latestAccount = bankAccountRepository.findFirstByOrderByIdDesc();
        if (latestAccount.isPresent()) {
            String lastAccountNumber = latestAccount.get().getAccountNumber();
            if (lastAccountNumber != null && lastAccountNumber.startsWith("ACC")) {
                try {
                    int numericPart = Integer.parseInt(lastAccountNumber.substring(3));
                    String nextAcc;
                    int offset = 1;
                    do {
                        nextAcc = String.format("ACC%04d", numericPart + offset);
                        offset++;
                    } while (bankAccountRepository.findByAccountNumber(nextAcc).isPresent());
                    return nextAcc;
                } catch (NumberFormatException e) {
                    // Fallback to sequential search if parse fails
                }
            }
        }
        
        String nextAcc = "ACC0001";
        int suffix = 1;
        while (bankAccountRepository.findByAccountNumber(nextAcc).isPresent()) {
            suffix++;
            nextAcc = String.format("ACC%04d", suffix);
        }
        return nextAcc;
    }

    @Override
    @CachePut(value = "bankAccounts", key = "#result.accountNumber")
    public BankAccount createAccount(BankAccount account) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setCreated_at(currentTime);
        account.setUpdated_at(currentTime);
        
        // Auto-generate and enforce unique account number
        String generatedNo = generateAccountNumber();
        account.setAccountNumber(generatedNo);

        BankAccount savedAccount = bankAccountRepository.save(account);
        if (savedAccount.getBalance() > 0) {
            transactionService.recordTransaction(savedAccount.getAccountNumber(), MessageConstants.TX_INITIAL_DEPOSIT, savedAccount.getBalance(), savedAccount.getBalance());
        }
        return savedAccount;
    }

    @Override
    @Transactional
    @CachePut(value = "bankAccounts", key = "#accountNumber")
    public BankAccount deposit(String accountNumber, double amount) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        account.setBalance(account.getBalance() + amount);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setUpdated_at(currentTime);
        BankAccount savedAccount = bankAccountRepository.save(account);
        meterRegistry.counter("bank.operations.total", "type", "deposit").increment();
        meterRegistry.counter("bank.operations.amount.total", "type", "deposit").increment(amount);
        transactionService.recordTransaction(accountNumber, MessageConstants.TX_DEPOSIT, amount, savedAccount.getBalance());
        return savedAccount;
    }

    @Override
    @Transactional
    @CachePut(value = "bankAccounts", key = "#accountNumber")
    public BankAccount withdraw(String accountNumber, double amount) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        if (account.getBalance() < amount) {
            throw new BankAccountExceptions(MessageConstants.INSUFFICIENT_BALANCE + account.getBalance());
        }
        account.setBalance(account.getBalance() - amount);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setUpdated_at(currentTime);
        BankAccount savedAccount = bankAccountRepository.save(account);
        meterRegistry.counter("bank.operations.total", "type", "withdrawal").increment();
        meterRegistry.counter("bank.operations.amount.total", "type", "withdrawal").increment(amount);
        transactionService.recordTransaction(accountNumber, MessageConstants.TX_WITHDRAWAL, amount, savedAccount.getBalance());
        return savedAccount;
    }

    @Override
    @Cacheable(value = "bankAccounts", key = "#accountNumber")
    public BankAccount getAccount(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
    }

    @Override
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    @Override
    @CachePut(value = "bankAccounts", key = "#result.accountNumber")
    public BankAccount updateAccountHolderData(BankAccount account) {
        Optional<String> validationError = BankAccountValidator.validateUpdate(account);
        if (validationError.isPresent()) {
            throw new BankAccountExceptions(validationError.get());
        }
        BankAccount existingAccount = bankAccountRepository.findByAccountNumber(account.getAccountNumber())
                .orElseThrow(
                        () -> new BankAccountExceptions(
                                MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + account.getAccountNumber()));
        if (account.getFirstName() != null && !account.getFirstName().isBlank()) {
            existingAccount.setFirstName(account.getFirstName());
        }
        if (account.getLastName() != null && !account.getLastName().isBlank()) {
            existingAccount.setLastName(account.getLastName());
        }
        existingAccount.setUpdated_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return bankAccountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    @CacheEvict(value = "bankAccounts", key = "#accountNumber")
    public void deleteAccount(String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        transactionService.deleteTransactions(accountNumber);
        bankAccountRepository.delete(account);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bankAccounts", key = "#sourceAccountNumber"),
        @CacheEvict(value = "bankAccounts", key = "#destAccountNumber")
    })
    public void transfer(String sourceAccountNumber, String destAccountNumber, double amount) {
        if (amount <= 0) {
            throw new BankAccountExceptions(MessageConstants.INVALID_TRANSFER_AMOUNT);
        }
        if (sourceAccountNumber.equals(destAccountNumber)) {
            throw new BankAccountExceptions(MessageConstants.CANNOT_TRANSFER_TO_SELF);
        }

        BankAccount sourceAccount = bankAccountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + sourceAccountNumber));
        
        BankAccount destAccount = bankAccountRepository.findByAccountNumber(destAccountNumber)
                .orElseThrow(() -> new BankAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + destAccountNumber));

        if (sourceAccount.getBalance() < amount) {
            throw new BankAccountExceptions(MessageConstants.INSUFFICIENT_BALANCE + sourceAccount.getBalance());
        }

        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        destAccount.setBalance(destAccount.getBalance() + amount);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sourceAccount.setUpdated_at(currentTime);
        destAccount.setUpdated_at(currentTime);

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destAccount);

        transactionService.recordTransaction(sourceAccountNumber, MessageConstants.TX_TRANSFER_OUT, amount, sourceAccount.getBalance());
        transactionService.recordTransaction(destAccountNumber, MessageConstants.TX_TRANSFER_IN, amount, destAccount.getBalance());
    }
}
