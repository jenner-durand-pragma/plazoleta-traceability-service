package com.pragma.plazoleta.infrastructure.input.rest;

import com.pragma.plazoleta.application.dto.response.common.PagedResponseDto;
import com.pragma.plazoleta.application.dto.response.orderreport.EmployeeRankingResponseDto;
import com.pragma.plazoleta.application.dto.response.orderreport.OrderEfficiencyResponseDto;
import com.pragma.plazoleta.application.handler.IOrderReportHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/efficiency")
    public ResponseEntity<PagedResponseDto<OrderEfficiencyResponseDto>> getOrderEfficiency(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return null;
    }

    @GetMapping("/employees-ranking")
    public ResponseEntity<PagedResponseDto<EmployeeRankingResponseDto>> getEmployeeRanking(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return null;
    }
}
