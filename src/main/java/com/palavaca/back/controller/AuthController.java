package com.palavaca.back.controller;

import com.palavaca.back.dto.AuthDTO;
import com.palavaca.back.dto.UserDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Organization;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.OrganizationRepository;
import com.palavaca.back.repository.UserRepository;
import com.palavaca.back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private static final String SESSION_EXPIRY_ATTRIBUTE = AuthController.class.getName() + ".expiresAt";
    private static final int SESSION_DURATION_SECONDS = (int) Duration.ofDays(7).toSeconds();

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FarmRepository farmRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/auth/csrf")
    public AuthDTO.CsrfTokenResponse csrfToken(CsrfToken csrfToken) {
        return new AuthDTO.CsrfTokenResponse(csrfToken.getToken());
    }

    @PostMapping("/auth/session")
    public AuthDTO.SessionResponse login(@Valid @RequestBody AuthDTO.LoginRequest request,
                                         HttpServletRequest servletRequest,
                                         HttpServletResponse servletResponse) {
        HttpSession oldSession = servletRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
        HttpSession session = servletRequest.getSession(true);
        Instant expiresAt = Instant.now().plusSeconds(SESSION_DURATION_SECONDS);
        session.setMaxInactiveInterval(SESSION_DURATION_SECONDS);
        session.setAttribute(SESSION_EXPIRY_ATTRIBUTE, expiresAt);
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));
        return sessionResponse(user, expiresAt);
    }

    @GetMapping("/auth/session")
    public ResponseEntity<AuthDTO.SessionResponse> currentSession(
            Authentication authentication,
            HttpServletRequest request) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.noContent().build();
        }
        User user = requireAuthenticatedUser(authentication);
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "La sesión expiró");
        }
        Instant expiresAt = sessionExpiry(session);
        return ResponseEntity.ok(sessionResponse(user, expiresAt));
    }

    @PostMapping("/auth/session/renew")
    public AuthDTO.SessionResponse renewSession(Authentication authentication,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        User user = requireAuthenticatedUser(authentication);
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "La sesión expiró");
        }

        Instant expiresAt = Instant.now().plusSeconds(SESSION_DURATION_SECONDS);
        session.setMaxInactiveInterval(SESSION_DURATION_SECONDS);
        session.setAttribute(SESSION_EXPIRY_ATTRIBUTE, expiresAt);
        refreshSessionCookie(request, response, session.getId());
        return sessionResponse(user, expiresAt);
    }

    @DeleteMapping("/auth/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        response.addHeader("Set-Cookie", sessionCookie(request, "", 0));
    }

    @PostMapping("/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    public AuthDTO.SessionResponse register(@Valid @RequestBody AuthDTO.RegistrationRequest request) {
        if (request.userPass().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(BAD_REQUEST, "La contraseña no puede superar 72 bytes");
        }
        if (userRepository.existsByEmail(request.userEmail())) {
            throw new ResponseStatusException(BAD_REQUEST, "El correo ya está registrado");
        }

        Organization organization = organizationRepository.save(Organization.builder()
                .name(request.orgName())
                .legalName(request.orgLegalName())
                .taxId(request.orgTaxId())
                .phone(request.orgPhone())
                .email(request.orgEmail())
                .status("active")
                .build());

        Farm farm = null;
        if (request.farm() != null) {
            AuthDTO.FarmRegistration farmData = request.farm();
            farm = farmRepository.save(Farm.builder()
                    .organization(organization)
                    .name(farmData.name())
                    .code(farmData.code())
                    .state(farmData.state())
                    .municipality(farmData.municipality())
                    .locality(farmData.locality())
                    .address(farmData.address())
                    .totalAreaHectares(farmData.hectares())
                    .status("active")
                    .build());
        }

        User user = userRepository.save(User.builder()
                .organization(organization)
                .farm(farm)
                .name(request.userName())
                .email(request.userEmail())
                .phone(request.userPhone())
                .passwordHash(passwordEncoder.encode(request.userPass()))
                .role("Administrador")
                .status("active")
                .build());
        return new AuthDTO.SessionResponse(
                userService.findById(user.getId()),
                null,
                farm == null ? List.of() : List.of(farm.getId()));
    }

    @PostMapping("/auth/password-resets")
    public AuthDTO.PasswordResetResponse requestPasswordReset(
            @Valid @RequestBody AuthDTO.PasswordResetRequest request) {
        return new AuthDTO.PasswordResetResponse(
                "Si existe una cuenta para ese correo, recibirás instrucciones para recuperar el acceso.");
    }

    @GetMapping("/me")
    public UserDTO.Response currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "La sesión no está autenticada");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));
        return userService.findById(user.getId());
    }

    private User requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "La sesión no está autenticada");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));
    }

    private Instant sessionExpiry(HttpSession session) {
        Object expiresAt = session.getAttribute(SESSION_EXPIRY_ATTRIBUTE);
        if (expiresAt instanceof Instant instant) return instant;
        Instant fallback = Instant.ofEpochMilli(session.getLastAccessedTime())
                .plusSeconds(session.getMaxInactiveInterval());
        session.setAttribute(SESSION_EXPIRY_ATTRIBUTE, fallback);
        return fallback;
    }

    private void refreshSessionCookie(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String sessionId) {
        response.addHeader("Set-Cookie", sessionCookie(request, sessionId, SESSION_DURATION_SECONDS));
    }

    private String sessionCookie(HttpServletRequest request, String value, int maxAge) {
        String cookieName = request.getServletContext().getSessionCookieConfig().getName();
        if (cookieName == null || cookieName.isBlank()) cookieName = "JSESSIONID";
        return cookieName + "=" + value + "; Path=/; Max-Age=" + maxAge
                + "; HttpOnly; Secure; SameSite=None";
    }

    private AuthDTO.SessionResponse sessionResponse(User user, Instant expiresAt) {
        List<UUID> farmIds;
        if (user.getRole() != null && user.getRole().toLowerCase(Locale.ROOT).contains("admin")) {
            farmIds = farmRepository.findByOrganizationId(user.getOrganization().getId()).stream()
                    .filter(farm -> "active".equalsIgnoreCase(farm.getStatus()))
                    .map(Farm::getId)
                    .toList();
        } else if (user.getFarm() != null
                && user.getFarm().getOrganization().getId().equals(user.getOrganization().getId())
                && "active".equalsIgnoreCase(user.getFarm().getStatus())) {
            farmIds = List.of(user.getFarm().getId());
        } else {
            farmIds = List.of();
        }
        return new AuthDTO.SessionResponse(userService.findById(user.getId()), expiresAt, farmIds);
    }
}
