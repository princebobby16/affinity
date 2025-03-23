package com.example.affinity.affinity.request;

import com.example.affinity.affinity.model.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.sql.Date;

/**
 * DTO for {@link Invoice}
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceDto implements Serializable {
    String companyName;
    Long employeeId;
    Float rate;
    Date workDay;
    String startTime;
    String endTime;
}