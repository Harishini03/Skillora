package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.CodingDto;
import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.service.CodingPlatformService;
import com.placement.placement_intelligence.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coding")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
public class CodingPlatformController {

    private final CodingPlatformService codingService;
    private final CurrentUserService currentUserService;

    // ============= STAFF ENDPOINTS =============

    @PostMapping("/problems")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CodingDto.ProblemResponse> createProblem(@Valid @RequestBody CodingDto.CreateProblemRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        CodingProblem problem = codingService.createProblem(
            request.getTitle(),
            request.getDescription(),
            request.getDifficultyLevel(),
            request.getTopicTags(),
            request.getTimeLimitSeconds(),
            request.getMemoryLimitMb(),
            userId
        );
        return ResponseEntity.ok(mapToProblemResponse(problem));
    }

    @PostMapping("/problems/{problemId}/testcases")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CodingDto.TestCaseResponse> addTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody CodingDto.CreateTestCaseRequest request) {
        ProblemTestCase testCase = codingService.addTestCase(
            problemId,
            request.getInputData(),
            request.getExpectedOutput(),
            request.getIsSample(),
            request.getOrderIndex()
        );
        return ResponseEntity.ok(mapToTestCaseResponse(testCase));
    }

    // ============= STUDENT ENDPOINTS =============

    @GetMapping("/problems")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<CodingDto.ProblemResponse>> getAllProblems() {
        List<CodingProblem> problems = codingService.getAllProblems();
        return ResponseEntity.ok(
            problems.stream()
                .map(this::mapToProblemResponse)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/problems/{problemId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<CodingDto.ProblemResponse> getProblem(@PathVariable Long problemId) {
        CodingProblem problem = codingService.getProblem(problemId);
        return ResponseEntity.ok(mapToProblemResponse(problem));
    }

    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<List<CodingDto.TestCaseResponse>> getSampleTestCases(@PathVariable Long problemId) {
        List<ProblemTestCase> testCases = codingService.getTestCases(problemId, true);
        return ResponseEntity.ok(
            testCases.stream()
                .map(this::mapToTestCaseResponse)
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/execute")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<CodingDto.SubmissionResponse> executeCode(@Valid @RequestBody CodingDto.ExecuteCodeRequest request) {
        Long studentId = currentUserService.getCurrentStudentId();
        CodeSubmission submission = codingService.executeCode(
            request.getProblemId(),
            studentId,
            request.getLanguage(),
            request.getCode()
        );
        return ResponseEntity.ok(mapToSubmissionResponse(submission));
    }

    @GetMapping("/submissions")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<CodingDto.SubmissionResponse>> getMySubmissions() {
        Long studentId = currentUserService.getCurrentStudentId();
        List<CodeSubmission> submissions = codingService.getStudentSubmissions(studentId);
        return ResponseEntity.ok(
            submissions.stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<CodingDto.SubmissionResponse> getSubmission(@PathVariable Long submissionId) {
        CodeSubmission submission = codingService.getSubmission(submissionId);
        return ResponseEntity.ok(mapToSubmissionResponse(submission));
    }

    @GetMapping("/submissions/{submissionId}/results")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<CodingDto.TestResultResponse>> getSubmissionResults(@PathVariable Long submissionId) {
        List<SubmissionTestResult> results = codingService.getSubmissionResults(submissionId);
        return ResponseEntity.ok(
            results.stream()
                .map(this::mapToTestResultResponse)
                .collect(Collectors.toList())
        );
    }

    // ============= MAPPERS =============

    private CodingDto.ProblemResponse mapToProblemResponse(CodingProblem problem) {
        CodingDto.ProblemResponse response = new CodingDto.ProblemResponse();
        response.setId(problem.getId());
        response.setTitle(problem.getTitle());
        response.setDescription(problem.getDescription());
        response.setDifficultyLevel(problem.getDifficultyLevel());
        response.setTopicTags(problem.getTopicTags());
        response.setTimeLimitSeconds(problem.getTimeLimitSeconds());
        response.setMemoryLimitMb(problem.getMemoryLimitMb());
        try {
            response.setCreatedByName(problem.getCreatedBy() != null ? problem.getCreatedBy().getName() : "");
        } catch (Exception e) {
            response.setCreatedByName("");
        }
        response.setCreatedAt(problem.getCreatedAt());
        return response;
    }

    private CodingDto.TestCaseResponse mapToTestCaseResponse(ProblemTestCase testCase) {
        CodingDto.TestCaseResponse response = new CodingDto.TestCaseResponse();
        response.setId(testCase.getId());
        response.setInputData(testCase.getInputData());
        response.setExpectedOutput(testCase.getExpectedOutput());
        response.setIsSample(testCase.getIsSample());
        response.setOrderIndex(testCase.getOrderIndex());
        return response;
    }

    private CodingDto.SubmissionResponse mapToSubmissionResponse(CodeSubmission submission) {
        CodingDto.SubmissionResponse response = new CodingDto.SubmissionResponse();
        response.setId(submission.getId());
        response.setProblemId(submission.getProblem().getId());
        response.setProblemTitle(submission.getProblem().getTitle());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getName());
        response.setLanguage(submission.getLanguage());
        response.setCodeContent(submission.getCodeContent());
        response.setStatus(submission.getStatus());
        response.setExecutionTimeMs(submission.getExecutionTimeMs());
        response.setMemoryUsedMb(submission.getMemoryUsedMb());
        response.setTestCasesPassed(submission.getTestCasesPassed());
        response.setTestCasesTotal(submission.getTestCasesTotal());
        response.setSubmittedAt(submission.getSubmittedAt());
        return response;
    }

    private CodingDto.TestResultResponse mapToTestResultResponse(SubmissionTestResult result) {
        CodingDto.TestResultResponse response = new CodingDto.TestResultResponse();
        response.setId(result.getId());
        response.setTestCaseId(result.getTestCase().getId());
        response.setPassed(result.getPassed());
        response.setActualOutput(result.getActualOutput());
        response.setErrorMessage(result.getErrorMessage());
        response.setExecutionTimeMs(result.getExecutionTimeMs());
        response.setInputData(result.getTestCase().getInputData());
        response.setExpectedOutput(result.getTestCase().getExpectedOutput());
        response.setIsSample(result.getTestCase().getIsSample());
        return response;
    }
}
