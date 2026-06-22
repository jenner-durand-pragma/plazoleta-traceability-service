package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import com.pragma.plazoleta.infrastructure.out.mongo.mapper.IOrderStateDocumentMapper;
import com.pragma.plazoleta.infrastructure.out.mongo.repository.IOrderStateDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderTraceabilityMongoAdapter implements IOrderTraceabilityPersistencePort {

    private final IOrderStateDocumentRepository orderStateDocumentRepository;
    private final IOrderStateDocumentMapper orderStateDocumentMapper;

    @Override
    public OrderState save(OrderState orderState) {
        var document = orderStateDocumentMapper.toDocument(orderState);
        var savedDocument = orderStateDocumentRepository.save(document);

        return orderStateDocumentMapper.toModel(savedDocument);
    }

    @Override
    public List<OrderState> findByOrderId(Long orderId) {
        return orderStateDocumentRepository
                .findByOrderId(orderId, Sort.by(Sort.Direction.ASC, "changedAt"))
                .stream()
                .map(orderStateDocumentMapper::toModel)
                .collect(Collectors.toList());
    }
}
