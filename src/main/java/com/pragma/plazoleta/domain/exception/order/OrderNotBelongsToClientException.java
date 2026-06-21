package com.pragma.plazoleta.domain.exception.order;

import com.pragma.plazoleta.domain.exception.NotFoundException;
import com.pragma.plazoleta.domain.model.OrderState;

public class OrderNotBelongsToClientException extends NotFoundException {

    private static final String ERROR_MESSAGE = "No traceability records found for order: %d";

    public OrderNotBelongsToClientException(Long orderId) {
        super(String.format(ERROR_MESSAGE, orderId), OrderState.class.getSimpleName(), orderId);
    }
}
