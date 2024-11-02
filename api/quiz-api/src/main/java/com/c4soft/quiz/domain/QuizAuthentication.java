/* (C)2024 */
package com.c4soft.quiz.domain;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import com.c4_soft.springaddons.security.oidc.OAuthentication;
import com.c4_soft.springaddons.security.oidc.OpenidToken;
import lombok.Getter;

@Getter
public class QuizAuthentication extends OAuthentication<OpenidToken> {
  private static final long serialVersionUID = 1L;
  public static final String AUTHORITY_MODERATOR = "moderator";
  public static final String AUTHORITY_TRAINER = "trainer";
  public static final String SPEL_IS_MODERATOR_OR_TRAINER =
      "hasAnyAuthority('moderator', 'trainer')";
  public static final String SPEL_IS_MODERATOR = "hasAuthority('moderator')";
  public static final String SPEL_IS_TRAINER = "hasAuthority('trainer')";

  private final boolean isModerator;
  private final boolean isTrainer;

  public QuizAuthentication(OpenidToken token,
      Collection<? extends GrantedAuthority> authorities) {
    super(token, authorities);
    final var authoritiesStrings =
        authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    this.isModerator = authoritiesStrings.contains(AUTHORITY_MODERATOR);
    this.isTrainer = authoritiesStrings.contains(AUTHORITY_TRAINER);
  }
}
