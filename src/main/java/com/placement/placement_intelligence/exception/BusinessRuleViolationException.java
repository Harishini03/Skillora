package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when a business rule is violated.
 * Results in HTTP 400 Bad Request response.
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    private final String ruleCode;

    public BusinessRuleViolationException(String message) {
        super(message);
        this.ruleCode = null;
    }

    public BusinessRuleViolationException(String message, String ruleCode) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
