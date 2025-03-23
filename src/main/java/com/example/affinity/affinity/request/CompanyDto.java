package com.example.affinity.affinity.request;

import com.example.affinity.affinity.model.Company;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link Company}
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto implements Serializable {
    String name;
}