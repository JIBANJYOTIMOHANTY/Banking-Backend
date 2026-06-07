package com.learning.Project.validation;

import java.util.Optional;

public class UserValidator {

    private UserValidator() {
        // Prevent instantiation
    }

    public static Optional<String> validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return Optional.of("Password is mandatory");
        }

        if (password.length() < 8) {
            return Optional.of("Password must be at least 8 characters long");
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        String specialChars = "!@#$%^&*()_+-=[]{};':\"|,.<>/?";

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (specialChars.indexOf(ch) >= 0) {
                hasSpecialChar = true;
            }
        }

        if (!hasUppercase) {
            return Optional.of("Password must contain at least one uppercase letter (Capital)");
        }
        if (!hasLowercase) {
            return Optional.of("Password must contain at least one lowercase letter (Small)");
        }
        if (!hasDigit) {
            return Optional.of("Password must contain at least one number");
        }
        if (!hasSpecialChar) {
            return Optional.of("Password must contain at least one special character");
        }

        return Optional.empty();
    }
}
