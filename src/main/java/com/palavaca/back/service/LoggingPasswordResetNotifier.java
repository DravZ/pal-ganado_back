package com.palavaca.back.service;

import com.palavaca.back.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementación por defecto de {@link PasswordResetNotifier} que escribe el enlace de
 * recuperación en el log del servidor. No envía correos porque el proyecto aún no tiene un
 * proveedor SMTP configurado.
 */
@Slf4j
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    @Override
    public void sendResetLink(User user, String resetUrl) {
        log.info("Enlace de recuperación de contraseña para {}: {}", user.getEmail(), resetUrl);
    }
}
