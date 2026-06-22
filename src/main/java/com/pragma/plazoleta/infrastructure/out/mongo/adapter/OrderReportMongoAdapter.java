package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.common.PagedResult;
import com.pragma.plazoleta.domain.model.EmployeeEfficiency;
import com.pragma.plazoleta.domain.model.OrderEfficiency;
import com.pragma.plazoleta.domain.spi.IOrderReportPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;


@RequiredArgsConstructor
public class OrderReportMongoAdapter implements IOrderReportPersistencePort {

    private final MongoTemplate mongoTemplate;

    @Override
    public PagedResult<OrderEfficiency> findOrderEfficiencyByRestaurantId(
            Long restaurantId,
            Integer page,
            Integer size
    ) {
        return null;
    }

    @Override
    public PagedResult<EmployeeEfficiency> findEmployeeEfficiencyByRestaurantId(
            Long restaurantId,
            Integer page,
            Integer size
    ) {
        return null;
    }
}
