package com.example.affinity.affinity.service;

import com.example.affinity.affinity.model.Company;
import com.example.affinity.affinity.repository.CompanyRepository;
import com.example.affinity.affinity.request.CompanyDto;
import com.example.affinity.affinity.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public StandardResponse save(CompanyDto dto) {

        Company company = companyRepository.findCompanyByName(dto.getName());

        if (company == null) {
            company = Company.builder()
                    .name(dto.getName())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
        } else {
            company.setName(dto.getName());
        }
        Company c = new Company();
        try {
            c = companyRepository.save(company);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Data data = Data.builder()
                    .message("failed to save company")
                    .id(c.getId())
                    .build();

            Meta meta = Meta.builder()
                    .status("ERROR")
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .traceId("")
                    .build();

            return StandardResponse.builder()
                    .data(data)
                    .meta(meta)
                    .build();
        }

        Data data = Data.builder()
                .message("company saved successfully")
                .id(c.getId())
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

    public StandardTypeResponse<Company> findOneById(Long id) {
        Optional<Company> record = companyRepository.findById(id);

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();

        return StandardTypeResponse.<Company>builder()
                .data(record.orElseGet(Company::new))
                .meta(meta)
                .build();
    }

    public StandardListResponse<Company> findAll() {
        List<Company> companies = companyRepository.findAll();

        Meta meta = Meta.builder()
                .status("SUCCESS")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .traceId("")
                .build();

        return StandardListResponse.<Company>builder()
                .data(companies)
                .meta(meta)
                .build();
    }

}
