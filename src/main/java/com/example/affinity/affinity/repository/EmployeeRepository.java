package com.example.affinity.affinity.repository;

import com.example.affinity.affinity.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findEmployeeByIdOrFullName(Long id, String fullName);
}