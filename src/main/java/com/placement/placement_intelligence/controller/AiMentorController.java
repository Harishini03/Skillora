package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.SkilloraAiRequest;
import com.placement.placement_intelligence.dto.SkilloraAiResponse;
import com.placement.placement_intelligence.service.CurrentUserService;
import com.placement.placement_intelligence.service.SkilloraAiMentorService;
import com.placement.placement_intelligence.service.StudentDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI Mentor Controller implementing Requirement 6: AI Learning System with Five Modes
 * 
 * Endpoints:
 * - POST /api/ai-mentor/generate - Generate AI content with mode-specific responses
 * - GET /api/ai-mentor/modes - Get available AI modes
 * - GET /api/ai-mentor/student-context - Get student context for personalization
 * 
 * Modes Supported:
 * - LEARN: Concept explanations with examples
 * - PRACTICE: Practice questions with immediate feedback
 * - ADAPTIVE: Difficulty adjustment based on performance
 * - REVISION: Concise summaries of topics
 * - MOCK_TEST: Timed assessments with scoring
 */
@RestController
@RequestMapping("/api/ai-mentor")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
public class AiMentorController {

    private static final Logger logger = LoggerFactory.getLogger(AiMentorController.class);

    private final SkilloraAiMentorService aiMentorService;
    private final CurrentUserService currentUserService;
    private final StudentDashboardService dashboardService;

    public AiMentorController(SkilloraAiMentorService aiMentorService,
                             CurrentUserService currentUserService,
                             StudentDashboardService dashboardService) {
        this.aiMentorService = aiMentorService;
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
    }

    /**
     * Generate AI content with personalized context.
     * Implements content freshness guarantee and adaptive difficulty.
     */
    @PostMapping("/generate")
    public ResponseEntity<SkilloraAiResponse> generateContent(@RequestBody SkilloraAiRequest request) {
        try {
            // Enhance request with student context for personalization
            SkilloraAiRequest enhancedRequest = enhanceWithStudentContext(request);
            
            logger.info("Generating AI content - Mode: {}, Topic: {}, Student: {}", 
                    enhancedRequest.getMode(), enhancedRequest.getTopic(), 
                    currentUserService.getCurrentStudentId());
            
            SkilloraAiResponse response = aiMentorService.generate(enhancedRequest);
            
            logger.info("AI content generated successfully - Mode: {}, AI Generated: {}", 
                    response.getMode(), response.isAiGenerated());
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("AI content generation failed", ex);
            return ResponseEntity.ok(createFallbackResponse(request));
        }
    }

    /**
     * Get available AI modes with descriptions.
     */
    @GetMapping("/modes")
    public ResponseEntity<Map<String, Object>> getAvailableModes() {
        Map<String, Object> modes = Map.of(
                "modes", List.of(
                        Map.of(
                                "mode", "LEARN",
                                "name", "Learn Mode",
                                "description", "Get comprehensive explanations with examples and concepts",
                                "icon", "📚",
                                "estimatedTime", "10-15 minutes"
                        ),
                        Map.of(
                                "mode", "PRACTICE",
                                "name", "Practice Mode", 
                                "description", "Solve practice questions with immediate feedback",
                                "icon", "💪", 
                                "estimatedTime", "15-30 minutes"
                        ),
                        Map.of(
                                "mode", "ADAPTIVE",
                                "name", "Adaptive Mode",
                                "description", "Personalized difficulty based on your performance",
                                "icon", "🎯",
                                "estimatedTime", "20-40 minutes"
                        ),
                        Map.of(
                                "mode", "REVISION",
                                "name", "Revision Mode",
                                "description", "Quick summaries and key points for fast review", 
                                "icon", "⚡",
                                "estimatedTime", "5-10 minutes"
                        ),
                        Map.of(
                                "mode", "MOCK_TEST",
                                "name", "Mock Test Mode",
                                "description", "Timed assessments to simulate real test conditions",
                                "icon", "🏆", 
                                "estimatedTime", "30-60 minutes"
                        )
                ),
                "totalModes", 5,
                "defaultMode", "LEARN"
        );
        
        return ResponseEntity.ok(modes);
    }

    /**
     * Get student context for AI personalization.
     */
    @GetMapping("/student-context")
    public ResponseEntity<Map<String, Object>> getStudentContext() {
        try {
            Long studentId = currentUserService.getCurrentStudentId();
            com.placement.placement_intelligence.dto.StudentDashboardResponse dashboard = dashboardService.buildDashboard(studentId);
            
            Map<String, Object> context = Map.of(
                    "studentLevel", determineStudentLevel(dashboard.getReadinessScore()),
                    "weakAreas", dashboard.getWeakAreas(),
                    "accuracy", calculateOverallAccuracy(dashboard),
                    "readinessScore", dashboard.getReadinessScore(),
                    "aptitudeProgress", dashboard.getAptitudeProgress(),
                    "codingProgress", dashboard.getCodingProgress(),
                    "softSkillsProgress", dashboard.getSoftSkillsProgress()
            );
            
            return ResponseEntity.ok(context);
        } catch (Exception ex) {
            logger.error("Failed to get student context", ex);
            return ResponseEntity.ok(Map.of(
                    "studentLevel", "Beginner",
                    "weakAreas", List.of(),
                    "accuracy", 0.0,
                    "readinessScore", 0.0
            ));
        }
    }

    /**
     * Test AI mentor connectivity and configuration.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Test with a simple request
            SkilloraAiRequest testRequest = new SkilloraAiRequest();
            testRequest.setMode("LEARN");
            testRequest.setTopic("Test");
            testRequest.setNumberOfQuestions(1);
            
            SkilloraAiResponse testResponse = aiMentorService.generate(testRequest);
            
            Map<String, Object> health = Map.of(
                    "status", "healthy",
                    "aiAvailable", testResponse.isAiGenerated(),
                    "fallbackWorking", !testResponse.getContent().isEmpty(),
                    "modesSupported", 5,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(health);
        } catch (Exception ex) {
            logger.error("AI mentor health check failed", ex);
            
            Map<String, Object> health = Map.of(
                    "status", "degraded",
                    "aiAvailable", false,
                    "fallbackWorking", true,
                    "error", ex.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(health);
        }
    }

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Enhance request with student context for personalization.
     */
    private SkilloraAiRequest enhanceWithStudentContext(SkilloraAiRequest request) {
        try {
            Long studentId = currentUserService.getCurrentStudentId();
            com.placement.placement_intelligence.dto.StudentDashboardResponse dashboard = dashboardService.buildDashboard(studentId);
            
            // Set student context if not provided
            if (request.getStudentLevel() == null || request.getStudentLevel().isEmpty()) {
                request.setStudentLevel(determineStudentLevel(dashboard.getReadinessScore()));
            }
            
            if (request.getWeakTopics() == null || request.getWeakTopics().isEmpty()) {
                request.setWeakTopics(dashboard.getWeakAreas());
            }
            
            if (request.getAccuracy() == null) {
                request.setAccuracy(calculateOverallAccuracy(dashboard));
            }
            
            // Set default values if missing
            if (request.getNumberOfQuestions() == null) {
                request.setNumberOfQuestions(getDefaultQuestionCount(request.getMode()));
            }
            
            return request;
        } catch (Exception ex) {
            logger.warn("Failed to enhance request with student context: {}", ex.getMessage());
            return request;
        }
    }

    /**
     * Determine student level based on readiness score.
     */
    private String determineStudentLevel(Double readinessScore) {
        if (readinessScore == null) return "Beginner";
        
        if (readinessScore >= 80) return "Advanced";
        if (readinessScore >= 60) return "Intermediate";
        if (readinessScore >= 40) return "Beginner+";
        return "Beginner";
    }

    /**
     * Calculate overall accuracy from dashboard data.
     */
    private Double calculateOverallAccuracy(com.placement.placement_intelligence.dto.StudentDashboardResponse dashboard) {
        double total = 0;
        int count = 0;
        
        if (dashboard.getAptitudeProgress() != null && dashboard.getAptitudeProgress() > 0) {
            total += dashboard.getAptitudeProgress();
            count++;
        }
        if (dashboard.getCodingProgress() != null && dashboard.getCodingProgress() > 0) {
            total += dashboard.getCodingProgress();
            count++;
        }
        if (dashboard.getSoftSkillsProgress() != null && dashboard.getSoftSkillsProgress() > 0) {
            total += dashboard.getSoftSkillsProgress();
            count++;
        }
        
        return count > 0 ? total / count : 0.0;
    }

    /**
     * Get default question count based on mode.
     */
    private int getDefaultQuestionCount(String mode) {
        if (mode == null) return 5;
        
        return switch (mode.toUpperCase()) {
            case "LEARN" -> 3;           // Few examples for learning
            case "PRACTICE" -> 10;       // Standard practice set
            case "ADAPTIVE" -> 15;       // Adaptive difficulty testing
            case "REVISION" -> 5;        // Quick revision
            case "MOCK_TEST" -> 20;      // Full mock test
            default -> 5;
        };
    }

    /**
     * Create fallback response when AI service fails.
     */
    private SkilloraAiResponse createFallbackResponse(SkilloraAiRequest request) {
        String mode = request.getMode() != null ? request.getMode() : "LEARN";
        String topic = request.getTopic() != null ? request.getTopic() : "General";
        
        String fallbackContent = """
                # %s Mode - %s
                
                ## AI Service Currently Unavailable
                
                The AI mentor service is temporarily unavailable, but here's some helpful content:
                
                ### Quick Tips
                - Focus on understanding concepts rather than memorizing
                - Practice regularly with varied question types
                - Review your mistakes to identify weak areas
                - Take breaks to avoid mental fatigue
                
                ### Recommended Study Approach
                1. **Learn** the basic concepts thoroughly
                2. **Practice** with easy questions first
                3. **Analyze** your performance and mistakes
                4. **Revise** weak areas identified
                5. **Test** yourself with mock assessments
                
                ### General Advice
                For %s topics, start with fundamentals and gradually increase difficulty.
                Regular practice and consistent review are key to improvement.
                
                *Please try again in a few minutes. The AI service should be restored shortly.*
                """.formatted(mode, topic, topic.toLowerCase());
        
        return new SkilloraAiResponse(mode, topic, fallbackContent, false);
    }
}