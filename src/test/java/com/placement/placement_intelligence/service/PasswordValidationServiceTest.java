package com.placement.placement_intelligence.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidationServiceTest {

    private PasswordValidationService passwordValidationService;

    @BeforeEach
    void setUp() {
        passwordValidationService = new PasswordValidationService();
    }

    @Test
    void testValidPassword_noViolations() {
        List<String> violations = passwordValidationService.validatePassword("Skillora@123");
        assertTrue(violations.isEmpty(), "Valid password should produce no violations");
    }

    @Test
    void testTooShort_violationAboutLength() {
        List<String> violations = passwordValidationService.validatePassword("Ab@1");
        assertFalse(violations.isEmpty(), "Short password should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("8 characters") || v.toLowerCase().contains("least 8")),
                "Should contain a violation about minimum length, but got: " + violations);
    }

    @Test
    void testNoUppercase_violationAboutUppercase() {
        List<String> violations = passwordValidationService.validatePassword("skillora@123");
        assertFalse(violations.isEmpty(), "Password without uppercase should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("uppercase")),
                "Should contain a violation about uppercase letter, but got: " + violations);
    }

    @Test
    void testNoLowercase_violationAboutLowercase() {
        List<String> violations = passwordValidationService.validatePassword("SKILLORA@123");
        assertFalse(violations.isEmpty(), "Password without lowercase should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("lowercase")),
                "Should contain a violation about lowercase letter, but got: " + violations);
    }

    @Test
    void testNoDigit_violationAboutDigit() {
        List<String> violations = passwordValidationService.validatePassword("Skillora@abc");
        assertFalse(violations.isEmpty(), "Password without digit should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("digit") || v.toLowerCase().contains("number")),
                "Should contain a violation about digit, but got: " + violations);
    }

    @Test
    void testNoSpecialChar_violationAboutSpecialChar() {
        List<String> violations = passwordValidationService.validatePassword("Skillora123");
        assertFalse(violations.isEmpty(), "Password without special character should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("special")),
                "Should contain a violation about special character, but got: " + violations);
    }

    @Test
    void testNullPassword_violationsNotEmpty() {
        List<String> violations = passwordValidationService.validatePassword(null);
        assertFalse(violations.isEmpty(), "Null password should produce violations");
    }

    @Test
    void testIsValid_withValidPassword() {
        assertTrue(passwordValidationService.isValid("Skillora@123"),
                "isValid should return true for a valid password");
    }

    @Test
    void testIsValid_withInvalidPassword() {
        assertFalse(passwordValidationService.isValid("weak"),
                "isValid should return false for a weak password");
        assertFalse(passwordValidationService.isValid(null),
                "isValid should return false for null");
        assertFalse(passwordValidationService.isValid("nouppercase@123"),
                "isValid should return false when missing uppercase");
        assertFalse(passwordValidationService.isValid("NOLOWERCASE@123"),
                "isValid should return false when missing lowercase");
        assertFalse(passwordValidationService.isValid("NoDigit@abc"),
                "isValid should return false when missing digit");
        assertFalse(passwordValidationService.isValid("NoSpecial123"),
                "isValid should return false when missing special char");
    }

    @Test
    void testExactly8Characters_validIfAllRulesMet() {
        // Exactly 8 chars: 1 upper, 1 lower, 1 digit, 1 special
        List<String> violations = passwordValidationService.validatePassword("Abc@1234");
        assertTrue(violations.isEmpty(), "Exactly 8-char password meeting all rules should be valid");
    }

    @Test
    void testExactly7Characters_failsLength() {
        List<String> violations = passwordValidationService.validatePassword("Abc@123");
        assertFalse(violations.isEmpty(), "7-char password should fail length check");
        assertTrue(violations.stream().anyMatch(v -> v.toLowerCase().contains("8")),
                "Violation should mention 8 characters");
    }
}
