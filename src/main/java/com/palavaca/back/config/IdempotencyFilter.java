package com.palavaca.back.config;

import com.palavaca.back.model.IdempotencyRecord;
import com.palavaca.back.repository.IdempotencyRecordRepository;
import com.palavaca.back.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyRecordRepository idempotencyRepository;
    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()
                || !isWriteMethod(request.getMethod())
                || key.length() > 128) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = userRepository.findByEmail(authentication.getName())
                .map(com.palavaca.back.model.User::getId)
                .orElseThrow(() -> new ServletException("El usuario autenticado ya no existe."));
        BufferedRequest requestCopy = new BufferedRequest(request);
        String requestHash = hash(request.getMethod() + "\n" + request.getRequestURI() + "\n"
                + new String(requestCopy.body(), StandardCharsets.UTF_8));

        IdempotencyRecord previous = idempotencyRepository
                .findByUserIdAndIdempotencyKey(userId, key).orElse(null);
        if (previous != null) {
            replayOrReject(previous, requestHash, response);
            return;
        }

        ContentCachingResponseWrapper responseCopy = new ContentCachingResponseWrapper(response);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                IdempotencyRecord record = new IdempotencyRecord();
                record.setId(UUID.randomUUID());
                record.setUserId(userId);
                record.setIdempotencyKey(key);
                record.setRequestHash(requestHash);
                record.setCreatedAt(Instant.now());
                idempotencyRepository.saveAndFlush(record);

                try {
                    filterChain.doFilter(requestCopy, responseCopy);
                } catch (IOException | ServletException exception) {
                    throw new RequestDispatchException(exception);
                }

                int responseStatus = responseCopy.getStatus();
                if (responseStatus >= 200 && responseStatus < 300) {
                    record.setResponseStatus(responseStatus);
                    record.setResponseContentType(responseCopy.getContentType());
                    record.setResponseBody(new String(responseCopy.getContentAsByteArray(), StandardCharsets.UTF_8));
                    idempotencyRepository.save(record);
                } else {
                    idempotencyRepository.delete(record);
                }
            });
        } catch (DataIntegrityViolationException duplicate) {
            IdempotencyRecord saved = idempotencyRepository
                    .findByUserIdAndIdempotencyKey(userId, key).orElse(null);
            if (saved == null) throw duplicate;
            responseCopy.resetBuffer();
            replayOrReject(saved, requestHash, response);
            return;
        } catch (UnexpectedRollbackException rollback) {
            if (responseCopy.getStatus() < HttpServletResponse.SC_BAD_REQUEST) throw rollback;
            responseCopy.copyBodyToResponse();
            return;
        } catch (RequestDispatchException dispatchException) {
            Throwable cause = dispatchException.getCause();
            if (cause instanceof IOException ioException) throw ioException;
            if (cause instanceof ServletException servletException) throw servletException;
            throw dispatchException;
        }

        responseCopy.copyBodyToResponse();
    }

    private void replayOrReject(
            IdempotencyRecord record,
            String requestHash,
            HttpServletResponse response) throws IOException {
        if (!MessageDigest.isEqual(
                record.getRequestHash().getBytes(StandardCharsets.UTF_8),
                requestHash.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "La clave de idempotencia ya se usó con otra solicitud.");
            return;
        }
        if (record.getResponseStatus() == null) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "La solicitud con esta clave sigue en proceso.");
            return;
        }

        response.setStatus(record.getResponseStatus());
        if (record.getResponseContentType() != null) response.setContentType(record.getResponseContentType());
        if (record.getResponseBody() != null && !record.getResponseBody().isEmpty()) {
            byte[] body = record.getResponseBody().getBytes(StandardCharsets.UTF_8);
            response.setContentLength(body.length);
            response.getOutputStream().write(body);
        }
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    private static class BufferedRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private BufferedRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        private byte[] body() {
            return body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return input.read();
                }

                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static class RequestDispatchException extends RuntimeException {
        private RequestDispatchException(Throwable cause) {
            super(cause);
        }
    }
}
