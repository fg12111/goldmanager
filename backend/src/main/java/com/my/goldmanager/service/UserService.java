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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.my.goldmanager.encoder.SHA3_256HexEncoder;
import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class UserService {

	private final SHA3_256HexEncoder passwordEncoder = new SHA3_256HexEncoder();

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private UserLoginRepository userLoginRepository;

	/**
	 * Create a new user
	 * 
	 * @param username
	 * @param password
	 * @throws ValidationException
	 */
	public void create(String username, String password) throws ValidationException {

		if (username == null || username.isBlank() || username.trim().contains(" ")) {
			throw new ValidationException("Username is mandatory and must not contain spaces.");
		}
		if (password == null || password.isBlank() || password.trim().contains(" ")) {
			throw new ValidationException("Password is mandatory and must not contain spaces.");
		}
		if (userLoginRepository.existsById(username.trim())) {
			throw new ValidationException("Username '" + username.trim() + "' already exists.");
		}
		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword(passwordEncoder.encode(password.trim()));
		userLogin.setUserid(username.trim());
		userLoginRepository.save(userLogin);

	}

	/**
	 * Deletes the specified user.
	 * 
	 * @param username
	 * @return
	 * @throws ValidationException
	 */
	public boolean deleteUser(String username) throws ValidationException {
		return deleteUser(username, false);
	}

	/**
	 * 
	 * @param username
	 * @param force
	 * @return
	 * @throws ValidationException
	 */
	public boolean deleteUser(String username, boolean force) throws ValidationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!force && authentication != null && username.equals(authentication.getName())) {
			throw new ValidationException("Users must not delete them self.");
		}
		if (userLoginRepository.existsById(username)) {
			userLoginRepository.deleteById(username);
			authenticationService.logout(username);
			return true;
		}
		return false;
	}

	/**
	 * Update the user's password
	 * 
	 * @param username
	 * @param newPassword
	 * @return
	 * @throws ValidationException
	 */
	public boolean updatePassword(String username, String newPassword) throws ValidationException {

		if (username == null || username.isBlank() || newPassword == null || newPassword.isBlank()) {
			throw new ValidationException("Username and newPassword are mandatory");
		}
		Optional<UserLogin> userlogin = userLoginRepository.findById(username);
		if (userlogin.isPresent()) {
			UserLogin login = userlogin.get();
			login.setPassword(passwordEncoder.encode(newPassword));
			userLoginRepository.save(login);
			return true;
		}
		return false;
	}

	/**
	 * Update user status
	 * 
	 * @param username
	 * @param active
	 * @return
	 * @throws ValidationException
	 */
	public boolean updateUserActivation(String username, boolean active) throws ValidationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && username.equals(authentication.getName())) {
			throw new ValidationException("Users must not activate or deactivate them self.");
		}

		Optional<UserLogin> userlogin = userLoginRepository.findById(username);

		if (userlogin.isPresent()) {

			UserLogin login = userlogin.get();
			login.setActive(active);
			userLoginRepository.save(login);
			if (!active) {
				authenticationService.logout(username);
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return returns the number of all existing users
	 */
	public long countUsers() {
		return userLoginRepository.count();
	}

	/**
	 * 
	 * @return all existing users
	 */
	public List<UserLogin> listAll() {
		return userLoginRepository.findAll();
	}
}
