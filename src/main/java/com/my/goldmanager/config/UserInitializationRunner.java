package com.my.goldmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.my.goldmanager.service.UserService;

@Component
public class UserInitializationRunner implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(UserInitializationRunner.class);
	public static final String ENV_DEFAULTUSER = "APP_DEFAULT_USER";
	public static final String ENV_DEFAULTPASSWORD = "APP_DEFAULT_PASSWORD";

	@Autowired
	private UserService userService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String defaultUser = readVariable(ENV_DEFAULTUSER);
		String defaultPassword = readVariable(ENV_DEFAULTPASSWORD);
		if (defaultUser != null && defaultPassword != null) {
			if (userService.countUsers() > 0) {
				logger.info("Users existing skipping creation of default user");
			} else {
				logger.info("Creating default user {}...", defaultUser);
				userService.create(defaultUser, defaultPassword);
				logger.info("Created default user {}.", defaultUser);
			}
		}

	}

	private static String readVariable(String name) {
		if (System.getenv(name) != null) {
			return System.getenv(name);
		}
		return System.getProperty(name);
	}
}
