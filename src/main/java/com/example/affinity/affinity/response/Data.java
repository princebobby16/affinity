package com.example.affinity.affinity.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private Long id;
    private String message;
}
