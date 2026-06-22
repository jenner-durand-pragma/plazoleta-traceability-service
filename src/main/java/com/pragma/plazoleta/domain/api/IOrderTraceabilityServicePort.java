package com.pragma.plazoleta.domain.api;

import com.pragma.plazoleta.domain.model.OrderState;

import java.util.List;

public interface IOrderTraceabilityServicePort {

    void saveState(OrderState orderState);
    List<OrderState> findByOrderId(Long orderId);
}
