package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        private String name;

        private String taxId;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;
        private String taxId;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private String name;
        private String taxId;
        private String phone;
        private String email;
        private String address;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}