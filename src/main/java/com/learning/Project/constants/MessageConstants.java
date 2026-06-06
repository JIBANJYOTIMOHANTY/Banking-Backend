package com.learning.Project.constants;

public final class MessageConstants {

    private MessageConstants() {
        // Prevent instantiation
    }

    // Success Messages
    public static final String SAVED_SUCCESS = "Saved Successfully";
    public static final String RETRIEVED_SUCCESS = "Accounts retrieved successfully";
    public static final String RETRIEVED_SINGLE_SUCCESS = "Account retrieved successfully";
    public static final String DEPOSITED_SUCCESS = "Deposited Successfully";
    public static final String WITHDRAWN_SUCCESS = "Withdrawn Successfully";
    public static final String DELETED_SUCCESS = "Account Number deleted successfully";
    public static final String ACCOUNT_HOLDER_UPDATED_SUCCESS = "Account holder data updated successfully";
    public static final String TRANSACTIONS_RETRIEVED_SUCCESS = "Transaction history retrieved successfully";
    public static final String TRANSFER_SUCCESS = "Transfer completed successfully";
    public static final String NO_TRANSACTIONS_FOUND = "No transactions found";

    // Transaction Types
    public static final String TX_INITIAL_DEPOSIT = "INITIAL_DEPOSIT";
    public static final String TX_DEPOSIT = "DEPOSIT";
    public static final String TX_WITHDRAWAL = "WITHDRAWAL";
    public static final String TX_TRANSFER_OUT = "TRANSFER_OUT";
    public static final String TX_TRANSFER_IN = "TRANSFER_IN";

    // Error Messages
    public static final String INVALID_ACCOUNT_NUMBER = "Account Number is not valid";
    public static final String FIRST_NAME_IS_MANDATORY = "First Name is mandatory";
    public static final String LAST_NAME_IS_MANDATORY = "Last Name is mandatory";
    public static final String BALANCE_IS_MANDATORY = "Balance is mandatory";
    public static final String ACCOUNT_NOT_FOUND = "Account Number not found";
    public static final String ACCOUNT_NOT_FOUND_WITH_NO = "Account not found with account number: ";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance. Available balance: ";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred: ";
    public static final String BALANCE_CANNOT_BE_UPDATED = "Balance cannot be updated via this system";
    public static final String NO_UPDATE_DATA_PROVIDED = "No update data provided. Please provide firstName or lastName";
    public static final String CANNOT_TRANSFER_TO_SELF = "Source and destination accounts must be different";
    public static final String INVALID_TRANSFER_AMOUNT = "Transfer amount must be greater than zero";
}
