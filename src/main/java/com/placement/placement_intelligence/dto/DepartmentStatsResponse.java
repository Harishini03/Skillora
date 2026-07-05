package com.placement.placement_intelligence.dto;

import java.util.List;

public class DepartmentStatsResponse {
    private long totalStudents;
    private long activeUsers;
    private long loggedInToday;
    private List<DepartmentPerformance> departmentPerformance;

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getLoggedInToday() {
        return loggedInToday;
    }

    public void setLoggedInToday(long loggedInToday) {
        this.loggedInToday = loggedInToday;
    }

    public List<DepartmentPerformance> getDepartmentPerformance() {
        return departmentPerformance;
    }

    public void setDepartmentPerformance(List<DepartmentPerformance> departmentPerformance) {
        this.departmentPerformance = departmentPerformance;
    }

    public static class DepartmentPerformance {
        private String department;
        private Double averagePerformance;

        public DepartmentPerformance() {
        }

        public DepartmentPerformance(String department, Double averagePerformance) {
            this.department = department;
            this.averagePerformance = averagePerformance;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public Double getAveragePerformance() {
            return averagePerformance;
        }

        public void setAveragePerformance(Double averagePerformance) {
            this.averagePerformance = averagePerformance;
        }
    }
}
