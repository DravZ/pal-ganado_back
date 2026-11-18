package com.palavaca.back.service;

import com.palavaca.back.model.PasswordResetToken;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.PasswordResetTokenRepository;
import com.palavaca.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetNotifier notifier;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.password-reset.token-ttl-minutes:60}")
    private long tokenTtlMinutes;

    /**
     * Genera (si el correo existe) un enlace de recuperación. La respuesta es siempre la misma
     * para no revelar si una cuenta está registrada.
     */
    @Transactional
    public void requestReset(String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email == null ? "" : email.trim());
        if (maybeUser.isEmpty()) {
            return;
        }

        User user = maybeUser.get();
        LocalDateTime now = LocalDateTime.now();

        List<PasswordResetToken> activeTokens =
                passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(user.getId());
        activeTokens.forEach(token -> token.setUsedAt(now));
        if (!activeTokens.isEmpty()) {
            passwordResetTokenRepository.saveAll(activeTokens);
        }

        String token = generateToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .expiresAt(now.plusMinutes(tokenTtlMinutes))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        notifier.sendResetLink(user, resetUrl);
    }

    /**
     * Valida el token y actualiza la contraseña del usuario.
     */
    @Transactional
    public void confirmReset(String token, String newPassword) {
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede superar 72 bytes");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El enlace de recuperación no es válido."));

        if (resetToken.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El enlace de recuperación ya fue utilizado.");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El enlace de recuperación expiró. Solicita uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo generar el hash del token", exception);
        }
    }
}
