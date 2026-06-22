package com.pragma.plazoleta.domain.spi;

import com.pragma.plazoleta.domain.common.PagedResult;
import com.pragma.plazoleta.domain.model.EmployeeEfficiency;
import com.pragma.plazoleta.domain.model.OrderEfficiency;

public interface IOrderReportPersistencePort {

    PagedResult<OrderEfficiency> findOrderEfficiencyByRestaurantId(Long restaurantId, Integer page, Integer size);
    PagedResult<EmployeeEfficiency> findEmployeeEfficiencyByRestaurantId(Long restaurantId, Integer page, Integer size);
}
