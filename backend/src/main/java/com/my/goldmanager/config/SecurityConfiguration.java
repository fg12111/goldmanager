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
package com.my.goldmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.my.goldmanager.encoder.SHA3_256HexEncoder;
import com.my.goldmanager.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
				(requests) -> requests.requestMatchers("/login").permitAll().requestMatchers("/swagger-ui/**")
						.permitAll().requestMatchers("/v3/**").permitAll().anyRequest().authenticated())
				.httpBasic(httpBasic -> httpBasic.disable()).csrf((csfr) -> csfr.disable());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return userDetailsService;

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new SHA3_256HexEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new AuthenticationManager() {

			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				UserDetails userdetails = userDetailsService.loadUserByUsername(authentication.getName());
				if (userdetails.isEnabled() && passwordEncoder()
						.matches(String.valueOf(authentication.getCredentials()), userdetails.getPassword())) {

					return new UsernamePasswordAuthenticationToken(
							new User(userdetails.getUsername(), userdetails.getPassword(),
									userdetails.getAuthorities()),
							userdetails.getPassword(), userdetails.getAuthorities());
				}
				throw new BadCredentialsException("Not Authenticated");
			}
		};
	}

}
