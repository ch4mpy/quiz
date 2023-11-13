package com.c4soft.quiz.feign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "quiz-admin")
public interface KeycloakAdminApiClient {
	@GetMapping(value = "/users")
	List<UserRepresentation> getUser(@RequestParam(value="username") String username, @RequestParam(value="exact") boolean exact);
	
	public static class UserRepresentation extends HashMap<String, Object> {

		private static final long serialVersionUID = -2288285379516753557L;
		
		public UserRepresentation() {
			super();
		}
		
		public UserRepresentation(Map<? extends String, ? extends Object> m) {
			super(m);
		}

		public String getEmail() {
			return Optional.ofNullable(this.get("email")).map(Object::toString).orElse(null);
		}

		public String getFirtsName() {
			return Optional.ofNullable(this.get("firstName")).map(Object::toString).orElse(null);
		}

		public String getLastName() {
			return Optional.ofNullable(this.get("lastName")).map(Object::toString).orElse(null);
		}
	}
}