package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.api.IOrderReportServicePort;
import com.pragma.plazoleta.domain.common.PagedResult;
import com.pragma.plazoleta.domain.model.EmployeeEfficiency;
import com.pragma.plazoleta.domain.model.OrderEfficiency;
import com.pragma.plazoleta.domain.spi.IOrderReportPersistencePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderReportUseCase implements IOrderReportServicePort {

    private final IOrderReportPersistencePort orderReportPersistencePort;

    @Override
    public PagedResult<OrderEfficiency> getOrderEfficiency(Long restaurantId, Integer page, Integer size) {
        PagedResult.validatePagination(page, size);

        return orderReportPersistencePort.findOrderEfficiencyByRestaurantId(restaurantId, page, size);
    }

    @Override
    public PagedResult<EmployeeEfficiency> getEmployeeRanking(Long restaurantId, Integer page, Integer size) {
        PagedResult.validatePagination(page, size);

        return orderReportPersistencePort.findEmployeeEfficiencyByRestaurantId(restaurantId, page, size);
    }
}
