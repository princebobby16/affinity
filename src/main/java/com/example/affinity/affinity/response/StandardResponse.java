package com.example.affinity.affinity.response;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse {
    private Data data;
    private Meta meta;
}
