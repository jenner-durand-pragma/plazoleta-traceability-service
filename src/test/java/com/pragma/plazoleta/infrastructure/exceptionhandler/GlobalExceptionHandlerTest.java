package com.pragma.plazoleta.infrastructure.exceptionhandler;

import com.pragma.plazoleta.domain.exception.BusinessRuleException;
import com.pragma.plazoleta.domain.exception.ConflictException;
import com.pragma.plazoleta.domain.exception.NotFoundException;
import com.pragma.plazoleta.domain.exception.UnauthorizedException;
import com.pragma.plazoleta.infrastructure.exceptionhandler.common.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(servletRequest.getRequestURI()).thenReturn("/api/v1/test");

        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    @DisplayName("Should return 409 Conflict with field details")
    void shouldReturnConflictError() {
        var ex = mock(ConflictException.class);
        when(ex.getMessage()).thenReturn("Entity already exists");
        when(ex.getField()).thenReturn(Optional.of("email"));

        var response = exceptionHandler.handleConflict(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.CONFLICT,
            "Entity already exists"
        );
        assertThat(body.getFieldErrors()).hasSize(1);
        assertThat(body.getFieldErrors().get(0).getField()).isEqualTo("email");
    }

    @Test
    @DisplayName("Should return 401 Unauthorized")
    void shouldReturnUnauthorizedError() {
        var ex = mock(UnauthorizedException.class);
        when(ex.getMessage()).thenReturn("Invalid credentials");

        var response = exceptionHandler.handleUnauthorized(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials"
        );
        assertThat(body.getFieldErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should return 403 Forbidden")
    void shouldReturnForbiddenError() {
        var ex = new AccessDeniedException("Access Denied");

        var response = exceptionHandler.handleAccessDenied(ex, webRequest);

        assertBasicErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    @Test
    @DisplayName("Should return 404 Not Found")
    void shouldReturnNotFoundError() {
        var ex = mock(NotFoundException.class);
        when(ex.getMessage()).thenReturn("User not found");

        var response = exceptionHandler.handleNotFound(ex, webRequest);

        assertBasicErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "User not found"
        );
    }

    @Test
    @DisplayName("Should return 422 Unprocessable Entity for Business Rule violation")
    void shouldReturnBusinessRuleError() {
        var ex = mock(BusinessRuleException.class);
        when(ex.getMessage()).thenReturn("Dish must be active");

        var response = exceptionHandler.handleBusinessRule(ex, webRequest);

        assertBasicErrorResponse(
                response,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Dish must be active"
        );
    }

    @Test
    @DisplayName("Should return 400 Bad Request for RequestBody validation (MethodArgumentNotValidException)")
    void shouldReturnBodyValidationError() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("dto", "name", "Name cannot be empty");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        var response = exceptionHandler.handleBodyValidation(ex, webRequest);

        var body = assertBasicErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );
        assertThat(body.getFieldErrors()).hasSize(1);
        assertThat(body.getFieldErrors().get(0).getField()).isEqualTo("name");
        assertThat(body.getFieldErrors().get(0).getMessage()).isEqualTo("Name cannot be empty");
    }

    @Test
    @DisplayName("Should return 400 Bad Request for Constraint Violation (RequestParam/PathVariable)")
    void shouldReturnConstraintViolationError() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        var path = mock(Path.class);
        when(path.toString()).thenReturn("id");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        Set<ConstraintViolation<?>> violations = Set.of(violation);
        var ex = new ConstraintViolationException(violations);

        var response = exceptionHandler.handleConstraintViolation(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.BAD_REQUEST,
            "Validation failed"
        );
        assertThat(body.getFieldErrors()).hasSize(1);
        assertThat(body.getFieldErrors().get(0).getField()).isEqualTo("id");
        assertThat(body.getFieldErrors().get(0).getMessage()).isEqualTo("must be greater than 0");
    }

    @Test
    @DisplayName("Should return 400 Bad Request for Malformed JSON")
    void shouldReturnMalformedRequestError() {
        var ex = mock(HttpMessageNotReadableException.class);

        var response = exceptionHandler.handleNotReadable(ex, webRequest);

        assertBasicErrorResponse(
            response,
            HttpStatus.BAD_REQUEST,
            "Malformed or unreadable request body"
        );
    }

    @Test
    @DisplayName("Should return 400 Bad Request for Missing Parameter")
    void shouldReturnMissingParamError() {
        var ex = new MissingServletRequestParameterException("categoryId", "Long");

        var response = exceptionHandler.handleMissingParam(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.BAD_REQUEST,
            "Required parameter 'categoryId' is missing"
        );
        assertThat(body.getFieldErrors()).hasSize(1);
        assertThat(body.getFieldErrors().get(0).getField()).isEqualTo("categoryId");
    }

    @Test
    @DisplayName("Should return 400 Bad Request for Type Mismatch")
    void shouldReturnTypeMismatchError() {
        var ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("restaurantId");

        doReturn(Long.class).when(ex).getRequiredType();

        var response = exceptionHandler.handleTypeMismatch(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.BAD_REQUEST,
            "Parameter 'restaurantId' should be of type Long"
        );
        assertThat(body.getFieldErrors()).hasSize(1);
        assertThat(body.getFieldErrors().get(0).getField()).isEqualTo("restaurantId");
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed")
    void shouldReturnMethodNotSupportedError() {
        var ex = new HttpRequestMethodNotSupportedException("POST");

        var response = exceptionHandler.handleMethodNotSupported(ex, webRequest);

        assertBasicErrorResponse(
            response,
            HttpStatus.METHOD_NOT_ALLOWED,
            "Request method 'POST' not supported"
        );
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for unhandled exceptions")
    void shouldReturnInternalServerError() {
        var ex = new Exception("Database is down");

        var response = exceptionHandler.handleGeneric(ex, webRequest);

        var body = assertBasicErrorResponse(
            response,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        assertThat(body.getMessage()).doesNotContain("Database is down");
    }

    // Helper Method
    private ErrorResponse assertBasicErrorResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);

        var body = response.getBody();
        assertThat(body).isNotNull();

        var safeBody = java.util.Objects.requireNonNull(body);

        assertThat(safeBody.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(safeBody.getError()).isEqualTo(expectedStatus.getReasonPhrase());
        assertThat(safeBody.getMessage()).isEqualTo(expectedMessage);
        assertThat(safeBody.getPath()).isEqualTo("/api/v1/test");
        assertThat(safeBody.getTimestamp()).isNotNull();

        return safeBody;
    }
}
