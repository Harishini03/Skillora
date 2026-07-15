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
                You are **Skillora AI** — the world's most advanced campus placement preparation mentor for Indian B.Tech students.
                You combine the expertise of a JEE Math professor, a FAANG software engineer, and a seasoned HR placement coach.
                
                Your mission: Make every student placement-ready at their dream company.
                
                STRICT RULES:
                - Return ONLY clean, well-structured Markdown. Use emojis sparingly for headers.
                - Generate 100%% original, unique content on every request. NEVER copy templates.
                - Never repeat questions from the previouslyGeneratedQuestions list.
                - For aptitude: use REAL numbers, word problems, and placement-style scenarios.
                - For DSA: provide working code snippets (Python or Java), explain time complexity.
                - For SQL: show actual query examples with expected output tables.
                - Questions must match real TCS, Infosys, Wipro, Cognizant, Capgemini, Amazon, and Zoho placement papers.
                - Always include difficulty tags: [Easy] [Medium] [Hard]
                - Ensure solutions are step-by-step so students can learn, not just memorize.
                - Coverage should span the full topic — beginners to advanced in one session.
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
        String title = company != null ? company + " Style Mock Test" : "Skillora Placement Mock Test";
        // Mock test always: 20 aptitude MCQs + 2 coding problems (1 easy, 1 medium)
        return """
## 🏆 %s
%s
%s

---
### 📝 SECTION A — APTITUDE (20 Questions)
Generate exactly **20 aptitude MCQ questions** covering: Quantitative Aptitude, Logical Reasoning, and Verbal Ability.
Difficulty: 8 Easy, 9 Medium, 3 Hard.

Format each as:
**Q{number}. [Question] [Difficulty: Easy/Medium/Hard]**
A) [Option A]
B) [Option B]
C) [Option C]
D) [Option D]
✅ **Answer:** [Letter] | 💡 **Explanation:** [2-3 line explanation] | ⏱ [time]
---

### 💻 SECTION B — CODING (2 Problems)
Generate exactly **2 coding problems**:

**Problem 1 [Easy]:**
- Problem Statement: [Clear description with constraints]
- Input Format: [Describe input]
- Output Format: [Describe output]
- Sample Input: [example]
- Sample Output: [example]
- Hint: [one helpful hint]
- Solution Approach: [brief algorithm description]

**Problem 2 [Medium]:**
- Problem Statement: [Clear description with constraints]
- Input Format: [Describe input]
- Output Format: [Describe output]
- Sample Input: [example]
- Sample Output: [example]
- Hint: [one helpful hint]
- Solution Approach: [brief algorithm description]

---
## 📊 Test Summary
- Total Questions: 22 (20 Aptitude + 2 Coding)
- Recommended Time: 90 minutes
- Scoring: +4 per correct aptitude, -1 for wrong | Coding: pass all test cases
""".formatted(title, companyCtx, prevQ);
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
        String topicLower = request.getTopic().toLowerCase();
        
        if (topicLower.contains("sql") || topicLower.contains("join") || topicLower.contains("dbms")) {
            return """
                ## 📊 SQL Revision Notes
                - **ACID properties**: Atomicity, Consistency, Isolation, Durability.
                - **JOIN vs UNION**: JOIN combines horizontally (columns), UNION combines vertically (rows).
                - **Indexing**: Use indexes for frequently searched columns, but avoid over-indexing to save insert/update time.
                
                ## 🚀 Quick Tips
                - Always filter data using `WHERE` before joining if possible.
                - Remember `GROUP BY` must include all non-aggregated columns in SELECT.
                
                ## ⚠️ Common Mistakes
                - Missing the `ON` condition in JOINS leading to Cartesian products.
                - Confusing `HAVING` (filters after grouping) with `WHERE` (filters before grouping).
                """.formatted();
        } else if (topicLower.contains("tree") || topicLower.contains("graph") || topicLower.contains("array") || topicLower.contains("dynamic")) {
            return """
                ## 💻 DSA Revision Notes
                - **Arrays**: Master Two-Pointer and Sliding Window techniques.
                - **Trees**: Know Preorder (Root-L-R), Inorder (L-Root-R), and Postorder (L-R-Root).
                - **Graphs**: Use BFS for shortest path in unweighted graphs, DFS for topological sorting or cycle detection.
                - Always clarify Time (Big-O) and Space constraints before coding.
                
                ## 🚀 Quick Tips
                - If the array is sorted, immediately think Binary Search (O(log N)).
                - If you need to keep track of frequencies, use a Hash Map.
                
                ## ⚠️ Common Mistakes
                - Missing base cases in recursive tree traversals.
                - Not bounds-checking array indices (`IndexOutOfBoundsException`).
                """.formatted();
        }
        
        return """
                ## 📈 Aptitude Revision Notes
                - Percentage = Part / Whole x 100
                - Average = Total / Count
                - Profit %% = Profit / Cost Price x 100
                
                ## 🚀 Shortcut Tricks
                - Use option elimination before full calculation.
                - Round values only when options are far apart.
                
                ## ⚠️ Common Mistakes
                - Using final value as base in percentage questions.
                - Forgetting to convert hours to minutes.
                
                ## 🎯 Summary
                Revise formulas, solve a few mixed questions, and maintain a mistake notebook for weak topics: %s.
                """.formatted(String.join(", ", request.getWeakTopics()));
    }

    private String fallbackAdaptive(SkilloraAiRequest request) {
        String direction = request.getAccuracy() < 60 ? "Start with easier drills before moving to mixed practice." : "Move into medium drills and add timed sets.";
        String topicLower = request.getTopic().toLowerCase();
        
        String notesAndMistakes;
        if (topicLower.contains("sql") || topicLower.contains("join") || topicLower.contains("dbms")) {
            notesAndMistakes = """
                ## 📝 DBMS Revision Notes
                Focus on query optimization and understanding relationships (1:1, 1:N, M:N).
                - Use INNER JOIN for strict matches.
                - Use LEFT JOIN for optional relationships.
                
                ## ⚠️ Mistakes to Avoid
                - Ignoring NULL values in aggregate functions.
                - Using SELECT * in production queries.
                """;
        } else if (topicLower.contains("tree") || topicLower.contains("graph") || topicLower.contains("array") || topicLower.contains("dynamic")) {
            notesAndMistakes = """
                ## 📝 DSA Revision Notes
                Focus on breaking down problems into subproblems.
                - Trees: Practice recursion base cases carefully.
                - Graphs: Always track visited nodes.
                - DP: Draw the state-transition matrix.
                
                ## ⚠️ Mistakes to Avoid
                - Hardcoding array lengths.
                - Using O(N^2) loops when O(N) HashMap would work.
                """;
        } else {
            notesAndMistakes = """
                ## 📝 Aptitude Revision Notes
                Focus on concept recognition, unit conversion, and step-by-step solving.
                - Percentage = Part / Whole x 100
                - Speed = Distance / Time
                
                ## ⚠️ Mistakes to Avoid
                - Rushing through question wording.
                - Not validating answers with estimation.
                """;
        }
        
        return """
                ## 🎯 Personalized Feedback
                Accuracy: %.2f%%. %s

                ## 🔍 Weak Concepts
                - %s
                
                %s
                
                ## 🟢 Five Easy Questions
                %s

                ## 🟡 Five Medium Questions
                %s

                ## 🔴 Three Hard Questions
                %s

                ## 📅 Daily Practice Plan
                - Day 1: Revise formulas/concepts and solve 10 easy questions.
                - Day 2: Solve 10 medium questions and review errors.
                - Day 3: Take a 20-minute mixed quiz.
                """.formatted(
                request.getAccuracy(),
                direction,
                request.getWeakTopics().isEmpty() ? request.getTopic() : String.join(", ", request.getWeakTopics()),
                notesAndMistakes,
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
        String topic = request.getTopic();
        String topicLower = topic.toLowerCase();
        int seed = Math.abs((topic + difficulty + System.nanoTime()).hashCode() % 50) + 10;

        // Categorize topic for appropriate question generation
        boolean isDSA = topicLower.contains("tree") || topicLower.contains("graph") || topicLower.contains("array")
                || topicLower.contains("stack") || topicLower.contains("queue") || topicLower.contains("link")
                || topicLower.contains("dynamic") || topicLower.contains("sort") || topicLower.contains("search")
                || topicLower.contains("recursion") || topicLower.contains("hash") || topicLower.contains("heap");
        boolean isSQL = topicLower.contains("sql") || topicLower.contains("join") || topicLower.contains("dbms")
                || topicLower.contains("database") || topicLower.contains("query");
        boolean isOS = topicLower.contains("process") || topicLower.contains("thread") || topicLower.contains("operating") || topicLower.contains("memory");
        boolean isProbability = topicLower.contains("probability") || topicLower.contains("permutation") || topicLower.contains("combination");
        boolean isWorkTime = topicLower.contains("time") && topicLower.contains("work");
        boolean isProfit = topicLower.contains("profit") || topicLower.contains("loss") || topicLower.contains("discount");
        boolean isSpeed = topicLower.contains("speed") || topicLower.contains("distance") || topicLower.contains("train");
        boolean isPercentage = topicLower.contains("percent");
        boolean isHCF = topicLower.contains("hcf") || topicLower.contains("lcm");
        boolean isAverage = topicLower.contains("average") || topicLower.contains("mean");
        boolean isRatio = topicLower.contains("ratio") || topicLower.contains("proportion");

        String[] aptitudeTemplates = null;
        if (isProbability) {
            aptitudeTemplates = new String[]{
                "A bag has %d red, %d blue, and %d green balls. What is the probability of picking a blue ball?",
                "%d students appear for an exam. %d pass. What is the probability of randomly selecting a student who passed?",
                "Two dice are thrown. What is the probability that the sum is %d?",
                "In a group of %d people, %d are women. If one person is chosen randomly, what is the probability of choosing a man?"
            };
        } else if (isWorkTime) {
            aptitudeTemplates = new String[]{
                "A can complete a job in %d days and B in %d days. In how many days will they together complete the job?",
                "%d workers can build a wall in %d days. How many days will %d workers take?",
                "A pipe fills a tank in %d hours and another empties it in %d hours. In how long will the tank fill if both are open?"
            };
        } else if (isProfit) {
            aptitudeTemplates = new String[]{
                "A shopkeeper sells an item for ₹%d that costs ₹%d. What is the profit percentage?",
                "An article is marked at ₹%d and sold at %d%% discount. What is the selling price?",
                "Cost price of %d items equals selling price of %d items. What is the profit/loss percentage?"
            };
        } else if (isSpeed) {
            aptitudeTemplates = new String[]{
                "A train %d m long crosses a pole in %d seconds. What is the speed of the train in km/h?",
                "Two cars start from cities %d km apart at %d km/h and %d km/h towards each other. In how long will they meet?",
                "A man rows %d km upstream in %d hours and the same downstream in %d hours. What is the speed of the stream?"
            };
        } else if (isPercentage) {
            aptitudeTemplates = new String[]{
                "If %d is %d%% of a number, what is %d%% of that number?",
                "A salary is increased by %d%% then decreased by %d%%. What is the net percentage change?",
                "%d out of %d voters voted for candidate A. What percentage of voters voted for candidate B?"
            };
        } else if (isHCF) {
            aptitudeTemplates = new String[]{
                "Find the LCM of %d and %d.",
                "The HCF of two numbers is %d and their LCM is %d. If one number is %d, find the other.",
                "Three bells ring at intervals of %d, %d, and %d minutes. After how many minutes will they ring together?"
            };
        } else if (isAverage) {
            aptitudeTemplates = new String[]{
                "The average of %d numbers is %d. If one number is removed, the average becomes %d. What was the removed number?",
                "%d students scored an average of %d. %d new students join with an average of %d. What is the new average?",
                "The average age of a group of %d is %d. If %d people with average age %d leave, find the new average."
            };
        } else if (isRatio) {
            aptitudeTemplates = new String[]{
                "Divide ₹%d among A, B, C in ratio %d:%d:%d. How much does B get?",
                "A mixture has milk and water in ratio %d:%d. If %d liters of water is added, the ratio becomes %d:%d. Find original quantity.",
                "Salaries of A and B are in ratio %d:%d. If both get a %d%% raise, find the new ratio."
            };
        } else {
            aptitudeTemplates = new String[]{
                "%d candidates apply for %d positions. After %d%% are shortlisted, how many proceed to interview?",
                "A placement test has %d questions worth %d marks each and %d questions worth %d marks. Maximum marks?",
                "In a coding round, %d students pass out of %d. What percentage failed?"
            };
        }

        for (int i = 1; i <= count; i++) {
            int a = seed + i * 7;
            int b = seed + i * 3;
            int c = seed + i * 2;
            builder.append("\n---\n");
            builder.append("**Q").append(i).append(". ");

            if (isDSA) {
                // DSA questions with algorithm context
                String[] dsaQuestions = {
                    "What is the time complexity of searching in a **Balanced BST** with " + (a * 1000) + " nodes? A) O(N) B) O(log N) C) O(N log N) D) O(1) ✅ **Answer:** B",
                    "For a **Graph** with " + a + " vertices and " + (a * 2) + " edges, BFS/DFS time complexity is: A) O(V) B) O(E) C) O(V+E) D) O(V*E) ✅ **Answer:** C",
                    "What is the worst-case time of QuickSort for " + (a * 100) + " elements? A) O(N log N) B) O(N^2) C) O(N) D) O(log N) ✅ **Answer:** B",
                    "A stack has push operations: " + a + ", " + b + ", " + c + ". After 2 pops, the top is: A) " + a + " B) " + b + " C) " + c + " D) Empty ✅ **Answer:** B",
                    "Inorder traversal of a BST gives output in: A) Descending order B) Ascending order C) Random order D) Level order ✅ **Answer:** B"
                };
                builder.append(dsaQuestions[i % dsaQuestions.length]).append("**\n");
                if (!compact) {
                    builder.append("💡 **Explanation:** This is a fundamental " + topic + " concept tested at all top product companies.\n");
                    builder.append("⏱ **Time:** ").append("Hard".equalsIgnoreCase(difficulty) ? "4 min" : "2 min").append("\n");
                }
            } else if (isSQL) {
                String[] sqlQuestions = {
                    "Which JOIN returns all rows from the left table and matched rows from right? A) INNER JOIN B) LEFT JOIN C) RIGHT JOIN D) CROSS JOIN ✅ **Answer:** B",
                    "Which clause filters aggregated data? A) WHERE B) HAVING C) GROUP BY D) ORDER BY ✅ **Answer:** B",
                    "What does SELECT COUNT(*) FROM employees WHERE dept='HR' return if HR has " + a + " employees? A) " + (a-1) + " B) " + a + " C) All names D) NULL ✅ **Answer:** B",
                    "Which SQL constraint ensures unique non-null values in a column? A) DEFAULT B) CHECK C) PRIMARY KEY D) FOREIGN KEY ✅ **Answer:** C",
                    "Which statement removes all rows but keeps table structure? A) DELETE B) DROP C) TRUNCATE D) ALTER ✅ **Answer:** C"
                };
                builder.append(sqlQuestions[i % sqlQuestions.length]).append("**\n");
                if (!compact) {
                    builder.append("💡 **Explanation:** This is a core SQL/DBMS concept frequently tested in placement written rounds.\n");
                    builder.append("⏱ **Time:** 90 sec\n");
                }
            } else {
                // Aptitude questions with varied templates
                String[] template = aptitudeTemplates;
                String q = template[i % template.length];
                // Replace placeholders with calculated values
                q = q.replace("%d", "").replace("%d", ""); // safety
                // Generate with actual values formatted
                builder.append(String.format("[" + difficulty + "] " + topic + " Question " + i + ":** "));
                // Simplified structured Q&A format
                builder.append("In a placement test with " + (a * 5) + " total marks, a student scores " + (b * 4) + " marks. " +
                    "After a " + (i * 5 + 10) + "% bonus, what is their final score?\n");
                int correctAns = (b * 4) + ((b * 4) * (i * 5 + 10) / 100);
                builder.append("A) " + (correctAns - 15) + "  B) " + correctAns + "  C) " + (correctAns + 20) + "  D) " + (correctAns - 5) + "\n");
                builder.append("✅ **Answer:** B\n");
                if (!compact) {
                    builder.append("💡 **Explanation:** Final = " + (b*4) + " × (1 + " + (i * 5 + 10) + "/100) = " + correctAns + ". This tests percentage increase applied to a base score.\n");
                    builder.append("⏱ **Time:** " + ("Hard".equalsIgnoreCase(difficulty) ? "3 min" : "90 sec") + "\n");
                }
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
