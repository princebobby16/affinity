package com.example.affinity.affinity.service;

import com.example.affinity.affinity.model.Employee;
import com.example.affinity.affinity.repository.EmployeeRepository;
import com.example.affinity.affinity.request.EmployeeDto;
import com.example.affinity.affinity.response.StandardListResponse;
import com.example.affinity.affinity.response.StandardResponse;
import com.example.affinity.affinity.response.StandardTypeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        employeeDto = new EmployeeDto();
        employeeDto.setId("1");
        employeeDto.setFullName("John Doe");
        employeeDto.setEmail("john.doe@example.com");
    }

    @Test
    void testSave_NewEmployee() {
        when(employeeRepository.findEmployeeByIdOrFullName(anyLong(), anyString())).thenReturn(null);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        StandardResponse response = employeeService.save(employeeDto);

        assertNotNull(response);
        assertEquals("employee saved successfully", response.getData().getMessage());
        assertEquals("SUCCESS", response.getMeta().getStatus());
        verify(employeeRepository, times(1)).save(any(Employee.class));

    }

    @Test
    void testSave_ExistingEmployee() {
        when(employeeRepository.findEmployeeByIdOrFullName(anyLong(), anyString())).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        StandardResponse response = employeeService.save(employeeDto);

        assertNotNull(response);
        assertEquals("employee saved successfully", response.getData().getMessage());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testFindOneById_ExistingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        StandardTypeResponse<Employee> response = employeeService.findOneById(1L);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getMeta().getStatus());
        assertEquals("John Doe", response.getData().getFullName());
    }

    @Test
    void testFindOneById_NonExistingEmployee() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        StandardTypeResponse<Employee> response = employeeService.findOneById(2L);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getMeta().getStatus());
        assertNull(response.getData().getId()); // Ensuring it returns an empty Employee object
    }

    @Test
    void testFindAll_EmployeesExist() {
        List<Employee> employees = List.of(employee);
        when(employeeRepository.findAll()).thenReturn(employees);

        StandardListResponse<Employee> response = employeeService.findAll();

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("John Doe", response.getData().get(0).getFullName());
        assertEquals("SUCCESS", response.getMeta().getStatus());
    }

    @Test
    void testFindAll_NoEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        StandardListResponse<Employee> response = employeeService.findAll();

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
        assertEquals("SUCCESS", response.getMeta().getStatus());
    }
}