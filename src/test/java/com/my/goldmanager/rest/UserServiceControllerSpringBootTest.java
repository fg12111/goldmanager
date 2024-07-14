package com.my.goldmanager.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.rest.request.CreateUserRequest;
import com.my.goldmanager.rest.request.UpdateUserPasswordRequest;
import com.my.goldmanager.rest.request.UpdateUserStatusRequest;
import com.my.goldmanager.rest.response.ErrorResponse;
import com.my.goldmanager.rest.response.ListUserResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceControllerSpringBootTest {

	@Autowired
	private UserLoginRepository userLoginRepository;
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	public void cleanUp() {
		userLoginRepository.deleteAll();
	}

	@Test
	public void testCreateUser() throws Exception {
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername("myUser");

		mockMvc.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createUserRequest))).andExpect(status().isCreated());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertEquals(createUserRequest.getUsername(), result.getUserid());
		assertTrue(result.isActive());
		assertEquals(DigestUtils.sha256Hex(createUserRequest.getPassword()), result.getPassword());
	}

	@Test
	public void testUpdateUserPassword() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("MyNewPass");

		mockMvc.perform(
				put("/userService/updatePassword/" + userLogin.getUserid()).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isNoContent());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertEquals(userLogin.getUserid(), result.getUserid());
		assertTrue(result.isActive());
		assertEquals(DigestUtils.sha256Hex(updateUserPasswordRequest.getNewPassword()), result.getPassword());
	}

	@Test
	public void testUpdateUserStatus() throws Exception {
		UserLogin userLogin = new UserLogin();
		userLogin.setActive(false);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest();
		updateUserStatusRequest.setActive(true);

		mockMvc.perform(put("/userService/setStatus/" + userLogin.getUserid()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isNoContent());

		Optional<UserLogin> optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		UserLogin result = optional.get();
		assertTrue(result.isActive());

		updateUserStatusRequest.setActive(false);
		mockMvc.perform(put("/userService/setStatus/" + userLogin.getUserid()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isNoContent());

		optional = userLoginRepository.findById("myUser");
		assertTrue(optional.isPresent());
		result = optional.get();
		assertFalse(result.isActive());

		mockMvc.perform(put("/userService/setStatus/invaliduser").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserStatusRequest))).andExpect(status().isNotFound());

	}

	@Test
	public void testUpdateUserPasswordInvalid() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("MyEncryptedPass");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword(null);

		String body = mockMvc
				.perform(put("/userService/updatePassword/" + userLogin.getUserid())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username and newPassword are mandatory", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("");

		body = mockMvc
				.perform(put("/userService/updatePassword/" + userLogin.getUserid())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateUserPasswordRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username and newPassword are mandatory", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

	}

	@Test
	public void testUpdateUserPasswordUserNotExists() throws Exception {

		UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
		updateUserPasswordRequest.setNewPassword("MyNewPass");

		mockMvc.perform(put("/userService/updatePassword/invaliduser").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateUserPasswordRequest))).andExpect(status().isNotFound());
	}

	@Test
	public void testCreateUserExisting() throws Exception {

		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword("EncodedPassword");
		userLogin.setUserid("myUser");
		userLoginRepository.save(userLogin);

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername("myUser");

		String body = mockMvc
				.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username 'myUser' already exists.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testCreateUserInvalidUser() throws Exception {

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername(null);

		String body = mockMvc
				.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username is mandatory.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("MyPass");
		createUserRequest.setUsername("");

		body = mockMvc
				.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Username is mandatory.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testCreateUserInvalidPassword() throws Exception {

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword(null);
		createUserRequest.setUsername("MyUser");

		String body = mockMvc
				.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());

		createUserRequest = new CreateUserRequest();
		createUserRequest.setPassword("");
		createUserRequest.setUsername("MyUser");

		body = mockMvc
				.perform(post("/userService").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createUserRequest)))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		assertEquals("Password is mandatory.", errorResponse.getMessage());
		assertEquals(400, errorResponse.getStatus());
	}

	@Test
	public void testListAll() throws Exception {

		for (int current = 0; current < 10; current++) {
			UserLogin userLogin = new UserLogin();
			userLogin.setActive(current % 2 == 0);
			userLogin.setPassword("MyEncryptedPass" + current);
			userLogin.setUserid("myuser" + current);
			userLoginRepository.save(userLogin);
		}

		List<UserLogin> expectedUsers = userLoginRepository.findAll();
		String body = mockMvc.perform(get("/userService")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse()
				.getContentAsString();
		ListUserResponse listUserResponse = objectMapper.readValue(body, ListUserResponse.class);
		assertEquals(expectedUsers.size(), listUserResponse.getUserInfos().size());

		for (int current = 0; current < expectedUsers.size(); current++) {

			assertEquals(expectedUsers.get(current).getUserid(),
					listUserResponse.getUserInfos().get(current).getUserName());
			assertEquals(expectedUsers.get(current).isActive(),
					listUserResponse.getUserInfos().get(current).isActive());
		}
	}
}
