package com.example.affinity.affinity.repository;

import com.example.affinity.affinity.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findCompanyByName(String name);
}