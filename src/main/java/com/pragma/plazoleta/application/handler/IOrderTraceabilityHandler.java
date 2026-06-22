package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderTraceabilityResponseDto;

public interface IOrderTraceabilityHandler {
    void saveState(OrderStateRequestDto request);

    OrderTraceabilityResponseDto findByOrderId(Long orderId);
}
