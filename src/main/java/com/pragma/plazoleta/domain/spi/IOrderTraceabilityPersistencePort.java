package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.model.OrderState;

import java.util.List;

public interface IOrderTraceabilityPersistencePort {

    OrderState save(OrderState orderState);
    List<OrderState> findByOrderId(Long orderId);
}
