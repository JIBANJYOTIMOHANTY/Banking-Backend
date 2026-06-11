package com.learning.Project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learning.Project.model.CustomerAccount;
import com.learning.Project.repository.CustomerAccountRepository;
import com.learning.Project.repository.BroadcastAlertRepository;
import com.learning.Project.model.BroadcastAlert;
import com.learning.Project.service.Implementation.CustomerServiceImpl;
import com.learning.Project.exceptions.CustomerAccountExceptions;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerAccountRepository customerAccountRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @Mock
    private BroadcastAlertRepository broadcastAlertRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;


    @Test
    public void testDeleteAccount_SoftDeletes() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsDeleted(0);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));
        when(customerAccountRepository.save(any(CustomerAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        customerService.deleteAccount(accountNumber);

        // Assert
        assertEquals(1, account.getIsDeleted());
        verify(customerAccountRepository, times(1)).save(account);
        verify(transactionService, never()).deleteTransactions(anyString());
    }

    @Test
    public void testGetAccount_OnlyReturnsActive() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        
        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));

        // Act
        CustomerAccount result = customerService.getAccount(accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
    }

    @Test
    public void testGetAccount_ThrowsExceptionIfDeletedOrNotFound() {
        // Arrange
        String accountNumber = "ACC0001";
        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.getAccount(accountNumber);
        });
    }

    @Test
    public void testGetAllAccounts_OnlyReturnsActive() {
        // Arrange
        List<CustomerAccount> activeAccounts = new ArrayList<>();
        activeAccounts.add(new CustomerAccount(1L, "ACC0001", "John", "Doe", 100.0));
        
        when(customerAccountRepository.findByIsDeleted(0)).thenReturn(activeAccounts);

        // Act
        List<CustomerAccount> result = customerService.getAllAccounts();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ACC0001", result.get(0).getAccountNumber());
    }

    @Test
    public void testFreezeAccount_SetsIsFrozen() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsFrozen(0);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));
        when(customerAccountRepository.save(any(CustomerAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        customerService.freezeAccount(accountNumber);

        // Assert
        assertEquals(1, account.getIsFrozen());
        verify(customerAccountRepository, times(1)).save(account);
    }

    @Test
    public void testDeposit_ThrowsExceptionIfFrozen() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsFrozen(1);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.deposit(accountNumber, 50.0);
        });
        assertEquals("Account is frozen.", exception.getMessage());
    }

    @Test
    public void testWithdraw_ThrowsExceptionIfFrozen() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsFrozen(1);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.withdraw(accountNumber, 50.0);
        });
        assertEquals("Account is frozen.", exception.getMessage());
    }

    @Test
    public void testFreezeAccount_ThrowsExceptionIfAlreadyFrozen() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsFrozen(1);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.freezeAccount(accountNumber);
        });
        assertEquals("Account is already frozen.", exception.getMessage());
    }

    @Test
    public void testUnfreezeAccount_ThrowsExceptionIfAlreadyActive() {
        // Arrange
        String accountNumber = "ACC0001";
        CustomerAccount account = new CustomerAccount(1L, accountNumber, "John", "Doe", 100.0);
        account.setIsFrozen(0);

        when(customerAccountRepository.findByAccountNumberAndIsDeleted(accountNumber, 0))
            .thenReturn(Optional.of(account));

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.unfreezeAccount(accountNumber);
        });
        assertEquals("Account is already active.", exception.getMessage());
    }

    @Test
    public void testDeposit_ThrowsExceptionIfCriticalAlertActive() {
        // Arrange
        String accountNumber = "ACC0001";
        List<BroadcastAlert> alerts = new ArrayList<>();
        alerts.add(new BroadcastAlert("Lockdown message", "2026-06-11 12:00:00", 1, "CRITICAL"));
        when(broadcastAlertRepository.findByIsActive(1)).thenReturn(alerts);

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.deposit(accountNumber, 50.0);
        });
        assertEquals("Transactions are disabled during system maintenance.", exception.getMessage());
    }

    @Test
    public void testWithdraw_ThrowsExceptionIfCriticalAlertActive() {
        // Arrange
        String accountNumber = "ACC0001";
        List<BroadcastAlert> alerts = new ArrayList<>();
        alerts.add(new BroadcastAlert("Lockdown message", "2026-06-11 12:00:00", 1, "CRITICAL"));
        when(broadcastAlertRepository.findByIsActive(1)).thenReturn(alerts);

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.withdraw(accountNumber, 50.0);
        });
        assertEquals("Transactions are disabled during system maintenance.", exception.getMessage());
    }

    @Test
    public void testTransfer_ThrowsExceptionIfCriticalAlertActive() {
        // Arrange
        String srcAcc = "ACC0001";
        String destAcc = "ACC0002";
        List<BroadcastAlert> alerts = new ArrayList<>();
        alerts.add(new BroadcastAlert("Lockdown message", "2026-06-11 12:00:00", 1, "CRITICAL"));
        when(broadcastAlertRepository.findByIsActive(1)).thenReturn(alerts);

        // Act & Assert
        CustomerAccountExceptions exception = assertThrows(CustomerAccountExceptions.class, () -> {
            customerService.transfer(srcAcc, destAcc, 50.0);
        });
        assertEquals("Transactions are disabled during system maintenance.", exception.getMessage());
    }
}

