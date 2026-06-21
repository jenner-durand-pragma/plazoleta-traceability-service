package com.pragma.plazoleta.infrastructure.out.mongo.document;

import com.pragma.plazoleta.domain.enums.OrderStatus;
import com.pragma.plazoleta.domain.model.UserInformation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "order_state_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateDocument {
    @Id
    private String id;

    @Indexed
    private Long orderId;

    @Indexed
    private Long restaurantId;

    private OrderStatus previousStatus;
    private OrderStatus newStatus;

    private LocalDateTime changedAt;

    private UserInformationSubDocument client;
    private UserInformationSubDocument employee;
}
