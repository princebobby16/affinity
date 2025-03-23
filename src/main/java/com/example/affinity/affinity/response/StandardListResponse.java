package com.example.affinity.affinity.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardListResponse<T> {
    private List<T> data;
    private Meta meta;
}
