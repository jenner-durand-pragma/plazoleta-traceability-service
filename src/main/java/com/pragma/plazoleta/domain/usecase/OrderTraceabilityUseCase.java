package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderTraceabilityUseCase implements IOrderTraceabilityServicePort {

    private final IOrderTraceabilityPersistencePort orderTraceabilityPersistencePort;

    @Override
    public void saveState(OrderState orderState) {
        return;
    }

    @Override
    public List<OrderState> findByOrderIdForClient(Long orderId, Long clientId) {
        return null;
    }
}
