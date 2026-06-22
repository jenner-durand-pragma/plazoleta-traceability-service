package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.infrastructure.out.mongo.document.OrderStateDocument;
import com.pragma.plazoleta.infrastructure.out.mongo.document.UserInformationSubDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class OrderReportMongoAdapterTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private OrderReportMongoAdapter orderReportMongoAdapter;

    private static final String COLLECTION = "order_state_history";
    private static final Long RESTAURANT_ID = 10L;
    private static final Set<OrderStatus> ORDER_STATUSES_WHERE_EMPLOYEE_IS_REQUIRED =
            Set.of(OrderStatus.IN_PREPARATION, OrderStatus.READY, OrderStatus.DELIVERED);

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(COLLECTION);

        orderReportMongoAdapter = new OrderReportMongoAdapter(mongoTemplate);
    }

    private void saveOrderState(
            Long orderId,
            Long restaurantId,
            OrderStatus previousState,
            OrderStatus newStatus,
            LocalDateTime changedAt
    ) {
        UserInformationSubDocument employee = null;

        var client = UserInformationSubDocument.builder()
                .id(5L)
                .name("Jenner")
                .lastName("Durand")
                .email("jenner.durand@plazoleta.com")
                .build();

        if (ORDER_STATUSES_WHERE_EMPLOYEE_IS_REQUIRED.contains(newStatus)) {
            employee = UserInformationSubDocument.builder()
                    .id(6L)
                    .name("Employee")
                    .lastName("Second")
                    .email("employee@plazoleta.com")
                    .build();
        }

        var document = OrderStateDocument.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .previousStatus(previousState)
                .newStatus(newStatus)
                .changedAt(changedAt)
                .client(client)
                .employee(employee)
                .build();

        mongoTemplate.save(document, COLLECTION);
    }

    @Test
    @DisplayName(
            "Should aggregate and calculate efficiency correctly when " +
            "a restaurant has finished orders in find order efficiency by restaurant id"
    )
    void shouldCalculateEfficiencyCorrectlyForFinishedOrdersInFindOrderEfficiencyByRestaurantId() {
        var orderId = 42L;
        var startedAt = LocalDateTime.of(2026, 6, 21, 10, 0, 0);
        var endedAt = LocalDateTime.of(2026, 6, 21, 10, 15, 30);

        saveOrderState(
                orderId, RESTAURANT_ID,
                null, OrderStatus.PENDING,
                startedAt
        );
        saveOrderState(
                orderId, RESTAURANT_ID,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                startedAt.plusMinutes(5)
        );
        saveOrderState(
                orderId, RESTAURANT_ID,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                startedAt.plusMinutes(10)
        );
        saveOrderState(
                orderId, RESTAURANT_ID,
                OrderStatus.READY, OrderStatus.DELIVERED,
                endedAt
        );

        var result = orderReportMongoAdapter.findOrderEfficiencyByRestaurantId(RESTAURANT_ID, 0, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);

        var efficiency = result.getItems().get(0);
        assertThat(efficiency.getOrderId()).isEqualTo(orderId);
        assertThat(efficiency.getDurationMinutes()).isEqualTo(15.5);
        assertThat(efficiency.getClient().getName()).isEqualTo("Jenner");
    }

    @Test
    @DisplayName(
            "Should ignore unfinished orders and orders from other restaurants " +
            "in find order efficiency by restaurant id"
    )
    void shouldIgnoreUnfinishedOrdersAndOrdersFromOtherRestaurantsInFindOrderEfficiencyByRestaurantId() {
        var otherRestaurantId = 999L;

        saveOrderState(
                1L, RESTAURANT_ID,
                null, OrderStatus.PENDING,
                LocalDateTime.now()
        );
        saveOrderState(
                1L, RESTAURANT_ID,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                LocalDateTime.now().plusMinutes(5)
        );
        saveOrderState(
                1L, RESTAURANT_ID,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                LocalDateTime.now().plusMinutes(10)
        );

        saveOrderState(
                2L, otherRestaurantId,
                null, OrderStatus.PENDING,
                LocalDateTime.now()
        );
        saveOrderState(
                2L, otherRestaurantId,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                LocalDateTime.now().plusMinutes(1)
        );
        saveOrderState(
                2L, otherRestaurantId,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                LocalDateTime.now().plusMinutes(2)
        );
        saveOrderState(
                2L, otherRestaurantId,
                OrderStatus.READY, OrderStatus.DELIVERED,
                LocalDateTime.now().plusMinutes(5)
        );

        var result = orderReportMongoAdapter.findOrderEfficiencyByRestaurantId(RESTAURANT_ID, 0, 10);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName(
            "Should paginate correctly and sort by duration minutes descending (slowest first) " +
            "when there are multiple finished orders in find order efficiency by restaurant id"
    )
    void shouldPaginateAndSortByDurationDescendingWhenMultipleFinishedOrdersExistInFindOrderEfficiency() {
        saveOrderState(
                1L, RESTAURANT_ID,
                null, OrderStatus.PENDING,
                LocalDateTime.of(2026, 6, 21, 9, 0)
        );
        saveOrderState(
                1L, RESTAURANT_ID,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                LocalDateTime.of(2026, 6, 21, 9, 10)
        );
        saveOrderState(
                1L, RESTAURANT_ID,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                LocalDateTime.of(2026, 6, 21, 9, 20)
        );
        saveOrderState(
                1L, RESTAURANT_ID,
                OrderStatus.READY, OrderStatus.DELIVERED,
                LocalDateTime.of(2026, 6, 21, 9, 30)
        );

        saveOrderState(
                2L, RESTAURANT_ID,
                null, OrderStatus.PENDING,
                LocalDateTime.of(2026, 6, 21, 10, 0)
        );
        saveOrderState(
                2L, RESTAURANT_ID,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                LocalDateTime.of(2026, 6, 21, 10, 5)
        );
        saveOrderState(
                2L, RESTAURANT_ID,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                LocalDateTime.of(2026, 6, 21, 10, 10)
        );
        saveOrderState(
                2L, RESTAURANT_ID,
                OrderStatus.READY, OrderStatus.DELIVERED,
                LocalDateTime.of(2026, 6, 21, 10, 15)
        );

        saveOrderState(
                3L, RESTAURANT_ID,
                null, OrderStatus.PENDING,
                LocalDateTime.of(2026, 6, 21, 8, 0)
        );
        saveOrderState(
                3L, RESTAURANT_ID,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION,
                LocalDateTime.of(2026, 6, 21, 8, 15)
        );
        saveOrderState(
                3L, RESTAURANT_ID,
                OrderStatus.IN_PREPARATION, OrderStatus.READY,
                LocalDateTime.of(2026, 6, 21, 8, 30)
        );
        saveOrderState(
                3L, RESTAURANT_ID,
                OrderStatus.READY, OrderStatus.DELIVERED,
                LocalDateTime.of(2026, 6, 21, 8, 45)
        );

        var page0 = orderReportMongoAdapter.findOrderEfficiencyByRestaurantId(RESTAURANT_ID, 0, 2);

        assertThat(page0.getItems()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(3L);
        assertThat(page0.getTotalPages()).isEqualTo(2);

        assertThat(page0.getItems().get(0).getOrderId()).isEqualTo(3L);
        assertThat(page0.getItems().get(0).getDurationMinutes()).isEqualTo(45.0);

        assertThat(page0.getItems().get(1).getOrderId()).isEqualTo(1L);
        assertThat(page0.getItems().get(1).getDurationMinutes()).isEqualTo(30.0);

        var page1 = orderReportMongoAdapter.findOrderEfficiencyByRestaurantId(RESTAURANT_ID, 1, 2);

        assertThat(page1.getItems()).hasSize(1);

        assertThat(page1.getItems().get(0).getOrderId()).isEqualTo(2L);
        assertThat(page1.getItems().get(0).getDurationMinutes()).isEqualTo(15.0);
    }
}
