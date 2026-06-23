package com.pragma.plazoleta.infrastructure.configuration;

import com.pragma.plazoleta.domain.api.IOrderReportServicePort;
import com.pragma.plazoleta.domain.api.IOrderTraceabilityServicePort;
import com.pragma.plazoleta.domain.spi.IOrderReportPersistencePort;
import com.pragma.plazoleta.domain.spi.IOrderTraceabilityPersistencePort;
import com.pragma.plazoleta.domain.usecase.OrderReportUseCase;
import com.pragma.plazoleta.domain.usecase.OrderTraceabilityUseCase;
import com.pragma.plazoleta.infrastructure.configuration.security.token.ITokenValidationPort;
import com.pragma.plazoleta.infrastructure.out.mongo.adapter.OrderReportMongoAdapter;
import com.pragma.plazoleta.infrastructure.out.mongo.adapter.OrderTraceabilityMongoAdapter;
import com.pragma.plazoleta.infrastructure.out.mongo.mapper.IOrderStateDocumentMapper;
import com.pragma.plazoleta.infrastructure.out.mongo.repository.IOrderStateDocumentRepository;
import com.pragma.plazoleta.infrastructure.out.security.jwt.JwtAdapter;
import com.pragma.plazoleta.infrastructure.out.security.jwt.configuration.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final IOrderStateDocumentRepository orderStateDocumentRepository;
    private final IOrderStateDocumentMapper orderStateDocumentMapper;

    private final MongoTemplate mongoTemplate;

    private final JwtProperties jwtProperties;

    @Bean
    public ITokenValidationPort tokenValidationPort() {
        return new JwtAdapter(jwtProperties);
    }

    @Bean
    public IOrderTraceabilityPersistencePort orderTraceabilityPersistencePort() {
        return new OrderTraceabilityMongoAdapter(
                orderStateDocumentRepository,
                orderStateDocumentMapper
        );
    }

    @Bean
    public IOrderTraceabilityServicePort orderTraceabilityServicePort() {
        return new OrderTraceabilityUseCase(
                orderTraceabilityPersistencePort()
        );
    }

    @Bean
    public IOrderReportPersistencePort orderReportPersistencePort() {
        return new OrderReportMongoAdapter(
                mongoTemplate
        );
    }

    @Bean
    public IOrderReportServicePort orderReportServicePort() {
        return new OrderReportUseCase(
                orderReportPersistencePort()
        );
    }
}
