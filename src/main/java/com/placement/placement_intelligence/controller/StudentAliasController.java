package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.ProgressResponse;
import com.placement.placement_intelligence.dto.CodeExecutionRequest;
import com.placement.placement_intelligence.dto.CodeExecutionResponse;
import com.placement.placement_intelligence.dto.StudentDashboardResponse;
import com.placement.placement_intelligence.dto.TestQuestionResponse;
import com.placement.placement_intelligence.dto.TestSubmissionRequest;
import com.placement.placement_intelligence.dto.TestSubmissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentAliasController {

    private final StudentController studentController;

    public StudentAliasController(StudentController studentController) {
        this.studentController = studentController;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> dashboard() {
        return studentController.dashboard();
    }

    @GetMapping("/tests")
    public ResponseEntity<TestQuestionResponse> tests(@RequestParam(defaultValue = "aptitude") String section,
                                                      @RequestParam(required = false) String topic) {
        return studentController.tests(section, topic);
    }

    @PostMapping("/submit-test")
    public ResponseEntity<TestSubmissionResponse> submitTest(@RequestBody TestSubmissionRequest request) {
        return studentController.submitTest(request);
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> progress() {
        return studentController.progress();
    }

    @PostMapping("/code/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(@RequestBody CodeExecutionRequest request) {
        return studentController.executeCode(request);
    }
}
