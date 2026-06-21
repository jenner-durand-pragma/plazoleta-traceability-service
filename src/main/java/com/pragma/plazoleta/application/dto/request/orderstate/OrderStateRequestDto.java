package com.pragma.plazoleta.application.dto.request.orderstate;

import com.pragma.plazoleta.application.dto.request.user.UserInformationRequestDto;
import com.pragma.plazoleta.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateRequestDto {
    @NotNull(message = "Order id is required")
    @Positive(message = "Order id must be positive")
    @Schema(example = "42")
    private Long orderId;

    @NotNull(message = "Restaurant id is required")
    @Positive(message = "Restaurant id must be positive")
    @Schema(example = "10")
    private Long restaurantId;

    @Schema(
            description = "Previous order status, null when the transition is the initial creation",
            example = "IN_PREPARATION"
    )
    private OrderStatus previousStatus;

    @NotNull(message = "New order status is required")
    @Schema(description = "New order status", example = "READY")
    private OrderStatus newStatus;

    @NotNull(message = "Changed at is required")
    @Schema(description = "Datetime when changed occurs", example = "2026-05-31T15:23:45")
    private LocalDateTime changedAt;

    @NotNull(message = "Client information is required")
    @Valid
    @Schema(description = "Client information")
    private UserInformationRequestDto client;

    @Valid
    @Schema(description = "Employee information, null in createOrder and cancelOrder transitions")
    private UserInformationRequestDto employee;
}
