package com.example.affinity.affinity.service;

import com.example.affinity.affinity.model.Employee;
import com.example.affinity.affinity.request.EmployeeDto;
import com.example.affinity.affinity.repository.EmployeeRepository;
import com.example.affinity.affinity.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public StandardResponse save(EmployeeDto dto) {

        long employeeId = 0L;
        if (dto.getId() != null) {
            employeeId = Long.parseLong(dto.getId());
        }
        Employee employee = employeeRepository.findEmployeeByIdOrFullName(employeeId, dto.getFullName());

        if (employee == null) {
            employee = Employee.builder()
                    .fullName(dto.getFullName())
                    .email(dto.getEmail())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
        } else {
            employee.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            employee.setFullName(dto.getFullName());
            employee.setEmail(dto.getEmail());
        }

        employeeRepository.save(employee);

        Data data = Data.builder()
                .message("employee saved successfully")
                .id(employee.getId())
                .build();

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();
        return StandardResponse.builder()
                .data(data)
                .meta(meta)
                .build();
    }

    public StandardTypeResponse<Employee> findOneById(Long id) {
        Optional<Employee> record = employeeRepository.findById(id);

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();

        return StandardTypeResponse.<Employee>builder()
                .data(record.orElseGet(Employee::new))
                .meta(meta)
                .build();
    }

    public StandardListResponse<Employee> findAll() {
        List<Employee> employees = employeeRepository.findAll();

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();

        return StandardListResponse.<Employee>builder()
                .data(employees)
                .meta(meta)
                .build();
    }

}
