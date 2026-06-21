package com.pragma.plazoleta.application.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInformationRequestDto {
    @NotNull(message = "User id is required")
    @Positive(message = "User id must be positive")
    @Schema(description = "User id", example = "1")
    private Long id;

    @NotBlank(message = "User first name must have value")
    @Schema(description = "User first name", example = "Jenner")
    private String name;

    @NotBlank(message = "User last name must have value")
    @Schema(description = "User last name", example = "Durand")
    private String lastName;

    @NotBlank(message = "User email must have value")
    @Email
    @Schema(description = "User email", example = "jenner.durand@plazoleta.com")
    private String email;
}
