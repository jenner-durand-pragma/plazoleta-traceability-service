package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import com.pragma.plazoleta.infrastructure.out.mongo.mapper.IOrderStateDocumentMapper;
import com.pragma.plazoleta.infrastructure.out.mongo.repository.IOrderStateDocumentRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderTraceabilityMongoAdapter implements IOrderTraceabilityPersistencePort {

    private final IOrderStateDocumentRepository orderStateDocumentRepository;
    private final IOrderStateDocumentMapper orderStateDocumentMapper;

    @Override
    public OrderState save(OrderState orderState) {
        return null;
    }

    @Override
    public List<OrderState> findByOrderId(Long orderId) {
        return null;
    }
}
