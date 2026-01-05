package com.smartaccess.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirstLoginChangePasswordDto {
    private String email;
    private String oldPassword;
    private String newPassword;
}
