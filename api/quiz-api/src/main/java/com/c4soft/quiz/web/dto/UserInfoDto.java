/* (C)2024 */
package com.c4soft.quiz.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @param username the user unique name
 * @param roles the user roles
 * @param exp seconds since epoch time at which access will expire
 */
public record UserInfoDto(
        @NotNull String username, @NotNull List<String> roles, @NotNull Long exp) {
    public static final UserInfoDto ANONYMOUS = new UserInfoDto("", List.of(), Long.MAX_VALUE);
}
