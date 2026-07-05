package com.placement.placement_intelligence.dto;

public class EligibilityEvaluateRequest {
    private Long studentId;
    private Long companyId;

    public EligibilityEvaluateRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
