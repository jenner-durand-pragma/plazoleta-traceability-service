package com.pragma.plazoleta.infrastructure.out.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInformationSubDocument {

    private Long id;
    private String name;
    private String lastName;
    private String email;
}
