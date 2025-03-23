package com.example.affinity.affinity.response;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardTypeResponse<T> {
    private T data;
    private Meta meta;
}
