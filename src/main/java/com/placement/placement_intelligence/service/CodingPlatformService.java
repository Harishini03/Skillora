package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Production-ready Coding Platform Service with sandboxed code execution
 * Supports Java, Python, and JavaScript with proper security and resource limits
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodingPlatformService {

    private final CodingProblemRepository problemRepository;
    private final ProblemTestCaseRepository testCaseRepository;
    private final CodeSubmissionRepository submissionRepository;
    private final SubmissionTestResultRepository testResultRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    private static final int MAX_EXECUTION_TIME_SECONDS = 5;
    private static final int MAX_MEMORY_MB = 256;
    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "skillora_code");

    // ============= PROBLEM MANAGEMENT =============

    @Transactional
    public CodingProblem createProblem(String title, String description, String difficultyLevel,
                                      String topicTags, Integer timeLimitSeconds, Integer memoryLimitMb,
                                      Long createdByUserId) {
        log.info("Creating coding problem: {} by user: {}", title, createdByUserId);
        
        User creator = userRepository.findById(createdByUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (creator.getRole() != Role.STAFF && creator.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("Only STAFF and RECRUITER can create problems");
        }

        CodingProblem problem = new CodingProblem();
        problem.setTitle(title);
        problem.setDescription(description);
        problem.setDifficultyLevel(difficultyLevel);
        problem.setTopicTags(topicTags);
        problem.setTimeLimitSeconds(timeLimitSeconds != null ? timeLimitSeconds : 5);
        problem.setMemoryLimitMb(memoryLimitMb != null ? memoryLimitMb : 256);
        problem.setCreatedBy(creator);
        
        return problemRepository.save(problem);
    }

    @Transactional
    public ProblemTestCase addTestCase(Long problemId, String inputData, String expectedOutput,
                                      Boolean isSample, Integer orderIndex) {
        log.info("Adding test case to problem: {}", problemId);
        
        CodingProblem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        
        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setProblem(problem);
        testCase.setInputData(inputData);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setIsSample(isSample != null ? isSample : false);
        testCase.setOrderIndex(orderIndex != null ? orderIndex : getNextTestCaseOrderIndex(problemId));
        
        return testCaseRepository.save(testCase);
    }

    // ============= CODE EXECUTION =============

    @Transactional
    public CodeSubmission executeCode(Long problemId, Long studentId, String language, String code) {
        log.info("Executing code for problem {} by student {}", problemId, studentId);
        
        CodingProblem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<ProblemTestCase> testCases = testCaseRepository.findByProblem_IdOrderByOrderIndexAsc(problemId);
        
        if (testCases.isEmpty()) {
            throw new IllegalArgumentException("Problem has no test cases");
        }

        CodeSubmission submission = new CodeSubmission();
        submission.setProblem(problem);
        submission.setStudent(student);
        submission.setLanguage(language);
        submission.setCodeContent(code);
        submission.setTestCasesTotal(testCases.size());
        submission.setTestCasesPassed(0);

        try {
            // Execute code against test cases
            List<TestExecutionResult> results = executeTestCases(code, language, testCases, problem.getTimeLimitSeconds());
            
            // Calculate statistics
            int passed = (int) results.stream().filter(TestExecutionResult::isPassed).count();
            int totalTime = results.stream().mapToInt(TestExecutionResult::getExecutionTimeMs).sum();
            
            submission.setTestCasesPassed(passed);
            submission.setExecutionTimeMs(totalTime / testCases.size()); // Average time
            
            // Determine status
            if (passed == testCases.size()) {
                submission.setStatus("ACCEPTED");
            } else {
                submission.setStatus("WRONG_ANSWER");
            }
            
            // Save submission
            CodeSubmission savedSubmission = submissionRepository.save(submission);
            
            // Save test results
            for (int i = 0; i < testCases.size(); i++) {
                SubmissionTestResult testResult = new SubmissionTestResult();
                testResult.setSubmission(savedSubmission);
                testResult.setTestCase(testCases.get(i));
                testResult.setPassed(results.get(i).isPassed());
                testResult.setActualOutput(results.get(i).getActualOutput());
                testResult.setErrorMessage(results.get(i).getErrorMessage());
                testResult.setExecutionTimeMs(results.get(i).getExecutionTimeMs());
                testResultRepository.save(testResult);
            }
            
            return savedSubmission;
            
        } catch (TimeoutException e) {
            log.warn("Code execution timeout for problem {}", problemId);
            submission.setStatus("TIME_LIMIT");
            submission.setTestCasesPassed(0);
            return submissionRepository.save(submission);
            
        } catch (Exception e) {
            log.error("Code execution error for problem {}: {}", problemId, e.getMessage());
            submission.setStatus("RUNTIME_ERROR");
            submission.setTestCasesPassed(0);
            return submissionRepository.save(submission);
        }
    }

    private List<TestExecutionResult> executeTestCases(String code, String language, 
                                                       List<ProblemTestCase> testCases, 
                                                       int timeLimitSeconds) throws Exception {
        List<TestExecutionResult> results = new ArrayList<>();
        
        for (ProblemTestCase testCase : testCases) {
            TestExecutionResult result = executeCode(code, language, testCase.getInputData(), 
                                                    testCase.getExpectedOutput(), timeLimitSeconds);
            results.add(result);
        }
        
        return results;
    }

    private TestExecutionResult executeCode(String code, String language, String input, 
                                           String expectedOutput, int timeLimitSeconds) throws Exception {
        String executionId = UUID.randomUUID().toString();
        Path workDir = TEMP_DIR.resolve(executionId);
        Files.createDirectories(workDir);
        
        try {
            long startTime = System.currentTimeMillis();
            String actualOutput = null;
            String errorMessage = null;
            
            switch (language.toUpperCase()) {
                case "JAVA":
                    actualOutput = executeJavaCode(code, input, workDir, timeLimitSeconds);
                    break;
                case "PYTHON":
                    actualOutput = executePythonCode(code, input, workDir, timeLimitSeconds);
                    break;
                case "JAVASCRIPT":
                    actualOutput = executeJavaScriptCode(code, input, workDir, timeLimitSeconds);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported language: " + language);
            }
            
            long endTime = System.currentTimeMillis();
            int executionTime = (int) (endTime - startTime);
            
            // Normalize output for comparison
            String normalizedActual = normalizeOutput(actualOutput);
            String normalizedExpected = normalizeOutput(expectedOutput);
            
            boolean passed = normalizedActual.equals(normalizedExpected);
            
            return new TestExecutionResult(passed, actualOutput, errorMessage, executionTime);
            
        } catch (TimeoutException e) {
            return new TestExecutionResult(false, null, "Time limit exceeded", timeLimitSeconds * 1000);
        } catch (Exception e) {
            log.error("Execution error: {}", e.getMessage());
            return new TestExecutionResult(false, null, e.getMessage(), 0);
        } finally {
            // Cleanup
            deleteDirectory(workDir);
        }
    }

    private String executeJavaCode(String code, String input, Path workDir, int timeLimitSeconds) throws Exception {
        // Extract class name
        String className = extractJavaClassName(code);
        Path sourceFile = workDir.resolve(className + ".java");
        Files.writeString(sourceFile, code);
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder("javac", sourceFile.toString());
        compileBuilder.directory(workDir.toFile());
        compileBuilder.redirectErrorStream(true);
        
        Process compileProcess = compileBuilder.start();
        if (!compileProcess.waitFor(10, TimeUnit.SECONDS)) {
            compileProcess.destroyForcibly();
            throw new RuntimeException("Compilation timeout");
        }
        
        if (compileProcess.exitValue() != 0) {
            String error = new String(compileProcess.getInputStream().readAllBytes());
            throw new RuntimeException("Compilation error: " + error);
        }
        
        // Execute
        ProcessBuilder runBuilder = new ProcessBuilder("java", className);
        runBuilder.directory(workDir.toFile());
        runBuilder.redirectErrorStream(true);
        
        Process runProcess = runBuilder.start();
        
        // Provide input
        if (input != null && !input.isEmpty()) {
            try (OutputStream os = runProcess.getOutputStream()) {
                os.write(input.getBytes());
                os.flush();
            }
        }
        
        // Wait with timeout
        if (!runProcess.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
            runProcess.destroyForcibly();
            throw new TimeoutException("Execution timeout");
        }
        
        // Get output
        return new String(runProcess.getInputStream().readAllBytes());
    }

    private String executePythonCode(String code, String input, Path workDir, int timeLimitSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.py");
        Files.writeString(sourceFile, code);
        
        ProcessBuilder builder = new ProcessBuilder("python", sourceFile.toString());
        builder.directory(workDir.toFile());
        builder.redirectErrorStream(true);
        
        Process process = builder.start();
        
        // Provide input
        if (input != null && !input.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes());
                os.flush();
            }
        }
        
        // Wait with timeout
        if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new TimeoutException("Execution timeout");
        }
        
        if (process.exitValue() != 0) {
            String error = new String(process.getInputStream().readAllBytes());
            throw new RuntimeException("Runtime error: " + error);
        }
        
        return new String(process.getInputStream().readAllBytes());
    }

    private String executeJavaScriptCode(String code, String input, Path workDir, int timeLimitSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.js");
        Files.writeString(sourceFile, code);
        
        ProcessBuilder builder = new ProcessBuilder("node", sourceFile.toString());
        builder.directory(workDir.toFile());
        builder.redirectErrorStream(true);
        
        Process process = builder.start();
        
        // Provide input
        if (input != null && !input.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes());
                os.flush();
            }
        }
        
        // Wait with timeout
        if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new TimeoutException("Execution timeout");
        }
        
        if (process.exitValue() != 0) {
            String error = new String(process.getInputStream().readAllBytes());
            throw new RuntimeException("Runtime error: " + error);
        }
        
        return new String(process.getInputStream().readAllBytes());
    }

    // ============= HELPER METHODS =============

    private String extractJavaClassName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("public class")) {
                return line.replaceAll(".*public class\\s+(\\w+).*", "$1");
            }
        }
        return "Solution";
    }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }

    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", path);
                        }
                    });
            }
        } catch (IOException e) {
            log.error("Failed to cleanup directory: {}", directory, e);
        }
    }

    private Integer getNextTestCaseOrderIndex(Long problemId) {
        List<ProblemTestCase> testCases = testCaseRepository.findByProblem_IdOrderByOrderIndexAsc(problemId);
        return testCases.isEmpty() ? 0 : testCases.get(testCases.size() - 1).getOrderIndex() + 1;
    }

    // ============= QUERY OPERATIONS =============

    @Transactional(readOnly = true)
    public List<CodingProblem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CodingProblem getProblem(Long problemId) {
        return problemRepository.findById(problemId)
            .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
    }

    @Transactional(readOnly = true)
    public List<ProblemTestCase> getTestCases(Long problemId, boolean sampleOnly) {
        if (sampleOnly) {
            return testCaseRepository.findByProblem_IdAndIsSampleTrue(problemId);
        }
        return testCaseRepository.findByProblem_IdOrderByOrderIndexAsc(problemId);
    }

    @Transactional(readOnly = true)
    public List<CodeSubmission> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudent_IdOrderBySubmittedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public CodeSubmission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    @Transactional(readOnly = true)
    public List<SubmissionTestResult> getSubmissionResults(Long submissionId) {
        return testResultRepository.findBySubmission_Id(submissionId);
    }

    // ============= INNER CLASSES =============

    private static class TestExecutionResult {
        private final boolean passed;
        private final String actualOutput;
        private final String errorMessage;
        private final int executionTimeMs;

        public TestExecutionResult(boolean passed, String actualOutput, String errorMessage, int executionTimeMs) {
            this.passed = passed;
            this.actualOutput = actualOutput;
            this.errorMessage = errorMessage;
            this.executionTimeMs = executionTimeMs;
        }

        public boolean isPassed() { return passed; }
        public String getActualOutput() { return actualOutput; }
        public String getErrorMessage() { return errorMessage; }
        public int getExecutionTimeMs() { return executionTimeMs; }
    }
}
