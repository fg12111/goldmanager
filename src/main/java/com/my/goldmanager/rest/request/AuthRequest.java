package com.my.goldmanager.rest.request;

import lombok.Getter;
import lombok.Setter;

public class AuthRequest {
	@Getter
	@Setter
	private String username;
	@Getter
	@Setter
	private String password;
}
