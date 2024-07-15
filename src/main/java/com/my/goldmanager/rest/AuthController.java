package com.my.goldmanager.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.my.goldmanager.rest.request.AuthRequest;
import com.my.goldmanager.service.AuthenticationService;

@RestController
public class AuthController {

	@Autowired
	AuthenticationService authenticationService;

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
		try {

			return ResponseEntity
					.ok(authenticationService.getJWTToken(authRequest.getUsername(), authRequest.getPassword()));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401).body("Unauthorized");
		}
	}

	@GetMapping("/logoutuser")
	public ResponseEntity<Void> logout() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		authenticationService.logout(authentication.getName());
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

}
