package com.learning.Project.validation;

import com.learning.Project.constants.MessageConstants;
import com.learning.Project.model.BankAccount;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BankAccountValidator {

    private BankAccountValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a BankAccount object, checking only the specified fields.
     * If no fields are specified, all fields are validated.
     * @param account the bank account to validate
     * @param fieldsToCheck the names of fields to validate (e.g., "accountNumber", "firstName", "lastName", "balance")
     * @return an Optional containing the error message if invalid, or empty if valid
     */
    public static Optional<String> validate(BankAccount account, String... fieldsToCheck) {
        if (account == null) {
            return Optional.of("Account data must not be null");
        }

        List<String> checks = (fieldsToCheck != null && fieldsToCheck.length > 0)
                ? Arrays.asList(fieldsToCheck)
                : Arrays.asList("firstName", "lastName", "balance");

        if (checks.contains("accountNumber") && (account.getAccountNumber() == null || account.getAccountNumber().isBlank())) {
            return Optional.of(MessageConstants.INVALID_ACCOUNT_NUMBER);
        }
        if (checks.contains("firstName") && (account.getFirstName() == null || account.getFirstName().isBlank())) {
            return Optional.of(MessageConstants.FIRST_NAME_IS_MANDATORY);
        }
        if (checks.contains("lastName") && (account.getLastName() == null || account.getLastName().isBlank())) {
            return Optional.of(MessageConstants.LAST_NAME_IS_MANDATORY);
        }
        if (checks.contains("balance") && account.getBalance() <= 0) {
            return Optional.of(MessageConstants.BALANCE_IS_MANDATORY);
        }
        return Optional.empty();
    }

    /**
     * Validates a BankAccount object specifically for updates.
     * Requires accountNumber, rejects balance modifications, and ensures at least
     * one of firstName or lastName is provided and not blank.
     * @param account the bank account to validate for updates
     * @return an Optional containing the error message if invalid, or empty if valid
     */
    public static Optional<String> validateUpdate(BankAccount account) {
        if (account == null) {
            return Optional.of("Account data must not be null");
        }

        // 1. accountNumber must be present and valid
        if (account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
            return Optional.of(MessageConstants.INVALID_ACCOUNT_NUMBER);
        }

        // 2. balance must not be provided/updated
        if (account.getBalance() > 0.0) {
            return Optional.of(MessageConstants.BALANCE_CANNOT_BE_UPDATED);
        }

        // 3. Either firstName or lastName must be provided (not null) and not blank
        boolean hasFirstName = account.getFirstName() != null;
        boolean hasLastName = account.getLastName() != null;

        if (!hasFirstName && !hasLastName) {
            return Optional.of(MessageConstants.NO_UPDATE_DATA_PROVIDED);
        }

        if (hasFirstName && account.getFirstName().isBlank()) {
            return Optional.of(MessageConstants.FIRST_NAME_IS_MANDATORY);
        }
        if (hasLastName && account.getLastName().isBlank()) {
            return Optional.of(MessageConstants.LAST_NAME_IS_MANDATORY);
        }

        return Optional.empty();
    }
}
