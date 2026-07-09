package com.placement.placement_intelligence.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.placement_intelligence.dto.SkilloraAiRequest;
import com.placement.placement_intelligence.dto.SkilloraAiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SkilloraAiMentorService {
    private static final List<String> MODES = List.of("LEARN", "PRACTICE", "ADAPTIVE", "REVISION", "MOCK_TEST");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${app.groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    public SkilloraAiMentorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public SkilloraAiResponse generate(SkilloraAiRequest request) {
        String mode = normalizeMode(request.getMode());
        String topic = clean(defaultText(request.getTopic(), "Aptitude"), 80);
        SkilloraAiRequest normalized = normalizeRequest(request, mode, topic);

        if (apiKey == null || apiKey.isBlank()) {
            return new SkilloraAiResponse(mode, topic, fallbackContent(normalized), false);
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0.72,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", buildPrompt(normalized))
                    )
            ));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(35))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new SkilloraAiResponse(mode, topic, fallbackContent(normalized), false);
            }
            String content = extractContent(response.body());
            if (content.isBlank()) {
                return new SkilloraAiResponse(mode, topic, fallbackContent(normalized), false);
            }
            return new SkilloraAiResponse(mode, topic, content, true);
        } catch (Exception ex) {
            return new SkilloraAiResponse(mode, topic, fallbackContent(normalized), false);
        }
    }

    private SkilloraAiRequest normalizeRequest(SkilloraAiRequest source, String mode, String topic) {
        SkilloraAiRequest request = new SkilloraAiRequest();
        request.setMode(mode);
        request.setTopic(topic);
        request.setSubtopic(clean(defaultText(source.getSubtopic(), ""), 80));
        request.setDifficulty(clean(defaultText(source.getDifficulty(), "Medium"), 30));
        request.setNumberOfQuestions(Math.max(1, Math.min(30, source.getNumberOfQuestions() == null ? 10 : source.getNumberOfQuestions())));
        request.setStudentLevel(clean(defaultText(source.getStudentLevel(), "Intermediate"), 30));
        request.setCompany(source.getCompany() != null ? clean(source.getCompany(), 50) : null);
        request.setExamType(source.getExamType() != null ? clean(source.getExamType(), 30) : null);
        request.setPreviousTopicsCovered(limitList(source.getPreviousTopicsCovered()));
        request.setPreviouslyGeneratedQuestions(limitList(source.getPreviouslyGeneratedQuestions()));
        request.setWeakTopics(limitList(source.getWeakTopics()));
        request.setCorrect(Math.max(0, source.getCorrect() == null ? 0 : source.getCorrect()));
        request.setWrong(Math.max(0, source.getWrong() == null ? 0 : source.getWrong()));
        request.setAccuracy(Math.max(0, Math.min(100, source.getAccuracy() == null ? 0 : source.getAccuracy())));
        return request;
    }

    private String systemPrompt() {
        return """
                You are Skillora AI — an expert Placement Preparation Mentor for Indian engineering students.
                You generate high-quality, unique, and fresh educational content for campus placement preparation.
                RULES:
                - Return ONLY clean Markdown. No JSON unless mode is PRACTICE/MOCK_TEST.
                - Generate completely original content every time.
                - Never repeat any question from previouslyGeneratedQuestions list.
                - Use concrete numbers, real scenarios, and step-by-step solutions.
                - Questions must feel like real TCS/Infosys/Wipro/Amazon placement exams.
                - Prioritize clarity, relevance, and practical applicability.
                """;
    }

    private String buildPrompt(SkilloraAiRequest request) {
        String topic   = request.getTopic();
        String sub     = request.getSubtopic();
        String level   = request.getStudentLevel();
        String diff    = request.getDifficulty();
        int    n       = request.getNumberOfQuestions();
        String company = (request.getCompany() != null && !request.getCompany().isBlank()) ? request.getCompany() : null;
        String companyCtx = company != null
                ? "Focus on how " + company + " tests this topic. Match " + company + "'s actual placement question style."
                : "Make questions suitable for top Indian IT company placements (TCS, Infosys, Wipro, Cognizant, Capgemini).";
        String topicLine = sub != null && !sub.isBlank() ? topic + " — " + sub : topic;
        String prevQ = request.getPreviouslyGeneratedQuestions() != null && !request.getPreviouslyGeneratedQuestions().isEmpty()
                ? "DO NOT repeat these questions: " + request.getPreviouslyGeneratedQuestions()
                : "";
        String weakCtx = request.getWeakTopics() != null && !request.getWeakTopics().isEmpty()
                ? "Student weak areas: " + request.getWeakTopics() + ". Address these in your content."
                : "";

        return switch (request.getMode()) {
            case "LEARN" -> buildLearnPrompt(topicLine, level, company, companyCtx, weakCtx);
            case "PRACTICE" -> buildPracticePrompt(topicLine, n, diff, level, companyCtx, prevQ, weakCtx);
            case "ADAPTIVE" -> buildAdaptivePrompt(topicLine, n, request.getCorrect(), request.getWrong(), request.getAccuracy(), level, weakCtx, prevQ);
            case "REVISION" -> buildRevisionPrompt(topicLine, level, weakCtx);
            case "MOCK_TEST" -> buildMockTestPrompt(topicLine, n, level, company, companyCtx, prevQ);
            default -> buildLearnPrompt(topicLine, level, company, companyCtx, weakCtx);
        };
    }

    private String buildLearnPrompt(String topic, String level, String company, String companyCtx, String weakCtx) {
        return """
Generate a comprehensive learning module on **%s** for a **%s** student.
%s
%s

Structure your response EXACTLY as follows:

## 📖 Introduction
Brief overview of the topic and why it matters in placements.

## 🔑 Core Concepts
Explain each key concept clearly with simple language. Use bullet points.

## 📐 Important Formulas & Rules
List all critical formulas, theorems, or rules with a brief explanation of when to use each.

## ⚡ Shortcuts & Tricks
List powerful time-saving tricks specifically useful for MCQ-based placement tests.

## ❌ Common Mistakes
List 4-5 mistakes students commonly make with this topic, and how to avoid them.

## 🔍 Solving Strategy
Step-by-step approach to tackle questions: Read → Identify → Formula → Solve → Verify.

## 📝 5 Worked Examples
For each: show the problem, step-by-step solution, and the key insight.
Use realistic, concrete numbers.

## 💡 Interview Tips
3-4 specific tips for performing well on this topic in placement tests.

## ❓ FAQs
3 common student questions with clear answers.

## 📌 Quick Revision Summary
10-line cheatsheet covering the most important points.
""".formatted(topic, level, companyCtx, weakCtx);
    }

    private String buildPracticePrompt(String topic, int n, String diff, String level, String companyCtx, String prevQ, String weakCtx) {
        return """
Generate **%d unique %s-level MCQ questions** on **%s** for a **%s** student.
%s
%s
%s

For EACH question, use this EXACT format:

---
**Q{number}. [Question text — must be a word problem with specific numbers and a clear scenario]**

A) [Option A]
B) [Option B]
C) [Option C]
D) [Option D]

✅ **Answer:** [Letter]

💡 **Explanation:** [2-4 lines showing the solving method step-by-step with the actual calculation]

⏱ **Time:** [30-90 seconds estimate]
---

Rules:
- Questions must require actual calculation or multi-step reasoning
- Options must be plausible numbers (not obviously wrong)
- Mix different subtopics and approaches within the topic
- Word problems preferred over theoretical questions
""".formatted(n, diff, topic, level, companyCtx, prevQ, weakCtx);
    }

    private String buildAdaptivePrompt(String topic, int n, int correct, int wrong, double accuracy, String level, String weakCtx, String prevQ) {
        String perfFeedback = accuracy >= 80 ? "Excellent performance! Increase difficulty significantly."
                : accuracy >= 60 ? "Good performance. Mix medium and hard questions."
                : accuracy >= 40 ? "Average performance. Start with easy-medium questions."
                : "Struggling. Begin with foundational easy questions and build up.";
        return """
A student scored **%d correct, %d wrong (%.1f%% accuracy)** on **%s** (level: %s).
%s
%s
%s

Generate an adaptive response:

## 📊 Performance Analysis
Analyze their accuracy and identify which concepts they likely struggle with.

## 🎯 Personalized Feedback
Give specific, encouraging feedback based on their score.

## 📚 Targeted Revision
Quick revision of the most important concepts they likely missed.

## 💊 Weak Area Treatment
For each weak concept: brief explanation + 1 worked example.

## 🏋️ Practice Set (%d Questions)
Generate questions at the right difficulty:
- If accuracy < 50%%: mostly Easy questions
- If accuracy 50-70%%: mix of Easy/Medium
- If accuracy > 70%%: mostly Medium/Hard

Use the same MCQ format as PRACTICE mode with explanations.

## 📅 3-Day Recovery Plan
Day 1: [Specific actions]
Day 2: [Specific actions]
Day 3: [Specific actions]
""".formatted(correct, wrong, accuracy, topic, level, perfFeedback, weakCtx, prevQ, n);
    }

    private String buildRevisionPrompt(String topic, String level, String weakCtx) {
        return """
Create a **comprehensive revision sheet** for **%s** (student level: %s).
%s

## ⚡ Formula Flash Cards
All critical formulas in a scannable format. For each: Formula | When to use | Example.

## 🔑 Core Concepts at a Glance
7-10 bullet points covering every major concept. Keep each under 2 lines.

## 🚀 Speed Tricks
5-8 tricks to solve questions faster under exam conditions.

## ⚠️ Avoid These Traps
5 common errors with "WRONG ❌" and "CORRECT ✅" examples side by side.

## 📋 Question Pattern Recognition
4-5 common question types with the approach to solve each:
- Pattern: [description] → Approach: [method]

## 🎯 10-Minute Drill
5 quick questions (with answers) to warm up before an exam.

## 📌 Last-Minute Checklist
10 bullet points — the absolute essentials to remember.
""".formatted(topic, level, weakCtx);
    }

    private String buildMockTestPrompt(String topic, int n, String level, String company, String companyCtx, String prevQ) {
        String title = company != null ? company + " Style Mock Test: " + topic : "Placement Mock Test: " + topic;
        return """
Generate a **%d-question placement mock test** titled "%s".
%s
%s
Difficulty distribution: 30%% Easy, 50%% Medium, 20%% Hard.

Format each question as:

---
**Q{number}. [Question] [Difficulty: Easy/Medium/Hard]**

A) [Option]
B) [Option]
C) [Option]
D) [Option]

✅ **Answer:** [Letter] | 💡 **Explanation:** [Brief explanation] | ⏱ [Time estimate]
---

At the end, add:

## 📊 Test Overview
- Total Questions: %d
- Recommended Time: %d minutes
- Topics Covered: [list]
- Difficulty Breakdown: Easy (30%%) | Medium (50%%) | Hard (20%%)
""".formatted(n, title, companyCtx, prevQ, n, n);
    }

    private String extractContent(String completionJson) throws Exception {
        JsonNode root = objectMapper.readTree(completionJson);
        return root.path("choices").path(0).path("message").path("content").asText("").trim();
    }

    private String fallbackContent(SkilloraAiRequest request) {
        return switch (request.getMode()) {
            case "LEARN" -> fallbackLearn(request);
            case "PRACTICE" -> fallbackQuestions(request, request.getNumberOfQuestions(), false);
            case "ADAPTIVE" -> fallbackAdaptive(request);
            case "REVISION" -> fallbackRevision(request);
            case "MOCK_TEST" -> fallbackMock(request);
            default -> fallbackLearn(request);
        };
    }

    private String fallbackLearn(SkilloraAiRequest request) {
        String topic = request.getTopic();
        String topicLower = topic.toLowerCase();
        
        if (topicLower.contains("sql") || topicLower.contains("join") || topicLower.contains("dbms")) {
            return """
                ## 📖 Introduction
                **%s** is a crucial topic for database rounds in technical interviews. It's used to efficiently organize, manipulate, and combine data across multiple tables.
                
                ## 🔑 Core Concepts
                - **Normalization**: Reducing redundancy in data by organizing schemas (1NF, 2NF, 3NF).
                - **INNER JOIN**: Returns records that have matching values in both tables.
                - **LEFT (OUTER) JOIN**: Returns all records from the left table, and matched records from the right.
                - **ACID Properties**: Atomicity, Consistency, Isolation, Durability.
                
                ## 📐 Syntax & Examples
                ```sql
                SELECT e.name, d.dept_name
                FROM employees e
                INNER JOIN departments d ON e.dept_id = d.id;
                ```
                
                ## ⚡ Shortcuts & Tricks
                - Always draw a Venn diagram in your head when visualizing Joins.
                - Use LEFT JOIN with a `WHERE right.id IS NULL` check to find "missing" records.
                
                ## ❌ Common Mistakes
                - Forgetting the ON clause which results in a massive Cartesian product (CROSS JOIN).
                - Using a subquery when a JOIN would be significantly faster.
                
                ## 💡 Interview Tips
                - Interviewers at top companies often ask you to optimize a query or explain execution plans. Always mention indexing the joining columns!
                
                ## 📌 Quick Revision Summary
                - Inner = Intersection. Left = Left + Intersection.
                - Ensure primary keys and foreign keys are indexed.
                """.formatted(topic);
        } else if (topicLower.contains("tree") || topicLower.contains("graph") || topicLower.contains("array") || topicLower.contains("dynamic")) {
            return """
                ## 📖 Introduction
                **%s** is a foundational algorithmic concept that frequently appears in data structure rounds of top product companies.
                
                ## 🔑 Core Concepts
                - **Traversal / Iteration**: Systematically visiting every element.
                - **Time & Space Complexity**: Measuring how execution scales with input size (Big-O notation).
                - **DFS (Depth First Search)**: Goes as deep as possible before backtracking (uses a Stack or Recursion).
                - **BFS (Breadth First Search)**: Explores level by level (uses a Queue).
                
                ## ⚡ Shortcuts & Tricks
                - If the question asks for the "shortest path" in an unweighted grid/graph, always use BFS.
                - If it involves permutations, combinations, or backtracking, think DFS/Recursion.
                - For arrays, the Two-Pointer or Sliding Window techniques solve 80%% of O(N) optimization problems.
                
                ## ❌ Common Mistakes
                - Forgetting to track a `visited` state, leading to infinite loops!
                - Not handling base cases for null/empty inputs properly (`if input == null return`).
                
                ## 💡 Interview Tips
                - Always start by drawing out a small example on the whiteboard before writing code.
                - Clarify constraints first: Can numbers be negative? Is the array sorted?
                
                ## 📌 Quick Revision Summary
                - Search in BST: O(log N) average.
                - Graph DFS/BFS: O(V + E).
                - Array Sort: O(N log N).
                """.formatted(topic);
        }
        
        return """
                ## 📖 Introduction
                **%s** is a common placement preparation area. Companies use it to check whether you can read a problem, identify the pattern, and solve it efficiently under time pressure.
                
                ## 🔑 Core Concepts
                - Understand the question completely before you start calculating.
                - Convert word statements into variables, ratios, tables, or linear equations.
                - Identify if the answer should increase, decrease, or stay proportional.
                
                ## 📐 Important Formulae
                - Percentage change = (Change / Original) x 100
                - Average = Sum of values / Number of values
                - Speed = Distance / Time
                - Work rate = Work done / Time taken
                
                ## ⚡ Shortcuts & Tricks
                - Estimate the answer first, then calculate. This catches silly math errors.
                - Eliminate options that break basic logic (e.g. if speed increases, time MUST decrease).
                - Plug in simple numbers (like 100) to test complex algebraic formulas.
                
                ## ❌ Common Mistakes
                - Mixing up the "base value" and the "final value" in percentage changes.
                - Ignoring units (mixing up minutes vs hours, meters vs kilometers).
                
                ## 💡 Interview Tips
                - Always explain your approach before arriving at the final answer.
                - Mention any assumptions you make clearly.
                
                ## 📌 Quick Revision Summary
                - Track units carefully.
                - Estimate before choosing.
                - Review every wrong answer in your mock tests.
                """.formatted(topic);
    }

    private String fallbackRevision(SkilloraAiRequest request) {
        return """
                ## Important Formulae
                - Percentage = Part / Whole x 100
                - Average = Total / Count
                - Profit %% = Profit / Cost Price x 100
                - Time complexity: one loop is usually O(n), nested loops are often O(n^2)

                ## Shortcut Tricks
                - Use option elimination before full calculation.
                - Round values only when options are far apart.
                - Draw a quick table for comparison problems.

                ## Quick Tips
                - Spend the first 10 seconds understanding the ask.
                - Write units beside values.
                - Recheck signs in increase/decrease problems.

                ## Common Mistakes
                - Using final value as base in percentage questions.
                - Forgetting to convert hours to minutes.
                - Not reading words like except, minimum, maximum, or not.

                ## Examples
                - If 60 is 20%% of x, then x = 60 x 100 / 20 = 300.
                - If 4 people finish work in 6 days, total work = 24 person-days.

                ## Summary
                Revise formulas, solve a few mixed questions, and maintain a mistake notebook for weak topics: %s.
                """.formatted(String.join(", ", request.getWeakTopics()));
    }

    private String fallbackAdaptive(SkilloraAiRequest request) {
        String direction = request.getAccuracy() < 60 ? "Start with easier drills before moving to mixed practice." : "Move into medium drills and add timed sets.";
        return """
                ## Personalized Feedback
                Accuracy: %.2f%%. %s

                ## Weak Concepts
                - %s
                - Calculation accuracy
                - Choosing the right formula or logic pattern

                ## Mistakes
                - Rushing through question wording
                - Not validating answers with estimation

                ## Revision Notes
                Focus on concept recognition, unit conversion, and step-by-step solving.

                ## Formula Sheet
                - Percentage = Part / Whole x 100
                - Average = Sum / Count
                - Speed = Distance / Time

                ## Memory Tricks
                - G-A-S: Given, Asked, Solve.
                - E-C-V: Estimate, Calculate, Verify.

                ## Five Easy Questions
                %s

                ## Five Medium Questions
                %s

                ## Three Hard Questions
                %s

                ## Daily Practice Plan
                - Day 1: Revise formulas and solve 10 easy questions.
                - Day 2: Solve 10 medium questions and review errors.
                - Day 3: Take a 20-minute mixed quiz.
                - Day 4: Redo wrong questions without seeing solutions.
                - Day 5: Attempt a mock test.

                ## Estimated Time to Master Topic
                5 to 7 focused practice days.
                """.formatted(
                request.getAccuracy(),
                direction,
                request.getWeakTopics().isEmpty() ? request.getTopic() : String.join(", ", request.getWeakTopics()),
                numberedQuestions(request, 5, "Easy"),
                numberedQuestions(request, 5, "Medium"),
                numberedQuestions(request, 3, "Hard")
        );
    }

    private String fallbackMock(SkilloraAiRequest request) {
        int count = request.getNumberOfQuestions();
        int easy = Math.max(1, Math.round(count * 0.4f));
        int medium = Math.max(1, Math.round(count * 0.4f));
        int hard = Math.max(1, count - easy - medium);
        return "## Placement Mock Test\n\n"
                + numberedQuestions(request, easy, "Easy")
                + "\n"
                + numberedQuestions(request, medium, "Medium")
                + "\n"
                + numberedQuestions(request, hard, "Hard");
    }

    private String fallbackQuestions(SkilloraAiRequest request, int count, boolean compact) {
        return "## Practice Questions\n\n" + numberedQuestions(request, count, request.getDifficulty(), compact);
    }

    private String numberedQuestions(SkilloraAiRequest request, int count, String difficulty) {
        return numberedQuestions(request, count, difficulty, false);
    }

    private String numberedQuestions(SkilloraAiRequest request, int count, String difficulty, boolean compact) {
        StringBuilder builder = new StringBuilder();
        int seed = Math.abs((request.getTopic() + request.getSubtopic() + System.nanoTime()).hashCode() % 37) + 6;
        String topicLower = request.getTopic().toLowerCase();
        
        boolean isCoding = topicLower.contains("tree") || topicLower.contains("graph") || topicLower.contains("array") || topicLower.contains("sql") || topicLower.contains("dbms") || topicLower.contains("string");
        
        for (int i = 1; i <= count; i++) {
            int base = seed + (i * 3);
            builder.append("### Question ").append(i).append(" (").append(difficulty).append(")\n");
            
            if (isCoding) {
                builder.append("Which of the following data structures or queries best optimizes the process for a dataset of size ").append(base * 100).append(" in a standard **").append(request.getTopic()).append("** scenario?\n\n");
                builder.append("A. O(N) linear traversal\n");
                builder.append("B. O(log N) optimized tree navigation or indexing\n");
                builder.append("C. O(N^2) nested iterations\n");
                builder.append("D. O(1) direct hash mapping\n\n");
                builder.append("**Correct Answer:** B\n\n");
                if (!compact) {
                    builder.append("**Explanation:** Optimizing to logarithmic or constant time is highly preferred for large datasets. In the context of ").append(request.getTopic()).append(", applying binary search techniques or proper indexing reduces operations significantly compared to linear scans.\n\n");
                }
            } else {
                builder.append("A placement team reviews ").append(base).append(" candidates and shortlists ")
                        .append(Math.max(2, base / 3)).append(" based on their **").append(request.getTopic()).append("** aptitude scores. What is the approximate shortlist percentage?\n\n");
                builder.append("A. 33.33%\n");
                builder.append("B. 25.00%\n");
                builder.append("C. 15.50%\n");
                builder.append("D. 40.00%\n\n");
                builder.append("**Correct Answer:** A\n\n");
                if (!compact) {
                    builder.append("**Explanation:** Calculating (shortlisted / total) * 100 gives approximately 33.33%, which tests basic proportional reasoning applied to ").append(request.getTopic()).append(" scenarios.\n\n");
                }
            }
            
            if (!compact) {
                builder.append("**Estimated Solving Time:** ").append("Hard".equalsIgnoreCase(difficulty) ? "3 minutes" : "90 seconds").append("\n\n");
                builder.append("**Placement Company Level:** ").append("Hard".equalsIgnoreCase(difficulty) ? "Amazon/Microsoft" : "TCS/Infosys/Accenture").append("\n\n");
            }
        }
        return builder.toString();
    }

    private String normalizeMode(String value) {
        String mode = clean(defaultText(value, "LEARN"), 30).toUpperCase(Locale.ENGLISH).replace('-', '_').replace(' ', '_');
        return MODES.contains(mode) ? mode : "LEARN";
    }

    private List<String> limitList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> clean(value, 180))
                .limit(20)
                .toList();
    }

    private String clean(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        return cleaned.substring(0, maxLength);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
