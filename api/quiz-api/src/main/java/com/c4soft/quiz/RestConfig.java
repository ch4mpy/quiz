package com.c4soft.quiz;

import org.keycloak.admin.api.UsersApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.c4_soft.springaddons.rest.SpringAddonsRestClientSupport;

@Configuration
public class RestConfig {
  @Bean
  UsersApi usersApi(SpringAddonsRestClientSupport restSupport) {
    return restSupport.service("keycloak-admin-api", UsersApi.class);
  }

}
