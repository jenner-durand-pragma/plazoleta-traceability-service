package com.pragma.plazoleta.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.dto.request.user.UserInformationRequestDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderTraceabilityResponseDto;
import com.pragma.plazoleta.application.handler.IOrderTraceabilityHandler;
import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.exception.order.OrderTraceabilityNotFoundException;
import com.pragma.plazoleta.infrastructure.configuration.SecurityConfiguration;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAccessDeniedHandler;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAuthenticationEntryPoint;
import com.pragma.plazoleta.infrastructure.configuration.security.CustomAuthenticationFilter;
import com.pragma.plazoleta.infrastructure.configuration.security.token.ITokenValidationPort;
import com.pragma.plazoleta.infrastructure.configuration.security.token.dto.AuthenticatedUser;
import com.pragma.plazoleta.infrastructure.exceptionhandler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderTraceabilityRestController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfiguration.class,
        CustomAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class OrderTraceabilityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITokenValidationPort tokenValidationPort;

    @MockBean
    private IOrderTraceabilityHandler orderTraceabilityHandler;

    private ObjectMapper objectMapper;

    private UsernamePasswordAuthenticationToken employeeAuthentication;
    private UsernamePasswordAuthenticationToken clientAuthentication;

    private OrderStateRequestDto validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        var employeePrincipal = new AuthenticatedUser(1L, "employee@plazoleta.com", "EMPLOYEE");
        employeeAuthentication = new UsernamePasswordAuthenticationToken(
                employeePrincipal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );

        var clientPrincipal = new AuthenticatedUser(10L, "jenner.durand@plazoleta.com", "CLIENT");
        clientAuthentication = new UsernamePasswordAuthenticationToken(
                clientPrincipal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );

        validRequest = OrderStateRequestDto.builder()
                .orderId(42L)
                .restaurantId(10L)
                .previousStatus(OrderStatus.IN_PREPARATION)
                .newStatus(OrderStatus.READY)
                .changedAt(LocalDateTime.now())
                .client(UserInformationRequestDto.builder()
                        .id(10L)
                        .name("Jenner")
                        .lastName("Durand")
                        .email("jenner.durand@plazoleta.com")
                        .build()
                )
                .employee(UserInformationRequestDto.builder()
                        .id(1L)
                        .name("Employee")
                        .lastName("Plazoleta")
                        .email("employee@plazoleta.com")
                        .build()
                )
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created when payload is valid in save state")
    void shouldReturn201CreatedWhenPayloadIsValidInSaveState() throws Exception {
        mockMvc.perform(post("/api/v1/traceability/orders/states")
                        .with(authentication(employeeAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(orderTraceabilityHandler).saveState(any(OrderStateRequestDto.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when orderId is missing in save state")
    void shouldReturn400BadRequestWhenOrderIdIsMissingInSaveState() throws Exception {
        validRequest.setOrderId(null);

        mockMvc.perform(post("/api/v1/traceability/orders/states")
                        .with(authentication(employeeAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(orderTraceabilityHandler, never()).saveState(any());
    }

    @Test
    @DisplayName(
            "Should return 200 OK with traceability when " +
            "user requests order history in find by order id"
    )
    void shouldReturn200OkWithTraceabilityInFindByOrderId() throws Exception {
        var response = OrderTraceabilityResponseDto.builder()
                .orderId(42L)
                .transitions(List.of())
                .build();

        when(orderTraceabilityHandler.findByOrderId(42L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/traceability/orders/42")
                        .with(authentication(clientAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(42));
    }

    @Test
    @DisplayName(
            "Should return 404 Not Found when " +
            "traceability records do not exist in find by order id"
    )
    void shouldReturn404NotFoundWhenTraceabilityRecordsAreAbsentForTheCallerInFindByOrderId() throws Exception {
        doThrow(new OrderTraceabilityNotFoundException(42L))
                .when(orderTraceabilityHandler).findByOrderId(42L);

        mockMvc.perform(get("/api/v1/traceability/orders/42")
                        .with(authentication(clientAuthentication)))
                .andExpect(status().isNotFound());
    }
}