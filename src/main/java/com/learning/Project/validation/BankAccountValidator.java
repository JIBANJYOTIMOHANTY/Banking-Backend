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
     * If no fields are specified, all mandatory fields are validated.
     * @param account the bank account to validate
     * @param fieldsToCheck the names of fields to validate
     * @return an Optional containing the error message if invalid, or empty if valid
     */
    public static Optional<String> validate(BankAccount account, String... fieldsToCheck) {
        if (account == null) {
            return Optional.of("Account data must not be null");
        }

        List<String> checks = (fieldsToCheck != null && fieldsToCheck.length > 0)
                ? Arrays.asList(fieldsToCheck)
                : Arrays.asList("firstName", "lastName", "balance", "mobileNumber", "govtIdType", "govtId", "dob", "address");

        if (checks.contains("accountNumber") && (account.getAccountNumber() == null || account.getAccountNumber().isBlank())) {
            return Optional.of(MessageConstants.INVALID_ACCOUNT_NUMBER);
        }
        if (checks.contains("firstName") && (account.getFirstName() == null || account.getFirstName().isBlank())) {
            return Optional.of(MessageConstants.FIRST_NAME_IS_MANDATORY);
        }
        if (checks.contains("lastName") && (account.getLastName() == null || account.getLastName().isBlank())) {
            return Optional.of(MessageConstants.LAST_NAME_IS_MANDATORY);
        }
        if (checks.contains("mobileNumber") && (account.getMobileNumber() == null || account.getMobileNumber().isBlank())) {
            return Optional.of("Mobile number is mandatory");
        }
        if (checks.contains("govtIdType") && (account.getGovtIdType() == null || account.getGovtIdType().isBlank())) {
            return Optional.of("Government ID type is mandatory");
        }
        if (checks.contains("govtId") && (account.getGovtId() == null || account.getGovtId().isBlank())) {
            return Optional.of("Government ID number is mandatory");
        }
        if (checks.contains("dob") && (account.getDob() == null || account.getDob().isBlank())) {
            return Optional.of("Date of birth is mandatory");
        }
        if (checks.contains("address") && (account.getAddress() == null || account.getAddress().isBlank())) {
            return Optional.of("Address is mandatory");
        }
        if (checks.contains("balance") && account.getBalance() <= 0) {
            return Optional.of(MessageConstants.BALANCE_IS_MANDATORY);
        }
        return Optional.empty();
    }

    /**
     * Validates a BankAccount object specifically for updates.
     * Requires accountNumber, rejects balance modifications, and ensures at least
     * one valid updated field is provided.
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

        // 3. At least one of the fields to update must be provided
        boolean hasFirstName = account.getFirstName() != null;
        boolean hasLastName = account.getLastName() != null;
        boolean hasDob = account.getDob() != null;
        boolean hasEmail = account.getEmail() != null;
        boolean hasMobileNumber = account.getMobileNumber() != null;
        boolean hasGovtId = account.getGovtId() != null;
        boolean hasGovtIdType = account.getGovtIdType() != null;
        boolean hasOccupation = account.getOccupation() != null;
        boolean hasNomineeName = account.getNomineeName() != null;
        boolean hasNomineeRelation = account.getNomineeRelation() != null;
        boolean hasAddress = account.getAddress() != null;

        if (!hasFirstName && !hasLastName && !hasDob && !hasEmail && !hasMobileNumber &&
            !hasGovtId && !hasGovtIdType && !hasOccupation && !hasNomineeName && !hasNomineeRelation && !hasAddress) {
            return Optional.of(MessageConstants.NO_UPDATE_DATA_PROVIDED);
        }

        if (hasFirstName && account.getFirstName().isBlank()) {
            return Optional.of(MessageConstants.FIRST_NAME_IS_MANDATORY);
        }
        if (hasLastName && account.getLastName().isBlank()) {
            return Optional.of(MessageConstants.LAST_NAME_IS_MANDATORY);
        }
        if (hasDob && account.getDob().isBlank()) {
            return Optional.of("Date of birth must not be blank");
        }
        if (hasEmail && account.getEmail().isBlank()) {
            return Optional.of("Email must not be blank");
        }
        if (hasMobileNumber && account.getMobileNumber().isBlank()) {
            return Optional.of("Mobile number must not be blank");
        }
        if (hasGovtId && account.getGovtId().isBlank()) {
            return Optional.of("Government ID must not be blank");
        }
        if (hasGovtIdType && account.getGovtIdType().isBlank()) {
            return Optional.of("Government ID type must not be blank");
        }
        if (hasOccupation && account.getOccupation().isBlank()) {
            return Optional.of("Occupation must not be blank");
        }
        if (hasNomineeName && account.getNomineeName().isBlank()) {
            return Optional.of("Nominee name must not be blank");
        }
        if (hasNomineeRelation && account.getNomineeRelation().isBlank()) {
            return Optional.of("Nominee relation must not be blank");
        }
        if (hasAddress && account.getAddress().isBlank()) {
            return Optional.of("Address must not be blank");
        }

        return Optional.empty();
    }
}
