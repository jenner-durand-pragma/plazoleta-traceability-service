package com.pragma.plazoleta.application.mapper;

import com.pragma.plazoleta.application.dto.request.orderstate.OrderStateRequestDto;
import com.pragma.plazoleta.application.dto.request.user.UserInformationRequestDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderStateResponseDto;
import com.pragma.plazoleta.application.dto.response.orderstate.OrderTraceabilityResponseDto;
import com.pragma.plazoleta.application.dto.response.user.UserInformationResponseDto;
import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.model.UserInformation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderTraceabilityMapper {

    OrderState toOrderState(OrderStateRequestDto dto);
    UserInformation toUserInformation(UserInformationRequestDto dto);

    OrderStateResponseDto toStateResponse(OrderState transition);
    UserInformationResponseDto toUserInformationResponse(UserInformation user);
    List<OrderStateResponseDto> toStatesResponseList(List<OrderState> transitions);

    default OrderTraceabilityResponseDto toResponse(List<OrderState> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return null;
        }

        var first = transitions.get(0);
        return OrderTraceabilityResponseDto.builder()
                .orderId(first.getOrderId())
                .client(toUserInformationResponse(first.getClient()))
                .transitions(toStatesResponseList(transitions))
                .build();
    }
}
