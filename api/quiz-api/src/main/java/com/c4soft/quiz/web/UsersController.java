package com.c4soft.quiz.web;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c4soft.quiz.domain.QuizAuthentication;
import com.c4soft.quiz.web.dto.UserInfoDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users")
public class UsersController {

	@GetMapping(path = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE })
	public UserInfoDto getMe(Authentication auth) {
		if (auth instanceof QuizAuthentication quizAuth) {
			return new UserInfoDto(
					quizAuth.getName(),
					quizAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
					quizAuth.getAttributes().getExpiresAt().getEpochSecond());
		}
		return UserInfoDto.ANONYMOUS;
	}
}
