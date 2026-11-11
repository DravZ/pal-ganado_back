package com.palavaca.back;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackApplicationTests {

    private static final String FRONTEND_ORIGIN =
            "https://drav-z-pal-ganado-front-phi.vercel.app";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contextLoads() {
    }

    @Test
    void preflightAllowsFrontendCsrfAndIdempotencyHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/session")
                        .header("Origin", FRONTEND_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers",
                                "content-type,x-xsrf-token,idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        containsString("x-xsrf-token")));
    }

    @Test
    void csrfEndpointIsPublicAndIssuesCrossSiteSecureCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf").header("Origin", FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN))
                .andExpect(content().string(containsString("\"token\"")))
                .andDo(result -> {
                    var csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
                    assertNotNull(csrfCookie);
                    assertTrue(csrfCookie.getSecure());
                    assertEquals("None", csrfCookie.getAttribute("SameSite"));
                });
    }

    @Test
    void sessionEndpointReturnsNoContentWhenUnauthenticatedAndIncludesCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session").header("Origin", FRONTEND_ORIGIN))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN));
    }

    @Test
    void loginWithCsrfTokenReachesAuthenticationAndReturnsUnauthorizedForUnknownUser() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/v1/auth/csrf").header("Origin", FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode csrfBody = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/session")
                        .header("Origin", FRONTEND_ORIGIN)
                        .header("X-XSRF-TOKEN", csrfBody.get("token").asText())
                        .cookie(csrfCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing-user@example.invalid\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN));
    }
}
