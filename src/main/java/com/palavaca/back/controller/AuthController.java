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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

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
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));
        return new AuthDTO.SessionResponse(userService.findById(user.getId()));
    }

    @DeleteMapping("/auth/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    @PostMapping("/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    public AuthDTO.SessionResponse register(@Valid @RequestBody AuthDTO.RegistrationRequest request) {
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
        return new AuthDTO.SessionResponse(userService.findById(user.getId()));
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
}
