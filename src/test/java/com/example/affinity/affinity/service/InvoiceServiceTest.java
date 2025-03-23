package com.example.affinity.affinity.service;

import com.example.affinity.affinity.model.Employee;
import com.example.affinity.affinity.model.Invoice;
import com.example.affinity.affinity.repository.EmployeeRepository;
import com.example.affinity.affinity.repository.InvoiceRepository;
import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.request.InvoiceDto;
import com.example.affinity.affinity.response.Data;
import com.example.affinity.affinity.response.Meta;
import com.example.affinity.affinity.response.StandardListResponse;
import com.example.affinity.affinity.response.StandardResponse;
import com.example.affinity.affinity.utils.Helpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private InvoiceService invoiceService;

    private InvoiceDto invoiceDto;
    private Employee employee;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);

        invoiceDto = new InvoiceDto("Company XYZ", 1L, 100.0f, Date.valueOf("2024-07-01"), "09:00", "17:00");
        invoice = Invoice.builder()
                .id(1L)
                .companyName("Company XYZ")
                .employeeId(1L)
                .noOfHours(8.0f)
                .unitPrice(100.0f)
                .cost(800.0f)
                .workDay(Date.valueOf("2025-03-01"))
                .build();
    }

    @Test
    void saveInvoiceSuccessfully() {
        // Ensure workDay is within current or previous month
        LocalDate validDate = LocalDate.now().withDayOfMonth(10); // 10th of this month
        invoiceDto.setWorkDay(Date.valueOf(validDate));

        when(employeeRepository.findById(invoiceDto.getEmployeeId())).thenReturn(Optional.of(employee));

        // Fix: Wrap Data in StandardResponse
        when(companyService.save(any(CompanyDto.class)))
                .thenReturn(StandardResponse.builder()
                        .data(new Data(1L, "company saved successfully"))
                        .meta(Meta.builder().status("SUCCESS").build())
                        .build());

        when(invoiceRepository.existsByCompanyAndEmployeeAndMonth(anyString(), anyLong(), anyFloat())).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        Invoice savedInvoice = invoiceService.save(invoiceDto);

        assertNotNull(savedInvoice);
        assertEquals("Company XYZ", savedInvoice.getCompanyName());
        assertEquals(800.0f, savedInvoice.getCost());

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }


    @Test
    void saveInvoiceFailsWhenEmployeeNotFound() {
        when(employeeRepository.findById(invoiceDto.getEmployeeId())).thenReturn(Optional.empty());

        Invoice result = invoiceService.save(invoiceDto);

        assertNull(result);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void saveInvoiceFailsWhenCompanyNotCreated() {
        when(employeeRepository.findById(invoiceDto.getEmployeeId())).thenReturn(Optional.of(employee));
        when(companyService.save(any(CompanyDto.class)))
                .thenReturn(StandardResponse.builder()
                        .data(Data.builder()
                                .message("failed to save company")
                                .id(1L)
                                .build())
                        .meta(Meta.builder()
                                .status("ERROR")
                                .timestamp(new Timestamp(System.currentTimeMillis()))
                                .traceId("")
                                .build()
                        ).build()); // Return a failed response

        Invoice result = invoiceService.save(invoiceDto);

        assertNull(result);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void findCompanyInvoiceReturnsInvoices() {
        // Ensure invoice has a valid workDay
        invoice.setWorkDay(Date.valueOf(LocalDate.now().withDayOfMonth(10)));

        when(invoiceRepository.findByCompanyName("Company XYZ")).thenReturn(Optional.of(Collections.singletonList(invoice)));

        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class)) {
            mockedHelpers.when(() -> Helpers.isWithinCurrentOrPreviousMonth(any(Date.class))).thenReturn(true);

            StandardListResponse<Invoice> response = invoiceService.findCompanyInvoice("Company XYZ");

            assertNotNull(response);
            assertFalse(response.getData().isEmpty());
            assertEquals(1, response.getData().size());
            assertEquals("Company XYZ", response.getData().get(0).getCompanyName());
        }
    }


    @Test
    void findCompanyInvoiceReturnsEmptyWhenNoInvoices() {
        when(invoiceRepository.findByCompanyName("Company XYZ")).thenReturn(Optional.empty());

        StandardListResponse<Invoice> response = invoiceService.findCompanyInvoice("Company XYZ");

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
    }
}
