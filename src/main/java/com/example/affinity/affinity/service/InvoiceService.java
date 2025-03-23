package com.example.affinity.affinity.service;

import com.example.affinity.affinity.model.Employee;
import com.example.affinity.affinity.model.Invoice;
import com.example.affinity.affinity.repository.EmployeeRepository;
import com.example.affinity.affinity.repository.InvoiceRepository;
import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.request.InvoiceDto;
import com.example.affinity.affinity.response.*;
import com.example.affinity.affinity.utils.Helpers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;
    private final EmployeeRepository employeeRepository;

    public Invoice save(InvoiceDto dto) {
        // make sure employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElse(null);
        if (employee == null) {
            System.out.println("Employee not found");
            return null;
        }

        // make sure company exists or create one
        CompanyDto companyDto = CompanyDto.builder()
                .email("")
                .name(dto.getCompanyName())
                .build();
        Data data = companyService.save(companyDto).getData();
        if (data == null) {
            System.out.println("Company not found");
            return null;
        }
        System.out.println(data.getMessage());

        // calculate the hours
        Float workHours = Helpers.calculateHours(dto.getStartTime(), dto.getEndTime());

        // calculate the cost
        Float cost = Helpers.calculateCost(workHours, dto.getRate());

        // ensure the workDay is either this month or the previous month not future months
        boolean isDateValid = Helpers.isWithinCurrentOrPreviousMonth(dto.getWorkDay());
        if (!isDateValid) {
            System.out.println("Date not valid");
            return null;
        }


        // todo: ensure there aren't any duplicate invoices
        boolean exists = invoiceRepository.existsByCompanyAndEmployeeAndMonth(
                companyDto.getName(),
                employee.getId(),
                workHours
        );

        if (exists) {
            System.out.println("Duplicate invoice detected for this employee and company in the current or previous month.");
            return null;
        }

        Invoice invoice = Invoice.builder()
                .companyName(companyDto.getName())
                .employeeId(employee.getId())
                .noOfHours(workHours)
                .unitPrice(dto.getRate())
                .cost(cost)
                .workDay(dto.getWorkDay())
                .build();
        return invoiceRepository.save(invoice);
    }

    public StandardListResponse<Invoice> findCompanyInvoice(String companyName) {
        Optional<List<Invoice>> records = invoiceRepository.findByCompanyName(companyName);

        return getInvoiceStandardListResponse(records);
    }

    public List<Invoice> findCompanyInvoiceList(String companyName) {
        Optional<List<Invoice>> records = invoiceRepository.findByCompanyName(companyName);

        return records.orElseGet(Collections::emptyList).stream()
                .filter(invoice -> {
                    boolean isDateValid = Helpers.isWithinCurrentOrPreviousMonth(invoice.getWorkDay());
                    if (!isDateValid) {
                        System.out.println("Date not valid for invoice ID: " + invoice.getId());
                    }
                    return isDateValid;
                })
                .collect(Collectors.toList());
    }

    public StandardListResponse<Invoice> findEmployeeWorkData(Long employeeId) {
        Optional<List<Invoice>> records = invoiceRepository.findByEmployeeId(employeeId);

        return getInvoiceStandardListResponse(records);
    }

    private StandardListResponse<Invoice> getInvoiceStandardListResponse(Optional<List<Invoice>> records) {
        List<Invoice> validInvoices = records.orElseGet(Collections::emptyList).stream()
                .filter(invoice -> {
                    boolean isDateValid = Helpers.isWithinCurrentOrPreviousMonth(invoice.getWorkDay());
                    if (!isDateValid) {
                        System.out.println("Date not valid for invoice ID: " + invoice.getId());
                    }
                    return isDateValid;
                })
                .collect(Collectors.toList()); // Ensures a mutable list

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();

        return StandardListResponse.<Invoice>builder()
                .data(validInvoices) // Returns an empty list if no data exists
                .meta(meta)
                .build();
    }

    public String storeInvoice(String data) {
        String[] parts = data.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid input format");
        }

        try {
            Long employeeId = Long.parseLong(parts[0].trim());
            Float rate = Float.parseFloat(parts[1].trim());
            String companyName = parts[2].trim();

            // Parse the date as java.util.Date, then convert to java.sql.Date
            java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(parts[3].trim());
            Date workDay = new Date(parsedDate.getTime()); // Convert to java.sql.Date

            String startTime = parts[4].trim();
            String endTime = parts[5].trim();

            InvoiceDto dto = new InvoiceDto(companyName, employeeId, rate, workDay, startTime, endTime);

            System.out.println("================================== " + dto);

            Invoice inv = save(dto);

            System.out.println(inv);

            if (inv == null) {
                return null;
            }

            return inv.getCompanyName();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing invoice data", e);
        }
    }

}
