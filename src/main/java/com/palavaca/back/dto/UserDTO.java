package com.palavaca.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        @Size(max = 150)
        private String name;
        private String firstName;
        private String lastName;

        @NotBlank(message = "El email es obligatorio")
        @Email
        @Size(max = 150)
        private String email;

        @Size(max = 30)
        private String phone;
        private String role;
        private UUID farmId;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;

        @Email
        @Size(max = 150)
        private String email;

        @Size(max = 30)
        private String phone;

        private String status;
        private String role;
        private UUID farmId;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private String organizationName;
        private String name;
        private String email;
        private String phone;
        private String status;
        private String role;
        private UUID farmId;
        private String farmName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}