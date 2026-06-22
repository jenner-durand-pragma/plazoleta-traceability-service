package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.exception.order.OrderTraceabilityNotFoundException;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.model.UserInformation;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTraceabilityUseCaseTest {

    @Mock
    private IOrderTraceabilityPersistencePort orderTraceabilityPersistencePort;

    @InjectMocks
    private OrderTraceabilityUseCase orderTraceabilityUseCase;

    private static final Long ORDER_ID = 42L;
    private static final Long RESTAURANT_ID = 12L;
    private static final Long CLIENT_ID = 5L;

    private OrderState pendingOrderState;

    @BeforeEach
    void setUp() {
        var client = UserInformation.builder()
                .id(CLIENT_ID)
                .name("Client")
                .lastName("Example 1")
                .email("client.1@plazoleta.com")
                .build();

        pendingOrderState = OrderState.builder()
                .orderId(ORDER_ID)
                .previousStatus(null)
                .newStatus(OrderStatus.PENDING)
                .changedAt(LocalDateTime.now())
                .restaurantId(RESTAURANT_ID)
                .client(client)
                .employee(null)
                .build();
    }

    @Test
    @DisplayName("Should persist order state in save state")
    void shouldPersistOrderStateInSaveState() {
        orderTraceabilityUseCase.saveState(pendingOrderState);

        verify(orderTraceabilityPersistencePort).save(pendingOrderState);
    }

    @Test
    @DisplayName("Should return all order states in find by order id")
    void shouldReturnAllOrderStatesInFindByOrderId() {
        pendingOrderState.setId("EXAMPLE_ID");
        when(orderTraceabilityPersistencePort.findByOrderId(ORDER_ID))
                .thenReturn(List.of(pendingOrderState));

        var orderStates = orderTraceabilityUseCase.findByOrderId(ORDER_ID);

        assertThat(orderStates.size()).isEqualTo(1);
        verify(orderTraceabilityPersistencePort).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName(
            "Should throw TraceabilityNotFoundException when " +
            "there are no order states for the order id in find by order id"
    )
    void shouldThrowTraceabilityNotFoundExceptionWhenThereAreNoRecordsForTheOrderInFindByOrderId() {
        when(orderTraceabilityPersistencePort.findByOrderId(ORDER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> orderTraceabilityUseCase.findByOrderId(ORDER_ID))
                .isInstanceOf(OrderTraceabilityNotFoundException.class);
    }
}
