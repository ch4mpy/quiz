package com.c4soft.quiz;

import java.net.URISyntaxException;
import java.util.List;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "BFF")
public class BffController {
	private final List<LoginOptionDto> loginOptions;

	public BffController(
			OAuth2ClientProperties clientProps,
			ReactiveClientRegistrationRepository clientRegistrationRepository) {
		this.loginOptions = clientProps.getRegistration().entrySet().stream().filter(e -> "authorization_code".equals(e.getValue().getAuthorizationGrantType()))
				.map(e -> new LoginOptionDto(e.getValue().getProvider(), "/oauth2/authorization/%s".formatted(e.getKey())))
				.toList();
	}

	@GetMapping(path = "/login-options", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getLoginOptions")
	public Mono<List<LoginOptionDto>> getLoginOptions(Authentication auth) throws URISyntaxException {
		final boolean isAuthenticated = auth instanceof OAuth2AuthenticationToken;
		return Mono.just(isAuthenticated ? List.of() : this.loginOptions);
	}

	public static record LoginOptionDto(@NotEmpty String label, @NotEmpty String loginUri) {
	}
}
