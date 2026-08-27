package com.theraflow.dto;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {
}
