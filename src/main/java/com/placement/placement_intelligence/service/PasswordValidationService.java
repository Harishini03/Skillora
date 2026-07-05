package com.placement.placement_intelligence.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates passwords against the application's password policy.
 *
 * <p>Policy requirements:
 * <ul>
 *   <li>Minimum 8 characters</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one lowercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character from: {@code @#$%^&+=!}</li>
 * </ul>
 */
@Service
public class PasswordValidationService {

    private static final String SPECIAL_CHARS = "@#$%^&+=!";

    /**
     * Validates the given password and returns a list of policy violations.
     * An empty list means the password is valid.
     *
     * @param password the plain-text password to validate (may be {@code null})
     * @return a possibly-empty list of human-readable violation messages
     */
    public List<String> validatePassword(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.length() < 8) {
            violations.add("Password must be at least 8 characters");
            // Still check remaining rules if password is non-null but short
            if (password == null) {
                return violations;
            }
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (SPECIAL_CHARS.indexOf(ch) >= 0) {
                hasSpecial = true;
            }
        }

        if (!hasUpper) {
            violations.add("Password must contain at least one uppercase letter");
        }
        if (!hasLower) {
            violations.add("Password must contain at least one lowercase letter");
        }
        if (!hasDigit) {
            violations.add("Password must contain at least one digit");
        }
        if (!hasSpecial) {
            violations.add("Password must contain at least one special character");
        }

        return violations;
    }

    /**
     * Returns {@code true} when the password passes all policy rules.
     *
     * @param password the plain-text password to check
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isValid(String password) {
        return validatePassword(password).isEmpty();
    }
}
