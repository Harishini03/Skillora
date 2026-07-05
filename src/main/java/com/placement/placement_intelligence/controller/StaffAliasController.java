package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.DepartmentStatsResponse;
import com.placement.placement_intelligence.dto.StudentDetailsResponse;
import com.placement.placement_intelligence.dto.TopStudentsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffAliasController {

    private final StaffController staffController;

    public StaffAliasController(StaffController staffController) {
        this.staffController = staffController;
    }

    @GetMapping("/department-stats")
    public ResponseEntity<DepartmentStatsResponse> departmentStats() {
        return staffController.departmentStats();
    }

    @GetMapping("/top-students")
    public ResponseEntity<TopStudentsResponse> topStudents() {
        return staffController.topStudents();
    }

    @GetMapping("/student-details")
    public ResponseEntity<StudentDetailsResponse> studentDetails(@RequestParam Long studentId) {
        return staffController.studentDetails(studentId);
    }
}
