package com.my.goldmanager.config;

import java.security.Key;
import java.util.concurrent.ConcurrentHashMap;

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

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
	private final ConcurrentHashMap<String, Key> userSecretKeys = new ConcurrentHashMap<>();
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
				(requests) -> requests.requestMatchers("/login").permitAll().requestMatchers("/swagger-ui/**")
						.permitAll().requestMatchers("/v3/**").permitAll().anyRequest().authenticated())
				.httpBasic(httpBasic -> httpBasic.disable()).csrf((csfr) -> csfr.disable());

		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private Filter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(userSecretKeys);
	}

	@Bean
	public ConcurrentHashMap<String, Key> userSecretKeys() {
		return userSecretKeys;
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
