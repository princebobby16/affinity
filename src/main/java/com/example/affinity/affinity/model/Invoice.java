package com.example.affinity.affinity.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Table(name = "invoice", schema = "affinity")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "no_of_hours", nullable = false)
    private Float noOfHours;

    @Column(name = "unit_price", nullable = false)
    private Float unitPrice;

    @Column(name = "cost", nullable = false)
    private Float cost;

    @Column(name = "work_day", nullable = false)
    private Date workDay;

    @CreationTimestamp // Automatically sets timestamp on creation
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp // Updates timestamp automatically on update
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
