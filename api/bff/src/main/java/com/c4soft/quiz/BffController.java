package com.c4soft.quiz;

import java.net.URISyntaxException;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.c4_soft.springaddons.security.oidc.starter.properties.SpringAddonsOidcProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;

@RestController
@Tag(name = "BFF")
public class BffController {
  private final List<LoginOptionDto> loginOptions;

  @Autowired
  private final RabbitTemplate rabbitTemplate;

  public BffController(OAuth2ClientProperties clientProps,
      SpringAddonsOidcProperties addonsProperties, RabbitTemplate rabbitTemplate) {
    this.loginOptions =
        clientProps.getRegistration().entrySet().stream()
            .filter(e -> "authorization_code".equals(e.getValue().getAuthorizationGrantType()))
            .map(
                e -> new LoginOptionDto(e.getValue().getProvider(),
                    "%s/oauth2/authorization/%s"
                        .formatted(addonsProperties.getClient().getClientUri(), e.getKey())))
            .toList();
    this.rabbitTemplate = rabbitTemplate;
  }

  @GetMapping(path = "/login-options", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(operationId = "getLoginOptions",
      responses = {@ApiResponse(responseCode = "200", description = "List of login options",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              array = @ArraySchema(schema = @Schema(implementation = LoginOptionDto.class)))})})
  public List<LoginOptionDto> getLoginOptions(Authentication auth) throws URISyntaxException {
    final boolean isAuthenticated = auth instanceof OAuth2AuthenticationToken;
    return isAuthenticated ? List.of() : this.loginOptions;
  }

  @GetMapping("/ws/send")
  public ResponseEntity<String> sendMessage(@RequestParam String message) {
    rabbitTemplate.convertAndSend("amq.topic", "user-session-events-queue", message);
    return ResponseEntity.ok("Message sent: " + message);
  }

  public static record LoginOptionDto(@NotEmpty String label, @NotEmpty String loginUri) {
  }
}
