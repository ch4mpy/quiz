package com.c4soft.quiz;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.c4_soft.springaddons.security.oidc.OpenidClaimSet;
import com.c4_soft.springaddons.security.oidc.starter.LogoutRequestUriBuilder;
import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcClientProperties;
import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "BFF")
public class BffController {
	private final ReactiveClientRegistrationRepository clientRegistrationRepository;
	private final SpringAddonsOidcClientProperties addonsClientProps;
	private final LogoutRequestUriBuilder logoutRequestUriBuilder;
	private final ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();
	private final List<LoginOptionDto> loginOptions;

	public BffController(
			OAuth2ClientProperties clientProps,
			ReactiveClientRegistrationRepository clientRegistrationRepository,
			SpringAddonsOidcProperties addonsClientProps,
			LogoutRequestUriBuilder logoutRequestUriBuilder) {
		this.addonsClientProps = addonsClientProps.getClient();
		this.clientRegistrationRepository = clientRegistrationRepository;
		this.logoutRequestUriBuilder = logoutRequestUriBuilder;
		this.loginOptions = clientProps.getRegistration().entrySet().stream().filter(e -> "authorization_code".equals(e.getValue().getAuthorizationGrantType()))
				.map(e -> new LoginOptionDto(e.getValue().getProvider(), "%s/oauth2/authorization/%s".formatted(this.addonsClientProps.getClientUri(), e.getKey())))
				.toList();
	}

	@GetMapping(path = "/me", produces = "application/json")
	public Mono<UserInfoDto> getMe(Authentication auth) {
		if(auth instanceof AnonymousAuthenticationToken) {
			return Mono.just(UserInfoDto.ANONYMOUS);
		}
		return Mono.just(new UserInfoDto(auth.getName(), auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
	}

	@GetMapping(path = "/login/options", produces = "application/json")
	@Operation(operationId = "getLoginOptions")
	public Mono<List<LoginOptionDto>> getLoginOptions(Authentication auth) throws URISyntaxException {
		final boolean isAuthenticated = auth instanceof OAuth2AuthenticationToken;
		return Mono.just(isAuthenticated ? List.of() : this.loginOptions);
	}

	@PutMapping(path = "/logout", produces = "application/json")
	@Operation(operationId = "logout", responses = { @ApiResponse(responseCode = "204") })
	public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange, Authentication authentication) {
		final Mono<URI> uri;
		if (authentication instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidcUser) {
			uri = clientRegistrationRepository.findByRegistrationId(oauth.getAuthorizedClientRegistrationId()).map(clientRegistration -> {
				final var uriString = logoutRequestUriBuilder
						.getLogoutRequestUri(clientRegistration, oidcUser.getIdToken().getTokenValue(), addonsClientProps.getPostLogoutRedirectUri());
				return StringUtils.hasText(uriString) ? URI.create(uriString) : addonsClientProps.getPostLogoutRedirectUri();
			});
		} else {
			uri = Mono.just(addonsClientProps.getPostLogoutRedirectUri());
		}
		return uri.flatMap(logoutUri -> {
			return securityContextRepository.save(exchange, null).thenReturn(logoutUri);
		}).map(logoutUri -> {
			return ResponseEntity.noContent().location(logoutUri).build();
		});
	}

	public static record LoginOptionDto(@NotEmpty String label, @NotEmpty String loginUri) {
	}
	
	public static record UserInfoDto(@NotNull String username, @NotNull List<String> roles) {
		public static final UserInfoDto ANONYMOUS = new UserInfoDto("", List.of());
	}
}
