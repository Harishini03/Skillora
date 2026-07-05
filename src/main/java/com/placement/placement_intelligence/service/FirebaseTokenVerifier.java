package com.placement.placement_intelligence.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class FirebaseTokenVerifier {
    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenVerifier.class);

    @Value("${app.firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${app.firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${app.firebase.project-id:}")
    private String projectId;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream credStream = null;

                // Option 1: JSON string in env var
                if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                    credStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
                }
                // Option 2: Path to service account file
                else if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                    credStream = new FileInputStream(serviceAccountPath);
                }

                if (credStream != null) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(credStream);
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                    FirebaseApp.initializeApp(options);
                    initialized = true;
                    log.info("Firebase Admin SDK initialized successfully");
                } else {
                    log.warn("Firebase Admin SDK not configured — firebase-login endpoint will be unavailable. " +
                             "Set app.firebase.service-account-json or app.firebase.service-account-path");
                }
            } else {
                initialized = true;
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Verifies a Firebase ID token and returns the decoded token.
     * Throws if Firebase is not initialized or token is invalid.
     */
    public FirebaseToken verifyIdToken(String idToken) {
        if (!initialized) {
            throw new IllegalStateException("Firebase Admin SDK is not initialized");
        }
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Firebase ID token: " + e.getMessage());
        }
    }
}
