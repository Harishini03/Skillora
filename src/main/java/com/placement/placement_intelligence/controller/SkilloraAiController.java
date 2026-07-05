package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.SkilloraAiRequest;
import com.placement.placement_intelligence.dto.SkilloraAiResponse;
import com.placement.placement_intelligence.service.SkilloraAiMentorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/student/ai-mentor")
public class SkilloraAiController {

    private final SkilloraAiMentorService mentorService;

    // Daily challenge cache: date-string → response
    private final Map<String, SkilloraAiResponse> dailyChallengeCache = new ConcurrentHashMap<>();

    private static final List<String> DAILY_TOPICS = List.of(
        "HCF & LCM", "Arrays", "Probability", "Binary Trees", "Percentages",
        "Graphs", "Time & Work", "Sorting Algorithms", "Number Systems",
        "Dynamic Programming", "Profit & Loss", "Recursion", "Speed & Distance",
        "Stacks & Queues", "Permutation & Combination", "Binary Search",
        "Ratios & Proportions", "Linked Lists", "Average & Mixtures", "Hashing"
    );

    public SkilloraAiController(SkilloraAiMentorService mentorService) {
        this.mentorService = mentorService;
    }

    @PostMapping
    public ResponseEntity<SkilloraAiResponse> generate(@RequestBody SkilloraAiRequest request) {
        return ResponseEntity.ok(mentorService.generate(request == null ? new SkilloraAiRequest() : request));
    }

    /**
     * Returns today's daily challenge — same question for the whole day.
     * Topic rotates based on day-of-year modulo topic list size.
     */
    @GetMapping("/daily-challenge")
    public ResponseEntity<SkilloraAiResponse> dailyChallenge() {
        String today = LocalDate.now().toString();
        SkilloraAiResponse cached = dailyChallengeCache.get(today);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        // Rotate topic by day
        int dayOfYear = LocalDate.now().getDayOfYear();
        String topic = DAILY_TOPICS.get(dayOfYear % DAILY_TOPICS.size());

        SkilloraAiRequest req = new SkilloraAiRequest();
        req.setMode("PRACTICE");
        req.setTopic(topic);
        req.setDifficulty("Medium");
        req.setNumberOfQuestions(3);
        req.setStudentLevel("Intermediate");

        SkilloraAiResponse response = mentorService.generate(req);

        // Cache for the day (evict yesterday's entries)
        dailyChallengeCache.clear();
        dailyChallengeCache.put(today, response);

        return ResponseEntity.ok(response);
    }
}
