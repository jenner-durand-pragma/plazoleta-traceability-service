package com.pragma.plazoleta.application.dto.response.orderstate;

import com.pragma.plazoleta.application.dto.response.user.UserInformationResponseDto;
import com.pragma.plazoleta.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateResponseDto {
    @Schema(
            description = "Previous order status, null when the transition is the initial creation",
            example = "IN_PREPARATION"
    )
    private OrderStatus previousStatus;

    @Schema(description = "New order status", example = "READY")
    private OrderStatus newStatus;

    @Schema(description = "Datetime when changed occurs", example = "2026-05-31T15:23:45")
    private LocalDateTime changedAt;

    @Schema(description = "Employee information, null in createOrder and cancelOrder transitions")
    private UserInformationResponseDto employee;
}
