package com.pragma.plazoleta.infrastructure.out.mongo.adapter;

import com.pragma.plazoleta.domain.common.PagedResult;
import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.model.EmployeeEfficiency;
import com.pragma.plazoleta.domain.model.OrderEfficiency;
import com.pragma.plazoleta.domain.model.UserInformation;
import com.pragma.plazoleta.domain.spi.IOrderReportPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderReportMongoAdapter implements IOrderReportPersistencePort {

    private static final String ORDER_STATE_COLLECTION = "order_state_history";

    private final MongoTemplate mongoTemplate;

    @Override
    public PagedResult<OrderEfficiency> findOrderEfficiencyByRestaurantId(
            Long restaurantId,
            Integer page,
            Integer size
    ) {
        var filterStates = Aggregation.match(
                Criteria.where("restaurantId").is(restaurantId)
                        .and("newStatus").in(OrderStatus.PENDING.name(), OrderStatus.DELIVERED.name())
        );

        var groupStates = Aggregation.group("orderId")
                .min("changedAt").as("startedAt")
                .max("changedAt").as("endedAt")
                .first("client").as("client")
                .count().as("countStates");

        var matchTerminated = Aggregation.match(
                Criteria.where("countStates").is(2)
        );

        var calculateDuration = Aggregation.project("startedAt", "endedAt", "client")
                .andExpression("(endedAt - startedAt) / 60000").as("durationMinutes");

        var totalCount = Aggregation.count().as("total");

        var countAggregationStates = Aggregation.newAggregation(
                filterStates,
                groupStates,
                matchTerminated,
                totalCount
        );

        var countResultStates = mongoTemplate.aggregate(countAggregationStates, ORDER_STATE_COLLECTION, Map.class)
                .getMappedResults();

        var totalElements = countResultStates.isEmpty() ? 0L
                : ((Number) countResultStates.get(0).get("total")).longValue();
        var sortStage = Aggregation.sort(Sort.by(Sort.Direction.DESC, "durationMinutes"));
        var skipStage = Aggregation.skip((long) page * size);
        var limitStage = Aggregation.limit(size);

        var aggregationStates = Aggregation.newAggregation(
                filterStates,
                groupStates,
                matchTerminated,
                calculateDuration,
                sortStage,
                skipStage,
                limitStage
        );

        var mapResultStates = mongoTemplate.aggregate(aggregationStates, ORDER_STATE_COLLECTION, Map.class)
                .getMappedResults();

        var resultStates = mapResultStates.stream()
                .map(this::toOrderEfficiency)
                .collect(Collectors.toList());

        var totalPages = (int) Math.ceil((double) totalElements / size);

        return PagedResult.of(resultStates, page, size, totalElements, totalPages);
    }

    @Override
    public PagedResult<EmployeeEfficiency> findEmployeeEfficiencyByRestaurantId(
            Long restaurantId,
            Integer page,
            Integer size
    ) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private OrderEfficiency toOrderEfficiency(Map<String, Object> result) {
        var startedAt = ((Date) result.get("startedAt")).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        var endedAt = ((Date) result.get("endedAt")).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        var durationMinutes = roundToTwoDecimal(
                ((Number) result.get("durationMinutes")).doubleValue()
        );

        var clientResult = (Map<String, Object>) result.get("client");
        var client = clientResult == null
                ? null
                : UserInformation.builder()
                  .id(((Number) clientResult.get("_id")).longValue())
                  .name((String) clientResult.get("name"))
                  .lastName((String) clientResult.get("lastName"))
                  .email((String) clientResult.get("email"))
                  .build();

        return OrderEfficiency.builder()
                .orderId(((Number) result.get("_id")).longValue())
                .startedAt(startedAt)
                .endedAt(endedAt)
                .durationMinutes(durationMinutes)
                .client(client)
                .build();
    }

    private Double roundToTwoDecimal(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
