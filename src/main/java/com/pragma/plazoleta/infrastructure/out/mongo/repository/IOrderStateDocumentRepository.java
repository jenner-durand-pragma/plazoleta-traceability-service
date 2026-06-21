package com.pragma.plazoleta.infrastructure.out.mongo.repository;

import com.pragma.plazoleta.infrastructure.out.mongo.document.OrderStateDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IOrderStateDocumentRepository extends MongoRepository<OrderStateDocument, String> {

    List<OrderStateDocument> findByOrderId(Long orderId, Sort sort);
}
