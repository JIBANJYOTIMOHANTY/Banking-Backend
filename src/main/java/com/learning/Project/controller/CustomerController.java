package com.learning.Project.controller;

import com.learning.Project.repository.CustomerAccountRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.Project.model.CustomerAccount;
import com.learning.Project.service.CustomerService;
import com.learning.Project.dto.ApiResponse;
import com.learning.Project.constants.MessageConstants;
import com.learning.Project.validation.CustomerAccountValidator;
import com.learning.Project.validation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bank/customer")
@Tag(name = "Bank Accounts", description = "Operations related to bank accounts")
public class CustomerController {

    @Autowired
    private CustomerAccountRepository customerAccountRepository;
    @Autowired
    private CustomerService customerService;

    @PostMapping
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Create a new bank account", description = "Creates a new bank account with the provided details. Account number must be unique.")
    public ResponseEntity<ApiResponse<CustomerAccount>> createAccount(@RequestBody CustomerAccount account) {
        Optional<String> validationError = CustomerAccountValidator.validate(account);
        if (validationError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, validationError.get(), List.of()));
        }
        CustomerAccount savedAccount = customerService.createAccount(account);
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.SAVED_SUCCESS,
                List.of(savedAccount));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts", description = "Retrieves a list of all active bank accounts, optionally filtered by search query.")
    public ResponseEntity<ApiResponse<CustomerAccount>> getAllAccounts(@RequestParam(value = "query", required = false) String query) {
        List<CustomerAccount> accounts;
        if (query != null && !query.trim().isEmpty()) {
            accounts = customerService.searchAccounts(query.trim());
        } else {
            accounts = customerService.getAllAccounts();
        }
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.RETRIEVED_SUCCESS, accounts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Get account by account number", description = "Retrieves details of a specific bank account using its account number.")
    public ResponseEntity<ApiResponse<CustomerAccount>> getAccount(@PathVariable String accountNumber) {
        CustomerAccount account = customerService.getAccount(accountNumber);
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.RETRIEVED_SINGLE_SUCCESS,
                List.of(account));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/deposit/{accountNumber}/{amount}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Deposit money into account", description = "Deposits the specified amount into the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<CustomerAccount>> deposit(@PathVariable String accountNumber,
            @PathVariable double amount) {
        CustomerAccount account = customerService.deposit(accountNumber, amount);
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.DEPOSITED_SUCCESS,
                List.of(account));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/withdraw/{accountNumber}/{amount}")
    @RateLimit(limit = 5, period = 60)
    @Operation(summary = "Withdraw money from account", description = "Withdraws the specified amount from the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<CustomerAccount>> withdraw(@PathVariable String accountNumber,
            @PathVariable double amount) {
        CustomerAccount account = customerService.withdraw(accountNumber, amount);
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.WITHDRAWN_SUCCESS,
                List.of(account));
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    @Operation(summary = "Update account holder data", description = "Updates the account holder data for the bank account identified by the account number.")
    public ResponseEntity<ApiResponse<CustomerAccount>> updateAccountHolderData(@RequestBody CustomerAccount account) {
        Optional<String> validationError = CustomerAccountValidator.validate(account, "accountNumber");
        if (validationError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, validationError.get(), List.of()));
        }

        CustomerAccount accountData = customerService.updateAccountHolderData(account);
        ApiResponse<CustomerAccount> response = new ApiResponse<>(0, MessageConstants.ACCOUNT_HOLDER_UPDATED_SUCCESS,
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

        if (customerAccountRepository.findByAccountNumber(accountNumber).isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, MessageConstants.ACCOUNT_NOT_FOUND, List.of()));
        }
        customerService.deleteAccount(accountNumber);
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

        customerService.transfer(sourceAccountNumber, destAccountNumber, amount);
        ApiResponse<String> response = new ApiResponse<>(0, MessageConstants.TRANSFER_SUCCESS, List.of());
        return ResponseEntity.ok(response);
    }
}
