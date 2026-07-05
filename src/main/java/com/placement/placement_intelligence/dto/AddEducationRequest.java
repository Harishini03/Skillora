package com.placement.placement_intelligence.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Year;

public class AddEducationRequest {

    @NotBlank(message = "Institution name is required")
    @Size(max = 180)
    private String institutionName;

    @NotBlank(message = "Degree is required")
    @Size(max = 180)
    private String degree;

    @NotNull(message = "Year is required")
    @Min(value = 1950, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    @NotBlank(message = "CGPA / Percentage is required")
    @Size(max = 40)
    private String cgpaOrPercentage;

    public void validateYear() {
        int currentYear = Year.now().getValue();
        if (year != null && (year < 1950 || year > currentYear + 8)) {
            throw new IllegalArgumentException("Year must be between 1950 and " + (currentYear + 8));
        }
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCgpaOrPercentage() {
        return cgpaOrPercentage;
    }

    public void setCgpaOrPercentage(String cgpaOrPercentage) {
        this.cgpaOrPercentage = cgpaOrPercentage;
    }
}
