package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.Question;
import com.placement.placement_intelligence.repository.QuestionRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import com.placement.placement_intelligence.repository.TestSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentTestAttemptRepository attemptRepository;

    @Mock
    private StudentAnswerRepository answerRepository;

    @Mock
    private TestSessionRepository testSessionRepository;

    @Mock
    private GroqQuestionGeneratorService groqQuestionGeneratorService;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestService(
                questionRepository,
                studentRepository,
                attemptRepository,
                answerRepository,
                testSessionRepository,
                groqQuestionGeneratorService
        );
    }

    // --- evaluateAnswers ---

    @Test
    void testEvaluateAnswers_threeCorrectOneWrong_scoreIsThree() {
        // Set up 4 questions: q1,q2,q3 answered correctly, q4 answered wrongly
        Question q1 = makeQuestion(1L, "A");
        Question q2 = makeQuestion(2L, "B");
        Question q3 = makeQuestion(3L, "C");
        Question q4 = makeQuestion(4L, "D");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));
        when(questionRepository.findById(3L)).thenReturn(Optional.of(q3));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(q4));

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A"); // correct
        answers.put(2L, "B"); // correct
        answers.put(3L, "C"); // correct
        answers.put(4L, "B"); // wrong (correct is D)

        int score = testService.evaluateAnswers(answers);

        assertEquals(3, score, "Score should be 3 for 3 correct and 1 wrong answer");
    }

    @Test
    void testEvaluateAnswers_allCorrect_scoreEqualsAnswerCount() {
        Question q1 = makeQuestion(1L, "A");
        Question q2 = makeQuestion(2L, "C");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A"); // correct
        answers.put(2L, "C"); // correct

        int score = testService.evaluateAnswers(answers);

        assertEquals(2, score, "Score should equal total questions when all are correct");
    }

    @Test
    void testEvaluateAnswers_allWrong_scoreIsZero() {
        Question q1 = makeQuestion(1L, "A");
        Question q2 = makeQuestion(2L, "B");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "C"); // wrong
        answers.put(2L, "D"); // wrong

        int score = testService.evaluateAnswers(answers);

        assertEquals(0, score, "Score should be 0 when all answers are wrong");
    }

    @Test
    void testEvaluateAnswers_caseInsensitiveMatch() {
        Question q1 = makeQuestion(1L, "a"); // stored lowercase

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A"); // submitted uppercase — should still match

        int score = testService.evaluateAnswers(answers);

        assertEquals(1, score, "Answer matching should be case-insensitive");
    }

    // --- getRandomQuestions ---

    @Test
    void testGetRandomQuestions_repoHasFiveRequestThree_returnsThree() {
        List<Question> fiveQuestions = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            fiveQuestions.add(makeQuestion(i, "A"));
        }
        when(questionRepository.findByTopicIgnoreCase("APTITUDE")).thenReturn(fiveQuestions);

        List<Question> result = testService.getRandomQuestions("APTITUDE", 3);

        assertEquals(3, result.size(), "Should return exactly 3 questions when 3 are requested from 5 available");
    }

    @Test
    void testGetRandomQuestions_emptyRepo_returnsEmptyList() {
        when(questionRepository.findByTopicIgnoreCase("UNKNOWN_TOPIC")).thenReturn(new ArrayList<>());

        List<Question> result = testService.getRandomQuestions("UNKNOWN_TOPIC", 5);

        assertTrue(result.isEmpty(), "Should return empty list when repository has no questions for that topic");
    }

    @Test
    void testGetRandomQuestions_requestMoreThanAvailable_returnsAll() {
        List<Question> twoQuestions = List.of(makeQuestion(1L, "A"), makeQuestion(2L, "B"));
        when(questionRepository.findByTopicIgnoreCase("DSA")).thenReturn(twoQuestions);

        List<Question> result = testService.getRandomQuestions("DSA", 10);

        assertEquals(2, result.size(), "Should return all available questions when fewer than requested");
    }

    @Test
    void testGetRandomQuestions_requestExactCount_returnsExact() {
        List<Question> threeQuestions = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            threeQuestions.add(makeQuestion(i, "B"));
        }
        when(questionRepository.findByTopicIgnoreCase("SOFT_SKILLS")).thenReturn(threeQuestions);

        List<Question> result = testService.getRandomQuestions("SOFT_SKILLS", 3);

        assertEquals(3, result.size(), "Should return exactly 3 when 3 requested and 3 available");
    }

    // --- helper ---

    private Question makeQuestion(Long id, String correctOption) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Sample question " + id);
        q.setOptionA("A");
        q.setOptionB("B");
        q.setOptionC("C");
        q.setOptionD("D");
        q.setCorrectOption(correctOption);
        q.setDifficultyLevel("EASY");
        q.setTopic("APTITUDE");
        return q;
    }
}
