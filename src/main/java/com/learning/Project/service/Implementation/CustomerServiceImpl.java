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

import com.learning.Project.model.CustomerAccount;
import com.learning.Project.repository.CustomerAccountRepository;
import com.learning.Project.service.CustomerService;
import com.learning.Project.validation.CustomerAccountValidator;
import com.learning.Project.exceptions.CustomerAccountExceptions;
import com.learning.Project.constants.MessageConstants;
import io.micrometer.core.instrument.MeterRegistry;
import com.learning.Project.service.TransactionService;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MeterRegistry meterRegistry;

    private synchronized String generateAccountNumber() {
        Optional<CustomerAccount> latestAccount = customerAccountRepository.findFirstByOrderByIdDesc();
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
                    } while (customerAccountRepository.findByAccountNumber(nextAcc).isPresent());
                    return nextAcc;
                } catch (NumberFormatException e) {
                    // Fallback to sequential search if parse fails
                }
            }
        }
        
        String nextAcc = "ACC0001";
        int suffix = 1;
        while (customerAccountRepository.findByAccountNumber(nextAcc).isPresent()) {
            suffix++;
            nextAcc = String.format("ACC%04d", suffix);
        }
        return nextAcc;
    }

    @Override
    @CachePut(value = "bankAccounts", key = "#result.accountNumber")
    public CustomerAccount createAccount(CustomerAccount account) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setCreated_at(currentTime);
        account.setUpdated_at(currentTime);
        account.setIsDeleted(0);
        
        // Auto-generate and enforce unique account number
        String generatedNo = generateAccountNumber();
        account.setAccountNumber(generatedNo);

        CustomerAccount savedAccount = customerAccountRepository.save(account);
        if (savedAccount.getBalance() > 0) {
            transactionService.recordTransaction(savedAccount.getAccountNumber(), MessageConstants.TX_INITIAL_DEPOSIT, savedAccount.getBalance(), savedAccount.getBalance());
        }
        return savedAccount;
    }

    @Override
    @Transactional
    @CachePut(value = "bankAccounts", key = "#accountNumber")
    public CustomerAccount deposit(String accountNumber, double amount) {
        CustomerAccount account = customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        account.setBalance(account.getBalance() + amount);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setUpdated_at(currentTime);
        CustomerAccount savedAccount = customerAccountRepository.save(account);
        meterRegistry.counter("bank.operations.total", "type", "deposit").increment();
        meterRegistry.counter("bank.operations.amount.total", "type", "deposit").increment(amount);
        transactionService.recordTransaction(accountNumber, MessageConstants.TX_DEPOSIT, amount, savedAccount.getBalance());
        return savedAccount;
    }

    @Override
    @Transactional
    @CachePut(value = "bankAccounts", key = "#accountNumber")
    public CustomerAccount withdraw(String accountNumber, double amount) {
        CustomerAccount account = customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        if (account.getBalance() < amount) {
            throw new CustomerAccountExceptions(MessageConstants.INSUFFICIENT_BALANCE + account.getBalance());
        }
        account.setBalance(account.getBalance() - amount);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        account.setUpdated_at(currentTime);
        CustomerAccount savedAccount = customerAccountRepository.save(account);
        meterRegistry.counter("bank.operations.total", "type", "withdrawal").increment();
        meterRegistry.counter("bank.operations.amount.total", "type", "withdrawal").increment(amount);
        transactionService.recordTransaction(accountNumber, MessageConstants.TX_WITHDRAWAL, amount, savedAccount.getBalance());
        return savedAccount;
    }

    @Override
    @Cacheable(value = "bankAccounts", key = "#accountNumber")
    public CustomerAccount getAccount(String accountNumber) {
        return customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
    }

    @Override
    public List<CustomerAccount> getAllAccounts() {
        return customerAccountRepository.findByIsDeleted(0);
    }

    @Override
    @CachePut(value = "bankAccounts", key = "#result.accountNumber")
    public CustomerAccount updateAccountHolderData(CustomerAccount account) {
        Optional<String> validationError = CustomerAccountValidator.validateUpdate(account);
        if (validationError.isPresent()) {
            throw new CustomerAccountExceptions(validationError.get());
        }
        CustomerAccount existingAccount = customerAccountRepository.findByAccountNumberAndIsDeleted(account.getAccountNumber(), 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(
                                MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + account.getAccountNumber()));
        if (account.getFirstName() != null && !account.getFirstName().isBlank()) {
            existingAccount.setFirstName(account.getFirstName());
        }
        if (account.getLastName() != null && !account.getLastName().isBlank()) {
            existingAccount.setLastName(account.getLastName());
        }
        if (account.getDob() != null && !account.getDob().isBlank()) {
            existingAccount.setDob(account.getDob());
        }
        if (account.getEmail() != null && !account.getEmail().isBlank()) {
            existingAccount.setEmail(account.getEmail());
        }
        if (account.getMobileNumber() != null && !account.getMobileNumber().isBlank()) {
            existingAccount.setMobileNumber(account.getMobileNumber());
        }
        if (account.getGovtId() != null && !account.getGovtId().isBlank()) {
            existingAccount.setGovtId(account.getGovtId());
        }
        if (account.getGovtIdType() != null && !account.getGovtIdType().isBlank()) {
            existingAccount.setGovtIdType(account.getGovtIdType());
        }
        if (account.getOccupation() != null && !account.getOccupation().isBlank()) {
            existingAccount.setOccupation(account.getOccupation());
        }
        if (account.getNomineeName() != null && !account.getNomineeName().isBlank()) {
            existingAccount.setNomineeName(account.getNomineeName());
        }
        if (account.getNomineeRelation() != null && !account.getNomineeRelation().isBlank()) {
            existingAccount.setNomineeRelation(account.getNomineeRelation());
        }
        if (account.getAddress() != null && !account.getAddress().isBlank()) {
            existingAccount.setAddress(account.getAddress());
        }
        if (account.getPan() != null && !account.getPan().isBlank()) {
            existingAccount.setPan(account.getPan());
        }
        if (account.getLandmark() != null) {
            existingAccount.setLandmark(account.getLandmark());
        }
        if (account.getCity() != null && !account.getCity().isBlank()) {
            existingAccount.setCity(account.getCity());
        }
        if (account.getState() != null && !account.getState().isBlank()) {
            existingAccount.setState(account.getState());
        }
        if (account.getCountry() != null && !account.getCountry().isBlank()) {
            existingAccount.setCountry(account.getCountry());
        }
        if (account.getPincode() != null && !account.getPincode().isBlank()) {
            existingAccount.setPincode(account.getPincode());
        }
        existingAccount.setUpdated_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return customerAccountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    @CacheEvict(value = "bankAccounts", key = "#accountNumber")
    public void deleteAccount(String accountNumber) {
        CustomerAccount account = customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0)
                .orElseThrow(
                        () -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + accountNumber));
        account.setIsDeleted(1);
        customerAccountRepository.save(account);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bankAccounts", key = "#sourceAccountNumber"),
        @CacheEvict(value = "bankAccounts", key = "#destAccountNumber")
    })
    public void transfer(String sourceAccountNumber, String destAccountNumber, double amount) {
        if (amount <= 0) {
            throw new CustomerAccountExceptions(MessageConstants.INVALID_TRANSFER_AMOUNT);
        }
        if (sourceAccountNumber.equals(destAccountNumber)) {
            throw new CustomerAccountExceptions(MessageConstants.CANNOT_TRANSFER_TO_SELF);
        }

        CustomerAccount sourceAccount = customerAccountRepository.findByAccountNumberAndIsDeleted(sourceAccountNumber, 0)
                .orElseThrow(() -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + sourceAccountNumber));
        
        CustomerAccount destAccount = customerAccountRepository.findByAccountNumberAndIsDeleted(destAccountNumber, 0)
                .orElseThrow(() -> new CustomerAccountExceptions(MessageConstants.ACCOUNT_NOT_FOUND_WITH_NO + destAccountNumber));

        if (sourceAccount.getBalance() < amount) {
            throw new CustomerAccountExceptions(MessageConstants.INSUFFICIENT_BALANCE + sourceAccount.getBalance());
        }

        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        destAccount.setBalance(destAccount.getBalance() + amount);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sourceAccount.setUpdated_at(currentTime);
        destAccount.setUpdated_at(currentTime);

        customerAccountRepository.save(sourceAccount);
        customerAccountRepository.save(destAccount);

        transactionService.recordTransaction(sourceAccountNumber, MessageConstants.TX_TRANSFER_OUT, amount, sourceAccount.getBalance());
        transactionService.recordTransaction(destAccountNumber, MessageConstants.TX_TRANSFER_IN, amount, destAccount.getBalance());
    }

    @Override
    public List<CustomerAccount> searchAccounts(String query) {
        return customerAccountRepository.searchAccounts(query);
    }
}
