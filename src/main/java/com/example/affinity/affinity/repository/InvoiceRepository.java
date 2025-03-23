package com.example.affinity.affinity.repository;

import com.example.affinity.affinity.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<List<Invoice>> findByCompanyName(String companyName);

    Optional<List<Invoice>> findByEmployeeId(Long employeeId);

    @Query(value = """
            SELECT COUNT(i) > 0 FROM affinity.invoice i\s
                    WHERE i.company_name = :companyName\s
                      AND i.employee_id = :employeeId\s
                      AND i.no_of_hours = :noOfHours
                      AND i.work_day >= date_trunc('month', CURRENT_DATE) - INTERVAL '1 month';
            """, nativeQuery = true)
    boolean existsByCompanyAndEmployeeAndMonth(@Param("companyName") String companyName,
                                               @Param("employeeId") Long employeeId,
                                               @Param("noOfHours") Float noOfHours);
}