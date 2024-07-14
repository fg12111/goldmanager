package com.my.goldmanager.rest.request;

import lombok.Getter;
import lombok.Setter;

public  class CreateUserRequest {

	@Getter
	@Setter
	private String username;
	@Getter
	@Setter
	private String password;
}
