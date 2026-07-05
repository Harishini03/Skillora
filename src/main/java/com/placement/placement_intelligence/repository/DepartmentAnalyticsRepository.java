package com.placement.placement_intelligence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface DepartmentAnalyticsRepository extends Repository<com.placement.placement_intelligence.model.Department, Long> {

    @Query("select d.name, avg(coalesce(s.aptitudeScore, 0)), avg(coalesce(s.dsaScore, 0)), "
            + "avg(coalesce(s.mockTestScore, 0)) "
            + "from Student s join s.department d group by d.name")
    List<Object[]> averageScoresByDepartment();
}
