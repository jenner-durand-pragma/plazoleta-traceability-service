package com.pragma.plazoleta.infrastructure.out.mongo.mapper;

import com.pragma.plazoleta.domain.model.OrderState;
import com.pragma.plazoleta.domain.model.UserInformation;
import com.pragma.plazoleta.infrastructure.out.mongo.document.OrderStateDocument;
import com.pragma.plazoleta.infrastructure.out.mongo.document.UserInformationSubDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IOrderStateDocumentMapper {
    OrderStateDocument toDocument(OrderState orderState);
    OrderState toModel(OrderStateDocument document);

    UserInformation toUserInformation(UserInformationSubDocument subDocument);
    UserInformationSubDocument toSubdocument(UserInformation userInformation);
}
