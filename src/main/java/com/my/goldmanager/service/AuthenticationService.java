package com.my.goldmanager.service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service
public class AuthenticationService {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private ConcurrentHashMap<String, Key> userSecretKeys;

	private final ConcurrentHashMap<String, String> userKeyIDMap = new ConcurrentHashMap<>();

	public String getJWTToken(String username, String password) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		User user = (User) authentication.getPrincipal();
		Key secretKey = null;
		String keyId = null;
		synchronized (userSecretKeys) {

			keyId = userKeyIDMap.get(user.getUsername());
			if (keyId == null) {
				keyId = UUID.randomUUID().toString();
				userKeyIDMap.put(user.getUsername(), keyId);
			}
			secretKey = userSecretKeys.get(keyId);
			if (secretKey == null) {
				secretKey = Jwts.SIG.HS256.key().build();
				userSecretKeys.put(keyId, secretKey);
			}

		}


		return Jwts.builder().header().add("keyId", keyId).and().subject(user.getUsername()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 86400000)).signWith(secretKey).compact();

	}

	public void logout(String username) {
		String keyId = userKeyIDMap.get(username);
		if (keyId != null) {
			userSecretKeys.remove(keyId);
			userKeyIDMap.remove(keyId);
		}
	}

	public void logoutAll() {
		userKeyIDMap.clear();
		userSecretKeys.clear();
	}

}
