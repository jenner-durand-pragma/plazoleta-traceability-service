package com.pragma.plazoleta.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.plazoleta.infrastructure.configuration.SecurityConfiguration;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAccessDeniedHandler;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAuthenticationEntryPoint;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAuthenticationFilter;
import com.pragma.plazoleta.infrastructure.configuration.security.annotation.IsOwnerOrClient;
import com.pragma.plazoleta.infrastructure.configuration.security.token.ITokenValidationPort;
import com.pragma.plazoleta.infrastructure.configuration.security.token.dto.AuthenticatedUser;
import com.pragma.plazoleta.infrastructure.configuration.security.token.exception.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityTest.DummyController.class)
@Import({
        SecurityConfiguration.class,
        CustomAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        SecurityTest.DummyController.class
})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITokenValidationPort tokenValidationPort;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 401 Unauthorized when no Authorization header is present")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/dummy/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication is required to access this resource")
                );
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is invalid or expired")
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        var badToken = "invalid.bad.token";
        var exception = mock(IllegalArgumentException.class);
        when(tokenValidationPort.validate(badToken)).thenThrow(new InvalidTokenException("Expirado", exception));

        mockMvc.perform(get("/dummy/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + badToken))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication is required to access this resource")
                );
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is authenticated but lacks required role")
    void shouldReturn403WhenRoleIsWrong() throws Exception {
        var validToken = "valid.owner.token";
        var employeeUser = new AuthenticatedUser(5L, "employee@test.com", "EMPLOYEE");
        when(tokenValidationPort.validate(validToken)).thenReturn(employeeUser);

        mockMvc.perform(get("/dummy/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.message")
                                .value("You do not have permission to access this resource")
                );
    }

    @ParameterizedTest(name = "Should return 200 OK when token is valid and role is {0}")
    @ValueSource(strings = {"OWNER", "CLIENT"})
    void shouldReturn200WhenTokenAndRoleAreValid(String role) throws Exception {
        var validToken = "valid." + role.toLowerCase() + ".token";
        var allowedUser = new AuthenticatedUser(7L, role.toLowerCase() + "@test.com", role);
        when(tokenValidationPort.validate(validToken)).thenReturn(allowedUser);

        mockMvc.perform(get("/dummy/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data").value("Access Granted")
                );
    }

    @RestController
    public static class DummyController {

        @IsOwnerOrClient
        @GetMapping("/dummy/protected")
        public DummyResponse getProtectedData() {
            return new DummyResponse("Access Granted");
        }
    }

    public static class DummyResponse {
        public String data;
        public DummyResponse(String data) { this.data = data; }
    }
}