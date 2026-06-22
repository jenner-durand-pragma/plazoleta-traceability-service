package com.pragma.plazoleta.domain.usecase;

import com.pragma.plazoleta.domain.common.PagedResult;
import com.pragma.plazoleta.domain.exception.common.InvalidPaginationException;
import com.pragma.plazoleta.domain.model.EmployeeEfficiency;
import com.pragma.plazoleta.domain.model.OrderEfficiency;
import com.pragma.plazoleta.domain.model.UserInformation;
import com.pragma.plazoleta.domain.spi.IOrderReportPersistencePort;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReportUseCaseTest {

    @Mock
    private IOrderReportPersistencePort orderReportPersistencePort;

    @InjectMocks
    private OrderReportUseCase orderReportUseCase;

    private static final Long RESTAURANT_ID = 10L;
    private static final Integer VALID_PAGE = 0;
    private static final Integer VALID_SIZE = 10;

    private PagedResult<OrderEfficiency> orderEfficiencyPagedResult;
    private PagedResult<EmployeeEfficiency> employeeEfficiencyPagedResult;

    @BeforeEach
    void setUp() {
        var client = UserInformation.builder()
                .id(5L)
                .name("Jenner")
                .lastName("Durand")
                .email("jenner.durand@plazoleta.com")
                .build();

        var employee = UserInformation.builder()
                .id(6L)
                .name("Admin")
                .lastName("Plazoleta")
                .email("admin@plazoleta.com")
                .build();

        var orderEfficiency = OrderEfficiency.builder()
                .orderId(42L)
                .startedAt(LocalDateTime.now().minusMinutes(15))
                .endedAt(LocalDateTime.now())
                .durationMinutes(15.0)
                .client(client)
                .build();

        var employeeEfficiency = EmployeeEfficiency.builder()
                .employee(employee)
                .averageMinutes(12.5)
                .ordersHandled(20L)
                .build();

        orderEfficiencyPagedResult = PagedResult.of(
                List.of(orderEfficiency), VALID_PAGE, VALID_SIZE, 1L, 1
        );

        employeeEfficiencyPagedResult = PagedResult.of(
                List.of(employeeEfficiency), VALID_PAGE, VALID_SIZE, 1L, 1
        );
    }

    @Test
    @DisplayName(
            "Should return order efficiency paged result when " +
            "parameters are valid in get order efficiency"
    )
    void shouldReturnOrderEfficiencyPagedResultWhenParametersAreValidInGetOrderEfficiency() {
        when(orderReportPersistencePort.findOrderEfficiencyByRestaurantId(RESTAURANT_ID, VALID_PAGE, VALID_SIZE))
                .thenReturn(orderEfficiencyPagedResult);

        var result = orderReportUseCase.getOrderEfficiency(RESTAURANT_ID, VALID_PAGE, VALID_SIZE);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getDurationMinutes()).isEqualTo(15.0);
        verify(orderReportPersistencePort).findOrderEfficiencyByRestaurantId(RESTAURANT_ID, VALID_PAGE, VALID_SIZE);
    }

    @Test
    @DisplayName(
            "Should throw InvalidPaginationException when " +
            "page is invalid in get order efficiency"
    )
    void shouldThrowInvalidPaginationExceptionWhenPageIsInvalidInGetOrderEfficiency() {
        var invalidPage = -1;

        assertThatThrownBy(() -> orderReportUseCase.getOrderEfficiency(RESTAURANT_ID, invalidPage, VALID_SIZE))
                .isInstanceOf(InvalidPaginationException.class);

        verifyNoInteractions(orderReportPersistencePort);
    }

    @Test
    @DisplayName(
            "Should return employee ranking paged result when " +
            "parameters are valid in get employee ranking"
    )
    void shouldReturnEmployeeRankingPagedResultWhenParametersAreValidInGetEmployeeRanking() {
        when(orderReportPersistencePort.findEmployeeEfficiencyByRestaurantId(RESTAURANT_ID, VALID_PAGE, VALID_SIZE))
                .thenReturn(employeeEfficiencyPagedResult);

        var result = orderReportUseCase.getEmployeeRanking(RESTAURANT_ID, VALID_PAGE, VALID_SIZE);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getAverageMinutes()).isEqualTo(12.5);
        verify(orderReportPersistencePort).findEmployeeEfficiencyByRestaurantId(RESTAURANT_ID, VALID_PAGE, VALID_SIZE);
    }

    @Test
    @DisplayName(
            "Should throw InvalidPaginationException when " +
            "size is invalid in get employee ranking"
    )
    void shouldThrowInvalidPaginationExceptionWhenSizeIsInvalidInGetEmployeeRanking() {
        var invalidSize = 0;

        assertThatThrownBy(() -> orderReportUseCase.getEmployeeRanking(RESTAURANT_ID, VALID_PAGE, invalidSize))
                .isInstanceOf(InvalidPaginationException.class);

        verifyNoInteractions(orderReportPersistencePort);
    }
}
