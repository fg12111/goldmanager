package com.my.goldmanager.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.rest.request.AuthRequest;
import com.my.goldmanager.service.AuthenticationService;
import com.my.goldmanager.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerSpringBootTest {
	@Autowired
	private UserLoginRepository userLoginRepository;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	private UserService userService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		userLoginRepository.deleteAll();
		authenticationService.logoutAll();
	}

	@Test
	public void testLoginSuccess() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

		String token = mockMvc
				.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(authRequest)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		mockMvc.perform(get("/api/userService").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

	}

	@Test
	public void testLoginFailure() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password1");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(authRequest))).andExpect(status().isUnauthorized());

	}

	@Test
	public void testLogout() throws JsonProcessingException, Exception {
		userService.create("user", "password");
		AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("user");
		authRequest.setPassword("password");

		String token = mockMvc
				.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(authRequest)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	
		mockMvc.perform(get("/api/auth/logoutuser").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/userService").header("Authorization", "Bearer " + token))
				.andExpect(status().is(403));
	}
}
