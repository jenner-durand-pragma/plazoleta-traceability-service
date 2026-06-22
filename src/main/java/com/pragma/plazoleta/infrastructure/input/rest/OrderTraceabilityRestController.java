package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderTraceabilityResponseDto;
import com.pragma.plazoleta.application.handler.IOrderTraceabilityHandler;
import com.pragma.plazoleta.infrastructure.configuration.security.annotation.IsClient;
import com.pragma.plazoleta.infrastructure.exceptionhandler.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/traceability/orders")
@RequiredArgsConstructor
@Tag(name = "Order Traceability", description = "Manages order state transitions and history")
public class OrderTraceabilityRestController {

    private final IOrderTraceabilityHandler orderTraceabilityHandler;

    @Operation(summary = "Save an order state transition",
            description = "Called by plazoleta-main on every order state change.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order state saved"),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/states")
    public ResponseEntity<Void> saveState(@Valid @RequestBody OrderStateRequestDto request) {
        orderTraceabilityHandler.saveState(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @IsClient
    @Operation(summary = "Get the order states history of one of my orders",
            description = "Returns the full traceability log of the requested order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order history",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderTraceabilityResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Caller is not a CLIENT"),
            @ApiResponse(responseCode = "404", description = "No history found for this order id and caller",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderTraceabilityResponseDto> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                orderTraceabilityHandler.findByOrderId(orderId)
        );
    }
}
