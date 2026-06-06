package com.learning.Project.controller;

import com.learning.Project.constants.MessageConstants;
import com.learning.Project.dto.ApiResponse;
import com.learning.Project.model.Transaction;
import com.learning.Project.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank")
@Tag(name = "Transactions", description = "Operations related to transaction statements")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/{accountNumber}/transactions")
    @Operation(summary = "Get transaction history by account number", description = "Retrieves a list of all transactions for a specific bank account using its account number. Optionally filters by date (yyyy-MM-dd).")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String date) {
        if (accountNumber.matches("ACC[0-9]+") == false) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, MessageConstants.INVALID_ACCOUNT_NUMBER, List.of()));
        }
        if (date != null && !date.isBlank()) {
            if (date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$") == false) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(1, "Date format must be yyyy-MM-dd", List.of()));
            }
        }
        List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber, date);
        String message = transactions.isEmpty() ? MessageConstants.NO_TRANSACTIONS_FOUND : MessageConstants.TRANSACTIONS_RETRIEVED_SUCCESS;
        ApiResponse<Transaction> response = new ApiResponse<>(0, message, transactions);
        return ResponseEntity.ok(response);
    }
}
