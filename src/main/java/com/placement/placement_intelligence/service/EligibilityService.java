package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.EligibilityCriteria;
import com.placement.placement_intelligence.model.PlacementStatus;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentSkill;
import com.placement.placement_intelligence.model.Company;
import com.placement.placement_intelligence.repository.EligibilityCriteriaRepository;
import com.placement.placement_intelligence.repository.CompanyRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EligibilityService {

    private final EligibilityCriteriaRepository criteriaRepository;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;

    public EligibilityService(EligibilityCriteriaRepository criteriaRepository,
                              CompanyRepository companyRepository,
                              StudentRepository studentRepository,
                              StudentSkillRepository studentSkillRepository) {
        this.criteriaRepository = criteriaRepository;
        this.companyRepository = companyRepository;
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
    }

    @Transactional
    public Student evaluateStudentForCompany(Long studentId, Long companyId) {
        EligibilityCriteria criteria = criteriaRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Eligibility criteria not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        double cgpaScaled = scaleCgpa(student.getCgpa());
        double skillScore = computeAverageSkillScore(studentId);

        double finalScore = (criteria.getWeightCgpa() * cgpaScaled)
                + (criteria.getWeightDsa() * safeScore(student.getDsaScore()))
                + (criteria.getWeightAptitude() * safeScore(student.getAptitudeScore()))
                + (criteria.getWeightMock() * safeScore(student.getMockTestScore()))
                + (criteria.getWeightSkill() * skillScore);

        student.setFinalScore(finalScore);

        boolean meetsMinimums = student.getCgpa() >= criteria.getMinCgpa()
                && safeScore(student.getDsaScore()) >= criteria.getMinDsa()
                && safeScore(student.getAptitudeScore()) >= criteria.getMinAptitude();

        student.setPlacementStatus(meetsMinimums ? PlacementStatus.ELIGIBLE : PlacementStatus.NOT_ELIGIBLE);

        return studentRepository.save(student);
    }

    @Transactional
    public void rankStudentsByDepartment(Long departmentId) {
        List<Student> students = studentRepository.findByDepartmentIdOrderByFinalScoreDesc(departmentId);
        int rank = 1;
        for (Student student : students) {
            student.setRank(rank++);
        }
        studentRepository.saveAll(students);
    }

    @Transactional
    public EligibilityCriteria saveCriteria(EligibilityCriteria criteria) {
        Company company = companyRepository.findById(criteria.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        criteria.setCompany(company);
        return criteriaRepository.save(criteria);
    }

    private double computeAverageSkillScore(Long studentId) {
        List<StudentSkill> skills = studentSkillRepository.findByStudent_Id(studentId);
        if (skills.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (StudentSkill skill : skills) {
            total += safeScore(skill.getSkillScore());
        }
        return total / skills.size();
    }

    private double safeScore(Double value) {
        return value == null ? 0.0 : value;
    }

    private double scaleCgpa(Double cgpa) {
        if (cgpa == null) {
            return 0.0;
        }
        return cgpa * 10.0;
    }
}
