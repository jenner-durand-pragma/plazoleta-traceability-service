package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.response.common.PagedResponseDto;
import com.pragma.plazoleta.application.dto.response.orderreport.EmployeeRankingResponseDto;
import com.pragma.plazoleta.application.dto.response.orderreport.OrderEfficiencyResponseDto;
import com.pragma.plazoleta.application.handler.IOrderReportHandler;
import com.pragma.plazoleta.infrastructure.exceptionhandler.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/traceability/restaurants/{restaurantId}")
@RequiredArgsConstructor
@Tag(name = "Order Reports", description = "Internal orders reports endpoints")
public class OrderReportRestController {

    private final IOrderReportHandler orderReportHandler;

    @Operation(summary = "Order efficiency for a restaurant (internal)",
            description = "Returns paged orders that reached DELIVERED " +
                    "with the duration from PENDING to DELIVERED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paged efficiency report",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PagedResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "422", description = "Invalid pagination",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/efficiency")
    public ResponseEntity<PagedResponseDto<OrderEfficiencyResponseDto>> getOrderEfficiency(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(orderReportHandler.getOrderEfficiency(restaurantId, page, size));
    }

    @Operation(summary = "Employee ranking by average delivery time (internal)",
            description = "Returns employees of the restaurant ranked ascending " +
                    "by their average time from PENDING to DELIVERED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee ranking",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EmployeeRankingResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "422", description = "Invalid pagination",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/employees-ranking")
    public ResponseEntity<PagedResponseDto<EmployeeRankingResponseDto>> getEmployeeRanking(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(orderReportHandler.getEmployeeRanking(restaurantId, page, size));
    }
}
