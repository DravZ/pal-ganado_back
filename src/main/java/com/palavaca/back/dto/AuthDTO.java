package com.palavaca.back.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class AuthDTO {

    private AuthDTO() {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record FarmRegistration(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 50) String code,
            @Size(max = 100) String state,
            @Size(max = 100) String municipality,
            @Size(max = 100) String locality,
            String address,
            BigDecimal hectares) {}

    public record RegistrationRequest(
            @NotBlank @Size(max = 150) String userName,
            @Size(max = 30) String userPhone,
            @NotBlank @Email @Size(max = 150) String userEmail,
            @NotBlank @Size(min = 8, max = 100) String userPass,
            @NotBlank @Size(max = 150) String orgName,
            @Size(max = 200) String orgLegalName,
            @Size(max = 30) String orgTaxId,
            @Size(max = 30) String orgPhone,
            @Email @Size(max = 150) String orgEmail,
            @Valid FarmRegistration farm) {}

    public record SessionResponse(UserDTO.Response user) {}

    public record CsrfTokenResponse(String token) {}

    public record PasswordResetRequest(@NotBlank @Email String email) {}

    public record PasswordResetResponse(String message) {}
}
