/** Copyright 2024 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.KeyInfo;

import io.jsonwebtoken.Jwts;

@Service
public class AuthenticationService {

	@Value("${com.my.goldmanager.auth.jwtTokenValidity:7200000}") // Default: 2h
	private long jwtTokenValidity;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AuthKeyInfoService authKeyInfoService;

	public String getJWTToken(String username, String password) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		User user = (User) authentication.getPrincipal();
		KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName(user.getUsername());
		return buildJWTToken(user.getUsername(), keyInfo);

	}

	private String buildJWTToken(String username, KeyInfo keyInfo) {
		return Jwts.builder().header().add("keyId", keyInfo.getKeyId()).and().subject(username)
				.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + jwtTokenValidity))
				.signWith(keyInfo.getKey()).compact();
	}

	public String refresJWTToken(String username) {
		KeyInfo keyInfo = authKeyInfoService.getKeyInfoForUserName(username);
		return buildJWTToken(username, keyInfo);


	}

	public void logout(String username) {
		authKeyInfoService.removeKeyInfosForUserName(username);
	}

	public void logoutAll() {
		authKeyInfoService.clearAll();
	}

}
