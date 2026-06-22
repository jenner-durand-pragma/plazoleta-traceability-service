package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.model.UserInformation;
import com.pragma.plazoleta.infrastructure.out.mongo.mapper.IOrderStateDocumentMapper;
import com.pragma.plazoleta.infrastructure.out.mongo.mapper.IOrderStateDocumentMapperImpl;
import com.pragma.plazoleta.infrastructure.out.mongo.repository.IOrderStateDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(IOrderStateDocumentMapperImpl.class)
class OrderTraceabilityMongoAdapterTest {

    @Autowired
    private IOrderStateDocumentRepository orderStateDocumentRepository;

    @Autowired
    private IOrderStateDocumentMapper orderStateDocumentMapper;

    private OrderTraceabilityMongoAdapter orderTraceabilityMongoAdapter;

    @BeforeEach
    void setUp() {
        orderStateDocumentRepository.deleteAll();

        orderTraceabilityMongoAdapter = new OrderTraceabilityMongoAdapter(
                orderStateDocumentRepository,
                orderStateDocumentMapper
        );
    }

    private OrderState buildOrderState(OrderStatus newStatus, LocalDateTime changedAt) {
        return OrderState.builder()
                .orderId(42L)
                .restaurantId(10L)
                .previousStatus(OrderStatus.IN_PREPARATION)
                .newStatus(newStatus)
                .changedAt(changedAt)
                .client(UserInformation.builder()
                        .id(10L)
                        .name("Jenner")
                        .lastName("Durand")
                        .email("jenner.durand@plazoleta.com")
                        .build()
                )
                .employee(UserInformation.builder()
                        .id(1L)
                        .name("Admin")
                        .lastName("Plazoleta")
                        .email("admin@plazoleta.com")
                        .build()
                )
                .build();
    }

    @Test
    @DisplayName("Should save an order state and return it with a generated id")
    void shouldSaveOrderStateAndReturnWithId() {
        var orderState = buildOrderState(OrderStatus.READY, LocalDateTime.now());

        var saved = orderTraceabilityMongoAdapter.save(orderState);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo(42L);
        assertThat(saved.getClient().getEmail()).isEqualTo("jenner.durand@plazoleta.com");
        assertThat(saved.getEmployee().getEmail()).isEqualTo("admin@plazoleta.com");
    }

    @Test
    @DisplayName("Should return order states ordered by changed date ascending")
    void shouldReturnOrderStatesOrderedByDateAsc() {
        orderTraceabilityMongoAdapter.save(
                buildOrderState(
                        OrderStatus.READY,
                        LocalDateTime.now()
                )
        );
        orderTraceabilityMongoAdapter.save(
                buildOrderState(
                        OrderStatus.PENDING,
                        LocalDateTime.now().minusHours(2)
                )
        );
        orderTraceabilityMongoAdapter.save(
                buildOrderState(
                        OrderStatus.IN_PREPARATION,
                        LocalDateTime.now().minusHours(1)
                )
        );

        var result = orderTraceabilityMongoAdapter.findByOrderId(42L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getNewStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.get(1).getNewStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(result.get(2).getNewStatus()).isEqualTo(OrderStatus.READY);
    }

    @Test
    @DisplayName("Should return empty list when no order states match the order id")
    void shouldReturnEmptyListWhenNoOrderStatesMatch() {
        var result = orderTraceabilityMongoAdapter.findByOrderId(999L);

        assertThat(result).isEmpty();
    }
}