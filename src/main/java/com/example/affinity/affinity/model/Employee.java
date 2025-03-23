package com.example.affinity.affinity.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Table(name = "employee", schema = "affinity")
public class Employee {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(name = "full_name", columnDefinition = "text")
    private String fullName;
    @Column(name = "email", columnDefinition = "text", unique = true)
    private String email;
    @Column(name = "created_at", columnDefinition = "timestamp")
    private Timestamp createdAt;
    @Column(name = "updated_at", columnDefinition = "timestamp")
    private Timestamp updatedAt;
}
