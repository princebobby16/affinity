package com.example.affinity.affinity.response;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    private String traceId;
    private Timestamp timestamp;
    private String status;
}
