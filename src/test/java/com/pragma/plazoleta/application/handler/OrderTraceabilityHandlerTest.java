package com.pragma.plazoleta.application.handler;

import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.handler.impl.OrderTraceabilityHandler;
import com.pragma.plazoleta.application.mapper.IOrderTraceabilityMapper;
import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.model.UserInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTraceabilityHandlerTest {

    @Mock
    private IOrderTraceabilityServicePort orderTraceabilityServicePort;

    @Spy
    private IOrderTraceabilityMapper orderTraceabilityMapper = Mappers.getMapper(IOrderTraceabilityMapper.class);

    @InjectMocks
    private OrderTraceabilityHandler orderTraceabilityHandler;

    @Test
    @DisplayName("Should save the order state in save state")
    void shouldSaveOrderStateInSaveState() {
        var request = OrderStateRequestDto.builder()
                .orderId(42L)
                .newStatus(OrderStatus.PENDING)
                .build();

        var orderStateCaptor = ArgumentCaptor.forClass(OrderState.class);

        orderTraceabilityHandler.saveState(request);

        verify(orderTraceabilityMapper).toOrderState(request);
        verify(orderTraceabilityServicePort).saveState(orderStateCaptor.capture());

        var capturedOrderState = orderStateCaptor.getValue();
        assertThat(capturedOrderState.getOrderId()).isEqualTo(42L);
        assertThat(capturedOrderState.getNewStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should retrieve mapped order states for client id in find by order id for client")
    void shouldReturnMappedOrderStatesInFindByOrderIdForClient() {
        var orderId = 42L;
        var clientId = 7L;

        var orderState = OrderState.builder()
                .orderId(orderId)
                .newStatus(OrderStatus.READY)
                .client(UserInformation.builder().id(clientId).build())
                .build();

        var orderStates = List.of(orderState);

        when(orderTraceabilityServicePort.findByOrderIdForClient(orderId, clientId)).thenReturn(orderStates);

        var result = orderTraceabilityHandler.findByOrderIdForClient(orderId, clientId);

        verify(orderTraceabilityServicePort).findByOrderIdForClient(orderId, clientId);
        verify(orderTraceabilityMapper).toResponse(orderStates);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getClient().getId()).isEqualTo(clientId);
        assertThat(result.getTransitions().get(0).getNewStatus()).isEqualTo(OrderStatus.READY);
    }
}