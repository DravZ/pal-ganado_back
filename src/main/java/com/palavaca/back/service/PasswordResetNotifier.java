package com.palavaca.back.service;

import com.palavaca.back.model.User;

/**
 * Entrega el enlace de recuperación de contraseña al usuario.
 * <p>
 * La implementación por defecto ({@link LoggingPasswordResetNotifier}) registra el enlace en el
 * log del servidor, ya que el proyecto no tiene configurado un proveedor de correo. Para enviar
 * correos reales basta con registrar otro bean de este tipo (por ejemplo con JavaMailSender)
 * y anotarlo como {@code @Primary}.
 */
public interface PasswordResetNotifier {
    void sendResetLink(User user, String resetUrl);
}
