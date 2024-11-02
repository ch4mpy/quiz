package com.c4soft.quiz;

import org.keycloak.admin.api.UsersApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;

@Configuration
public class RestConfig {
  @Bean
  UsersApi usersApi(RestClient keycloakAdminClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(UsersApi.class, keycloakAdminClient).getObject();
  }

}
