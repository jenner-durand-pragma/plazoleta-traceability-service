package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import com.pragma.plazoleta.domain.exception.order.OrderNotBelongsToClientException;
import com.pragma.plazoleta.domain.exception.order.OrderTraceabilityNotFoundException;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class OrderTraceabilityUseCase implements IOrderTraceabilityServicePort {

    private final IOrderTraceabilityPersistencePort orderTraceabilityPersistencePort;

    @Override
    public void saveState(OrderState orderState) {
        orderTraceabilityPersistencePort.save(orderState);
    }

    @Override
    public List<OrderState> findByOrderIdForClient(Long orderId, Long clientId) {
        var orderStates = orderTraceabilityPersistencePort.findByOrderId(orderId);

        checkStatesNotEmpty(orderStates, orderId);
        checkStatesBelongsToClient(orderStates, clientId);

        return orderStates;
    }

    private void checkStatesNotEmpty(List<OrderState> orderStates, Long orderId) {
        if (orderStates.isEmpty()) {
            throw new OrderTraceabilityNotFoundException(orderId);
        }
    }

    private void checkStatesBelongsToClient(List<OrderState> orderStates, Long clientId) {
        var firstOrderState = orderStates.get(0);
        if (!Objects.equals(firstOrderState.getClient().getId(), clientId)) {

            throw new OrderNotBelongsToClientException(firstOrderState.getOrderId());
        }
    }
}
