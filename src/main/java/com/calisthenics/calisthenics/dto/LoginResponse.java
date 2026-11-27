package com.calisthenics.calisthenics.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String role;
    private Long userId;
}
