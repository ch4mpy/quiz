/* (C)2024 */
package com.c4soft.quiz;

import java.util.Collection;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import com.c4_soft.springaddons.security.oidc.OpenidClaimSet;
import com.c4_soft.springaddons.security.oidc.starter.OpenidProviderPropertiesResolver;
import com.c4_soft.springaddons.security.oidc.starter.properties.NotAConfiguredOpenidProviderException;
import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.JwtAbstractAuthenticationTokenConverter;
import com.c4soft.quiz.domain.QuizAuthentication;

@Configuration
@EnableMethodSecurity()
public class SecurityConfig {
  @Bean
  JwtAbstractAuthenticationTokenConverter authenticationFactory(
      Converter<Map<String, Object>, Collection<? extends GrantedAuthority>> authoritiesConverter,
      OpenidProviderPropertiesResolver addonsPropertiesResolver) {
    return jwt -> {
      final var opProperties = addonsPropertiesResolver.resolve(jwt.getClaims())
          .orElseThrow(() -> new NotAConfiguredOpenidProviderException(jwt.getClaims()));
      final var claims = new OpenidClaimSet(jwt.getClaims(), opProperties.getUsernameClaim());
      return new QuizAuthentication(claims, authoritiesConverter.convert(claims),
          jwt.getTokenValue());
    };
  }
}
