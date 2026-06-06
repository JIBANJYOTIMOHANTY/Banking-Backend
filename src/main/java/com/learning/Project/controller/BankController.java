package com.learning.Project.controller;

import com.learning.Project.repository.BankAccountRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.Project.model.BankAccount;
import com.learning.Project.service.BankService;
import com.learning.Project.dto.ApiResponse;
import com.learning.Project.constants.MessageConstants;
import com.learning.Project.validation.BankAccountValidator;
import com.learning.Project.validation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bank")
@Tag(name = "Bank Accounts", description = "Operations related to bank accounts")
public class BankController {

    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private BankService bankService;

    @PostMapping
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Create a new bank account", description = "Creates a new bank account with the provided details. Account number must be unique.")
    public ResponseEntity<ApiResponse<BankAccount>> createAccount(@RequestBody BankAccount account) {
        Optional<String> validationError = BankAccountValidator.validate(account);
        if (validationError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, validationError.get(), List.of()));
        }
        BankAccount savedAccount = bankService.createAccount(account);
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.SAVED_SUCCESS, List.of(savedAccount));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts", description = "Retrieves a list of all active bank accounts.")
    public ResponseEntity<ApiResponse<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankService.getAllAccounts();
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.RETRIEVED_SUCCESS, accounts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Get account by account number", description = "Retrieves details of a specific bank account using its account number.")
    public ResponseEntity<ApiResponse<BankAccount>> getAccount(@PathVariable String accountNumber) {
        BankAccount account = bankService.getAccount(accountNumber);
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.RETRIEVED_SINGLE_SUCCESS,
                List.of(account));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/deposit/{accountNumber}/{amount}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Deposit money into account", description = "Deposits the specified amount into the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<BankAccount>> deposit(@PathVariable String accountNumber,
            @PathVariable double amount) {
        BankAccount account = bankService.deposit(accountNumber, amount);
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.DEPOSITED_SUCCESS, List.of(account));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/withdraw/{accountNumber}/{amount}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Withdraw money from account", description = "Withdraws the specified amount from the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<BankAccount>> withdraw(@PathVariable String accountNumber,
            @PathVariable double amount) {
        BankAccount account = bankService.withdraw(accountNumber, amount);
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.WITHDRAWN_SUCCESS, List.of(account));
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    @Operation(summary = "Update account holder data", description = "Updates the account holder data for the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<BankAccount>> updateAccountHolderData(@RequestBody BankAccount account) {
        Optional<String> validationError = BankAccountValidator.validate(account, "accountNumber");
        if (validationError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, validationError.get(), List.of()));
        }

        BankAccount accountData = bankService.updateAccountHolderData(account);
        ApiResponse<BankAccount> response = new ApiResponse<>(0, MessageConstants.ACCOUNT_HOLDER_UPDATED_SUCCESS,
                List.of(accountData));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete bank account", description = "Deletes a bank account using its account number, after validating constraints.")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@PathVariable String accountNumber) {
        if (accountNumber.matches("ACC[0-9]+") == false) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, MessageConstants.INVALID_ACCOUNT_NUMBER, List.of()));
        }

        if (bankAccountRepository.findByAccountNumber(accountNumber).isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, MessageConstants.ACCOUNT_NOT_FOUND, List.of()));
        }
        bankService.deleteAccount(accountNumber);
        return ResponseEntity.ok(new ApiResponse<>(0, MessageConstants.DELETED_SUCCESS, List.of()));
    }

    @PutMapping("/transfer/{sourceAccountNumber}/{destAccountNumber}/{amount}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Transfer money between accounts", description = "Transfers the specified amount from the source bank account to the destination bank account.")
    public ResponseEntity<ApiResponse<String>> transfer(
            @PathVariable String sourceAccountNumber,
            @PathVariable String destAccountNumber,
            @PathVariable double amount) {

        if (sourceAccountNumber.matches("ACC[0-9]+") == false) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, "Source " + MessageConstants.INVALID_ACCOUNT_NUMBER, List.of()));
        }
        if (destAccountNumber.matches("ACC[0-9]+") == false) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, "Destination " + MessageConstants.INVALID_ACCOUNT_NUMBER, List.of()));
        }

        bankService.transfer(sourceAccountNumber, destAccountNumber, amount);
        ApiResponse<String> response = new ApiResponse<>(0, MessageConstants.TRANSFER_SUCCESS, List.of());
        return ResponseEntity.ok(response);
    }
}
