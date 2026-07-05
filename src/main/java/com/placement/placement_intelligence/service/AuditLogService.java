package com.placement.placement_intelligence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Security audit logging service.
 * All events are written to the dedicated {@code SECURITY_AUDIT} logger so they
 * can be routed to a separate appender (file, SIEM, etc.) in logback configuration.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");

    /**
     * Records an authentication attempt (login or signup).
     *
     * @param email     the email address used in the attempt
     * @param success   {@code true} if authentication succeeded
     * @param ipAddress the remote IP of the client
     */
    public void logAuthAttempt(String email, boolean success, String ipAddress) {
        log.info("AUTH_ATTEMPT | email={} | success={} | ip={}", email, success, ipAddress);
    }

    /**
     * Records an access-denied event for an authenticated user.
     *
     * @param username  the username of the requesting user
     * @param resource  the resource that was denied
     * @param ipAddress the remote IP of the client
     */
    public void logAccessDenied(String username, String resource, String ipAddress) {
        log.warn("ACCESS_DENIED | user={} | resource={} | ip={}", username, resource, ipAddress);
    }

    /**
     * Records suspicious activity detected by the application.
     *
     * @param description a short human-readable description of the event
     * @param ipAddress   the remote IP associated with the suspicious activity
     */
    public void logSuspiciousActivity(String description, String ipAddress) {
        log.warn("SUSPICIOUS_ACTIVITY | {} | ip={}", description, ipAddress);
    }

    /**
     * Records a data access event at DEBUG level (high-volume, informational).
     *
     * @param username the username accessing the resource
     * @param resource the resource being accessed
     */
    public void logDataAccess(String username, String resource) {
        log.debug("DATA_ACCESS | user={} | resource={}", username, resource);
    }
}
