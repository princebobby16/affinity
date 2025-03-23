package com.example.affinity.affinity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.affinity.affinity.model.Company;
import com.example.affinity.affinity.repository.CompanyRepository;
import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.response.*;
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

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private CompanyDto companyDto;
    private Company company;

    @BeforeEach
    void setUp() {
        companyDto = CompanyDto.builder()
                .name("Test Company").build();
        company = Company.builder()
                .id(1L)
                .name("Test Company")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    void saveCompanySuccessfully() {
        when(companyRepository.findCompanyByName(companyDto.getName())).thenReturn(null);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        StandardResponse response = companyService.save(companyDto);

        assertNotNull(response);
        assertEquals("company saved successfully", response.getData().getMessage());
        assertEquals(1L, response.getData().getId());
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    void saveCompanyFails() {
        when(companyRepository.findCompanyByName(companyDto.getName())).thenReturn(null);
        when(companyRepository.save(any(Company.class))).thenThrow(new RuntimeException("DB error"));

        StandardResponse response = companyService.save(companyDto);

        assertNotNull(response);
        assertEquals("failed to save company", response.getData().getMessage());
        assertEquals("ERROR", response.getMeta().getStatus());
    }

    @Test
    void findOneByIdReturnsCompany() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        StandardTypeResponse<Company> response = companyService.findOneById(1L);

        assertNotNull(response);
        assertEquals("Test Company", response.getData().getName());
    }

    @Test
    void findOneByIdReturnsEmptyCompanyWhenNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        StandardTypeResponse<Company> response = companyService.findOneById(1L);

        assertNotNull(response);
        assertNull(response.getData().getId());
    }

    @Test
    void findAllReturnsListOfCompanies() {
        when(companyRepository.findAll()).thenReturn(List.of(company));

        StandardListResponse<Company> response = companyService.findAll();

        assertNotNull(response);
        assertFalse(response.getData().isEmpty());
        assertEquals(1, response.getData().size());
        assertEquals("Test Company", response.getData().get(0).getName());
    }

    @Test
    void findAllReturnsEmptyListWhenNoCompaniesExist() {
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        StandardListResponse<Company> response = companyService.findAll();

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
    }
}