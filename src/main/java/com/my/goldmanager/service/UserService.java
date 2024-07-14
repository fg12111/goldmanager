package com.my.goldmanager.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.goldmanager.entity.UserLogin;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.exception.ValidationException;

@Service
public class UserService {

	@Autowired
	UserLoginRepository userLoginRepository;

	/**
	 * Create a new user
	 * 
	 * @param username
	 * @param password
	 * @throws ValidationException
	 */
	public void create(String username, String password) throws ValidationException {

		if (username == null || username.isBlank()) {
			throw new ValidationException("Username is mandatory.");
		}
		if (password == null || password.isBlank()) {
			throw new ValidationException("Password is mandatory.");
		}
		if (userLoginRepository.existsById(username)) {
			throw new ValidationException("Username '" + username + "' already exists.");
		}
		UserLogin userLogin = new UserLogin();
		userLogin.setActive(true);
		userLogin.setPassword(DigestUtils.sha256Hex(password));
		userLogin.setUserid(username);
		userLoginRepository.save(userLogin);

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
			login.setPassword(DigestUtils.sha256Hex(newPassword));
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
	 */
	public boolean updateUserActivation(String username, boolean active) {
		Optional<UserLogin> userlogin = userLoginRepository.findById(username);
		if (userlogin.isPresent()) {
			UserLogin login = userlogin.get();
			login.setActive(active);
			userLoginRepository.save(login);
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
