package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDepartmentIdOrderByFinalScoreDesc(Long departmentId);

    Student findByUser_Id(Long userId);

    @Query("select count(s) from Student s")
    long totalStudents();

    @Query("select count(s) from Student s where s.placementStatus = 'ELIGIBLE'")
    long eligibleStudents();

    @Query("select avg(s.cgpa) from Student s")
    Double averageCgpa();

    @Query("select s.department.id, avg(coalesce(s.finalScore, 0)) from Student s group by s.department.id")
    List<Object[]> averageFinalScoreByDepartment();

    @Query("select s.department.name, "
            + "sum(case when s.placementStatus = 'ELIGIBLE' then 1 else 0 end) as eligibleCount, "
            + "count(s) as totalCount "
            + "from Student s group by s.department.name")
    List<Object[]> departmentEligibilityStats();

    @Query("select s.placementStatus, avg(coalesce(s.finalScore, 0)) from Student s group by s.placementStatus")
    List<Object[]> averageFinalScoreByPlacementStatus();

    List<Student> findTop10ByOrderByFinalScoreDesc();
}
