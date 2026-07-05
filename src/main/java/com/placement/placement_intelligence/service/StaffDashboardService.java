package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.StaffDashboardResponse;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.repository.SkillRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StaffDashboardService {

    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final SkillRepository skillRepository;

    public StaffDashboardService(StudentRepository studentRepository,
                                 StudentSkillRepository studentSkillRepository,
                                 SkillRepository skillRepository) {
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public StaffDashboardResponse buildDashboard() {
        StaffDashboardResponse response = new StaffDashboardResponse();

        long total = studentRepository.totalStudents();
        long eligible = studentRepository.eligibleStudents();
        response.setTotalStudents(total);
        response.setEligibleStudents(eligible);
        response.setPlacementRate(total == 0 ? 0.0 : ((double) eligible / total) * 100.0);
        Double avgCgpa = studentRepository.averageCgpa();
        response.setAverageCgpa(avgCgpa == null ? 0.0 : avgCgpa);

        response.setDepartmentSummaries(buildDepartmentSummaries());
        response.setSkillGaps(buildSkillGaps());
        response.setTopStudents(buildTopStudents());

        return response;
    }

    private List<StaffDashboardResponse.DepartmentSummary> buildDepartmentSummaries() {
        List<StaffDashboardResponse.DepartmentSummary> summaries = new ArrayList<>();
        List<Object[]> rows = studentRepository.departmentEligibilityStats();
        List<Object[]> avgScoreRows = studentRepository.averageFinalScoreByDepartment();
        for (Object[] row : rows) {
            String department = row[0].toString();
            double eligible = ((Number) row[1]).doubleValue();
            double total = ((Number) row[2]).doubleValue();
            double eligibilityPercent = total == 0 ? 0.0 : (eligible / total) * 100.0;
            double placementPercent = eligibilityPercent;
            double avgFinalScore = avgScoreRows.stream()
                    .filter(entry -> entry[0].toString().equals(department))
                    .map(entry -> entry[1] == null ? 0.0 : ((Number) entry[1]).doubleValue())
                    .findFirst()
                    .orElse(0.0);
            summaries.add(new StaffDashboardResponse.DepartmentSummary(department, eligibilityPercent, placementPercent, avgFinalScore));
        }
        return summaries;
    }

    private List<StaffDashboardResponse.SkillGapSummary> buildSkillGaps() {
        List<StaffDashboardResponse.SkillGapSummary> summaries = new ArrayList<>();
        var allSkills = skillRepository.findAll();
        var studentSkills = studentSkillRepository.findAll();
        allSkills.forEach(skill -> {
            long count = studentSkills.stream()
                    .filter(studentSkill -> studentSkill.getSkill().getId().equals(skill.getId()))
                    .count();
            summaries.add(new StaffDashboardResponse.SkillGapSummary(skill.getName(), count));
        });

        summaries.sort(Comparator.comparingLong(StaffDashboardResponse.SkillGapSummary::getStudentCount));
        return summaries;
    }

    private List<StaffDashboardResponse.StudentSummary> buildTopStudents() {
        List<StaffDashboardResponse.StudentSummary> summaries = new ArrayList<>();
        for (Student student : studentRepository.findTop10ByOrderByFinalScoreDesc()) {
            summaries.add(new StaffDashboardResponse.StudentSummary(
                    student.getId(),
                    student.getName(),
                    student.getDepartment().getName(),
                    student.getFinalScore(),
                    student.getRank()
            ));
        }
        return summaries;
    }
}
