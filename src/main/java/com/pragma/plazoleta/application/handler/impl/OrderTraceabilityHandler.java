package com.pragma.plazoleta.application.handler.impl;

import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderTraceabilityResponseDto;
import com.pragma.plazoleta.application.handler.IOrderTraceabilityHandler;
import com.pragma.plazoleta.application.mapper.IOrderTraceabilityMapper;
import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderTraceabilityHandler implements IOrderTraceabilityHandler {
    private final IOrderTraceabilityServicePort orderTraceabilityServicePort;
    private final IOrderTraceabilityMapper orderTraceabilityMapper;

    @Override
    public void saveState(OrderStateRequestDto request) {
        var orderState = orderTraceabilityMapper.toOrderState(request);

        orderTraceabilityServicePort.saveState(orderState);
    }

    @Override
    public OrderTraceabilityResponseDto findByOrderIdForClient(Long orderId, Long clientId) {
        var orderStates = orderTraceabilityServicePort.findByOrderIdForClient(orderId, clientId);

        return orderTraceabilityMapper.toResponse(orderStates);
    }
}
