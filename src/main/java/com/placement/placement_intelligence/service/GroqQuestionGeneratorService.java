package com.placement.placement_intelligence.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.placement_intelligence.model.Question;
import com.placement.placement_intelligence.model.TestType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroqQuestionGeneratorService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${app.groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    public GroqQuestionGeneratorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<Question> generateQuestions(TestType type, String topic, int count) {
        if (count <= 0) {
            return List.of();
        }
        if (apiKey == null || apiKey.isBlank()) {
            return templateQuestions(type, topic, count);
        }
        String prompt = buildPrompt(type, topic, count);
        String body;
        try {
            body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0.4,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You generate high-quality MCQ interview questions and always return valid JSON."
                            ),
                            Map.of("role", "user", "content", prompt)
                    )
            ));
        } catch (Exception ex) {
            return templateQuestions(type, topic, count);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return templateQuestions(type, topic, count);
            }
            List<Question> parsed = parseQuestions(response.body(), topic);
            if (parsed.isEmpty()) {
                return templateQuestions(type, topic, count);
            }
            return parsed;
        } catch (Exception ex) {
            return templateQuestions(type, topic, count);
        }
    }

    private List<Question> parseQuestions(String completionJson, String topic) throws IOException {
        JsonNode root = objectMapper.readTree(completionJson);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            return List.of();
        }

        JsonNode payload = objectMapper.readTree(contentNode.asText());
        JsonNode questionsNode = payload.path("questions");
        if (!questionsNode.isArray()) {
            return List.of();
        }

        List<Question> questions = new ArrayList<>();
        for (JsonNode node : questionsNode) {
            String questionText = truncate(node.path("questionText").asText(""), 1500);
            String optionA = truncate(node.path("optionA").asText(""), 255);
            String optionB = truncate(node.path("optionB").asText(""), 255);
            String optionC = truncate(node.path("optionC").asText(""), 255);
            String optionD = truncate(node.path("optionD").asText(""), 255);
            String correctOption = node.path("correctOption").asText("").trim().toUpperCase();
            String difficulty = truncate(node.path("difficultyLevel").asText("Medium"), 30);
            // explanation_hint is parsed but not stored (Question entity has no such field)
            // node.path("explanation_hint").asText("") — intentionally ignored
            if (!isValidQuestion(questionText, optionA, optionB, optionC, optionD, correctOption)) {
                continue;
            }
            Question question = new Question();
            question.setQuestionText(questionText);
            question.setOptionA(optionA);
            question.setOptionB(optionB);
            question.setOptionC(optionC);
            question.setOptionD(optionD);
            question.setCorrectOption(correctOption);
            question.setDifficultyLevel(difficulty.isBlank() ? "Medium" : difficulty);
            question.setTopic(truncate(topic, 60));
            questions.add(question);
        }
        return questions;
    }

    private String buildPrompt(TestType type, String topic, int count) {
        if (type == TestType.APTITUDE || type == TestType.MOCK) {
            return buildAptitudePrompt(topic, count);
        }
        if (type == TestType.CODING) {
            return buildCodingPrompt(topic, count);
        }
        // default
        return "Generate " + count + " unique multiple-choice interview questions for "
                + type.name().replace('_', ' ') + " on topic \"" + topic + "\". "
                + "Return strict JSON with key 'questions' as array. "
                + "Each item must include: questionText, optionA, optionB, optionC, optionD, correctOption (A/B/C/D), difficultyLevel (Easy/Medium/Hard). "
                + "No markdown.";
    }

    private String buildAptitudePrompt(String topic, int count) {
        return """
Generate %d unique placement interview aptitude MCQs on the topic "%s".

STYLE REQUIREMENTS — questions must feel like real TCS/Infosys/Wipro campus placement aptitude:
- Word problems with concrete numbers (e.g. HCF/LCM with actual values, percentage calculations, time-speed-distance, profit/loss, age problems, work problems)
- Each question must require actual calculation or reasoning — NOT trivial definitional questions
- Options should be plausible numerical answers (e.g. for HCF: 15, 11, 20, 25)
- Mix difficulty: ~30%% Easy (straightforward formula), ~50%% Medium (2-step reasoning), ~20%% Hard (multi-step or tricky)
- Include explanation_hint: a brief hint showing the solving approach (e.g. "Subtract remainder from each, find HCF")

TOPIC-SPECIFIC GUIDELINES:
- HCF/GCD: Use "divide X, Y, Z leaving same remainder R" or "greatest common measure" problems
- LCM: Use distance/revolution problems, meeting point problems, bells ringing problems
- Percentages: Use population growth, profit/loss, discount, successive %% problems
- Time & Work: Use pipes, workers, efficiency ratio problems
- Speed & Distance: Use relative speed, trains, boats problems
- Probability: Use cards, dice, balls from bags
- Algebra: Use age problems, ratio problems, partnership profit sharing

Return ONLY strict JSON (no markdown, no explanation outside JSON):
{
  "questions": [
    {
      "questionText": "...",
      "optionA": "...",
      "optionB": "...",
      "optionC": "...",
      "optionD": "...",
      "correctOption": "A",
      "difficultyLevel": "Medium",
      "explanation_hint": "..."
    }
  ]
}
""".formatted(count, topic);
    }

    private String buildCodingPrompt(String topic, int count) {
        return """
Generate %d unique DSA coding MCQs on "%s" suitable for placement interviews.
Questions should test: time complexity, space complexity, algorithm selection, output prediction, code trace.
Include: questionText, optionA, optionB, optionC, optionD, correctOption (A/B/C/D), difficultyLevel (Easy/Medium/Hard).
Return ONLY strict JSON: {"questions": [...]}
""".formatted(count, topic);
    }

    private boolean isValidQuestion(String questionText, String optionA, String optionB, String optionC, String optionD, String correctOption) {
        if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) {
            return false;
        }
        return "A".equals(correctOption) || "B".equals(correctOption) || "C".equals(correctOption) || "D".equals(correctOption);
    }

    private List<Question> templateQuestions(TestType type, String topic, int count) {
        // For APTITUDE / MOCK fallback, use real placement-style sample questions
        if (type == TestType.APTITUDE || type == TestType.MOCK) {
            List<Question> samples = List.of(
                buildSampleQuestion(
                    "The greatest number that divides 65, 110 and 200 leaving remainder 5 in each case is",
                    "15", "11", "20", "25", "A", "Easy", topic),
                buildSampleQuestion(
                    "The LCM of two numbers is 2310. Their HCF is 30. If one number is 210, the other is",
                    "330", "310", "350", "280", "A", "Medium", topic),
                buildSampleQuestion(
                    "A number when divided by 296 gives a remainder 75. When the same number is divided by 37, the remainder will be",
                    "2", "1", "11", "8", "B", "Medium", topic),
                buildSampleQuestion(
                    "Three bells toll at intervals of 9, 12, and 15 minutes. All three toll together at 8 AM. When will they next toll together?",
                    "9:00 AM", "10:00 AM", "11:00 AM", "9:30 AM", "C", "Hard", topic),
                buildSampleQuestion(
                    "The circumference of front and back wheels of a carriage are 75 inches and 66 inches. Starting together, the least distance (in inches) they travel before both complete exact revolutions is",
                    "1650", "1605", "1560", "1056", "A", "Hard", topic)
            );
            return samples.subList(0, Math.min(count, samples.size()));
        }

        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Question q = new Question();
            if (type == TestType.CODING) {
                q.setQuestionText("[" + topic + "] What is the time complexity of the best approach for coding scenario " + i + "?");
                q.setOptionA("O(n^2)");
                q.setOptionB("O(n log n)");
                q.setOptionC("O(n)");
                q.setOptionD("Depends on constraints and data structure");
                q.setCorrectOption("D");
                q.setDifficultyLevel(i % 3 == 0 ? "Hard" : "Medium");
            } else if (type == TestType.SOFT_SKILLS) {
                q.setQuestionText("[" + topic + "] In interview situation " + i + ", what is the most professional response?");
                q.setOptionA("Interrupt to prove confidence");
                q.setOptionB("Listen, clarify, then respond concisely");
                q.setOptionC("Avoid answering difficult parts");
                q.setOptionD("Give very long generic responses");
                q.setCorrectOption("B");
                q.setDifficultyLevel(i % 3 == 0 ? "Hard" : "Easy");
            } else {
                q.setQuestionText("[" + topic + "] Practice question " + i + ": choose the best answer.");
                q.setOptionA("Option A");
                q.setOptionB("Option B");
                q.setOptionC("Option C");
                q.setOptionD("Option D");
                q.setCorrectOption(switch (i % 4) {
                    case 1 -> "A";
                    case 2 -> "B";
                    case 3 -> "C";
                    default -> "D";
                });
                q.setDifficultyLevel(i % 3 == 0 ? "Hard" : (i % 2 == 0 ? "Medium" : "Easy"));
            }
            q.setTopic(truncate(topic, 60));
            questions.add(q);
        }
        return questions;
    }

    private Question buildSampleQuestion(String text, String a, String b, String c, String d,
                                         String correct, String difficulty, String topic) {
        Question q = new Question();
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        q.setDifficultyLevel(difficulty);
        q.setTopic(truncate(topic, 60));
        return q;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.length() <= max) {
            return cleaned;
        }
        return cleaned.substring(0, max);
    }
}
