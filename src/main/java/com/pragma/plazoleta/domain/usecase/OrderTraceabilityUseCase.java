package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import com.pragma.plazoleta.domain.exception.order.OrderTraceabilityNotFoundException;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderTraceabilityUseCase implements IOrderTraceabilityServicePort {

    private final IOrderTraceabilityPersistencePort orderTraceabilityPersistencePort;

    @Override
    public void saveState(OrderState orderState) {
        orderTraceabilityPersistencePort.save(orderState);
    }

    @Override
    public List<OrderState> findByOrderId(Long orderId) {
        var orderStates = orderTraceabilityPersistencePort.findByOrderId(orderId);

        checkStatesNotEmpty(orderStates, orderId);

        return orderStates;
    }

    private void checkStatesNotEmpty(List<OrderState> orderStates, Long orderId) {
        if (orderStates.isEmpty()) {
            throw new OrderTraceabilityNotFoundException(orderId);
        }
    }
}
