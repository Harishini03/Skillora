package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
