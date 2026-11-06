package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrganizationDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "Máximo 150 caracteres")
        private String name;

        private String legalName;
        private String taxId;
        private String phone;
        private String email;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;
        private String legalName;
        private String taxId;
        private String phone;
        private String email;
        private String status;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private String name;
        private String legalName;
        private String taxId;
        private String phone;
        private String email;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}