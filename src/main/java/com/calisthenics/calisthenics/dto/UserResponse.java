package com.calisthenics.calisthenics.dto;

import java.time.LocalDateTime;

import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getLastName(), user.getUsername(),
                user.getEmail(), user.getPhone(), user.getRole(), user.getActive(), user.getCreatedAt());
    }
}
