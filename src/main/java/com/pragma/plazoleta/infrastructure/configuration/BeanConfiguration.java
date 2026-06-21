package com.pragma.plazoleta.infrastructure.configuration;

import com.pragma.plazoleta.infrastructure.configuration.security.token.ITokenValidationPort;
import com.pragma.plazoleta.infrastructure.out.security.jwt.JwtAdapter;
import com.pragma.plazoleta.infrastructure.out.security.jwt.configuration.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final JwtProperties jwtProperties;

    @Bean
    public ITokenValidationPort tokenValidationPort() {
        return new JwtAdapter(jwtProperties);
    }
}
