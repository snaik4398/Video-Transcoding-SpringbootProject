package com.transcodeservice.common.dto;

import com.transcodeservice.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;
}
