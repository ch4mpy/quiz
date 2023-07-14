package com.c4soft.quiz;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import com.c4_soft.springaddons.security.oidc.OAuthentication;
import com.c4_soft.springaddons.security.oidc.OpenidClaimSet;
import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties;
import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.JwtAbstractAuthenticationTokenConverter;

@Configuration
@EnableMethodSecurity()
public class SecurityConfig {
	@Bean
	JwtAbstractAuthenticationTokenConverter authenticationFactory(
			Converter<Map<String, Object>, Collection<? extends GrantedAuthority>> authoritiesConverter,
			SpringAddonsOidcProperties addonsProperties) {
		return jwt -> {
			final var opProperties = addonsProperties.getOpProperties(jwt.getClaims().get(JwtClaimNames.ISS));
			final var claims = new OpenidClaimSet(jwt.getClaims(), opProperties.getUsernameClaim());
			return new OAuthentication<>(claims, authoritiesConverter.convert(claims), jwt.getTokenValue());
		};
	}
}
